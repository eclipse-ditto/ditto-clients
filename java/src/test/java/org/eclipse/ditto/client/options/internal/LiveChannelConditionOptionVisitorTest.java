/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.Options;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Unit test for {@link LiveChannelConditionOptionVisitor}.
 */
public final class LiveChannelConditionOptionVisitorTest {

    private static final String LIVE_CHANNEL_CONDITION_EXPRESSION = "eq(attributes/value,\"livePolling\")";

    @Rule
    public final TestName testName = new TestName();

    private DittoHeaders dittoHeaders;

    private LiveChannelConditionOptionVisitor underTest;

    @Before
    public void before() {
        dittoHeaders = DittoHeaders.newBuilder().correlationId(testName.getMethodName()).build();
        underTest = new LiveChannelConditionOptionVisitor();
    }

    @Test
    public void visitWithNullOptionThrowsException() {
        assertThatNullPointerException()
                .isThrownBy(() -> underTest.visit(null))
                .withMessage("The option to be visited must not be null!")
                .withNoCause();
    }

    @Test
    public void getValueFromOptionWithUnexpectedValueTypeThrowsException() {
        final Option<DittoHeaders> headersOption = Options.headers(dittoHeaders);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> underTest.getValueFromOption(headersOption))
                .withMessage("Cannot cast %s to %s", dittoHeaders.getClass().getName(), String.class.getName())
                .withNoCause();
    }

    @Test
    public void visitOptionWithUnexpectedNameReturnsFalse() {
        final Option<DittoHeaders> headersOption = Options.headers(dittoHeaders);

        assertThat(underTest.visit(headersOption)).isFalse();
    }

    @Test
    public void visitAppropriateOptionReturnsTrue() {
        final Option<String> liveChannelConditionOption =
                Options.liveChannelCondition(LIVE_CHANNEL_CONDITION_EXPRESSION);

        assertThat(underTest.visit(liveChannelConditionOption)).isTrue();
    }

    @Test
    public void visitAppropriateOptionAndGetValueReturnsExpectedValue() {
        final Option<String> liveChannelConditionOption =
                Options.liveChannelCondition(LIVE_CHANNEL_CONDITION_EXPRESSION);

        underTest.visit(liveChannelConditionOption);

        assertThat(underTest.getValue()).contains(LIVE_CHANNEL_CONDITION_EXPRESSION);
    }

}