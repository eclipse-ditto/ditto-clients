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
package org.eclipse.ditto.client.options.internal;

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;

/**
 * This abstract implementation of {@link OptionVisitor} implements the parts which are common for all option visitors
 * like comparing the name of the option with the expected name, getting the value from the option and handling
 * ClassCastExceptions in case the option value has an unexpected type.
 *
 * @param <T> the supposed type of the value of the option this visitor is interested in.
 * @since 1.0.0
 */
@ThreadSafe
abstract class AbstractOptionVisitor<T> implements OptionVisitor<T> {

    private final OptionName expectedOptionName;
    private T value;

    /**
     * Constructs a new {@code AbstractOptionVisitor} object.
     *
     * @param expectedOptionName the name of the option this visitor is interested in.
     * @throws NullPointerException if {@code expectedOptionName} is {@code null}.
     */
    AbstractOptionVisitor(final OptionName expectedOptionName) {
        this.expectedOptionName = checkNotNull(expectedOptionName);
        value = null;
    }

    @Override
    public boolean visit(final Option<?> option) {
        checkNotNull(option, "option to be visited");

        if (Objects.equals(expectedOptionName, option.getName())) {
            value = tryToGetValueFromOption(option);
        }
        return null != value;
    }

    private T tryToGetValueFromOption(final Option<?> option) {
        try {
            return getValueFromOption(option);
        } catch (final ClassCastException e) {
            final String msgTemplate = "The option value <{0}> is not of expected type!";
            final String msg = MessageFormat.format(msgTemplate, option.getValue());
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * Gets the value from the given option and casts it to the supposed type.
     *
     * @param option the option the get the value from.
     * @return the value of the option in the supposed type;
     * @throws ClassCastException if the option value is not of the supposed type.
     */
    protected abstract T getValueFromOption(Option<?> option);

    @Override
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

}
