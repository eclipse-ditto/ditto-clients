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
package org.eclipse.ditto.client;

import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.configuration.CommonConfiguration;
import org.eclipse.ditto.client.internal.DittoClientImpl;

/**
 * Factory responsible for providing and configuring new instances of the {@link DittoClient Ditto Client}.
 * <p>
 * Requires built {@code Twin} {@link CommonConfiguration} and/or {@code Live} {@link CommonConfiguration} when creating
 * a new Client instance.
 * </p>
 * <h1>Usage example</h1>
 * <pre>
 * CredentialsAuthenticationConfiguration authenticationConfiguration = CredentialsAuthenticationConfiguration.newBuilder()
 *    .username(USERNAME)
 *    .password(PASSWORD)
 *    .build();
 * // optionally configure a proxy server or a truststore containing the trusted CAs for SSL connection establishment
 * ProxyConfiguration proxyConfiguration = ProxyConfiguration.newBuilder()
 *    .proxyHost(PROXY_HOST)
 *    .proxyPort(PROXY_PORT)
 *    .build();
 * TrustStoreConfiguration trustStoreConfiguration = TrustStoreConfiguration.newBuilder()
 *    .location(TRUSTSTORE_LOCATION)
 *    .password(TRUSTSTORE_PASSWORD)
 *    .build();
 * // provide configuration for "twin" client:
 * CommonConfiguration configuration = DittoClientFactory.configurationBuilder()
 *    .providerConfiguration(MessagingProviders.dittoWebsocketProviderBuilder()
 *       .authenticationConfiguration(authenticationConfiguration)
 *       .build()
 *    )
 *    .proxyConfiguration(proxyConfiguration)
 *    .trustStoreConfiguration(trustStoreConfiguration)
 *    .build();
 * DittoClient client = DittoClientFactory.newInstance(configuration);
 * </pre>
 * <h1>Logging</h1> The Ditto Client uses {@code SLF4J} as underlying logging framework. To activate the logging for
 * the client the favored {@code SLF4J} log-binding must be placed on the classpath and can be configured by the
 * appropriate configuration mechanism. The most important log messages are logged on {@code INFO} level.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
@Immutable
public final class DittoClientFactory {

    private DittoClientFactory() {
        throw new AssertionError();
    }

    /**
     * Creates a new {@link DittoClient Ditto Client} instance with a shared {@code Twin} and {@code Live}
     * configuration.
     *
     * @param sharedConfiguration the CommonConfiguration to use for this client - a Builder may be obtained by invoking
     * {@link #configurationBuilder()} on this Factory
     * @return the {@link DittoClient Ditto Client} instance with the configured {@code Twin} and {@code Live}
     * configuration
     * @throws org.eclipse.ditto.client.exceptions.ClientAuthenticationException if the authentication with the given
     * credentials failed
     * @throws org.eclipse.ditto.client.exceptions.ClientConnectException if a connection to the configured endpoint
     * could not be established
     */
    public static DittoClient newInstance(final CommonConfiguration sharedConfiguration) {
        return newInstance(Optional.of(sharedConfiguration), Optional.of(sharedConfiguration));
    }

    /**
     * Creates a new {@link DittoClient Ditto Client} instance with {@code Twin} AND {@code Live} configuration - both
     * {@code twin()} and {@code live()} API of the Client will be usable.
     *
     * @param twinConfiguration the CommonConfiguration to use for the {@code Twin} part of the client - a Builder may
     * be obtained by invoking {@link #configurationBuilder()} on this Factory
     * @param liveConfiguration the CommonConfiguration to use for the {@code Live} part of the client - a Builder may
     * be obtained by invoking {@link #configurationBuilder()} on this Factory
     * @return the {@link DittoClient Ditto Client} instance with the configured Twin+Live configuration
     * @throws org.eclipse.ditto.client.exceptions.ClientAuthenticationException if the authentication with the given
     * credentials failed
     * @throws org.eclipse.ditto.client.exceptions.ClientConnectException if a connection to the configured endpoint
     * could not be established
     */
    public static DittoClient newInstance(final CommonConfiguration twinConfiguration,
            final CommonConfiguration liveConfiguration) {

        return DittoClientImpl.newInstance(Optional.of(twinConfiguration), Optional.of(liveConfiguration));
    }

    /**
     * Creates a new {@link DittoClient Ditto Client} instance with Optional {@code Twin} AND {@code Live} configuration
     * - both {@code twin()} and {@code live()} API of the Client will be usable when both optionals were not empty.
     * Otherwise RuntimeExceptions will be thrown when one API is invoked whose optional was {@code empty}.
     *
     * @param twinConfiguration the CommonConfiguration to use for the {@code Twin} part of the client - a Builder may
     * be obtained by invoking {@link #configurationBuilder()} on this Factory
     * @param liveConfiguration the CommonConfiguration to use for the {@code Live} part of the client - a Builder may
     * be obtained by invoking {@link #configurationBuilder()} on this Factory
     * @return the {@link DittoClient Ditto Client} instance with the configured Twin+Live configuration
     * @throws org.eclipse.ditto.client.exceptions.ClientAuthenticationException if the authentication with the given
     * credentials failed
     * @throws org.eclipse.ditto.client.exceptions.ClientConnectException if a connection to the configured endpoint
     * could not be established
     */
    public static DittoClient newInstance(final Optional<CommonConfiguration> twinConfiguration,
            final Optional<CommonConfiguration> liveConfiguration) {

        return DittoClientImpl.newInstance(twinConfiguration, liveConfiguration);
    }

    /**
     * Initializes a new ConfigurationBuilder in order to conveniently build a {@link CommonConfiguration}.
     *
     * @return a new ConfigurationBuilder
     */
    public static CommonConfiguration.ConfigurationBuilder configurationBuilder() {
        return CommonConfiguration.newBuilder();
    }

}
