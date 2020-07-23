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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an easy way to implement a an action that should be retried.
 * In this case this action must return a result which also can be null.
 * As soon as a result is returned is by the supplier, the action is considered to be finished successfully.
 *
 * @param <T> Type of the result
 */
final class Retry<T> implements Supplier<CompletionStage<T>> {

    private static final int[] TIME_TO_WAIT_BETWEEN_RETRIES_IN_SECONDS = new int[]{1, 1, 2, 3, 5, 8, 13};
    private static final Logger LOGGER = LoggerFactory.getLogger(Retry.class.getName());

    private final String sessionId;
    private final String nameOfAction;
    private final Supplier<T> retriedSupplier;
    private final ScheduledExecutorService executorService;
    @Nullable private final Consumer<Throwable> errorConsumer;


    private Retry(final String nameOfAction,
            final String sessionId,
            final Supplier<T> retriedSupplier,
            final ScheduledExecutorService executorService,
            @Nullable final Consumer<Throwable> errorConsumer) {

        this.sessionId = sessionId;
        this.nameOfAction = nameOfAction;
        this.retriedSupplier = retriedSupplier;
        this.executorService = executorService;
        this.errorConsumer = errorConsumer;
    }

    private static int ensureIndexIntoTimeToWaitBounds(final int index) {
        if (index < 0) {
            return 0;
        } else if (index >= TIME_TO_WAIT_BETWEEN_RETRIES_IN_SECONDS.length) {
            return TIME_TO_WAIT_BETWEEN_RETRIES_IN_SECONDS.length - 1;
        } else {
            return index;
        }
    }

    /**
     * Executes the provided supplier until a result is returned.
     *
     * @return A completion stage which finally completes with the result of the supplier. Result can be null.
     */
    @Override
    public CompletionStage<T> get() {
        final CompletableFuture<T> result = new CompletableFuture<>();
        executorService.submit(() -> this.completeFutureEventually(1, result));
        return result;
    }

    private void completeFutureEventually(final int attempt, final CompletableFuture<T> resultToComplete) {
        try {
            resultToComplete.complete(retriedSupplier.get());
        } catch (final RuntimeException e) {
            // log error, but try again (don't end loop)
            LOGGER.error("Client <{}>: Failed to <{}>: {}.", sessionId, nameOfAction, e.getMessage());
            if (errorConsumer != null) {
                try {
                    errorConsumer.accept(e);
                } catch (final Throwable errorFromConsumer) {
                    LOGGER.warn("Got exception from error consumer: {}.\n" +
                                    "If you see this log, you most likely tried to throw an exception which you " +
                                    "wanted to handle in your application.\n" +
                                    "Keep in mind that this operation runs in a separate thread and therefore the " +
                                    "exception does not reach your application thread.\n" +
                                    "If this is the case, please try to move the logic for handling the exception" +
                                    "tho the error consumer.",
                            errorFromConsumer.getMessage(), errorFromConsumer);
                }
            }
            final int timeToWaitInSeconds = getTimeToWaitInSecondsForAttempt(attempt);
            LOGGER.info("Client <{}>: Waiting for <{}> second(s) before retrying to <{}>.",
                    sessionId, timeToWaitInSeconds, nameOfAction);
            executorService.schedule(() -> this.completeFutureEventually(attempt + 1, resultToComplete),
                    timeToWaitInSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private int getTimeToWaitInSecondsForAttempt(final int attempt) {
        final int attemptIndex = ensureIndexIntoTimeToWaitBounds(attempt - 1);
        return TIME_TO_WAIT_BETWEEN_RETRIES_IN_SECONDS[attemptIndex];
    }

    /**
     * Creates a builder to configure the parameters that are relevant for the retry logic.
     *
     * @param nameOfAction A name of the action that is retried. This is only used for logging. E.g. "fetch services".
     * @param supplierToRetry The action that should be retried until a result is returned. Result ca also be null.
     * @param <T> The type of the expected result.
     * @return The result of the supplier after it finally succeeds.
     */
    static <T> RetryBuilderStep1<T> retryTo(final String nameOfAction, final Supplier<T> supplierToRetry) {
        return new RetryBuilder<>(nameOfAction, supplierToRetry);
    }

    /**
     * First step which required the client session to be configured. Even though this is just used for logging,
     * this parameter is considered as mandatory, as it makes debugging and analyzing logs a lot easier.
     *
     * @param <T> The return type of the supplier.
     */
    interface RetryBuilderStep1<T> {

        /**
         * Configures the session ID that should be used for log statements made by performing the action and its
         * potential retries.
         *
         * @param sessionId the session ID of the client for which the action is performed.
         * @return the next builder step as a new instance.
         */
        RetryBuilderStep2<T> inClientSession(String sessionId);
    }


    /**
     * Second step which requires the scheduled executor service to be configured. This executor service is used
     * to schedule the supplier to be executed.
     *
     * @param <T> The return type of the supplier.
     */
    interface RetryBuilderStep2<T> {

        /**
         * Specifies the executor to use when performing the action and its potential retries.
         *
         * @param executorService the executor service.
         * @return a new instance of this builder step.
         */
        RetryBuilderFinal<T> withExecutor(final ScheduledExecutorService executorService);
    }

    /**
     * Final builder steps which are optionally required for the retry logic.
     *
     * @param <T> The return type of the supplier.
     */
    interface RetryBuilderFinal<T> extends Supplier<CompletionStage<T>> {

        /**
         * Sets a consumer which will be called with errors that happen during task to retry.
         *
         * @param errorConsumer consumer which will be called with errors that happen during task to retry.
         */
        RetryBuilderFinal<T> notifyOnError(@Nullable final Consumer<Throwable> errorConsumer);

        /**
         * Executes the provided supplier unit the supplier returns a result.
         *
         * @return A completion stage which finally completes with the result of the supplier. Result can be null.
         */
        @Override
        CompletionStage<T> get();

    }

    /**
     * Builder to configure the retry logic.
     *
     * @param <T> the return type of the supplier.
     */
    static class RetryBuilder<T> implements RetryBuilderStep1<T>, RetryBuilderStep2<T>, RetryBuilderFinal<T> {

        private final String nameOfAction;
        private final Supplier<T> retriedSupplier;
        private final String sessionId;
        @Nullable private final Consumer<Throwable> errorConsumer;
        @Nullable private final ScheduledExecutorService executorService;

        private RetryBuilder(final String nameOfAction, final Supplier<T> retriedSupplier) {
            this(nameOfAction, retriedSupplier, "", null, null);
        }

        private RetryBuilder(final String nameOfAction,
                final Supplier<T> retriedSupplier,
                final String sessionId,
                @Nullable final ScheduledExecutorService executorService,
                @Nullable final Consumer<Throwable> errorConsumer) {

            this.nameOfAction = nameOfAction;
            this.retriedSupplier = retriedSupplier;
            this.sessionId = sessionId;
            this.executorService = executorService;
            this.errorConsumer = errorConsumer;
        }

        @Override
        public RetryBuilderStep2<T> inClientSession(final String sessionId) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, executorService, errorConsumer);
        }

        @Override
        public RetryBuilderFinal<T> withExecutor(final ScheduledExecutorService executorService) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, executorService, errorConsumer);
        }

        @Override
        public RetryBuilderFinal<T> notifyOnError(@Nullable final Consumer<Throwable> errorConsumer) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, executorService, errorConsumer);
        }

        @Override
        public CompletionStage<T> get() {
            checkNotNull(executorService, "executorService");
            return new Retry<>(nameOfAction, sessionId, retriedSupplier, executorService, errorConsumer).get();
        }

    }

}
