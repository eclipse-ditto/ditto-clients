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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkArgument;
import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;

/**
 * Provides Ditto WebSocket messaging specific configuration.
 *
 * @since 1.0.0
 */
public final class WebSocketMessagingConfiguration implements MessagingConfiguration {

    private final Duration timeout;
    private final JsonSchemaVersion jsonSchemaVersion;
    @Nullable private final String defaultNamespace;
    private final URI endpointUri;
    private final boolean reconnectEnabled;
    private final boolean initialConnectRetryEnabled;
    @Nullable private final ProxyConfiguration proxyConfiguration;
    @Nullable private final TrustStoreConfiguration trustStoreConfiguration;
    @Nullable private final Consumer<Throwable> connectionErrorHandler;
    @Nullable private final Consumer<DisconnectedContext> disconnectedListener;
    private final Set<AcknowledgementLabel> declaredAcknowledgements;

    public WebSocketMessagingConfiguration(final WebSocketMessagingConfigurationBuilder builder,
            final URI endpointUri) {

        jsonSchemaVersion = builder.jsonSchemaVersion;
        defaultNamespace = builder.defaultNamespace;
        reconnectEnabled = builder.reconnectEnabled;
        initialConnectRetryEnabled = builder.initialConnectRetryEnabled;
        proxyConfiguration = builder.proxyConfiguration;
        trustStoreConfiguration = builder.trustStoreConfiguration;
        connectionErrorHandler = builder.connectionErrorHandler;
        disconnectedListener = builder.disconnectedListener;
        this.timeout = builder.timeout;
        this.declaredAcknowledgements = Collections.unmodifiableSet(builder.declaredAcknowledgements);
        this.endpointUri = endpointUri;
    }

    public static MessagingConfiguration.Builder newBuilder() {
        return new WebSocketMessagingConfigurationBuilder();
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public JsonSchemaVersion getJsonSchemaVersion() {
        return jsonSchemaVersion;
    }

    @Override
    public Optional<String> getDefaultNamespace() {
        return Optional.ofNullable(defaultNamespace);
    }

    @Override
    public URI getEndpointUri() {
        return endpointUri;
    }

    @Override
    public Set<AcknowledgementLabel> getDeclaredAcknowledgements() {
        return declaredAcknowledgements;
    }

    @Override
    public boolean isReconnectEnabled() {
        return reconnectEnabled;
    }

    @Override
    public boolean isInitialConnectRetryEnabled() {
        return initialConnectRetryEnabled;
    }

    @Override
    public Optional<ProxyConfiguration> getProxyConfiguration() {
        return Optional.ofNullable(proxyConfiguration);
    }

    @Override
    public Optional<TrustStoreConfiguration> getTrustStoreConfiguration() {
        return Optional.ofNullable(trustStoreConfiguration);
    }

    @Override
    public Optional<Consumer<Throwable>> getConnectionErrorHandler() {
        return Optional.ofNullable(connectionErrorHandler);
    }

    @Override
    public Optional<Consumer<DisconnectedContext>> getDisconnectedListener() {
        return Optional.ofNullable(disconnectedListener);
    }

    private static final class WebSocketMessagingConfigurationBuilder implements MessagingConfiguration.Builder {

        private static final List<String> ALLOWED_URI_SCHEME = Arrays.asList("wss", "ws");
        private static final String WS_PATH = "/ws/";
        private static final String WS_PATH_REGEX = "/ws/2/?";

        private JsonSchemaVersion jsonSchemaVersion;
        private Duration timeout = Duration.ofSeconds(60L);
        private URI endpointUri;
        @Nullable private String defaultNamespace;
        private boolean reconnectEnabled;
        private boolean initialConnectRetryEnabled;
        @Nullable private ProxyConfiguration proxyConfiguration;
        private TrustStoreConfiguration trustStoreConfiguration;
        @Nullable private Consumer<Throwable> connectionErrorHandler;
        @Nullable private Consumer<DisconnectedContext> disconnectedListener;
        private final Set<AcknowledgementLabel> declaredAcknowledgements = new HashSet<>();

        private WebSocketMessagingConfigurationBuilder() {
            jsonSchemaVersion = JsonSchemaVersion.LATEST;
            defaultNamespace = null;
            reconnectEnabled = true;
            initialConnectRetryEnabled = false;
            proxyConfiguration = null;
            connectionErrorHandler = null;
            disconnectedListener = null;
        }

        @Override
        public Builder timeout(final Duration timeout) {
            this.timeout = timeout;
            return this;
        }

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

            endpointUri = uri;
            return this;
        }

        @Override
        public Builder defaultNamespace(final String defaultNamespace) {
            this.defaultNamespace = checkNotNull(defaultNamespace, "defaultNamespace");
            return this;
        }

        @Override
        public Builder declaredAcknowledgements(final Collection<AcknowledgementLabel> acknowledgementLabels) {
            this.declaredAcknowledgements.clear();
            this.declaredAcknowledgements.addAll(acknowledgementLabels);
            return this;
        }

        @Override
        public MessagingConfiguration.Builder reconnectEnabled(final boolean reconnectEnabled) {
            this.reconnectEnabled = reconnectEnabled;
            return this;
        }

        @Override
        public MessagingConfiguration.Builder initialConnectRetryEnabled(final boolean initialConnectRetryEnabled) {
            this.initialConnectRetryEnabled = initialConnectRetryEnabled;
            return this;
        }

        @Override
        public MessagingConfiguration.Builder proxyConfiguration(
                @Nullable final ProxyConfiguration proxyConfiguration) {
            this.proxyConfiguration = proxyConfiguration;
            return this;
        }

        @Override
        public MessagingConfiguration.Builder trustStoreConfiguration(
                final TrustStoreConfiguration trustStoreConfiguration) {
            this.trustStoreConfiguration = checkNotNull(trustStoreConfiguration, "trustStoreConfiguration");
            return this;
        }

        @Override
        public Builder connectionErrorHandler(@Nullable final Consumer<Throwable> handler) {
            this.connectionErrorHandler = handler;
            return this;
        }

        @Override
        public Builder disconnectedListener(@Nullable final Consumer<DisconnectedContext> contextListener) {
            this.disconnectedListener = contextListener;
            return this;
        }

        @Override
        public MessagingConfiguration build() {
            final URI wsEndpointUri = appendWsPathIfNecessary(this.endpointUri, jsonSchemaVersion);
            return new WebSocketMessagingConfiguration(this, wsEndpointUri);
        }

        private static URI appendWsPathIfNecessary(final URI baseUri, final JsonSchemaVersion schemaVersion) {
            if (needToAppendWsPath(baseUri)) {
                final String pathWithoutTrailingSlashes = removeTrailingSlashFromPath(baseUri.getPath());
                final String newPath = pathWithoutTrailingSlashes + WS_PATH + schemaVersion;
                return baseUri.resolve(newPath);
            } else {
                checkIfBaseUriAndSchemaVersionMatch(baseUri, schemaVersion);
                return baseUri;
            }
        }

        private static boolean needToAppendWsPath(final URI baseUri) {
            final Pattern pattern = Pattern.compile(WS_PATH_REGEX);
            final Matcher matcher = pattern.matcher(baseUri.toString());
            return !matcher.find();
        }

        private static void checkIfBaseUriAndSchemaVersionMatch(final URI baseUri,
                final JsonSchemaVersion schemaVersion) {
            final String path = removeTrailingSlashFromPath(baseUri.getPath());
            final String apiVersion = path.substring(path.length() - 1);
            if (!schemaVersion.toString().equals(apiVersion)) {
                throw new IllegalArgumentException(
                        "The jsonSchemaVersion and apiVersion of the endpoint do not match. " +
                                "Either remove the ws path from the endpoint or " +
                                "use the same jsonSchemaVersion as in the ws path of the endpoint.");
            }
        }

        private static String removeTrailingSlashFromPath(final String path) {
            return path.replaceFirst("/+$", "");
        }

    }

}
