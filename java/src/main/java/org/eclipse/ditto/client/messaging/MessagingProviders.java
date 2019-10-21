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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private static ExecutorService createDefaultExecutorService(final String name) {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                0, availableProcessors * 8, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new DefaultThreadFactory("ditto-client-" + name),
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

}
