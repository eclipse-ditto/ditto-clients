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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.client.internal.DefaultThreadFactory;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.JsonWebTokenSupplier;
import org.eclipse.ditto.model.jwt.JsonWebToken;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Common base implementation for authentication providers based on {@link org.eclipse.ditto.model.jwt.JsonWebToken}.
 *
 * @since 1.0.0
 */
abstract class AbstractTokenAuthenticationProvider implements AuthenticationProvider<WebSocket> {

    private static final String PROTOCOL_CMD_JWT_TOKEN_TEMPLATE = "JWT-TOKEN?jwtToken=%s";

    private final Map<String, String> additionalHeaders;
    private final JsonWebTokenSupplier jsonWebTokenSupplier;
    private final JwtRefreshScheduler jwtRefreshScheduler;

    AbstractTokenAuthenticationProvider(final Map<String, String> additionalHeaders,
            final JsonWebTokenSupplier jsonWebTokenSupplier,
            final Duration expiryGracePeriod) {
        this.additionalHeaders = checkNotNull(additionalHeaders, "additionalHeaders");
        this.jsonWebTokenSupplier = checkNotNull(jsonWebTokenSupplier, "accessTokenSupplier");
        jwtRefreshScheduler = JwtRefreshScheduler.newInstance(jsonWebTokenSupplier, expiryGracePeriod);
    }

    @Override
    public void prepareAuthentication(final WebSocket webSocket) {
        final JsonWebToken jwt = jsonWebTokenSupplier.get();
        final String authorizationHeader = String.format("Bearer %s", jwt.getToken());
        webSocket.addHeader("Authorization", authorizationHeader);
        additionalHeaders.forEach(webSocket::addHeader);
        jwtRefreshScheduler.scheduleRefresh(jwt.getExpirationTime(), newJwt -> sendJwt(webSocket, newJwt));
    }

    private void sendJwt(final WebSocket webSocket, final JsonWebToken jsonWebToken) {
        webSocket.sendText(String.format(PROTOCOL_CMD_JWT_TOKEN_TEMPLATE, jsonWebToken.getToken()));
    }

    @Override
    public void destroy() {
        jwtRefreshScheduler.destroy();
    }

    @ThreadSafe
    private static final class JwtRefreshScheduler {

        private final JsonWebTokenSupplier jsonWebTokenSupplier;
        private final Duration expiryGracePeriod;
        private final ScheduledExecutorService executorService;

        private JwtRefreshScheduler(final JsonWebTokenSupplier jsonWebTokenSupplier, final Duration expiryGracePeriod) {
            this.jsonWebTokenSupplier = jsonWebTokenSupplier;
            this.expiryGracePeriod = expiryGracePeriod;
            executorService = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("scheduler"));
        }

        /**
         * Creates a new {@code JwtRefreshScheduler}.
         *
         * @param jsonWebTokenSupplier a supplier for jwt tokens.
         * @param expiryGracePeriod the grace period before the actual expiry to account for network latency.
         * @return the JwtRefreshScheduler.
         */
        static JwtRefreshScheduler newInstance(final JsonWebTokenSupplier jsonWebTokenSupplier,
                final Duration expiryGracePeriod) {
            checkNotNull(jsonWebTokenSupplier, "jsonWebTokenSupplier");
            checkNotNull(expiryGracePeriod, "expiryGracePeriod");
            return new JwtRefreshScheduler(jsonWebTokenSupplier, expiryGracePeriod);
        }

        /**
         * Schedules a fresh {@code JsonWebToken} from the configured {@code JsonWebTokenSupplier}.
         *
         * @param due the instant when the fresh token is due.
         * @param consumer provides the fresh token.
         */
        void scheduleRefresh(final Instant due, final Consumer<JsonWebToken> consumer) {
            final Instant expiration = due.minus(expiryGracePeriod);
            final Instant now = Instant.now();
            if (now.isBefore(expiration)) {
                final long delay = Duration.between(now, expiration).toMillis();
                executorService.schedule(() -> doRefresh(consumer), delay, TimeUnit.MILLISECONDS);
            }
        }

        private void doRefresh(final Consumer<JsonWebToken> consumer) {
            final JsonWebToken jsonWebToken = jsonWebTokenSupplier.get();
            consumer.accept(jsonWebToken);
            scheduleRefresh(jsonWebToken.getExpirationTime(), consumer);
        }

        void destroy() {
            executorService.shutdownNow();
        }

    }

}
