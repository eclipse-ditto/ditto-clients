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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.client.options.internal.MockOptionFactory.createOptionMock;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link OptionsValidator}.
 */
public final class OptionsValidatorTest {

    private OptionsValidator underTest = null;


    @Before
    public void setUp() {
        underTest = new OptionsValidator();
    }

    @Test
    public void allOptionsHaveDifferentNames() {
        final Option<?> responseRequiredOption = createOptionMock(OptionName.Modify.RESPONSE_REQUIRED, true);
        final Option<?>[] options = new Option<?>[]{responseRequiredOption};

        underTest.accept(options);
    }

    @Test
    public void twoOptionsHaveSameName() {
        final Option<?> firstOption = createOptionMock(OptionName.Modify.RESPONSE_REQUIRED, false);
        final Option<?> responseRequiredOption = createOptionMock(OptionName.Modify.RESPONSE_REQUIRED, true);
        final Option<?>[] options = new Option<?>[]{firstOption, responseRequiredOption};

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> underTest.accept(options))
                .withMessage(String.format("You provided at least two options with name <%s> but different value!",
                        OptionName.Modify.RESPONSE_REQUIRED));
    }

}
