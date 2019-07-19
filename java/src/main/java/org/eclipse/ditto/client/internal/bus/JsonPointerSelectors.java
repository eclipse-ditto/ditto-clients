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

import java.util.function.Predicate;

import org.eclipse.ditto.json.JsonPointer;

/**
 * Helper for building {@link JsonPointerSelector}s.
 *
 * @since 1.0.0
 */
public final class JsonPointerSelectors {

    private JsonPointerSelectors() {
        throw new AssertionError();
    }

    /**
     * Creates a {@link JsonPointerSelector} based on the given {@code JsonPointer}.
     *
     * @param jsonPointer the {@link JsonPointer} to use as selection key.
     * @return the newly created JsonPointerSelector.
     */
    public static JsonPointerSelector jsonPointer(final CharSequence jsonPointer) {
        return DefaultJsonPointerSelector.jsonPointerSelector(jsonPointer);
    }

    /**
     * Creates a {@link Predicate} based JsonPointerSelector with the passed {@code predicate}.
     *
     * @param predicate the Predicate which must test successful in order for the returned {@link JsonPointerSelector}
     * to match.
     * @return the newly created JsonPointerSelector based on the passed predicate.
     */
    static JsonPointerSelector predicate(final Predicate<JsonPointer> predicate) {
        return PredicateJsonPointerSelector.predicateSelector(predicate);
    }
}
