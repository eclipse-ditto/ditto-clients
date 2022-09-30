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
package org.eclipse.ditto.client.live.events;

import java.util.function.Function;

import org.eclipse.ditto.base.model.signals.events.Event;

/**
 * Provides functionality to emit {@link Event}s from the live client when they occur on devices.
 *
 * @param <F> the type of the EventFactory used for creating Events.
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventEmitter<F extends EventFactory> {

    /**
     * Takes a Function providing a {@link EventFactory} which is able to build {@link Event}s based on the scope.
     *
     * @param eventFunction Function providing a EventFactory and requiring a Event as result.
     * @throws NullPointerException if {@code eventFunction} is {@code null}.
     * @throws org.eclipse.ditto.client.management.ClientReconnectingException if the client is reconnecting.
     */
    void emitEvent(Function<F, Event<?>> eventFunction);

}
