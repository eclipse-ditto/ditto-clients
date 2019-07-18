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

import java.util.List;

import org.eclipse.ditto.json.JsonPointer;

/**
 * The Registry manages multiple {@link Registration}s and is capable of selecting a {@link Registration} based on a
 * {@link JsonPointer}.
 *
 * @param <T> the type of the data the registration's objects hold
 * @since 1.0.0
 */
public interface Registry<T> extends Iterable<Registration<T>> {

    /**
     * Assign the given {@link JsonPointerSelector} with the given object.
     *
     * @param sel The left-hand side of the {@literal JsonPointerSelector} comparison check.
     * @param obj The object to assign.
     * @return {@literal this}
     */
    Registration<T> register(JsonPointerSelector sel, T obj);

    /**
     * Remove any objects matching this {@code pointer}. This will unregister <b>all</b> objects matching the given
     * {@literal pointer}. There's no provision for removing only a specific object.
     *
     * @param pointer The pointer to be matched by the JsonPointerSelectors
     * @return {@literal true} if any objects were unassigned, {@literal false} otherwise.
     */
    boolean unregister(JsonPointer pointer);

    /**
     * Select {@link Registration}s whose {@link JsonPointerSelector} {@link JsonPointerSelector#matches(JsonPointer)}
     * the given {@code pointer}.
     *
     * @param pointer The pointer for the JsonPointerSelectors to match
     * @return A {@link List} of {@link Registration}s whose {@link JsonPointerSelector} matches the given pointer.
     */
    List<Registration<T>> select(JsonPointer pointer);

    /**
     * Clear the {@link Registry}, resetting its state and calling {@link Registration#cancel()} for any active {@link
     * Registration}.
     */
    void clear();
}
