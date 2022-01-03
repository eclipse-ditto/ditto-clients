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

import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit test for {@link Options}.
 */
public final class OptionsTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void assertImmutability() {
        assertInstancesOf(Options.class, areImmutable());
    }

    @Test
    public void lifeChannelConditionWithNullExpressionThrowsException() {
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> Options.liveChannelCondition(null))
                .withMessage("The liveChannelConditionExpression must not be null!")
                .withNoCause();
    }

    @Test
    public void lifeChannelConditionWithEmptyExpressionThrowsException() {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> Options.liveChannelCondition(""))
                .withMessage("The liveChannelConditionExpression must not be blank.")
                .withNoCause();
    }

    @Test
    public void lifeChannelConditionWithBlankExpressionThrowsException() {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> Options.liveChannelCondition("     "))
                .withMessage("The liveChannelConditionExpression must not be blank.")
                .withNoCause();
    }

    @Test
    public void lifeChannelConditionWithValidExpressionReturnsExpected() {
        final String liveChannelConditionExpression = "eq(attributes/value,\"livePolling\")";

        final Option<String> option = Options.liveChannelCondition(liveChannelConditionExpression);

        softly.assertThat(option.getName()).as("option name").isEqualTo(OptionName.Global.LIVE_CHANNEL_CONDITION);
        softly.assertThat(option.getValue()).as("option value").isEqualTo(liveChannelConditionExpression);
    }

}
