package org.eclipse.ditto.client.messaging.internal;/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.junit.Test;

public final class RetryTest {

    @Test
    public void getsResultOfRetryableSupplier() {
        final Supplier<String> retryableSupplier = () -> "foo";
        final String actualResult = Retry.retryTo("test the result", retryableSupplier)
                .inClientSession(UUID.randomUUID().toString())
                .get()
                .toCompletableFuture()
                .join();

        assertThat(actualResult).isEqualTo("foo");
    }

    @Test
    public void retriesSupplierIfFirstTryThrowsException() {
        final CountDownLatch latch = new CountDownLatch(2);
        final Supplier<String> retryableSupplier = () -> {
            latch.countDown();
            if (latch.getCount() == 1) {
                throw new RuntimeException("Expected exception in first iteration.");
            } else {
                return "bar";
            }
        };
        final String actualResult = Retry.retryTo("test the result", retryableSupplier)
                .inClientSession(UUID.randomUUID().toString())
                .get()
                .toCompletableFuture()
                .join();

        assertThat(actualResult).isEqualTo("bar");
        assertThat(latch.getCount()).isZero();
    }

}
