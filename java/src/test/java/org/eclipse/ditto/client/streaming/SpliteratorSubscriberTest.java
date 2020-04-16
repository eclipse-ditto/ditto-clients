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
package org.eclipse.ditto.client.streaming;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.Test;

/**
 * Verify reactive-streams compatibility of {@link org.eclipse.ditto.client.streaming.SpliteratorSubscriber}.
 */
public final class SpliteratorSubscriberTest {

    @Test
    public void parallelismEqualsBufferSize() {
        final int parallelism = 9;
        final SpliteratorSubscriber<?> underTest = SpliteratorSubscriber.of(Duration.ofMinutes(1L), 9, 7);
        for (int i = 1; i < parallelism; ++i) {
            assertThat(underTest.trySplit()).describedAs("Split #" + i + " should not be null").isNotNull();
        }
        assertThat(underTest.trySplit()).describedAs("Split #" + parallelism + " should be null").isNull();
    }
}
