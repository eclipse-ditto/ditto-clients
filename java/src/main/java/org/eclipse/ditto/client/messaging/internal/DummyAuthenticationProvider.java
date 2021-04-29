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

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.DummyAuthenticationConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Implements dummy authentication for a {@link com.neovisionaries.ws.client.WebSocket} channel.
 * <p>
 * Do not use this implementation in production!
 *
 * @since 1.0.0
 */
public final class DummyAuthenticationProvider implements AuthenticationProvider<WebSocket> {

    private static final String X_DITTO_DUMMY_AUTH_HEADER = "x-ditto-pre-authenticated";

    private final DummyAuthenticationConfiguration configuration;

    public DummyAuthenticationProvider(final DummyAuthenticationConfiguration configuration) {
        this.configuration = checkNotNull(configuration, "configuration");
    }

    @Override
    public AuthenticationConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void prepareAuthentication(final WebSocket webSocket) {
        final String dummyUsername = configuration.getDummyUsername();
        configuration.getAdditionalHeaders().forEach(webSocket::addHeader);
        webSocket.addHeader(X_DITTO_DUMMY_AUTH_HEADER, dummyUsername);
    }

    @Override
    public void destroy() {
        // nothing to destroy here
    }

}
