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
package org.eclipse.ditto.client.authentication;

import org.eclipse.ditto.client.authentication.internal.BasicAuthenticationProvider;
import org.eclipse.ditto.client.authentication.internal.ClientCredentialsAuthenticationProvider;
import org.eclipse.ditto.client.authentication.internal.DummyAuthenticationProvider;
import org.eclipse.ditto.client.configuration.internal.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.DummyAuthenticationConfiguration;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Factory for creating {@link org.eclipse.ditto.client.authentication.AuthenticationProvider} instances.
 *
 * @since 1.0.0
 */
public final class AuthenticationProviders {

    private AuthenticationProviders() {
        throw new AssertionError();
    }

    /**
     * Creates a new {@code AuthenticationProvider} for basic authentication.
     *
     * @param configuration the configuration of the provider.
     * @return the instance.
     */
    public static AuthenticationProvider<WebSocket> basic(
            final BasicAuthenticationConfiguration configuration) {

        return new BasicAuthenticationProvider(configuration);
    }

    /**
     * Creates a new {@code AuthenticationProvider} for client credentials authentication.
     *
     * @param configuration the configuration of the provider.
     * @return the instance.
     */
    public static AuthenticationProvider<WebSocket> clientCredentials(
            final ClientCredentialsAuthenticationConfiguration configuration) {

        return new ClientCredentialsAuthenticationProvider(configuration);
    }

    /**
     * Creates a new {@code AuthenticationProvider} for dummy authentication.
     *
     * @param configuration the configuration of the provider.
     * @return the instance.
     */
    public static AuthenticationProvider<WebSocket> dummy(
            final DummyAuthenticationConfiguration configuration) {

        return new DummyAuthenticationProvider(configuration);
    }

}
