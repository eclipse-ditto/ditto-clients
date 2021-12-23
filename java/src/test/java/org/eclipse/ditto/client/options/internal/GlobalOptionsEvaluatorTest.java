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

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.Options;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link OptionsEvaluator.Global}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class GlobalOptionsEvaluatorTest {

    private static final String CONDITION_EXPRESSION = "ne(attributes/test)";
    private static final String LIVE_CHANNEL_CONDITION_EXPRESSION = "eq(attributes/value,\"livePolling\")";

    private static Option<String> conditionOption;
    private static Option<String> liveChannelConditionOption;
    private static Option<?>[] emptyGlobalOptions;

    @Rule
    public final TestName testName = new TestName();

    private Option<DittoHeaders> dittoHeadersOption;
    private Option<?>[] allGlobalOptions;

    @BeforeClass
    public static void beforeClass() {
        conditionOption = Options.condition(CONDITION_EXPRESSION);
        liveChannelConditionOption = Options.liveChannelCondition(LIVE_CHANNEL_CONDITION_EXPRESSION);
        emptyGlobalOptions = new Option<?>[0];
    }

    @Before
    public void before() {
        dittoHeadersOption = Options.headers(DittoHeaders.newBuilder().correlationId(testName.getMethodName()).build());
        allGlobalOptions = new Option<?>[]{dittoHeadersOption, conditionOption, liveChannelConditionOption};
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(OptionsEvaluator.Global.class,
                areImmutable(),
                provided(OptionsEvaluator.class).isAlsoImmutable());
    }

    @Test
    public void tryToCreateInstanceWithNullOptions() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> OptionsEvaluator.forGlobalOptions(null))
                .withMessage("The options must not be null!");
    }

    @Test
    public void createInstanceWithEmptyOptions() {
        final OptionsEvaluator.Global underTest = OptionsEvaluator.forGlobalOptions(emptyGlobalOptions);

        assertThat(underTest).isNotNull();
    }

    @Test
    public void getDittoHeadersReturnsExpectedDittoHeadersIfOptionProvided() {
        final OptionsEvaluator.Global underTest = OptionsEvaluator.forGlobalOptions(allGlobalOptions);

        assertThat(underTest.getDittoHeaders()).contains(dittoHeadersOption.getValue());
    }

    @Test
    public void getDittoHeadersReturnsEmptyOptionalIfNotProvided() {
        final OptionsEvaluator.Global underTest = OptionsEvaluator.forGlobalOptions(emptyGlobalOptions);

        assertThat(underTest.getDittoHeaders()).isEmpty();
    }

    @Test
    public void conditionReturnsExpectedConditionExpressionIfOptionProvided() {
        final OptionsEvaluator.Global underTest = OptionsEvaluator.forGlobalOptions(allGlobalOptions);

        assertThat(underTest.condition()).contains(conditionOption.getValue());
    }

    @Test
    public void conditionReturnsEmptyOptionalIfOptionNotProvided() {
        final OptionsEvaluator.Global underTest = OptionsEvaluator.forGlobalOptions(emptyGlobalOptions);

        assertThat(underTest.condition()).isEmpty();
    }

    @Test
    public void getLiveChannelConditionReturnsExpectedLiveChannelConditionExpressionIfOptionProvided() {
        final OptionsEvaluator.Global underTest = OptionsEvaluator.forGlobalOptions(allGlobalOptions);

        assertThat(underTest.getLiveChannelCondition()).contains(liveChannelConditionOption.getValue());
    }

    @Test
    public void getLiveChannelConditionReturnsEmptyOptionalIfOptionNotProvided() {
        final OptionsEvaluator.Global underTest = OptionsEvaluator.forGlobalOptions(emptyGlobalOptions);

        assertThat(underTest.getLiveChannelCondition()).isEmpty();
    }

}
