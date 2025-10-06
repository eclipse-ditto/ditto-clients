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

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.JUnitSoftAssertions;
import org.eclipse.ditto.json.JsonPointer;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit test for {@link Options}.
 */
public final class OptionsTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

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

    @Test
    public void mergeThingPatchConditionsWithNullMapThrowsException() {
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> Options.mergeThingPatchConditions(null))
                .withMessage("The patchConditions must not be null!")
                .withNoCause();
    }

    @Test
    public void mergeThingPatchConditionsWithEmptyMapThrowsException() {
        final Map<JsonPointer, String> emptyMap = new HashMap<>();

        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> Options.mergeThingPatchConditions(emptyMap))
                .withMessage("The patchConditions map must not be empty.")
                .withNoCause();
    }

    @Test
    public void mergeThingPatchConditionsWithValidMapReturnsExpected() {
        final Map<JsonPointer, String> patchConditions = new HashMap<>();
        patchConditions.put(JsonPointer.of("features/temperature/properties/value"), "gt(features/temperature/properties/value, 20)");
        patchConditions.put(JsonPointer.of("features/humidity/properties/value"), "lt(features/humidity/properties/value, 80)");

        final Option<Map<JsonPointer, String>> option = Options.mergeThingPatchConditions(patchConditions);

        softly.assertThat(option.getName()).as("option name").isEqualTo(OptionName.Global.MERGE_THING_PATCH_CONDITIONS);
        softly.assertThat(option.getValue()).as("option value").isEqualTo(patchConditions);
    }

}
