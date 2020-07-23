/*
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
package org.eclipse.ditto.client.messaging.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.eclipse.ditto.client.internal.DefaultThreadFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public final class RetryTest {

    private static ScheduledExecutorService scheduledExecutorService;

    @BeforeClass
    public static void setup() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1, new DefaultThreadFactory("test"));
    }

    @AfterClass
    public static void tearDown() {
        scheduledExecutorService.shutdown();
    }


    @Test
    public void getsResultOfRetryableSupplier() {
        final Supplier<String> retryableSupplier = () -> "foo";
        final String actualResult = Retry.retryTo("test the result", retryableSupplier)
                .inClientSession(UUID.randomUUID().toString())
                .withExecutor(scheduledExecutorService)
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
                .withExecutor(scheduledExecutorService)
                .get()
                .toCompletableFuture()
                .join();

        assertThat(actualResult).isEqualTo("bar");
        assertThat(latch.getCount()).isZero();
    }

    @Test
    public void errorConsumerCalledOnError() {
        final int numberOfRetries = 2;
        final CountDownLatch errorConsumerLatch = new CountDownLatch(numberOfRetries);
        final CountDownLatch totalTriesLatch = new CountDownLatch(numberOfRetries + 1);
        final Supplier<String> retryableSupplier = () -> {
            totalTriesLatch.countDown();
            if (totalTriesLatch.getCount() > 0) {
                throw new RuntimeException("Expected exception in first iteration.");
            } else {
                return "bar";
            }
        };
        final String actualResult = Retry.retryTo("test the result", retryableSupplier)
                .inClientSession(UUID.randomUUID().toString())
                .withExecutor(scheduledExecutorService)
                .notifyOnError(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).isEqualTo("Expected exception in first iteration.");
                    errorConsumerLatch.countDown();
                })
                .get()
                .toCompletableFuture()
                .join();

        assertThat(actualResult).isEqualTo("bar");
        assertThat(totalTriesLatch.getCount()).isZero();
        assertThat(errorConsumerLatch.getCount()).isZero();
    }

}
