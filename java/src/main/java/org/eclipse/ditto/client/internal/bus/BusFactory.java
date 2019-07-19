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
package org.eclipse.ditto.client.internal.bus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.client.internal.DefaultThreadFactory;

/**
 * Factory for creating Buses (e.g. {@link PointerBus}).
 *
 * @since 1.0.0
 */
public final class BusFactory {

    private BusFactory() {
        throw new AssertionError();
    }

    /**
     * Creates a new {@link PointerBus} using the passed in {@code name} and {@code executor}.
     *
     * @param name the name of the bus (e.g. used in thread names).
     * @param executor the {@link ExecutorService} to use for asynchronous operations on the Bus.
     * @return the newly created PointerBus
     */
    public static PointerBus createPointerBus(final String name, final ExecutorService executor) {
        return new DefaultPointerBus(name, executor);
    }

    /**
     * Creates a new {@link PointerBus} using the passed in {@code name} with a default ThreadPool-based executor.
     *
     * @param name the name of the bus (e.g. used in thread names).
     * @return the newly created PointerBus
     */
    public static PointerBus createPointerBus(final String name) {
        return new DefaultPointerBus(name, createDefaultExecutorService(name));
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
