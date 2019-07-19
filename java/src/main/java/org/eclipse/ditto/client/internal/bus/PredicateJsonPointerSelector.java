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

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonPointer;

/**
 * @since 1.0.0
 */
final class PredicateJsonPointerSelector implements JsonPointerSelector {

    private final Predicate<JsonPointer> predicate;

    private PredicateJsonPointerSelector(final Predicate<JsonPointer> predicate) {
        this.predicate = predicate;
    }

    /**
     * Creates a {@link JsonPointerSelector} based on the given {@link Predicate}.
     *
     * @param predicate The {@link Predicate} to delegate to when matching objects.
     * @return PredicateJsonPointerSelector
     */
    static PredicateJsonPointerSelector predicateSelector(final Predicate<JsonPointer> predicate) {
        return new PredicateJsonPointerSelector(predicate);
    }

    @Override
    public JsonPointer getPointer() {
        return JsonPointer.empty();
    }

    @Override
    public boolean matches(@Nullable final JsonPointer pointer) {
        return predicate.test(pointer);
    }

    @Override
    public boolean test(final JsonPointer key) {
        return predicate.test(key);
    }
}
