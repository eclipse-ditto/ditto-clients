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
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link OptionsEvaluator.Consumption}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ConsumptionOptionsEvaluatorTest {

    private static final Option<JsonFieldSelector> EXTRA_FIELDS = Options.Consumption.extraFields(
            JsonFieldSelector.newInstance("foo/bar"));

    private OptionsEvaluator.Consumption underTest = null;


    @Before
    public void setUp() {
        final Option<?>[] options = new Option<?>[]{EXTRA_FIELDS};
        underTest = OptionsEvaluator.forConsumptionOptions(options);
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(OptionsEvaluator.Consumption.class, areImmutable(),
                provided(OptionsEvaluator.class).isAlsoImmutable());
    }

    @Test
    public void tryToCreateInstanceWithNullOptions() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> OptionsEvaluator.forConsumptionOptions(null))
                .withMessage("The options must not be null!");
    }

    @Test
    public void createInstanceWithEmptyOptions() {
        final OptionsEvaluator.Consumption underTest = OptionsEvaluator.forConsumptionOptions(new Option<?>[0]);

        assertThat(underTest).isNotNull();
    }

    @Test
    public void getResponseTimeoutReturnsExpectedIfProvided() {
        assertThat(underTest.getExtraFields()).contains(EXTRA_FIELDS.getValue());
    }

    @Test
    public void getResponseTimeoutReturnsEmptyOptionalIfNotProvided() {
        final Option<?>[] options = new Option<?>[]{};
        underTest = OptionsEvaluator.forConsumptionOptions(options);

        assertThat(underTest.getExtraFields()).isEmpty();
    }

}
