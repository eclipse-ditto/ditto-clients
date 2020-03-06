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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkArgument;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

/**
 * Provides Ditto WebSocket messaging specific configuration.
 *
 * @since 1.0.0
 */
public final class WebSocketMessagingConfiguration implements MessagingConfiguration {

    private final JsonSchemaVersion jsonSchemaVersion;
    private final URI endpointUri;
    private final boolean reconnectEnabled;
    @Nullable private final ProxyConfiguration proxyConfiguration;
    @Nullable private final TrustStoreConfiguration trustStoreConfiguration;

    private WebSocketMessagingConfiguration(final JsonSchemaVersion jsonSchemaVersion, final URI endpointUri,
            final boolean reconnectEnabled, @Nullable final ProxyConfiguration proxyConfiguration,
            @Nullable final TrustStoreConfiguration trustStoreConfiguration) {
        this.jsonSchemaVersion = jsonSchemaVersion;
        this.endpointUri = endpointUri;
        this.reconnectEnabled = reconnectEnabled;
        this.proxyConfiguration = proxyConfiguration;
        this.trustStoreConfiguration = trustStoreConfiguration;
    }

    public static MessagingConfiguration.Builder newBuilder() {
        return new WebSocketMessagingConfigurationBuilder();
    }

    @Override
    public JsonSchemaVersion getJsonSchemaVersion() {
        return jsonSchemaVersion;
    }

    @Override
    public URI getEndpointUri() {
        return endpointUri;
    }

    @Override
    public boolean isReconnectEnabled() {
        return reconnectEnabled;
    }

    @Override
    public Optional<ProxyConfiguration> getProxyConfiguration() {
        return Optional.ofNullable(proxyConfiguration);
    }

    @Override
    public Optional<TrustStoreConfiguration> getTrustStoreConfiguration() {
        return Optional.ofNullable(trustStoreConfiguration);
    }

    private static final class WebSocketMessagingConfigurationBuilder implements MessagingConfiguration.Builder {

        private static final List<String> ALLOWED_URI_SCHEME = Arrays.asList("wss", "ws");
        private static final String WS_PATH = "/ws/";

        private JsonSchemaVersion jsonSchemaVersion = JsonSchemaVersion.LATEST;
        private URI endpointUri;
        private boolean reconnectEnabled = true;
        private ProxyConfiguration proxyConfiguration;
        private TrustStoreConfiguration trustStoreConfiguration;
        
        @Override
        public MessagingConfiguration.Builder jsonSchemaVersion(final JsonSchemaVersion jsonSchemaVersion) {
            this.jsonSchemaVersion = checkNotNull(jsonSchemaVersion, "jsonSchemaVersion");
            return this;
        }

        @Override
        public MessagingConfiguration.Builder endpoint(final String endpoint) {
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
        public MessagingConfiguration.Builder reconnectEnabled(final boolean reconnectEnabled) {
            this.reconnectEnabled = reconnectEnabled;
            return this;
        }

        @Override
        public MessagingConfiguration.Builder proxyConfiguration(final ProxyConfiguration proxyConfiguration) {
            this.proxyConfiguration = checkNotNull(proxyConfiguration, "proxyConfiguration");
            return this;
        }

        @Override
        public MessagingConfiguration.Builder trustStoreConfiguration(
                final TrustStoreConfiguration trustStoreConfiguration) {
            this.trustStoreConfiguration = checkNotNull(trustStoreConfiguration, "trustStoreConfiguration");
            return this;
        }

        @Override
        public MessagingConfiguration build() {
            final URI wsEndpointUri = appendWsPath(this.endpointUri, jsonSchemaVersion);
            return new WebSocketMessagingConfiguration(jsonSchemaVersion, wsEndpointUri, reconnectEnabled,
                    proxyConfiguration, trustStoreConfiguration);
        }

        private static URI appendWsPath(final URI baseUri, final JsonSchemaVersion schemaVersion) {
            final String pathWithoutTrailingSlashes = baseUri.getPath().replaceFirst("/+$", "");
            final String newPath = pathWithoutTrailingSlashes + WS_PATH + schemaVersion.toString();
            return baseUri.resolve(newPath);
        }

    }

}
