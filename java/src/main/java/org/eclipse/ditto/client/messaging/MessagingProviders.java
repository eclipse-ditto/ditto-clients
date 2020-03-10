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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.internal.DefaultThreadFactory;
import org.eclipse.ditto.client.messaging.internal.WebSocketMessagingProvider;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Contains static methods for instantiating entry points (Builders) to different {@link MessagingProvider}
 * implementations.
 *
 * @since 1.0.0
 */
public final class MessagingProviders {

    private MessagingProviders() {
        throw new AssertionError("No instantiation.");
    }

    public static MessagingProvider webSocket(final MessagingConfiguration configuration,
            final AuthenticationProvider<WebSocket> authenticationProvider,
            final ExecutorService callbackExecutor) {
        return WebSocketMessagingProvider.newInstance(configuration, authenticationProvider, callbackExecutor);
    }

    public static MessagingProvider webSocket(final MessagingConfiguration configuration,
            final AuthenticationProvider<WebSocket> authenticationProvider) {
        final ExecutorService defaultExecutorService = createDefaultExecutorService(UUID.randomUUID().toString());
        return webSocket(configuration, authenticationProvider, defaultExecutorService);
    }

    public static ExecutorService createDefaultExecutorService(final String name) {
        return createScheduledExecutorService(name);
    }

    public static ScheduledExecutorService createScheduledExecutorService(final String name) {
        final int corePoolSize = Runtime.getRuntime().availableProcessors() * 8;
        return Executors.newScheduledThreadPool(corePoolSize, new DefaultThreadFactory("ditto-client-" + name));
    }

}
