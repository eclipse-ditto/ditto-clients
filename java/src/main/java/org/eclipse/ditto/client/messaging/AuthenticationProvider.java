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
package org.eclipse.ditto.client.messaging;

import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.configuration.ProxyConfiguration;

/**
 * Interface to be used when implementing a authentication provider for a {@link MessagingProvider}.
 *
 * @param <C> the channel type this authentication provider uses for authenticating (e.g. WebSocket)
 * @since 1.0.0
 */
public interface AuthenticationProvider<C> {

    /**
     * Returns the session identifier for this client - has to be unique for each newly instantiated client.
     *
     * @return the session identifier for this client
     */
    String getClientSessionId();

    /**
     * Prepares the authentication by e.g. using the passed in {@code channel} in order to attach authentication
     * information to it.
     *
     * @param channel the channel to perform authentication with.
     * @param additionalAuthenticationHeaders additional headers to use for authenticating.
     * @param proxyConfiguration the optional ProxyConfiguration which may be required during authentication.
     * @return the by authentication enhanced channel.
     */
    C prepareAuthentication(C channel, Map<String, String> additionalAuthenticationHeaders,
            @Nullable ProxyConfiguration proxyConfiguration);

}
