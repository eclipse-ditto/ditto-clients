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
package org.eclipse.ditto.client.internal.bus;

/**
 * Similar to Map.Entry but with object reference identity and fixed key type to act as identifier for
 * a subscription.
 */
final class Entry<T> implements AdaptableBus.SubscriptionId {

    private final Classification key;
    private final T value;

    public Entry(final Classification key, final T value) {
        this.key = key;
        this.value = value;
    }

    public Classification getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
