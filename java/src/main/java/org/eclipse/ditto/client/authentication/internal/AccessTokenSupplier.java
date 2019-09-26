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
package org.eclipse.ditto.client.authentication.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.authentication.AuthenticationException;
import org.eclipse.ditto.client.configuration.internal.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides JSON web tokens via client credentials flow.
 *
 * @since 1.0.0
 */
@Immutable
final class AccessTokenSupplier implements Supplier<JsonObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenSupplier.class);

    private static final String UNEXPECTED_HTTP_STATUS_CODE_TEMPLATE =
            "Unexpected HTTP status code from token endpoint. Expected status code <200> but was: <{}>";
    private static final String PARAMETERS_TEMPLATE =
            "grant_type=client_credentials&client_id=%s&client_secret=%s&scope=%s";

    private final ClientCredentialsAuthenticationConfiguration configuration;

    private AccessTokenSupplier(final ClientCredentialsAuthenticationConfiguration configuration) {
        this.configuration = configuration;
    }

    static AccessTokenSupplier newInstance(final ClientCredentialsAuthenticationConfiguration configuration) {
        return new AccessTokenSupplier(checkNotNull(configuration, "configuration"));
    }

    @Override
    public JsonObject get() {
        try {
            return requestToken();
        } catch (final IOException e) {
            throw AuthenticationException.of(configuration.getSessionId(), e);
        }
    }

    private JsonObject requestToken() throws IOException {
        final HttpURLConnection connection = openConnection(configuration.getTokenEndpoint());
        sendTokenRequest(connection);
        return receiveTokenResponse(connection);
    }

    private HttpURLConnection openConnection(final String endpoint) throws IOException {
        final URL url = new URL(endpoint);
        return (HttpURLConnection) url.openConnection(getProxy().orElse(Proxy.NO_PROXY));
    }

    private Optional<Proxy> getProxy() {
        return configuration.getProxyConfiguration()
                .map(proxyConfiguration -> {
                    final String host = proxyConfiguration.getHost();
                    final int port = proxyConfiguration.getPort();
                    return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                });
    }

    private void sendTokenRequest(final HttpURLConnection connection) throws IOException {
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        try (final DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.writeBytes(getTokenRequestParameters());
            out.flush();
        }
        connection.connect();
    }

    private String getTokenRequestParameters() {
        final String clientId = configuration.getClientId();
        final String clientSecret = configuration.getClientSecret();
        final String scope = String.join(" ", configuration.getScopes());
        return String.format(PARAMETERS_TEMPLATE, clientId, clientSecret, scope);
    }

    private JsonObject receiveTokenResponse(final HttpURLConnection connection) throws IOException {
        final int statusCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == statusCode) {
            final String response = readResponse(connection);
            return JsonObject.of(response);
        }
        LOGGER.error(UNEXPECTED_HTTP_STATUS_CODE_TEMPLATE, statusCode);
        throw new IllegalStateException(readError(connection));
    }

    private String readResponse(final HttpURLConnection connection) throws IOException {
        return readInputStream(connection.getInputStream());
    }

    private String readError(final HttpURLConnection connection) throws IOException {
        return readInputStream(connection.getErrorStream());
    }

    private String readInputStream(final InputStream inputStream) throws IOException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            final StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    static final class JsonFields {

        private JsonFields() {
            throw new AssertionError();
        }

        static final JsonFieldDefinition<String> JSON_ACCESS_TOKEN =
                JsonFactory.newStringFieldDefinition("access_token");
        static final JsonFieldDefinition<String> JSON_TOKEN_TYPE =
                JsonFactory.newStringFieldDefinition("token_type");
        static final JsonFieldDefinition<Long> JSON_EXPIRES_IN =
                JsonFactory.newLongFieldDefinition("expires_in");
        static final JsonFieldDefinition<String> JSON_REFRESH_TOKEN =
                JsonFactory.newStringFieldDefinition("refresh_token");

    }

}
