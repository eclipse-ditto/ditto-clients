/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.configuration;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

/**
 * Contains configuration about the bus to configure for asynchronous operations.
 *
 * @since 1.0.0
 */
public final class BusConfiguration {

    @Nullable
    private final ExecutorService executorService;

    private BusConfiguration(@Nullable final ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * @return a new builder used to create a BusConfiguration object.
     */
    public static BusConfigurationBuilder newBuilder() {
        return new Builder();
    }

    /**
     * Returns the custom {@link ExecutorService} to use (if any).
     *
     * @return the custom ExecutorService to use.
     */
    public Optional<ExecutorService> getExecutorService() {
        return Optional.ofNullable(executorService);
    }

    /**
     * Entry point for building a BusConfiguration object.
     */
    public interface BusConfigurationBuilder extends BusExecutorServiceOptionsSettable {
    }

    /**
     * Allows setting the options for the ExecutorService to use.
     */
    public interface BusExecutorServiceOptionsSettable extends BusConfigurationBuildable {

        /**
         * Configures a custom {@link ExecutorService} to use - if not configured, a default ExecutorService is used.
         *
         * @param executorService the custom ExecutorService to use.
         * @return a builder object to build the BusConfiguration.
         */
        BusConfigurationBuildable executorService(ExecutorService executorService);
    }

    /**
     * Allows building the DispatcherConfiguration.
     */
    public interface BusConfigurationBuildable {

        /**
         * @return new DispatcherConfiguration instance
         */
        BusConfiguration build();
    }

    private static final class Builder implements BusConfigurationBuilder, BusExecutorServiceOptionsSettable,
            BusConfigurationBuildable {

        private ExecutorService executorService = null;


        @Override
        public BusConfigurationBuildable executorService(final ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        @Override
        public BusConfiguration build() {
            return new BusConfiguration(executorService);
        }
    }
}
