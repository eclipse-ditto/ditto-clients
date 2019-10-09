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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.model.jwt.JsonWebToken;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Common base implementation for authentication providers based on {@link org.eclipse.ditto.model.jwt.JsonWebToken}.
 *
 * @since 1.0.0
 */
abstract class AbstractTokenAuthenticationProvider implements AuthenticationProvider<WebSocket> {

    private static final long TOKEN_GRACE_SECONDS = 5L;
    private static final String TOKEN_MESSAGE_TEMPLATE = "JWT-TOKEN?jwtToken=%s";

    private final Map<String, String> additionalHeaders;
    private final JsonWebTokenSupplier jsonWebTokenSupplier;
    private final Scheduler scheduler;

    AbstractTokenAuthenticationProvider(final Map<String, String> additionalHeaders,
            final JsonWebTokenSupplier jsonWebTokenSupplier,
            final ScheduledExecutorService executorService) {
        this.additionalHeaders = checkNotNull(additionalHeaders, "additionalHeaders");
        this.jsonWebTokenSupplier = checkNotNull(jsonWebTokenSupplier, "accessTokenSupplier");
        scheduler = new Scheduler(jsonWebTokenSupplier, executorService);
    }

    @Override
    public void prepareAuthentication(final WebSocket webSocket) {
        final JsonWebToken jsonWebToken = jsonWebTokenSupplier.get();
        final String authorizationHeader = String.format("Bearer %s", jsonWebToken.getToken());
        webSocket.addHeader("Authorization", authorizationHeader);
        additionalHeaders.forEach(webSocket::addHeader);
        scheduler.scheduleTokenRefresh(webSocket, jsonWebToken.getExpirationTime());
    }

    @Override
    public void destroy() {
        scheduler.destroy();
    }

    private static final class Scheduler {

        private final JsonWebTokenSupplier jsonWebTokenSupplier;
        private final ScheduledExecutorService executorService;

        private Scheduler(final JsonWebTokenSupplier jsonWebTokenSupplier,
                final ScheduledExecutorService executorService) {
            this.jsonWebTokenSupplier = checkNotNull(jsonWebTokenSupplier, "jsonWebTokenSupplier");
            this.executorService = checkNotNull(executorService, "executorService");
        }

        void scheduleTokenRefresh(final WebSocket webSocket, final Instant expiry) {
            final Instant expiration = expiry.minusSeconds(TOKEN_GRACE_SECONDS);
            final Duration delay = Duration.between(Instant.now(), expiration);
            executorService.schedule(() -> getToken(webSocket), delay.toMillis(), TimeUnit.MILLISECONDS);
        }

        private void getToken(final WebSocket webSocket) {
            final JsonWebToken jsonWebToken = jsonWebTokenSupplier.get();
            final String tokenMessage = String.format(TOKEN_MESSAGE_TEMPLATE, jsonWebToken.getToken());
            webSocket.sendText(tokenMessage);
            scheduleTokenRefresh(webSocket, jsonWebToken.getExpirationTime());
        }

        void destroy() {
            executorService.shutdownNow();
        }

    }

}
