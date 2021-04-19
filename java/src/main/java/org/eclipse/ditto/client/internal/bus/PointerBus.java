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
import java.util.function.Consumer;

import org.eclipse.ditto.json.JsonPointer;

/**
 * The PointerBus is similar to an EventBus. It can {@code notify} with a specific {@link JsonPointer} and an arbitrary
 * object and other parties which registered themselves with the {@code on} registration method are notified then.
 *
 * @since 1.0.0
 */
public interface PointerBus {

    /**
     * Notify this component that an {@code PointerWithData} consisting of the passed {@code key} and {@code object} is
     * ready to be processed.
     *
     * @param key the key to be matched by {@link JsonPointerSelector Selectors}
     * @param object the object contained in the PointerWithData
     * @param <T> the type of the object
     */
    default <T> void notify(final CharSequence key, final T object) {
        notify(JsonPointer.of(key), object);
    }

    /**
     * Notify this component that an {@code PointerWithData} consisting of the passed {@code key} and {@code object} is
     * ready to be processed.
     *
     * @param key the key to be matched by {@link JsonPointerSelector Selectors}.
     * @param object the object contained in the PointerWithData.
     * @param <T> the type of the object.
     */
    default <T> void notify(final JsonPointer key, final T object) {
        notify(PointerWithData.create(key, object));
    }

    /**
     * Notify this component that an {@link PointerWithData} is ready to be processed.
     *
     * @param pointerWithData the {@code PointerWithData} to notify about
     * @param <T> the type of the object of the PointerWithData
     */
    <T> void notify(PointerWithData<T> pointerWithData);

    /**
     * Register a {@link Consumer} to be triggered when a notification matches the given {@link JsonPointerSelector}.
     *
     * @param selector The {@literal JsonPointerSelector} to be used for matching
     * @param consumer The {@literal Consumer} to be triggered
     * @return A {@link Registration} object that allows the caller to interact with the given mapping
     */
    Registration<Consumer<PointerWithData<?>>> on(JsonPointerSelector selector, Consumer<PointerWithData<?>> consumer);

    /**
     * @return the ExecutorService used for this Bus instance.
     */
    ExecutorService getExecutor();

    /**
     * Closes the Bus.
     */
    void close();

}
