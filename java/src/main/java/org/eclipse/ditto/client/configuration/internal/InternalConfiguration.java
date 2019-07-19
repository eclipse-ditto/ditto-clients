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
package org.eclipse.ditto.client.configuration.internal;

import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.configuration.BusConfiguration;
import org.eclipse.ditto.client.configuration.CommonConfiguration;
import org.eclipse.ditto.client.configuration.ProviderConfiguration;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.internal.bus.BusFactory;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.live.LiveFeatureHandle;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds Ditto client internal runtime configuration.
 *
 * @since 1.0.0
 */
public final class InternalConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalConfiguration.class);
    private static final String NOT_DEFINED_ERROR_MSG = "The expected configuration is not defined.";

    @Nullable private final CommonConfiguration twinConfiguration;
    @Nullable private final CommonConfiguration liveConfiguration;
    @Nullable private final PointerBus twinBus;
    @Nullable private final PointerBus liveBus;
    private final ResponseForwarder responseForwarder;
    @Nullable private final MessagingProvider twinMessagingProvider;
    @Nullable private final MessagingProvider liveMessagingProvider;
    @Nullable private final HandlerRegistry<TwinThingHandle, TwinFeatureHandle> twinHandlerRegistry;
    @Nullable private final HandlerRegistry<LiveThingHandle, LiveFeatureHandle> liveHandlerRegistry;

    /**
     * Constructs a new {@code DittoClientConfiguration} object.
     *
     * @param twinConfiguration the twin configuration.
     * @param liveConfiguration the live configuration.
     */
    public InternalConfiguration(
            final Optional<CommonConfiguration> twinConfiguration,
            final Optional<CommonConfiguration> liveConfiguration) {

        this(
                twinConfiguration.orElse(null),
                liveConfiguration.orElse(null),
                twinConfiguration
                        .map(CommonConfiguration::getBusConfiguration)
                        .flatMap(BusConfiguration::getExecutorService)
                        .map(executorService -> BusFactory.createPointerBus("twin", executorService))
                        .orElseGet(() -> BusFactory.createPointerBus("twin")),
                liveConfiguration
                        .map(CommonConfiguration::getBusConfiguration)
                        .flatMap(BusConfiguration::getExecutorService)
                        .map(executorService -> BusFactory.createPointerBus("live", executorService))
                        .orElseGet(() -> BusFactory.createPointerBus("live")),
                twinConfiguration
                        .map(CommonConfiguration::getProviderConfiguration)
                        .orElse(null),
                liveConfiguration
                        .map(CommonConfiguration::getProviderConfiguration)
                        .orElse(null));
    }

    /**
     * Constructs a new {@code InternalConfiguration} object.
     * <p>
     * Package private constructor used for tests.
     * </p>
     */
    InternalConfiguration(@Nullable final CommonConfiguration twinConfiguration,
            @Nullable final CommonConfiguration liveConfiguration,
            @Nullable final PointerBus twinBus,
            @Nullable final PointerBus liveBus,
            @Nullable final ProviderConfiguration twinProviderConfiguration,
            @Nullable final ProviderConfiguration liveProviderConfiguration) {

        this.twinConfiguration = twinConfiguration;
        this.liveConfiguration = liveConfiguration;
        this.twinBus = twinBus;
        this.liveBus = liveBus;
        responseForwarder = ResponseForwarder.getInstance();
        twinMessagingProvider =
                twinProviderConfiguration != null ? twinProviderConfiguration.instantiateProvider() : null;
        // if the 2 instances are equal
        if (twinProviderConfiguration != null && twinProviderConfiguration.equals(liveProviderConfiguration)) {
            LOGGER.debug("Twin and Live ProviderConfigurations are equal - reusing Twin MessagingProvider as Live " +
                    "one..");
            liveMessagingProvider = twinMessagingProvider; // then use the twinMessagingProvider also as live one
        } else {
            LOGGER.debug("Twin and Live ProviderConfigurations differ - instantiating second MessagingProvider..");
            // otherwise, instantiate a new one:
            liveMessagingProvider =
                    liveProviderConfiguration != null ? liveProviderConfiguration.instantiateProvider() : null;
        }
        twinHandlerRegistry = twinBus != null ? new HandlerRegistry<>(twinBus) : null;
        liveHandlerRegistry = liveBus != null ? new HandlerRegistry<>(liveBus) : null;
    }

    /**
     * @return the twin configuration
     */
    public Optional<CommonConfiguration> getTwinConfiguration() {
        return Optional.ofNullable(twinConfiguration);
    }

    /**
     * Returns the Twin CommonConfiguration or fails if it is not present.
     *
     * @return the Twin CommonConfiguration
     * @throws IllegalStateException if the Twin CommonConfiguration is not present
     */
    public CommonConfiguration getTwinConfigurationOrFail() {
        return getTwinConfiguration().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return the live configuration
     */
    public Optional<CommonConfiguration> getLiveConfiguration() {
        return Optional.ofNullable(liveConfiguration);
    }

    /**
     * Returns the Live CommonConfiguration or fails if it is not present.
     *
     * @return the Live CommonConfiguration
     * @throws IllegalStateException if the Live CommonConfiguration is not present
     */
    public CommonConfiguration getLiveConfigurationOrFail() {
        return getLiveConfiguration().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return the event bus instance used for incoming twin events
     */
    public Optional<PointerBus> getTwinBus() {
        return Optional.ofNullable(twinBus);
    }

    /**
     * Returns the Twin Bus or fails if it is not present.
     *
     * @return the Twin Bus
     * @throws IllegalStateException if the Twin Bus is not present
     */
    public PointerBus getTwinBusOrFail() {
        return getTwinBus().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return the event bus instance used for incoming live events
     */
    public Optional<PointerBus> getLiveBus() {
        return Optional.ofNullable(liveBus);
    }

    /**
     * Returns the Live Bus or fails if it is not present.
     *
     * @return the Live Bus
     * @throws IllegalStateException if the Live Bus is not present
     */
    public PointerBus getLiveBusOrFail() {
        return getLiveBus().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return the MessagingProvider instance used for twin
     */
    public Optional<MessagingProvider> getTwinMessagingProvider() {
        return Optional.ofNullable(twinMessagingProvider);
    }

    /**
     * Returns the Twin MessagingProvider or fails if it is not present.
     *
     * @return the Twin MessagingProvider
     * @throws IllegalStateException if the Twin MessagingProvider is not present
     */
    public MessagingProvider getTwinMessagingProviderOrFail() {
        return getTwinMessagingProvider().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return the MessagingProvider instance used for live
     */
    public Optional<MessagingProvider> getLiveMessagingProvider() {
        return Optional.ofNullable(liveMessagingProvider);
    }

    /**
     * Returns the Live MessagingProvider or fails if it is not present.
     *
     * @return the Live MessagingProvider
     * @throws IllegalStateException if the Live MessagingProvider is not present
     */
    public MessagingProvider getLiveMessagingProviderOrFail() {
        return getLiveMessagingProvider().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return the HandlerRegistry instance used for twin
     */
    public Optional<HandlerRegistry<TwinThingHandle, TwinFeatureHandle>> getTwinHandlerRegistry() {
        return Optional.ofNullable(twinHandlerRegistry);
    }

    /**
     * Returns the Twin HandlerRegistry or fails if it is not present.
     *
     * @return the Twin HandlerRegistry
     * @throws IllegalStateException if the Twin HandlerRegistry is not present
     */
    public HandlerRegistry<TwinThingHandle, TwinFeatureHandle> getTwinHandlerRegistryOrFail() {
        return getTwinHandlerRegistry().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return the HandlerRegistry instance used for live
     */
    public Optional<HandlerRegistry<LiveThingHandle, LiveFeatureHandle>> getLiveHandlerRegistry() {
        return Optional.ofNullable(liveHandlerRegistry);
    }

    /**
     * Returns the Live HandlerRegistry or fails if it is not present.
     *
     * @return the Live HandlerRegistry
     * @throws IllegalStateException if the Live HandlerRegistry is not present
     */
    public HandlerRegistry<LiveThingHandle, LiveFeatureHandle> getLiveHandlerRegistryOrFail() {
        return getLiveHandlerRegistry().orElseThrow(() -> new IllegalStateException(NOT_DEFINED_ERROR_MSG));
    }

    /**
     * @return response handler
     */
    public ResponseForwarder getResponseForwarder() {
        return responseForwarder;
    }

}
