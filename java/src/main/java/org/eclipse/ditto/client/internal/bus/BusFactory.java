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

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.eclipse.ditto.client.messaging.MessagingProviders;

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
     */
    public static AdaptableBus createAdaptableBus() {
        final String name = "-adaptable-bus-" + UUID.randomUUID();
        // the executor service will shutdown when garbage-collected.
        return new DefaultAdaptableBus(MessagingProviders.createScheduledExecutorService(name))
                .addStringClassifier(Classifiers.identity())
                .addAdaptableClassifier(Classifiers.correlationId())
                .addAdaptableClassifier(Classifiers.streamingType())
                .addAdaptableClassifier(Classifiers.thingsSearch());
    }
}
