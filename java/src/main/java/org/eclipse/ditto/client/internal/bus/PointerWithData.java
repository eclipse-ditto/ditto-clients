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
 * @param <J> the type of the held additional data
 * @since 1.0.0
 */
public final class PointerWithData<T, J> {

    private final JsonPointer pointer;
    private final T data;
    private final J additionalData;

    private PointerWithData(final JsonPointer pointer, final T data, final J additionalData) {
        this.pointer = pointer;
        this.data = data;
        this.additionalData = additionalData;
    }

    /**
     * Constructs a new PointerWithData with the passed {@code pointer} and {@code data}.
     *
     * @param pointer the JsonPointer.
     * @param data the data.
     * @param additionalData the additional data.
     * @param <T> the type of the passed in data
     * @param <J> the type of the passed in additional data
     * @return the newly created PointerWithData.
     */
    static <T, J> PointerWithData<T, J> create(final JsonPointer pointer, final T data, final J additionalData) {
        return new PointerWithData<>(pointer, data, additionalData);
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

    /**
     * @return the held additional data.
     */
    public J getAdditionalData() {
        return additionalData;
    }

}

