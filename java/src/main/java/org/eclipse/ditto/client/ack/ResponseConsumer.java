/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.ack;

import java.util.function.BiConsumer;

import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;

/**
 * Interface encapsulating a {@link java.util.function.BiConsumer} which is notified about responses with either the
 * response of type {@link R} (if it was successful) or with an {@link Throwable} if there occurred an error.
 * Does also hold the type of the expected Message response.
 *
 * @param <R> the type of the expected response.
 */
public interface ResponseConsumer<R> {

    /**
     * Returns the type of the expected response.
     *
     * @return the type of the expected response.
     */
    Class<R> getResponseType();

    /**
     * The BiConsumer which is notified about responses with either
     * the response of type {@link R} (if it was successful) or with an {@link Throwable} if there occurred an error.
     *
     * @return the BiConsumer notified about responses.
     */
    BiConsumer<R, Throwable> getResponseConsumer();

    /**
     * Type-check the argument against the response type and call the response consumer with the right type or
     * with an exception.
     *
     * @param argument the argument to consume.
     */
    default void accept(final Object argument) {
        if (getResponseType().isInstance(argument)) {
            getResponseConsumer().accept(getResponseType().cast(argument), null);
        } else if (argument instanceof ThingErrorResponse) {
            getResponseConsumer().accept(null, ((ThingErrorResponse) argument).getDittoRuntimeException());
        } else if (argument != null) {
            getResponseConsumer().accept(null, new ClassCastException(
                    "Expected: " + getResponseType().getCanonicalName() +
                            "; Actual: " + argument.getClass().getCanonicalName() +
                            " (" + argument + ")"
            ));
        } else {
            getResponseConsumer().accept(null, new NullPointerException(
                    "Expected: " + getResponseType().getCanonicalName() + "; Actual: null")
            );
        }
    }
}
