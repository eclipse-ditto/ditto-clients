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

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;

/**
 * Interface to be used when implementing a authentication provider for a {@link org.eclipse.ditto.client.messaging.MessagingProvider}.
 *
 * @param <C> the channel type this authentication provider uses for authenticating (e.g. WebSocket)
 * @since 1.0.0
 */
public interface AuthenticationProvider<C> {

    /**
     * Returns the {@code AuthenticationConfiguration} of this provider.
     *
     * @return the configuration.
     */
    AuthenticationConfiguration getConfiguration();

    /**
     * Prepares the authentication by e.g. using the passed in {@code channel} in order to attach authentication
     * information to it.
     *
     * @param channel the channel to perform authentication with.
     */
    void prepareAuthentication(C channel);

    /**
     * Destroys this {@code AuthenticationConfiguration} and frees all resources.
     */
    void destroy();

}
