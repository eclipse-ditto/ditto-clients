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

import static org.eclipse.ditto.client.TestConstants.Feature.FLUX_CAPACITOR_ID;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.JUnitSoftAssertions;
import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.live.messages.internal.DefaultMessageSerializerRegistry;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.messages.model.MessageDirection;
import org.eclipse.ditto.messages.model.MessageHeaders;
import org.eclipse.ditto.messages.model.MessagesModelFactory;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit test for {@link OutgoingMessageFactory}.
 */
public final class OutgoingMessageFactoryTest {

    private static final JsonSchemaVersion JSON_SCHEMA_VERSION = JsonSchemaVersion.V_2;
    private static final String CONDITION_EXPRESSION = "ne(attributes/test)";
    private static final String LIVE_CHANNEL_CONDITION_EXPRESSION = "eq(attributes/value,\"livePolling\")";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private OutgoingMessageFactory underTest;

    @Before
    public void before() {
        underTest = OutgoingMessageFactory.newInstance(JSON_SCHEMA_VERSION);
    }

    @Test
    public void retrieveFeatureWithOnlyAllowedOptionsReturnsExpected() {
        final RetrieveFeature retrieveFeature = underTest.retrieveFeature(THING_ID,
                FLUX_CAPACITOR_ID,
                Options.condition(CONDITION_EXPRESSION),
                Options.liveChannelCondition(LIVE_CHANNEL_CONDITION_EXPRESSION));

        softly.assertThat((CharSequence) retrieveFeature.getEntityId())
                .as("entity ID")
                .isEqualTo(THING_ID);
        softly.assertThat(retrieveFeature.getFeatureId()).as("feature ID").isEqualTo(FLUX_CAPACITOR_ID);
        softly.assertThat(retrieveFeature.getDittoHeaders())
                .as("Ditto headers")
                .satisfies(dittoHeaders -> {
                    softly.assertThat(dittoHeaders)
                            .as("condition expression")
                            .containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION_EXPRESSION);
                    softly.assertThat(dittoHeaders)
                            .as("live channel condition expression")
                            .containsEntry(DittoHeaderDefinition.LIVE_CHANNEL_CONDITION.getKey(),
                                    LIVE_CHANNEL_CONDITION_EXPRESSION);
                });
    }

    @Test
    public void deleteThingWithLiveChannelConditionExpressionThrowsException() {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> underTest.deleteThing(THING_ID, Options.liveChannelCondition(LIVE_CHANNEL_CONDITION_EXPRESSION)))
                .withMessage("Option '%s' is not allowed. This operation only allows [%s, %s, %s].",
                        OptionName.Global.LIVE_CHANNEL_CONDITION,
                        OptionName.Global.CONDITION,
                        OptionName.Global.DITTO_HEADERS,
                        OptionName.Modify.RESPONSE_REQUIRED)
                .withNoCause();
    }

    @Test
    public void liveMessageWithOnlyAllowedOptionsReturnsExpected() {
        final Message<?> liveMessage = underTest.sendMessage(new DefaultMessageSerializerRegistry(), getMessage(),
                Options.condition(CONDITION_EXPRESSION));

        softly.assertThat((CharSequence) liveMessage.getEntityId())
                .as("entity ID")
                .isEqualTo(THING_ID);
        softly.assertThat(liveMessage.getHeaders())
                .as("Ditto headers")
                .satisfies(dittoHeaders -> {
                    softly.assertThat(dittoHeaders)
                            .as("condition expression")
                            .containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION_EXPRESSION);
                });
    }

    @Test
    public void liverMessageWithLiveChannelConditionExpressionThrowsException() {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> underTest.sendMessage(new DefaultMessageSerializerRegistry(), getMessage(),
                        Options.liveChannelCondition(LIVE_CHANNEL_CONDITION_EXPRESSION)))
                .withMessage("Option '%s' is not allowed. This operation only allows [%s, %s, %s].",
                        OptionName.Global.LIVE_CHANNEL_CONDITION,
                        OptionName.Global.CONDITION,
                        OptionName.Global.DITTO_HEADERS,
                        OptionName.Modify.RESPONSE_REQUIRED)
                .withNoCause();
    }

    private static Message<?> getMessage() {
        return MessagesModelFactory.newMessageBuilder(MessageHeaders.newBuilder(MessageDirection.TO,
                THING_ID, "subject").build()).build();
    }

}