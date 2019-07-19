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

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonPointer;

/**
 * Default implementation of {@link Registration}.
 *
 * @since 1.0.0
 */
final class DefaultRegistration<T> implements Registration<T> {

    private static final JsonPointerSelector NO_MATCH = new JsonPointerSelector() {
        @Override
        public boolean test(final JsonPointer jsonKeys) {
            return false;
        }

        @Override
        public JsonPointer getPointer() {
            return JsonPointer.empty();
        }

        @Override
        public boolean matches(@Nullable final JsonPointer pointer) {
            return false;
        }
    };

    private final JsonPointerSelector selector;
    private final T toRegister;
    private final Runnable onCancel;

    private volatile boolean cancelled = false;

    /**
     * Constructs a new DefaultRegistration with the passed JsonPointerSelector, the Object {@code toRegister} and a
     * Runnable to execute once a registration is canceled.
     *
     * @param selector the JsonPointerSelector to register the Object at.
     * @param toRegister the Object to register.
     * @param onCancel the Runnable to execute once a registration is canceled.
     */
    DefaultRegistration(final JsonPointerSelector selector, final T toRegister, final Runnable onCancel) {
        this.selector = selector;
        this.toRegister = toRegister;
        this.onCancel = onCancel;
    }

    @Override
    public JsonPointerSelector getSelector() {
        return (!cancelled ? selector : NO_MATCH);
    }

    @Override
    @Nullable
    public T getRegisteredObject() {
        return !cancelled ? toRegister : null;
    }

    @Override
    public void cancel() {
        if (!cancelled) {
            onCancel.run();
            this.cancelled = true;
        }
    }
}
