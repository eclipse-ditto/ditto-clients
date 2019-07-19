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
package org.eclipse.ditto.client.messaging.websocket;

import java.net.URI;

import org.eclipse.ditto.client.configuration.CredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProviderConfiguration;
import org.eclipse.ditto.client.messaging.MessagingProvider;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Contains the WebSocket {@link MessagingProvider} specific configuration options.
 *
 * @since 1.0.0
 */
public interface WsProviderConfiguration<T extends MessagingProvider> extends ProviderConfiguration<T, WebSocket> {

    /**
     * The default Ditto WS endpoint URI - the Ditto sandbox.
     */
    String DEFAULT_END_POINT_URI = "wss://ditto.eclipse.org/";

    /**
     * The default whether automatic reconnection on connection loss is enabled or not.
     */
    boolean DEFAULT_RECONNECTION_ENABLED = true;

    /**
     * Returns the Ditto WebSocket endpoint URI to use for the connection.
     *
     * @return the value of the endpoint URI.
     */
    URI getEndpointUri();

    /**
     * @return {@code true} if client should try to reconnect when connection to server is lost.
     */
    boolean isReconnectionEnabled();

    /**
     * Defines a method for setting the required authentication configuration for the message provider.
     */
    interface AuthenticationConfigurationSettable {

        /**
         * Sets the authentication configuration.
         *
         * @param authenticationConfiguration the authentication configuration.
         * @return an object handle for building a {@link WebSocketMessagingProviderConfigurationBuilder} object based
         * on the arguments provided to this builder.
         */
        WebSocketMessagingProviderConfigurationBuilder authenticationConfiguration(
                CredentialsAuthenticationConfiguration authenticationConfiguration);

    }

    /**
     * Defines a method for setting the Websocket endpoint.
     */
    interface EndpointSettable {

        /**
         * Sets the {@code endpoint} of the Websocket.
         *
         * @param endpoint the value of the Ditto WebSocket endpoint to connect to.
         * @return an object handle for building a {@link WsProviderConfiguration} object based on the
         * arguments provided to this builder.
         */
        WebSocketMessagingProviderConfigurationBuilder endpoint(String endpoint);

    }

    /**
     * Defines a method for setting the reconnectionEnabled value.
     */
    interface ReconnectionEnabledSettable {

        /**
         * Enables/disables reconnection to server. Reconnection is enabled by default. If enabled the client tries to
         * reconnect to the server continuously <em>every 5 seconds</em> if a connection was once established
         * successfully.
         *
         * @param reconnectionEnabled enables/disables reconnection
         * @return an object handle for building a {@link WsProviderConfiguration} object based on the
         * arguments provided to this builder.
         */
        WebSocketMessagingProviderConfigurationBuilder reconnectionEnabled(boolean reconnectionEnabled);

    }

    /**
     * Defines a method for building a {@link WsProviderConfiguration} object based on the arguments
     * provided to this builder.
     */
    interface WebSocketMessagingProviderConfigurationBuildable {

        /**
         * Creates a new instance of {@link WsProviderConfiguration}.
         *
         * @return a new {@code WsProviderConfiguration} object based on the arguments
         * provided to this builder.
         */
        WsProviderConfiguration build();

    }

    /**
     * Builder for creating an instance of {@link WsProviderConfiguration} by utilizing Object Scoping
     * and Method Chaining.
     */
    interface WebSocketMessagingProviderConfigurationBuilder
            extends EndpointSettable, WebSocketMessagingProviderConfigurationBuildable,
            ReconnectionEnabledSettable, AuthenticationConfigurationSettable {

        // noop

    }

}
