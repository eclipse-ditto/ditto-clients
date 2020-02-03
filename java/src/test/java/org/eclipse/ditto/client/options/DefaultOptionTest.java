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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.client.assertions.ThingsClientApiAssertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link DefaultOption}.
 */
public final class DefaultOptionTest {

    private static final OptionName DEFAULT_OPTION_NAME = OptionName.Modify.RESPONSE_REQUIRED;

    /**
     *
     */
    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DefaultOption.class).verify();
    }

    /**
     *
     */
    @Test
    public void tryToCreateNewInstanceWithNullName() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DefaultOption.newInstance(null, 0))
                .withMessage("The option name must not be null!");
    }

    /**
     *
     */
    @Test
    public void tryToCreateNewInstanceWithNullValue() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> DefaultOption.newInstance(DEFAULT_OPTION_NAME, null))
                .withMessage("The option value must not be null!");
    }

    /**
     *
     */
    @Test
    public void getNameReturnsExpected() {
        final DefaultOption<Integer> underTest = DefaultOption.newInstance(DEFAULT_OPTION_NAME, 0);

        assertThat(underTest).hasName(DEFAULT_OPTION_NAME);
    }

    /**
     *
     */
    @Test
    public void getValueReturnsExpected() {
        final int value = 1337;
        final Option<Integer> underTest = DefaultOption.newInstance(DEFAULT_OPTION_NAME, value);

        Assertions.assertThat(underTest.getValue()).isEqualTo(value);
    }

    /**
     *
     */
    @Test
    public void tryToGetValueAsNullType() {
        final int value = 1337;
        final Option<Integer> underTest = DefaultOption.newInstance(DEFAULT_OPTION_NAME, value);

        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> underTest.getValueAs(null))
                .withMessage("The target type to cast the value of this option to must not be null!");
    }

    /**
     *
     */
    @Test
    public void tryToGetIntValueAsString() {
        final int value = 1337;
        final Class<String> stringClass = String.class;
        final Option<Integer> underTest = DefaultOption.newInstance(DEFAULT_OPTION_NAME, value);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> underTest.getValueAs(stringClass));
    }

    /**
     *
     */
    @Test
    public void getHiddenIntegerValueAsInteger() {
        final Integer value = 1337;
        final Option<Object> underTest = DefaultOption.newInstance(DEFAULT_OPTION_NAME, value);

        Assertions.assertThat(underTest.getValueAs(value.getClass())).isEqualTo(value);
    }

}
