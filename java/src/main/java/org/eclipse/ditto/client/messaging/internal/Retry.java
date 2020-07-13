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

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(Retry.class.getName());

    private final String sessionId;
    private final String nameOfAction;
    private final Supplier<T> retriedSupplier;
    private final Duration timeToWaitBeforeRetry;
    private final ExecutorService executorService;


    private Retry(final String nameOfAction,
            final String sessionId,
            final Supplier<T> retriedSupplier,
            final Duration timeToWaitBeforeRetry,
            final ExecutorService executorService) {

        this.sessionId = sessionId;
        this.nameOfAction = nameOfAction;
        this.retriedSupplier = retriedSupplier;
        this.timeToWaitBeforeRetry = timeToWaitBeforeRetry;
        this.executorService = executorService;
    }

    /**
     * Executes the provided supplier until a result is returned.
     *
     * @return A completion stage which finally completes with the result of the supplier. Result can be null.
     */
    @Override
    public CompletionStage<T> get() {
        return CompletableFuture.supplyAsync(this::doGet, executorService);
    }

    private T doGet() {
        try {
            return retriedSupplier.get();
        } catch (final RuntimeException e) {
            // log error, but try again (don't end loop)
            LOGGER.error("Client <{}>: Failed to <{}>: {}.", sessionId, nameOfAction, e.getMessage());
            waitFor(timeToWaitBeforeRetry);
            return doGet();
        }
    }

    private void waitFor(final Duration timeToWait) {
        try {
            LOGGER.info("Client <{}>: Waiting for <{}> seconds before retrying to <{}>.",
                    sessionId, timeToWait.getSeconds(), nameOfAction);
            TimeUnit.SECONDS.sleep(timeToWait.getSeconds());
        } catch (final InterruptedException ie) {
            LOGGER.error("Client <{}>: Interrupted while waiting for the next try to <{}>.",
                    sessionId, nameOfAction, ie);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates a builder to configure the parameters that are relevant for the retry logic.
     *
     * @param nameOfAction A name of the action that is retried. This is only used for logging. E.g. "fetch services".
     * @param supplierToRetry The action that should be retried until a result is returned. Result ca also be null.
     * @param <T> The type of the expected result.
     * @return The result of the supplier after it finally succeeds.
     */
    static <T> RetryBuilderStep1<T> retryTo(final String nameOfAction, Supplier<T> supplierToRetry) {
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
        RetryBuilderFinal<T> inClientSession(String sessionId);
    }

    /**
     * Final builder steps which are optionally required for the retry logic.
     *
     * @param <T> The return type of the supplier.
     */
    interface RetryBuilderFinal<T> extends Supplier<CompletionStage<T>> {

        /**
         * Specifies the executor to use when performing the action and its potential retries.
         *
         * @param executorService the executor service.
         * @return a new instance of this builder step.
         */
        RetryBuilderFinal<T> withExecutor(final ExecutorService executorService);

        /**
         * Specifies the time to wait before a retry should be performed.
         *
         * @param timeToWaitBeforeRetry the time to wait before a retry should be performed.
         * @return a new instance of this builder step.
         */
        RetryBuilderFinal<T> andWaitBetweenRetriesFor(Duration timeToWaitBeforeRetry);

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
    static class RetryBuilder<T> implements RetryBuilderStep1<T>, RetryBuilderFinal<T> {

        private static final Duration DEFAULT_TIME_TO_WAIT_BETWEEN_RETRIES = Duration.ofSeconds(1);

        private final String nameOfAction;
        private final Supplier<T> retriedSupplier;
        private final String sessionId;
        private final Duration timeToWaitBeforeRetry;
        private final ExecutorService executorService;

        private RetryBuilder(final String nameOfAction, final Supplier<T> retriedSupplier) {
            this(nameOfAction, retriedSupplier, "", DEFAULT_TIME_TO_WAIT_BETWEEN_RETRIES, ForkJoinPool.commonPool());
        }

        private RetryBuilder(final String nameOfAction,
                final Supplier<T> retriedSupplier,
                final String sessionId,
                final Duration timeToWaitBeforeRetry,
                final ExecutorService executorService) {

            this.nameOfAction = nameOfAction;
            this.retriedSupplier = retriedSupplier;
            this.sessionId = sessionId;
            this.timeToWaitBeforeRetry = timeToWaitBeforeRetry;
            this.executorService = executorService;
        }

        @Override
        public RetryBuilderFinal<T> inClientSession(final String sessionId) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, timeToWaitBeforeRetry, executorService);
        }

        @Override
        public RetryBuilderFinal<T> withExecutor(final ExecutorService executorService) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, timeToWaitBeforeRetry, executorService);
        }

        @Override
        public RetryBuilderFinal<T> andWaitBetweenRetriesFor(final Duration timeToWaitBeforeRetry) {
            return new RetryBuilder<>(nameOfAction, retriedSupplier, sessionId, timeToWaitBeforeRetry, executorService);
        }


        @Override
        public CompletionStage<T> get() {
            return new Retry<>(nameOfAction, sessionId, retriedSupplier, timeToWaitBeforeRetry, executorService).get();
        }

    }

}
