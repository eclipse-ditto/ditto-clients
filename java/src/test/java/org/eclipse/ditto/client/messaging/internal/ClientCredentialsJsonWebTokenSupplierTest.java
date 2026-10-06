/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationException;
import org.eclipse.ditto.client.messaging.JsonWebTokenSupplier;
import org.eclipse.ditto.jwt.model.JsonWebToken;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Unit test for {@link org.eclipse.ditto.client.messaging.internal.ClientCredentialsJsonWebTokenSupplier}.
 */
public final class ClientCredentialsJsonWebTokenSupplierTest {

    private static final String FAKE_ACCESS_TOKEN =
            "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.c2ln";

    /**
     * The token request body is {@code application/x-www-form-urlencoded}, where a literal '+' decodes to a space.
     * Every parameter therefore has to be URL-encoded before it is written into the body, otherwise a client secret
     * such as {@code a+b} reaches the IdP as {@code a b} and the request fails with {@code invalid_client}.
     */
    @Test
    public void credentialsWithReservedCharactersAreUrlEncodedInTokenRequest() throws Exception {
        final String clientId = "client+id";
        final String clientSecret = "a+b/c=d&e";
        final String scope = "scope one";
        final Map<String, String> receivedFormParams = new ConcurrentHashMap<>();
        final Map<String, String> receivedHeaders = new ConcurrentHashMap<>();
        final HttpServer tokenEndpoint = startTokenEndpoint(exchange -> {
            final String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (null != contentType) {
                receivedHeaders.put("Content-Type", contentType);
            }
            final String requestBody = readRequestBody(exchange.getRequestBody());
            Arrays.stream(requestBody.split("&"))
                    .forEach(param -> {
                        final String[] keyValue = param.split("=", 2);
                        receivedFormParams.put(urlDecode(keyValue[0]),
                                urlDecode(1 < keyValue.length ? keyValue[1] : ""));
                    });
            respond(exchange, 200, String.format(
                    "{\"access_token\":\"%s\",\"token_type\":\"bearer\",\"expires_in\":300}", FAKE_ACCESS_TOKEN));
        });
        try {
            final ClientCredentialsAuthenticationConfiguration configuration =
                    configurationBuilder(tokenEndpoint)
                            .clientId(clientId)
                            .clientSecret(clientSecret)
                            .scopes(Collections.singletonList(scope))
                            .build();

            final JsonWebToken jwt = ClientCredentialsJsonWebTokenSupplier.newInstance(configuration).get();

            assertThat(receivedHeaders)
                    .as("the body is only form-decoded by the IdP if the request announces it as form-urlencoded")
                    .containsEntry("Content-Type", "application/x-www-form-urlencoded");
            assertThat(receivedFormParams)
                    .as("every parameter must survive form-decoding at the token endpoint unchanged")
                    .hasSize(4)
                    .containsEntry("grant_type", "client_credentials")
                    .containsEntry("client_id", clientId)
                    .containsEntry("client_secret", clientSecret)
                    .containsEntry("scope", scope);
            assertThat(jwt.getToken()).isEqualTo(FAKE_ACCESS_TOKEN);
        } finally {
            tokenEndpoint.stop(0);
        }
    }

    @Test
    public void rejectedCredentialsWithErrorBodyAreReportedAsAuthenticationException() throws Exception {
        final HttpServer tokenEndpoint =
                startTokenEndpoint(exchange -> respond(exchange, 401, "{\"error\":\"invalid_client\"}"));
        try {
            final JsonWebTokenSupplier underTest =
                    ClientCredentialsJsonWebTokenSupplier.newInstance(configurationBuilder(tokenEndpoint).build());

            final Throwable thrown = catchThrowable(underTest::get);

            assertThat(thrown)
                    .as("a rejected token request must surface as AuthenticationException, not as a bare runtime " +
                            "exception escaping from the HTTP plumbing")
                    .isInstanceOf(AuthenticationException.class);
            assertThat(thrown.getCause())
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("401")
                    .hasMessageContaining("invalid_client");
        } finally {
            tokenEndpoint.stop(0);
        }
    }

    @Test
    public void rejectedCredentialsWithoutErrorBodyAreReportedAsAuthenticationException() throws Exception {
        // a bare 401 leaves HttpURLConnection.getErrorStream() null, which must not turn into a NullPointerException
        final HttpServer tokenEndpoint = startTokenEndpoint(exchange -> {
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        try {
            final JsonWebTokenSupplier underTest =
                    ClientCredentialsJsonWebTokenSupplier.newInstance(configurationBuilder(tokenEndpoint).build());

            final Throwable thrown = catchThrowable(underTest::get);

            assertThat(thrown)
                    .as("an error response without a body must surface as AuthenticationException")
                    .isInstanceOf(AuthenticationException.class);
            assertThat(thrown.getCause())
                    .isInstanceOf(IOException.class)
                    .isNotInstanceOf(NullPointerException.class)
                    .hasMessageContaining("401");
        } finally {
            tokenEndpoint.stop(0);
        }
    }

    private static HttpServer startTokenEndpoint(final HttpHandler handler) throws IOException {
        final HttpServer tokenEndpoint = HttpServer.create(new InetSocketAddress(0), 0);
        tokenEndpoint.createContext("/token", handler);
        tokenEndpoint.start();
        return tokenEndpoint;
    }

    private static ClientCredentialsAuthenticationConfiguration.ClientCredentialsAuthenticationConfigurationBuilder
    configurationBuilder(final HttpServer tokenEndpoint) {

        return ClientCredentialsAuthenticationConfiguration.newBuilder()
                .tokenEndpoint("http://127.0.0.1:" + tokenEndpoint.getAddress().getPort() + "/token")
                .clientId("client-id")
                .clientSecret("client-secret")
                .scopes(Collections.singletonList("scope"));
    }

    private static void respond(final HttpExchange exchange, final int statusCode, final String body)
            throws IOException {

        final byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private static String urlDecode(final String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Missing standard charset UTF 8 for decoding.", e);
        }
    }

    private static String readRequestBody(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return new String(result.toByteArray(), StandardCharsets.UTF_8);
    }

}
