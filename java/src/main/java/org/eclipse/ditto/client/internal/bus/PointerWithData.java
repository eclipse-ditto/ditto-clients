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

import org.eclipse.ditto.json.JsonPointer;

/**
 * Holds a JsonPointer and some arbitrary data of type {@code <T>}.
 *
 * @param <T> the type of the held data
 * @since 1.0.0
 */
public final class PointerWithData<T> {

    private final JsonPointer pointer;
    private final T data;

    private PointerWithData(final JsonPointer pointer, final T data) {
        this.pointer = pointer;
        this.data = data;
    }

    /**
     * Constructs a new PointerWithData with the passed {@code pointer} and {@code data}.
     *
     * @param pointer the JsonPointer.
     * @param data the data.
     * @param <T> the type of the passed in data
     * @return the newly created PointerWithData.
     */
    static <T> PointerWithData<T> create(final JsonPointer pointer, final T data) {
        return new PointerWithData<>(pointer, data);
    }

    /**
     * @return the held JsonPointer.
     */
    public JsonPointer getPointer() {
        return pointer;
    }

    /**
     * @return the held data.
     */
    public T getData() {
        return data;
    }

}

