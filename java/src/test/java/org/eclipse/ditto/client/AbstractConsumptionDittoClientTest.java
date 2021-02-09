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
package org.eclipse.ditto.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.internal.ConcurrentConsumptionRequestException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests different sequences of failing/succeeding startConsumption invocations.
 */
abstract class AbstractConsumptionDittoClientTest extends AbstractDittoClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDittoClientTest.class);

    @Test
    public void concurrentStartConsumptionFails() {
        try {
            final CompletionStage<Void> firstRequest = startConsumptionRequest();
            final CompletionStage<Void> concurrentRequest = startConsumptionRequest();

            replyToConsumptionRequest();

            assertCompletion(firstRequest);
            concurrentRequest.toCompletableFuture().get(1, TimeUnit.SECONDS);
        } catch (final Exception e) {
            assertThat(e)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(ConcurrentConsumptionRequestException.class);
        }
    }

    protected abstract CompletionStage<Void> startConsumptionRequest();

    protected abstract void replyToConsumptionRequest();

    @Test
    public void testStartConsumptionCombinations() {
        // test all combinations of startConsumption invocations expecting success or failure of the given length
        testStartConsumptionSequence(4, Collections.emptyList());
    }

    protected void testStartConsumptionSequence(int remaining, final List<Boolean> sequence) {
        testSequence(sequence);
        if (remaining > 0) {
            testStartConsumptionSequence(remaining - 1, addElement(sequence, true));
            testStartConsumptionSequence(remaining - 1, addElement(sequence, false));
        }
    }

    private void testSequence(final List<Boolean> sequence) {
        if (sequence.isEmpty()) {
            return;
        }
        LOGGER.info("Testing startConsumption sequence: {}",
                sequence.stream()
                        .map(success -> success ? "expect success" : "expect failure")
                        .collect(Collectors.joining(" -> ")));
        sequence.forEach(expectSuccess -> {
            messaging.clearEmitted();
            if (expectSuccess) {
                startConsumptionSucceeds();
            } else {
                startConsumptionAndExpectError();
            }
        });
    }

    private List<Boolean> addElement(final List<Boolean> sequence, final boolean b) {
        final ArrayList<Boolean> success = new ArrayList<>(sequence);
        success.add(b);
        return success;
    }

    protected void assertFailedCompletion(final CompletionStage<Void> future,
            final Class<? extends Exception> exception) {
        try {
            future.toCompletableFuture().get(1L, TimeUnit.SECONDS);
        } catch (final Exception e) {
            assertThat(e)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(exception);
        }
    }

    /**
     * Tests a succeeding startConsumption invocation.
     */
    protected abstract void startConsumptionSucceeds();

    /**
     * Tests a failing startConsumption invocation.
     */
    protected abstract void startConsumptionAndExpectError();
}
