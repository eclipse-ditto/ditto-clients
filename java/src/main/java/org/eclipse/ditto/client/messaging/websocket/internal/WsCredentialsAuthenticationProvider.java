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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.client.configuration.CredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Establishes a web socket connection to the Ditto backend using credentials for authentication.
 *
 * @since 1.0.0
 */
@NotThreadSafe
final class WsCredentialsAuthenticationProvider implements AuthenticationProvider<WebSocket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsCredentialsAuthenticationProvider.class);

    private final CredentialsAuthenticationConfiguration configuration;

    private WsCredentialsAuthenticationProvider(final CredentialsAuthenticationConfiguration theConfiguration) {

        configuration = theConfiguration;
    }

    /**
     * Returns an instance of {@code CredentialsAuthenticationConfiguration}.
     *
     * @param configuration the AuthenticationConfiguration to use when connecting.
     * @return the instance.
     * @throws NullPointerException if {@code } is {@code null}.
     */
    public static WsCredentialsAuthenticationProvider getInstance(
            final CredentialsAuthenticationConfiguration configuration) {

        checkNotNull(configuration, "configuration");
        LOGGER.info("Using Basic Auth. Authenticating user <{}>", configuration.getUsername());
        return new WsCredentialsAuthenticationProvider(configuration);
    }

    @Override
    public String getClientSessionId() {
        return configuration.getClientSessionId();
    }

    @Override
    public WebSocket prepareAuthentication(final WebSocket channel,
            final Map<String, String> additionalAuthenticationHeaders,
            @Nullable final ProxyConfiguration proxyConfiguration) {

        additionalAuthenticationHeaders.forEach(channel::addHeader);
        return channel
                .setUserInfo(configuration.getUsername(), configuration.getPassword());
    }
}
