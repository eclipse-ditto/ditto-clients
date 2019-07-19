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
package org.eclipse.ditto.client.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;

/**
 * An assert for {@link Option}.
 */
public final class OptionAssert<T> extends AbstractAssert<OptionAssert<T>, Option<T>> {

    /**
     * Creates a new instance of {@code OptionAssert}.
     *
     * @param actual the option to be checked.
     */
    public OptionAssert(final Option<T> actual) {
        super(actual, OptionAssert.class);
    }

    public OptionAssert<T> hasValue(final T expectedUnwrapped) {
        return checkIfEqual(actual.getValue(), expectedUnwrapped, "unwrapped value");
    }

    private <C> OptionAssert<T> checkIfEqual(final C actual, final C expected, final String propertyName) {
        isNotNull();
        Assertions.assertThat(actual)
                .overridingErrorMessage("Expected %s of option to be \n<%s> but it was \n<%s>", propertyName, expected,
                        actual)
                .isEqualTo(expected);
        return myself;
    }

    public OptionAssert<T> hasName(final OptionName expectedName) {
        return checkIfEqual(actual.getName(), expectedName, "name");
    }

}
