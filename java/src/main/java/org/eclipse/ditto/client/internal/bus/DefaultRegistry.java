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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ditto.json.JsonPointer;

/**
 * Default implementation of {@link Registry}.
 *
 * @since 1.0.0
 */
final class DefaultRegistry<T> implements Registry<T> {

    private final ConcurrentHashMap<JsonPointer, List<Registration<T>>> pointerCache = new
            ConcurrentHashMap<>();
    private final ConcurrentHashMap<JsonPointerSelector, List<Registration<T>>> registrationMap = new
            ConcurrentHashMap<>();

    private final boolean useCache;

    /**
     * Constructs a new DefaultRegistry with JsonPointer caching enabled.
     */
    DefaultRegistry() {
        this(true);
    }

    /**
     * Constructs a new DefaultRegistry withe the passed options.
     *
     * @param useJsonPointerCache whether to use a JsonPointer cache or not.
     */
    private DefaultRegistry(final boolean useJsonPointerCache) {
        this.useCache = useJsonPointerCache;
    }

    @Override
    public synchronized Registration<T> register(final JsonPointerSelector sel, final T obj) {
        List<Registration<T>> registrations;
        if (null == (registrations = this.registrationMap.get(sel))) {
            registrations = this.registrationMap.computeIfAbsent(sel, selector -> new ArrayList<>());
        }

        final Registration<T> reg = new DefaultRegistration<>(sel, obj, () -> {
            this.registrationMap.remove(sel);
            pointerCache.clear();
        });
        registrations.add(reg);

        pointerCache.clear();
        return reg;
    }

    @Override
    public synchronized boolean unregister(final JsonPointer pointer) {
        boolean found = false;
        for (final JsonPointerSelector sel : registrationMap.keySet()) {
            if (!sel.matches(pointer)) {
                continue;
            }
            if (null != registrationMap.remove(sel) && !found) {
                found = true;
            }
        }

        if (useCache) {
            pointerCache.remove(pointer);
        }
        return found;
    }

    @Override
    public synchronized List<Registration<T>> select(final JsonPointer pointer) {
        List<Registration<T>> selectedRegs;
        if (null != (selectedRegs = pointerCache.get(pointer))) {
            return selectedRegs;
        }

        final List<Registration<T>> regs = new ArrayList<>();
        registrationMap.forEach((selector, theRegistrations) -> {
            if (selector.matches(pointer)) {
                regs.addAll(theRegistrations);
            }
        });

        if (useCache && !regs.isEmpty()) {
            pointerCache.put(pointer, regs);
        }

        return regs;
    }

    @Override
    public synchronized void clear() {
        pointerCache.clear();
        registrationMap.clear();
    }

    @Override
    public synchronized Iterator<Registration<T>> iterator() {
        final List<Registration<T>> regs = new ArrayList<>();
        registrationMap.forEach((selector, theRegistrations) -> regs.addAll(theRegistrations));
        return regs.iterator();
    }
}
