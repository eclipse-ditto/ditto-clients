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
package org.eclipse.ditto.client.messaging.internal;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.Executors;

import org.eclipse.ditto.client.configuration.internal.AccessTokenAuthenticationConfiguration;
import org.eclipse.ditto.model.jwt.ImmutableJsonWebToken;
import org.eclipse.ditto.model.jwt.JsonWebToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Unit test for {@link org.eclipse.ditto.client.messaging.internal.AccessTokenAuthenticationProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AccessTokenAuthenticationProviderTest {

    @Mock
    private WebSocket webSocket;

    @Test
    public void tokenRefreshIsCalledBeforeExpiry() {
        final AccessTokenAuthenticationProvider underTest = getAccessTokenAuthenticationProvider(6L);

        underTest.prepareAuthentication(webSocket);

        verify(webSocket, timeout(12000L)).sendText(startsWith("JWT-TOKEN?jwtToken="));
    }

    private static AccessTokenAuthenticationProvider getAccessTokenAuthenticationProvider(final long exp) {
        return new AccessTokenAuthenticationProvider(AccessTokenAuthenticationConfiguration.newBuilder()
                .identifier("bumlux")
                .accessTokenSupplier(() -> getJsonWebToken(exp))
                .build(), Executors.newSingleThreadScheduledExecutor());
    }

    private static JsonWebToken getJsonWebToken(final long exp) {
        final String header = "{\"header\":\"value\"}";
        final String payload = String.format("{\"exp\":%d}", Instant.now().plusSeconds(exp).getEpochSecond());
        final String signature = "{\"signature\":\"foo\"}";
        final String token = base64(header) + "." + base64(payload) + "." + base64(signature);
        return ImmutableJsonWebToken.fromToken(token);
    }

    private static String base64(final String value) {
        return new String(Base64.getEncoder().encode(value.getBytes()));
    }

}