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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

    /**
     *  Creates a new {@code WebSocketMessagingProvider}.
     *
     * @param configuration configuration of websocket messaging.
     * @param authenticationProvider provides authentication.
     * @param defaultExecutor the executor for messages.
     * @return the created WebSocket based MessagingProvider.
     */
    public static MessagingProvider webSocket(final MessagingConfiguration configuration,
            final AuthenticationProvider<WebSocket> authenticationProvider,
            final ExecutorService defaultExecutor) {
        final ScheduledExecutorService defaultScheduledExecutor = createScheduledExecutorService(
                "abus-" + authenticationProvider.getConfiguration().getSessionId());
        return WebSocketMessagingProvider.newInstance(configuration, authenticationProvider, defaultExecutor,
                defaultScheduledExecutor);
    }

    /**
     * Creates a new {@code WebSocketMessagingProvider}.
     *
     * @param configuration configuration of websocket messaging.
     * @param authenticationProvider provides authentication.
     * @param callbackExecutor the executor for messages.
     * @param internalBusExecutor the scheduled executor for the internal bus.
     * @return the created WebSocket based MessagingProvider.
     * @since 2.1.0
     */
    public static MessagingProvider webSocket(final MessagingConfiguration configuration,
            final AuthenticationProvider<WebSocket> authenticationProvider,
            final ExecutorService callbackExecutor,
            final ScheduledExecutorService internalBusExecutor) {
        return WebSocketMessagingProvider.newInstance(configuration, authenticationProvider, callbackExecutor,
                internalBusExecutor);
    }

    /**
     * Creates a new {@code WebSocketMessagingProvider} with default executors/thread pools.
     *
     * @param configuration configuration of websocket messaging.
     * @param authenticationProvider provides authentication.
     * @return the created WebSocket based MessagingProvider.
     * @since 2.1.0
     */
    public static MessagingProvider webSocket(final MessagingConfiguration configuration,
            final AuthenticationProvider<WebSocket> authenticationProvider) {
        final ExecutorService defaultCallbackExecutor = createDefaultExecutorService("default-" +
                authenticationProvider.getConfiguration().getSessionId());
        return webSocket(configuration, authenticationProvider, defaultCallbackExecutor);
    }

    /**
     * Creates the default {@code ExecutorService} the Ditto client uses if no other executor service was
     * configured.
     *
     * @param name the name to use in the created threads.
     * @return the default {@code ExecutorService}.
     */
    public static ExecutorService createDefaultExecutorService(final String name) {
        return createExecutorService(name);
    }

    /**
     * Creates an {@code ExecutorService} backed by a {@code ThreadPoolExecutor} dynamic in thread size with 0 Threads
     * core pool size and the maximum pool size depending on the available processors (times 8).
     *
     * @param name the name to use in the created threads.
     * @return the default {@code ExecutorService}.
     * @since 2.1.0
     */
    public static ExecutorService createExecutorService(final String name) {
        final int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 8; // limit by default to this max pool size
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                0, maximumPoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new DefaultThreadFactory("ditto-client-" + name),
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /**
     * Creates a {@code ScheduledExecutorService} backed by a {@code ScheduledThreadPoolExecutor} dynamic in thread
     * size with 0 Threads and an unbounded maximum pool size.
     * <p>
     * Only use for scheduling tasks in the future or at a fixed rate!
     *
     * @param name the name to use in the created threads.
     * @return the {@code ScheduledExecutorService}.
     */
    public static ScheduledExecutorService createScheduledExecutorService(final String name) {
        // ScheduledThreadPool executor is by default unbounded in max size, so start with core-size of 0:
        return Executors.newScheduledThreadPool(0,
                new DefaultThreadFactory("ditto-client-scheduled-" + name));
    }

}
