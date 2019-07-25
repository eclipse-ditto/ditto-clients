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
package org.eclipse.ditto.client.messaging.websocket.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkArgument;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.websocket.WsProviderConfiguration;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Provides Ditto WebSocket messaging specific configuration.
 *
 * @since 1.0.0
 */
public final class WsMessagingProviderConfigurationImpl implements WsProviderConfiguration<WsMessagingProvider> {

    private final URI endpointUri;
    private final boolean reconnectionEnabled;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationProvider<WebSocket> authenticationProvider;

    private WsMessagingProviderConfigurationImpl(final URI endpointUri,
            final boolean reconnectionEnabled, final AuthenticationConfiguration authenticationConfiguration,
            final AuthenticationProvider<WebSocket> authenticationProvider) {
        this.endpointUri = endpointUri;
        this.reconnectionEnabled = reconnectionEnabled;
        this.authenticationConfiguration = authenticationConfiguration;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * Returns a builder for creating an instance of {@code WsProviderConfiguration}.
     *
     * @return a builder for creating a Ditto WebSocket provider configuration object.
     */
    public static WebSocketMessagingProviderConfigurationBuilder newBuilder() {
        return new DefaultWebSocketMessagingProviderConfigurationBuilder();
    }

    @Override
    public WsMessagingProvider instantiateProvider() {
        return new WsMessagingProvider(this);
    }

    @Override
    public URI getEndpointUri() {
        return endpointUri;
    }

    @Override
    public boolean isReconnectionEnabled() {
        return reconnectionEnabled;
    }

    @Override
    public AuthenticationConfiguration getAuthenticationConfiguration() {
        return authenticationConfiguration;
    }

    @Override
    public AuthenticationProvider<WebSocket> getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    @SuppressWarnings({"squid:S00122", "squid:S1067"})
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WsMessagingProviderConfigurationImpl that = (WsMessagingProviderConfigurationImpl) o;
        return reconnectionEnabled == that.reconnectionEnabled &&
                Objects.equals(endpointUri, that.endpointUri) &&
                Objects.equals(authenticationConfiguration, that.authenticationConfiguration) &&
                Objects.equals(authenticationProvider, that.authenticationProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointUri, reconnectionEnabled, authenticationConfiguration, authenticationProvider);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "endpointUri=" + endpointUri +
                ", reconnectionEnabled=" + reconnectionEnabled +
                ", authenticationConfiguration=" + authenticationConfiguration +
                ", authenticationProvider=" + authenticationProvider +
                "]";
    }

    private static final class DefaultWebSocketMessagingProviderConfigurationBuilder
            implements WebSocketMessagingProviderConfigurationBuilder {

        private static final List<String> ALLOWED_URI_SCHEME = Arrays.asList("wss", "ws");

        private URI endpointUri;
        private boolean reconnectionEnabled;
        private AuthenticationConfiguration authenticationConfiguration;
        private AuthenticationProvider<WebSocket> authenticationProvider;

        DefaultWebSocketMessagingProviderConfigurationBuilder() {
            reconnectionEnabled = WsProviderConfiguration.DEFAULT_RECONNECTION_ENABLED;
        }

        @Override
        public WsMessagingProviderConfigurationImpl build() {
            return new WsMessagingProviderConfigurationImpl(endpointUri, reconnectionEnabled,
                    authenticationConfiguration, authenticationProvider);
        }

        @Override
        public AuthenticationConfigurationSettable endpoint(final String endpoint) {
            final URI uri = URI.create(checkNotNull(endpoint));
            final String uriScheme = uri.getScheme();
            checkArgument(uriScheme, ALLOWED_URI_SCHEME::contains, () -> {
                final String msgTemplate = "Scheme {0} not allowed for endpoint URI! Must be one of {1}.";
                return MessageFormat.format(msgTemplate, uriScheme, ALLOWED_URI_SCHEME);
            });

            this.endpointUri = uri;
            return this;
        }

        @Override
        public WebSocketMessagingProviderConfigurationBuilder reconnectionEnabled(
                final boolean reconnectionEnabled) {
            this.reconnectionEnabled = reconnectionEnabled;
            return this;
        }

        @Override
        public WebSocketMessagingProviderConfigurationBuilder authenticationConfiguration(
                final AuthenticationConfiguration authenticationConfiguration) {
            this.authenticationConfiguration = authenticationConfiguration;
            this.authenticationProvider = WsAuthenticationProvider.getInstance(authenticationConfiguration);
            return this;
        }

    }

}
