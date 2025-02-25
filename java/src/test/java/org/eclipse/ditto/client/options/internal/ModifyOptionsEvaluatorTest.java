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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.Options;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link OptionsEvaluator.Modify}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ModifyOptionsEvaluatorTest {

    private static final Option<Boolean> RESPONSE_REQUIRED_OPTION = Options.Modify.responseRequired(false);

    private OptionsEvaluator.Modify underTest = null;


    @Before
    public void setUp() {
        final Option<?>[] options = new Option<?>[]{RESPONSE_REQUIRED_OPTION};
        underTest = OptionsEvaluator.forModifyOptions(options);
    }

    @Test
    public void tryToCreateInstanceWithNullOptions() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> OptionsEvaluator.forModifyOptions(null))
                .withMessage("The options must not be null!");
    }

    @Test
    public void createInstanceWithEmptyOptions() {
        final OptionsEvaluator.Modify underTest = OptionsEvaluator.forModifyOptions(new Option<?>[0]);

        assertThat(underTest).isNotNull();
    }

    @Test
    public void getResponseTimeoutReturnsExpectedIfProvided() {
        assertThat(underTest.isResponseRequired()).contains(RESPONSE_REQUIRED_OPTION.getValue());
    }

    @Test
    public void getResponseTimeoutReturnsEmptyOptionalIfNotProvided() {
        final Option<?>[] options = new Option<?>[]{};
        underTest = OptionsEvaluator.forModifyOptions(options);

        assertThat(underTest.isResponseRequired()).isEmpty();
    }

}
