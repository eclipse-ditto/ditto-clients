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

/**
 * Contains {@link JsonPointerSelector}s and registered objects on the {@code Bus} and adds capabilities to cancel a
 * registration again.
 *
 * @param <T> the type of the data the registration's object holds
 * @since 1.0.0
 */
public interface Registration<T> {

    /**
     * The {@link JsonPointerSelector} that was used when the registration was made.
     *
     * @return the registration's selector
     */
    JsonPointerSelector getSelector();

    /**
     * The object that was registered
     *
     * @return the registered object
     */
    @Nullable
    T getRegisteredObject();

    /**
     * Cancel this {@literal Registration} by removing it from its registry.
     */
    void cancel();
}
