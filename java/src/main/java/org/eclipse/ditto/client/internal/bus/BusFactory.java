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
import java.util.concurrent.ScheduledExecutorService;

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
     * Create an adaptable bus.
     *
     * @return the adaptable bus.
     * @param defaultExecutor the default executor to run non-scheduled tasks on.
     * @param scheduledExecutor the {@code ScheduledExecutorService} to use for scheduling tasks.
     */
    public static AdaptableBus createAdaptableBus(final ExecutorService defaultExecutor,
            final ScheduledExecutorService scheduledExecutor) {
        // the executor service will shutdown when garbage-collected.
        return new DefaultAdaptableBus(defaultExecutor, scheduledExecutor)
                .addStringClassifier(Classifiers.identity())
                .addAdaptableClassifier(Classifiers.correlationId())
                .addAdaptableClassifier(Classifiers.streamingType())
                .addAdaptableClassifier(Classifiers.thingsSearch())
                .addAdaptableClassifier(Classifiers.errors())
                .addAdaptableClassifier(Classifiers.errorCode());
    }
}
