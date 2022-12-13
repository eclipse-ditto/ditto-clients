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

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;

/**
 * Provides configuration for messaging.
 *
 * @since 1.0.0
 */
public interface MessagingConfiguration {

    /**
     * Returns how long to wait for a response before giving up.
     *
     * @return the timeout.
     */
    Duration getTimeout();

    /**
     * Returns the JSON schema version to use for messaging.
     *
     * @return the schema version.
     */
    JsonSchemaVersion getJsonSchemaVersion();

    /**
     * Returns the Ditto endpoint URI to use for messaging.
     *
     * @return the URI.
     */
    URI getEndpointUri();

    /**
     * Returns the labels of all acknowledgements that are declared to be provided by this connection.
     *
     * @return the acknowledgment labels.
     * @since 1.4.0
     */
    Set<AcknowledgementLabel> getDeclaredAcknowledgements();

    /**
     * @return {@code true} if client should try to reconnect when connection is lost.
     */
    boolean isReconnectEnabled();

    /**
     * @return {@code true} if client should retry when connection initialization failed.
     *
     * @since 1.3.0
     */
    boolean isInitialConnectRetryEnabled();

    /**
     * Returns the proxy configuration.
     *
     * @return the configuration or an empty optional.
     */
    Optional<ProxyConfiguration> getProxyConfiguration();

    /**
     * Returns the trust store configuration.
     *
     * @return the configuration or an empty optional.
     */
    Optional<TrustStoreConfiguration> getTrustStoreConfiguration();

    /**
     * Returns the connection error handler.
     *
     * @return the connection error handler or an empty optional.
     */
    Optional<Consumer<Throwable>> getConnectionErrorHandler();

    /**
     * Returns the disconnected listener.
     *
     * @return the disconnected listener or an empty optional.
     */
    Optional<Consumer<DisconnectedContext>> getDisconnectedListener();

    /**
     * Builder for creating an instance of {@code MessagingConfiguration} by utilizing Object Scoping and Method
     * Chaining.
     */
    interface Builder {

        /**
         * Set the timeout waiting for a response.
         *
         * @param timeout the timeout.
         * @return this builder.
         */
        Builder timeout(Duration timeout);

        /**
         * Sets the {@code JSON schema version}.
         * <p>
         * Default is {@link org.eclipse.ditto.base.model.json.JsonSchemaVersion#LATEST}.
         *
         * @param jsonSchemaVersion the schema version to set.
         * @return this builder.
         */
        Builder jsonSchemaVersion(JsonSchemaVersion jsonSchemaVersion);

        /**
         * Sets the {@code endpoint}.
         *
         * @param endpoint the endpoint to set.
         * @return this builder.
         */
        Builder endpoint(String endpoint);

        /**
         * Sets the labels of all acknowledgements that are declared to be provided by this client session/connection.
         *
         * @param acknowledgementLabels the acknowledgement labels
         * @return this builder.
         * @since 1.4.0
         */
        Builder declaredAcknowledgements(Collection<AcknowledgementLabel> acknowledgementLabels);

        /**
         * Sets if {@code reconnectEnabled}.
         * <p> Default is enabled. If a connection was established once, the client tries to reconnect <em>every 5
         * seconds</em>.
         *
         * @param reconnectEnabled enables/disables reconnect.
         * @return this builder.
         */
        Builder reconnectEnabled(boolean reconnectEnabled);

        /**
         * Sets if {@code initialConnectRetryEnabled}.
         * <p> Default is disabled. When establishing a new connection, the client doesn't try to reconnect.
         *
         * @param initialConnectRetryEnabled enables/disables retrying connection initialization.
         * @return this builder.
         *
         * @since 1.3.0
         */
        Builder initialConnectRetryEnabled(boolean initialConnectRetryEnabled);

        /**
         * Sets the {@code proxyConfiguration}.
         *
         * @param proxyConfiguration the proxy configuration to set.
         * @return this builder.
         */
        Builder proxyConfiguration(@Nullable ProxyConfiguration proxyConfiguration);

        /**
         * Sets the {@code trustStoreConfiguration}.
         *
         * @param trustStoreConfiguration the trust store configuration to set.
         * @return this builder.
         */
        Builder trustStoreConfiguration(TrustStoreConfiguration trustStoreConfiguration);

        /**
         * Register a consumer of errors which occur during opening the connection initially and on reconnects.
         *
         * @param handler the handler that will be called with the cause of the connection error.
         * @return this builder.
         * @since 1.2.0
         */
        Builder connectionErrorHandler(@Nullable Consumer<Throwable> handler);

        /**
         * Register a contextListener which is notified whenever the connection is disconnected.
         *
         * @param contextListener the handler that will be called with details about the disconnection.
         * @return this builder.
         * @since 2.1.0
         */
        Builder disconnectedListener(@Nullable Consumer<DisconnectedContext> contextListener);

        /**
         * Creates a new instance of {@code MessagingConfiguration}.
         *
         * @return a new {@code MessagingConfiguration} object based on the arguments provided to this builder.
         */
        MessagingConfiguration build();

    }

}
