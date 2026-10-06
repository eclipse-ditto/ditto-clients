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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.eclipse.ditto.client.configuration.AccessTokenAuthenticationConfiguration;
import org.eclipse.ditto.jwt.model.ImmutableJsonWebToken;
import org.eclipse.ditto.jwt.model.JsonWebToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Unit test for {@link org.eclipse.ditto.client.messaging.internal.AccessTokenAuthenticationProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class AccessTokenAuthenticationProviderTest {

    private static final String JWT_TOKEN_COMMAND_PREFIX = "JWT-TOKEN?jwtToken=";

    private static final long EXPIRES_IN_SECONDS = 6L;

    /**
     * The "exp" claim only has second precision, so a token created at {@code t} expires at
     * {@code floor(t) + EXPIRES_IN_SECONDS} and the refresh is scheduled for
     * {@code floor(t) + EXPIRES_IN_SECONDS - EXPIRY_GRACE_PERIOD}. With the default grace period of 5s that instant is
     * less than a second away and {@link java.time.Instant#now()} can already have passed it by the time
     * {@code scheduleRefresh} runs, in which case no refresh is scheduled at all. A smaller grace period keeps the
     * scheduled instant 1-2s in the future regardless of where in the current second the token was created.
     */
    private static final Duration EXPIRY_GRACE_PERIOD = Duration.ofSeconds(4);

    /**
     * Standard base64 of {@code 0xF9 0x00 0x00 0x88 0x4F}, i.e. {@code +QAAiE8=}. The signature is the only segment of
     * a JWT that realistically contains a {@code '+'}, and only for issuers emitting standard base64 instead of
     * base64url.
     */
    private static final String SIGNATURE_INCLUDING_PLUS =
            base64(new byte[]{(byte) 0xF9, 0x00, 0x00, (byte) 0x88, 0x4F});

    /**
     * A signature as emitted by a conforming (RFC 7515, base64url) issuer: no {@code '+'}, no {@code '/'} and no
     * padding.
     */
    private static final String SIGNATURE_BASE64_URL = "-_9AbC0zZQ";

    @Mock
    private WebSocket webSocket;

    @Test
    public void tokenRefreshIsCalledBeforeExpiry() {
        final AccessTokenAuthenticationProvider underTest = getAccessTokenAuthenticationProvider(EXPIRES_IN_SECONDS);

        try {
            underTest.prepareAuthentication(webSocket);

            // the JWT refresh scheduler keeps resending the refreshed JWT (roughly every second until expiry),
            // therefore at least one send of the JWT-TOKEN protocol command is expected
            verify(webSocket, timeout(10000L).atLeastOnce()).sendText(startsWith(JWT_TOKEN_COMMAND_PREFIX));
        } finally {
            underTest.destroy();
        }
    }

    @Test
    public void tokenRefreshIsNotCalledWithNegativeExpiry() {
        final AccessTokenAuthenticationProvider underTest = getAccessTokenAuthenticationProvider(0L);

        try {
            underTest.prepareAuthentication(webSocket);

            verify(webSocket, never()).sendText(startsWith(JWT_TOKEN_COMMAND_PREFIX));
        } finally {
            underTest.destroy();
        }
    }

    @Test
    public void jwtTokenWithPlusCharacterIsUrlEncodedInProtocolCommand() {
        assertThat(SIGNATURE_INCLUDING_PLUS).isEqualTo("+QAAiE8=");
        final JsonWebToken jwtIncludingPlus = getJsonWebToken(EXPIRES_IN_SECONDS, SIGNATURE_INCLUDING_PLUS);
        assertThat(jwtIncludingPlus.getToken()).contains("+");
        final AccessTokenAuthenticationProvider underTest = getAccessTokenAuthenticationProvider(jwtIncludingPlus);

        try {
            underTest.prepareAuthentication(webSocket);

            final ArgumentCaptor<String> sentTextCaptor = ArgumentCaptor.forClass(String.class);
            verify(webSocket, timeout(10000L).atLeastOnce()).sendText(sentTextCaptor.capture());
            assertThat(sentTextCaptor.getAllValues())
                    .as("JWT must be URL-encoded before sending in the JWT-TOKEN protocol command because Ditto " +
                            "URL-decodes the jwtToken parameter and would otherwise turn '+' into a space")
                    .isNotEmpty()
                    .allSatisfy(sentText -> assertThat(sentText)
                            .startsWith(JWT_TOKEN_COMMAND_PREFIX)
                            .endsWith("%2BQAAiE8%3D")
                            .doesNotContain("+"));
        } finally {
            underTest.destroy();
        }
    }

    @Test
    public void conformingBase64UrlJwtIsSentUnchangedInProtocolCommand() {
        final JsonWebToken conformingJwt = getBase64UrlJsonWebToken(EXPIRES_IN_SECONDS);
        assertThat(conformingJwt.getToken()).doesNotContain("+", "/", "=");
        final AccessTokenAuthenticationProvider underTest = getAccessTokenAuthenticationProvider(conformingJwt);

        try {
            underTest.prepareAuthentication(webSocket);

            final ArgumentCaptor<String> sentTextCaptor = ArgumentCaptor.forClass(String.class);
            verify(webSocket, timeout(10000L).atLeastOnce()).sendText(sentTextCaptor.capture());
            assertThat(sentTextCaptor.getAllValues())
                    .as("URL-encoding must leave a conforming base64url JWT byte-identical, otherwise the encoding " +
                            "would change the wire format for every issuer that already emits spec-compliant tokens")
                    .isNotEmpty()
                    .allSatisfy(sentText ->
                            assertThat(sentText).isEqualTo(JWT_TOKEN_COMMAND_PREFIX + conformingJwt.getToken()));
        } finally {
            underTest.destroy();
        }
    }

    private static AccessTokenAuthenticationProvider getAccessTokenAuthenticationProvider(final long exp) {
        return getAccessTokenAuthenticationProvider(getJsonWebToken(exp));
    }

    private static AccessTokenAuthenticationProvider getAccessTokenAuthenticationProvider(
            final JsonWebToken jsonWebToken) {

        return new AccessTokenAuthenticationProvider(AccessTokenAuthenticationConfiguration.newBuilder()
                .identifier("bumlux")
                .accessTokenSupplier(() -> jsonWebToken)
                .expiryGracePeriod(EXPIRY_GRACE_PERIOD)
                .build());
    }

    private static JsonWebToken getJsonWebToken(final long exp) {
        return getJsonWebToken(exp, base64("{\"signature\":\"foo\"}"));
    }

    private static JsonWebToken getJsonWebToken(final long exp, final String encodedSignature) {
        final String token = base64(header()) + "." + base64(payload(exp)) + "." + encodedSignature;
        return ImmutableJsonWebToken.fromToken(token);
    }

    private static JsonWebToken getBase64UrlJsonWebToken(final long exp) {
        final String token = base64Url(header()) + "." + base64Url(payload(exp)) + "." + SIGNATURE_BASE64_URL;
        return ImmutableJsonWebToken.fromToken(token);
    }

    private static String header() {
        return "{\"header\":\"value\"}";
    }

    private static String payload(final long exp) {
        return String.format("{\"exp\":%d}", Instant.now().plusSeconds(exp).getEpochSecond());
    }

    private static String base64(final String value) {
        return new String(Base64.getEncoder().encode(value.getBytes()));
    }

    private static String base64(final byte[] value) {
        return new String(Base64.getEncoder().encode(value));
    }

    private static String base64Url(final String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes());
    }

}
