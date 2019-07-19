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
package org.eclipse.ditto.client.options;

/**
 * This interface represents an option consisting of a name and a value.
 *
 * @since 1.0.0
 */
public interface Option<T> {

    /**
     * Returns the name of this option.
     *
     * @return the name of this option.
     */
    OptionName getName();

    /**
     * Returns the plain wrapped option value.
     *
     * @return the wrapped value.
     */
    T getValue();

    /**
     * Returns the wrapped value as given type.
     *
     * @param type the class of type.
     * @param <E> the type to return the wrapped value as.
     * @return the wrapped value as {@code type}.
     * @throws NullPointerException if {@code type} is {@code null}.
     * @throws ClassCastException if the wrapped value cannot be cast to {@code type}.
     */
    <E> E getValueAs(Class<E> type);

}
