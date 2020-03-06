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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Objects;


/**
 * Default implementation of {@link Option}. This class cannot be immutable because it wraps an unknown value which can
 * be indeed mutable.
 *
 * @param <T> the type of the wrapped option value.
 * @since 1.0.0
 */
final class DefaultOption<T> implements Option<T> {

    private final OptionName name;
    private final T value;

    private DefaultOption(final OptionName theName, final T theValue) {
        name = theName;
        value = theValue;
    }

    /**
     * Returns a new instance of {@code DefaultOption}.
     *
     * @param name the name of the option.
     * @param value the value to be wrapped by the new option.
     * @param <T> the type of the wrapped option value.
     * @return the new option.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static <T> DefaultOption<T> newInstance(final OptionName name, final T value) {
        checkNotNull(name, "option name");
        checkNotNull(value, "option value");

        return new DefaultOption<>(name, value);
    }

    @Override
    public OptionName getName() {
        return name;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public <E> E getValueAs(final Class<E> type) {
        checkNotNull(type, "target type to cast the value of this option to");
        return type.cast(value);
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultOption<?> that = (DefaultOption<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "name=" + name + ", value=" + value + "]";
    }

}
