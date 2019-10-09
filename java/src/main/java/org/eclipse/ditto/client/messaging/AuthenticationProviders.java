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

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.ditto.client.configuration.internal.AccessTokenAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.DummyAuthenticationConfiguration;
import org.eclipse.ditto.client.internal.DefaultThreadFactory;
import org.eclipse.ditto.client.messaging.internal.AccessTokenAuthenticationProvider;
import org.eclipse.ditto.client.messaging.internal.BasicAuthenticationProvider;
import org.eclipse.ditto.client.messaging.internal.ClientCredentialsAuthenticationProvider;
import org.eclipse.ditto.client.messaging.internal.DummyAuthenticationProvider;

/**
 * Factory for creating {@link AuthenticationProvider} instances.
 *
 * @since 1.0.0
 */
public final class AuthenticationProviders {

    private AuthenticationProviders() {
        throw new AssertionError();
    }

    /**
     * Creates a new {@code AuthenticationProvider} for access token authentication.
     *
     * @param configuration the configuration of the provider.
     * @return the instance.
     */
    public static AuthenticationProvider accessToken(final AccessTokenAuthenticationConfiguration configuration) {

        return new AccessTokenAuthenticationProvider(configuration,
                createDefaultExecutorService(UUID.randomUUID().toString()));
    }

    /**
     * Creates a new {@code AuthenticationProvider} for basic authentication.
     *
     * @param configuration the configuration of the provider.
     * @return the instance.
     */
    public static AuthenticationProvider basic(final BasicAuthenticationConfiguration configuration) {

        return new BasicAuthenticationProvider(configuration);
    }

    /**
     * Creates a new {@code AuthenticationProvider} for client credentials authentication.
     *
     * @param configuration the configuration of the provider.
     * @return the instance.
     */
    public static AuthenticationProvider clientCredentials(
            final ClientCredentialsAuthenticationConfiguration configuration) {

        return new ClientCredentialsAuthenticationProvider(configuration,
                createDefaultExecutorService(UUID.randomUUID().toString()));
    }

    /**
     * Creates a new {@code AuthenticationProvider} for dummy authentication.
     *
     * @param configuration the configuration of the provider.
     * @return the instance.
     */
    public static AuthenticationProvider dummy(
            final DummyAuthenticationConfiguration configuration) {

        return new DummyAuthenticationProvider(configuration);
    }

    private static ScheduledExecutorService createDefaultExecutorService(final String name) {
        return Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("ditto-client-scheduler-" + name));
    }

}
