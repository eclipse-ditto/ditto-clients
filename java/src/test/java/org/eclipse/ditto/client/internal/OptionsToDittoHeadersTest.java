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
package org.eclipse.ditto.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.EnumSet;

import org.eclipse.ditto.base.model.assertions.DittoBaseAssertions;
import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.client.options.Options;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Unit test for {@link OptionsToDittoHeaders}.
 */
public final class OptionsToDittoHeadersTest {

    private static final JsonSchemaVersion SCHEMA_VERSION = JsonSchemaVersion.V_2;
    private static final String CONDITION_EXPRESSION = "ne(attributes/test)";
    private static final String LIVE_CHANNEL_CONDITION_EXPRESSION = "eq(attributes/value,\"livePolling\")";

    @Rule
    public final TestName testName = new TestName();

    @Test
    public void getDittoHeadersForNullJsonSchemaVersion() {
        assertThatNullPointerException()
                .isThrownBy(() -> OptionsToDittoHeaders.getDittoHeaders(null, Collections.emptySet(), new Option<?>[0]))
                .withMessage("The schemaVersion must not be null!")
                .withNoCause();
    }

    @Test
    public void getDittoHeadersForNullExplicitlyAllowedOptions() {
        assertThatNullPointerException()
                .isThrownBy(() -> OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION, null, new Option<?>[0]))
                .withMessage("The explicitlyAllowedOptions must not be null!")
                .withNoCause();
    }

    @Test
    public void getDittoHeadersForNullOptions() {
        assertThatNullPointerException()
                .isThrownBy(() -> OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION, Collections.emptySet(), null))
                .withMessage("The options must not be null!")
                .withNoCause();
    }

    @Test
    public void getDittoHeadersForAdditionalHeadersOption() {
        final DittoHeaders additionalHeaders = DittoHeaders.newBuilder()
                .correlationId(testName.getMethodName())
                .putHeader("foo", "bar")
                .build();

        final DittoHeaders expectedDittoHeaders = DittoHeaders.newBuilder()
                .schemaVersion(SCHEMA_VERSION)
                .responseRequired(true)
                .putHeaders(additionalHeaders)
                .build();

        final Option<DittoHeaders> dittoHeadersOption = Options.headers(additionalHeaders);

        final DittoHeaders actualDittoHeaders = OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                EnumSet.allOf(OptionName.Global.class),
                new Option[]{dittoHeadersOption});

        assertThat(actualDittoHeaders).isEqualTo(expectedDittoHeaders);
    }

    @Test
    public void getDittoHeadersForEmptyOptions() {
        final DittoHeaders actualDittoHeaders = OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                EnumSet.allOf(OptionName.Modify.class),
                new Option<?>[0]);

        DittoBaseAssertions.assertThat(actualDittoHeaders)
                .hasCorrelationId()
                .hasSchemaVersion(SCHEMA_VERSION)
                .hasIsResponseRequired(true);
    }

    @Test
    public void getDittoHeadersForResponseRequiredOption() {
        final DittoHeaders dittoHeaders = OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                Collections.emptySet(), // response required option is implicitly allowed
                new Option[]{Options.Modify.responseRequired(false)});

        DittoBaseAssertions.assertThat(dittoHeaders)
                .hasCorrelationId()
                .hasSchemaVersion(SCHEMA_VERSION)
                .hasIsResponseRequired(false);
    }

    @Test
    public void getDittoHeadersForAllowedExistsEntityTagMatcherOption() {
        final DittoHeaders dittoHeaders = OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                EnumSet.of(OptionName.Modify.EXISTS),
                new Option[]{Options.Modify.exists(true)});

        DittoBaseAssertions.assertThat(dittoHeaders)
                .hasCorrelationId()
                .hasIsResponseRequired(true);

        assertThat(dittoHeaders).containsEntry(DittoHeaderDefinition.IF_MATCH.getKey(), "*");
    }

    @Test
    public void getDittoHeadersForDisallowedExistsEntityTagMatcherOption() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                        Collections.emptySet(),
                        new Option[]{Options.Modify.exists(true)}))
                .withMessage("Option '%s' is not allowed. This operation only allows [%s, %s].",
                        OptionName.Modify.EXISTS,
                        OptionName.Global.DITTO_HEADERS,
                        OptionName.Modify.RESPONSE_REQUIRED)
                .withNoCause();
    }

    @Test
    public void getDittoHeadersForAllowedNotExistsEntityTagMatcherOption() {
        final DittoHeaders dittoHeaders = OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                EnumSet.of(OptionName.Modify.EXISTS),
                new Option[]{Options.Modify.exists(false)});

        DittoBaseAssertions.assertThat(dittoHeaders)
                .hasCorrelationId()
                .hasIsResponseRequired(true);

        assertThat(dittoHeaders).containsEntry(DittoHeaderDefinition.IF_NONE_MATCH.getKey(), "*");
    }

    @Test
    public void getDittoHeadersForDisallowedNotExistsEntityTagMatcherOption() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                        Collections.emptySet(),
                        new Option[]{Options.Modify.exists(false)}))
                .withMessage("Option '%s' is not allowed. This operation only allows [%s, %s].",
                        OptionName.Modify.EXISTS,
                        OptionName.Global.DITTO_HEADERS,
                        OptionName.Modify.RESPONSE_REQUIRED)
                .withNoCause();
    }

    @Test
    public void getDittoHeadersForAllowedConditionOption() {
        final String condition = CONDITION_EXPRESSION;
        final DittoHeaders dittoHeaders = OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                EnumSet.of(OptionName.Global.CONDITION),
                new Option[]{Options.condition(condition)});

        DittoBaseAssertions.assertThat(dittoHeaders)
                .hasCorrelationId()
                .hasIsResponseRequired(true);

        assertThat(dittoHeaders).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), condition);
    }

    @Test
    public void getDittoHeadersForDisallowedConditionOption() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                        Collections.emptySet(),
                        new Option[]{Options.condition(CONDITION_EXPRESSION)}))
                .withMessage("Option '%s' is not allowed. This operation only allows [%s, %s].",
                        OptionName.Global.CONDITION,
                        OptionName.Global.DITTO_HEADERS,
                        OptionName.Modify.RESPONSE_REQUIRED)
                .withNoCause();
    }

    @Test
    public void getDittoHeadersForAllowedLiveChannelConditionOption() {
        final String expression = LIVE_CHANNEL_CONDITION_EXPRESSION;
        final DittoHeaders dittoHeaders = OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                EnumSet.of(OptionName.Global.LIVE_CHANNEL_CONDITION),
                new Option<?>[]{Options.liveChannelCondition(expression)});

        DittoBaseAssertions.assertThat(dittoHeaders)
                .hasCorrelationId()
                .hasIsResponseRequired(true);

        assertThat(dittoHeaders).containsEntry(DittoHeaderDefinition.LIVE_CHANNEL_CONDITION.getKey(), expression);
    }

    @Test
    public void getDittoHeadersForDisallowedLiveChannelConditionOption() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> OptionsToDittoHeaders.getDittoHeaders(SCHEMA_VERSION,
                        Collections.emptySet(),
                        new Option[]{Options.liveChannelCondition(LIVE_CHANNEL_CONDITION_EXPRESSION)}))
                .withMessage("Option '%s' is not allowed. This operation only allows [%s, %s].",
                        OptionName.Global.LIVE_CHANNEL_CONDITION,
                        OptionName.Global.DITTO_HEADERS,
                        OptionName.Modify.RESPONSE_REQUIRED)
                .withNoCause();
    }

}