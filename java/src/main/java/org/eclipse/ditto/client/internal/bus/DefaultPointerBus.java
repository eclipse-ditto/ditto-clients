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

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.ditto.json.JsonPointer;

/**
 * Default implementation of {@link PointerBus}.
 *
 * @since 1.0.0
 */
final class DefaultPointerBus implements PointerBus {

    private static final int TERMINATION_TIMEOUT_SECONDS = 2;

    private final String name;
    private final ExecutorService executor;
    private final Registry<Consumer<PointerWithData>> consumerRegistry;

    DefaultPointerBus(final String name, final ExecutorService executor) {
        this.name = name;
        this.executor = executor;
        consumerRegistry = new DefaultRegistry<>();
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public <T> void notify(final JsonPointer key, final T object) {

        @SuppressWarnings("unchecked") final PointerWithData<T> wrap = PointerWithData.create(key, object);
        notify(wrap);
    }

    @Override
    public <T> void notify(final PointerWithData<T> pointerWithData) {

        consumerRegistry.select(pointerWithData.getPointer())
                .stream()
                .filter(reg -> Objects.nonNull(reg.getRegisteredObject()))
                .forEach(reg -> reg.getRegisteredObject().accept(pointerWithData));
    }

    @Override
    public Registration<Consumer<PointerWithData>> on(final JsonPointerSelector selector,
            final Consumer<PointerWithData> consumer) {
        return consumerRegistry.register(selector, consumer);
    }

    @Override
    public void close() {
        consumerRegistry.clear();
        executor.shutdown();
        try {
            executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "name=" + name +
                ", executor=" + executor +
                "]";
    }

}
