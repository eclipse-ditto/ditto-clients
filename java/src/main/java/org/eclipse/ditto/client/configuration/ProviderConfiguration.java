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
package org.eclipse.ditto.client.configuration;

import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.MessagingProvider;

/**
 * Contains {@link MessagingProvider} specific configuration. Consult the documentation of {@code MessagingProvider}
 * implementation to find supported configuration options.
 *
 * @param <T> the type of the {@link MessagingProvider} this ProviderConfiguration configures
 * @param <C> the type of the channel the MessagingProvider provides, e.g. a WebSocket type
 * @since 1.0.0
 */
public interface ProviderConfiguration<T extends MessagingProvider, C> {

    /**
     * Instantiates the concrete {@link MessagingProvider}.
     *
     * @return the instantiated {@link MessagingProvider} backed by this {@code ProviderConfiguration}.
     */
    T instantiateProvider();

    /**
     * Returns the {@link AuthenticationConfiguration} that should be used for this messaging provider.
     *
     * @return the AuthenticationProvider that should be used for this messaging provider.
     */
    AuthenticationConfiguration getAuthenticationConfiguration();

    /**
     * Returns the {@link AuthenticationProvider} that should be used for this messaging provider.
     *
     * @return the AuthenticationProvider that should be used for this messaging provider.
     */
    AuthenticationProvider<C> getAuthenticationProvider();

}
