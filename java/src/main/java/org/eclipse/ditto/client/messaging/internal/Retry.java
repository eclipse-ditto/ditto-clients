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
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.messaging.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an easy way to implement a an action that should be retried.
 * In this case this action must return a result which also can be null.
 * As soon as a result is returned is by the supplier, the action is considered to be finished successfully.
 *
 * @param <T> Type of the result
 */
final class Retry<T> {

    private static final int[] TIME_TO_WAIT_BETWEEN_RETRIES_IN_SECONDS = new int[]{1, 1, 2, 3, 5, 8, 13};
    private static final Logger LOGGER = LoggerFactory.getLogger(Retry.class.getName());

    private final String sessionId;
    private final String nameOfAction;
    private final Supplier<CompletionStage<T>> retriedSupplier;
    private final ScheduledExecutorService reconnectExecutor;
    private final ExecutorService callbackExecutor;
    @Nullable
    private final Consumer<Throwable> errorConsumer;
    private final Predicate<Throwable> isRecoverable;

    private Retry(final String nameOfAction,
            final String sessionId,
            final Supplier<CompletionStage<T>> retriedSupplier,
            final ScheduledExecutorService reconnectExecutor,
            final ExecutorService callbackExecutor,
            @Nullable final Consumer<Throwable> errorConsumer,
            final Predicate<Throwable> isRecoverable) {

        this.sessionId = sessionId;
        this.nameOfAction = nameOfAction;
        this.retriedSupplier = retriedSupplier;
        this.reconnectExecutor = reconnectExecutor;
        this.callbackExecutor = callbackExecutor;
        this.errorConsumer = errorConsumer;
        this.isRecoverable = isRecoverable;
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
     * @param toComplete the future to complete after a result is returned.
     * @return the future this method is given.
     */
    public CompletionStage<T> completeFutureEventually(final CompletableFuture<T> toComplete) {
        reconnectExecutor.submit(() -> this.completeFutureEventually(1, toComplete));
        return toComplete;
    }

    private void completeFutureEventually(final int attempt, final CompletableFuture<T> resultToComplete) {
        try {
            retriedSupplier.get().whenComplete((result, error) -> {
                if (result != null) {
                    resultToComplete.complete(result);
                } else {
                    reschedule(attempt, resultToComplete, error);
                }
            });
        } catch (final Exception e) {
            reschedule(attempt, resultToComplete, e);
        }
    }

    private void reschedule(final int attempt, final CompletableFuture<T> resultToComplete, final Throwable error) {
        try {
            // log error, but try again (don't end loop)
            final Throwable cause = error instanceof CompletionException ? error.getCause() : error;
            if (isRecoverable.test(cause)) {
                LOGGER.error("Client <{}>: Failed to <{}>: {}", sessionId, nameOfAction, error.getMessage());
                notifyErrorConsumer(cause);
                final int timeToWaitInSeconds = getTimeToWaitInSecondsForAttempt(attempt);
                LOGGER.info("Client <{}>: Waiting for <{}> second(s) before retrying to <{}>.",
                        sessionId, timeToWaitInSeconds, nameOfAction);
                reconnectExecutor.schedule(() -> this.completeFutureEventually(attempt + 1, resultToComplete),
                        timeToWaitInSeconds,
                        TimeUnit.SECONDS);
            } else {
                LOGGER.error("Client <{}>: Permanently failed to {}: {}", sessionId, nameOfAction, error.getMessage());
                notifyErrorConsumer(cause);
                resultToComplete.completeExceptionally(error);
            }
        } catch (Exception e) {
            final MessagingException recreateFailed = MessagingException.recreateFailed(sessionId, e);
            resultToComplete.completeExceptionally(recreateFailed);
        }
    }

    private void notifyErrorConsumer(final Throwable cause) {
        if (errorConsumer != null) {
            callbackExecutor.submit(() -> {
                try {
                    errorConsumer.accept(cause);
                } catch (final Throwable errorFromConsumer) {
                    LOGGER.warn("Got exception from error consumer: {}.\n" +
                                    "If you see this log, you most likely tried to throw an exception which you " +
                                    "wanted to handle in your application.\n" +
                                    "Keep in mind that this operation runs in a separate thread and therefore the " +
                                    "exception does not reach your application thread.\n" +
                                    "If this is the case, please try to move the logic for handling the exception " +
                                    "to the error consumer.",
                            errorFromConsumer.getMessage(), errorFromConsumer);
                }
            });
        }
    }

    private int getTimeToWaitInSecondsForAttempt(final int attempt) {
        final int attemptIndex = ensureIndexIntoTimeToWaitBounds(attempt - 1);
        return TIME_TO_WAIT_BETWEEN_RETRIES_IN_SECONDS[attemptIndex];
    }

    /**
     * Creates a builder to configure the parameters that are relevant for the retry logic.
     *
     * @param <T> The type of the expected result.
     * @param nameOfAction A name of the action that is retried. This is only used for logging. E.g. "fetch services".
     * @param supplierToRetry The action that should be retried until a result is returned. Result ca also be null.
     * @return The result of the supplier after it finally succeeds.
     */
    static <T> RetryBuilderStep1<T> retryTo(final String nameOfAction,
            final Supplier<CompletionStage<T>> supplierToRetry) {
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
         * Configures the session ID that should be used for log statements made by performing the action and
         * its potential retries.
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
         * Specifies the executors to use when performing the action and its potential retries.
         *
         * @param reconnectExecutor the executor service to schedule reconnection on.
         * @param callbackExecutor the executor to run callbacks on.
         * @return a new instance of this builder step.
         */
        RetryBuilderFinal<T> withExecutors(final ScheduledExecutorService reconnectExecutor,
                final ExecutorService callbackExecutor);
    }

    /**
     * Final builder steps which are optionally required for the retry logic.
     *
     * @param <T> The return type of the supplier.
     */
    interface RetryBuilderFinal<T> {

        /**
         * Sets a consumer which will be called with errors that happen during task to retry.
         *
         * @param errorConsumer consumer which will be called with errors that happen during task to retry.
         */
        RetryBuilderFinal<T> notifyOnError(@Nullable final Consumer<Throwable> errorConsumer);

        /**
         * Test whether an exception can be recovered from.
         * If not, no further attempts will be made.
         * All exceptions are considered recoverable when not set.
         *
         * @param isRecoverable whether the exception can be recovered from.
         * @return this builder.
         */
        RetryBuilderFinal<T> isRecoverable(final Predicate<Throwable> isRecoverable);

        /**
         * Executes the provided supplier unit the supplier returns a result.
         *
         * @param future the future to complete when the supplier returns a result.
         * @return A completion stage which finally completes with the result of the supplier. Result can be null.
         */
        CompletionStage<T> completeFutureEventually(final CompletableFuture<T> future);
    }

    /**
     * Builder to configure the retry logic.
     *
     * @param <T> the return type of the supplier.
     */
    static class RetryBuilder<T> implements RetryBuilderStep1<T>, RetryBuilderStep2<T>, RetryBuilderFinal<T> {

        private final String nameOfAction;
        private final Supplier<CompletionStage<T>> retriedSupplier;
        private final String sessionId;
        @Nullable private final Consumer<Throwable> errorConsumer;
        @Nullable private final ScheduledExecutorService reconnectExecutor;
        @Nullable private final ExecutorService callbackExecutor;
        private final Predicate<Throwable> isRecoverable;

        private RetryBuilder(final String nameOfAction, final Supplier<CompletionStage<T>> retriedSupplier) {
            this(nameOfAction, retriedSupplier, "", null, null, null,
                    Exception.class::isInstance);
        }

        private RetryBuilder(final String nameOfAction,
                final Supplier<CompletionStage<T>> retriedSupplier,
                final String sessionId,
                @Nullable final ScheduledExecutorService reconnectExecutor,
                @Nullable final ExecutorService callbackExecutor,
                @Nullable final Consumer<Throwable> errorConsumer,
                final Predicate<Throwable> isRecoverable) {

            this.nameOfAction = nameOfAction;
            this.retriedSupplier = retriedSupplier;
            this.sessionId = sessionId;
            this.isRecoverable = isRecoverable;
            this.reconnectExecutor = reconnectExecutor;
            this.callbackExecutor = callbackExecutor;
            this.errorConsumer = errorConsumer;
        }

        @Override
        public RetryBuilderStep2<T> inClientSession(final String sessionId) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, reconnectExecutor, callbackExecutor,
                    errorConsumer, isRecoverable
            );
        }

        @Override
        public RetryBuilderFinal<T> withExecutors(final ScheduledExecutorService reconnectExecutor,
                final ExecutorService callbackExecutor) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, reconnectExecutor, callbackExecutor,
                    errorConsumer, isRecoverable);
        }

        @Override
        public RetryBuilderFinal<T> notifyOnError(@Nullable final Consumer<Throwable> errorConsumer) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, reconnectExecutor, callbackExecutor,
                    errorConsumer, isRecoverable);
        }

        @Override
        public RetryBuilderFinal<T> isRecoverable(final Predicate<Throwable> isRecoverable) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, reconnectExecutor, callbackExecutor,
                    errorConsumer, isRecoverable);
        }

        @Override
        public CompletionStage<T> completeFutureEventually(final CompletableFuture<T> future) {
            return new Retry<>(nameOfAction, sessionId, retriedSupplier,
                    checkNotNull(reconnectExecutor, "reconnectExecutor"),
                    checkNotNull(callbackExecutor, "callbackExecutor"),
                    errorConsumer, isRecoverable).completeFutureEventually(future);
        }
    }

}
