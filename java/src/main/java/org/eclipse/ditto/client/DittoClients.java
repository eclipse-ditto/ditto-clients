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
package org.eclipse.ditto.client;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.internal.DefaultDittoClient;
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.messaging.MessagingProvider;

/**
 * Factory responsible for providing new instances of the {@link DittoClient Ditto Client}.
 * <p>
 * A client instance requires at least one {@link org.eclipse.ditto.client.messaging.MessagingProvider} which is then
 * used for both {@code Twin} and {@code Live}. Instances of {@code MessagingProvider} can be obtained from factory
 * {@link org.eclipse.ditto.client.messaging.MessagingProviders}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
@Immutable
public final class DittoClients {

    private DittoClients() {
        throw new AssertionError();
    }

    /**
     * Creates a new {@link org.eclipse.ditto.client.DittoClient} with a shared {@code Twin} and {@code Live}
     * {@link org.eclipse.ditto.client.messaging.MessagingProvider}.
     *
     * @param messagingProvider the messaging provider for this client.
     * @return the client.
     * @throws org.eclipse.ditto.client.authentication.AuthenticationException if authentication failed.
     * @throws org.eclipse.ditto.client.messaging.ConnectException if a connection to the configured endpoint
     * could not be established
     */
    public static DittoClient newInstance(final MessagingProvider messagingProvider) {
        return newInstance(messagingProvider, messagingProvider);
    }

    /**
     * Creates a new {@link org.eclipse.ditto.client.DittoClient} with a specific {@code Twin} and {@code Live}
     * {@link org.eclipse.ditto.client.messaging.MessagingProvider}.
     *
     * @param twinMessagingProvider the messaging provider for the {@code Twin} part of the client.
     * @param liveMessagingProvider the messaging provider for the {@code Live} part of the client.
     * @return the client.
     * @throws org.eclipse.ditto.client.authentication.AuthenticationException if authentication failed.
     * @throws org.eclipse.ditto.client.messaging.ConnectException if a connection to the configured endpoint
     * could not be established
     */
    public static DittoClient newInstance(final MessagingProvider twinMessagingProvider,
            final MessagingProvider liveMessagingProvider) {

        final ResponseForwarder responseForwarder = ResponseForwarder.getInstance();
        return DefaultDittoClient.newInstance(twinMessagingProvider, liveMessagingProvider, responseForwarder);
    }

}
