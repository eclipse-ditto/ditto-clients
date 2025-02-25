/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.live.commands.query;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.base.model.signals.commands.assertions.CommandAssertions.assertThat;

import java.text.MessageFormat;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.client.live.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureDesiredProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.query.RetrieveFeatureDesiredPropertyLiveCommandImpl}.
 */
public final class RetrieveFeatureDesiredPropertyLiveCommandImplTest {

    private RetrieveFeatureDesiredProperty retrieveFeatureDesiredPropertyTwinCommand;
    private RetrieveFeatureDesiredPropertyLiveCommand underTest;

    @Before
    public void setUp() {
        retrieveFeatureDesiredPropertyTwinCommand = RetrieveFeatureDesiredProperty.of(TestConstants.Thing.THING_ID,
                TestConstants.Feature.HOVER_BOARD_ID, TestConstants.Feature.HOVER_BOARD_PROPERTY_POINTER,
                DittoHeaders.empty());
        underTest = RetrieveFeatureDesiredPropertyLiveCommandImpl.of(retrieveFeatureDesiredPropertyTwinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveFeatureDesiredPropertyLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingQueryCommand", "featureId", "desiredPropertyPointer")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetRetrieveFeatureDesiredPropertyLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> RetrieveFeatureDesiredPropertyLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetRetrieveFeatureDesiredPropertyLiveCommandForCreateAttributeCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> RetrieveFeatureDesiredPropertyLiveCommandImpl.of(commandMock))
                .withMessageContaining(RetrieveFeatureDesiredProperty.class.getName())
                .withNoCause();
    }

    @Test
    public void getRetrieveFeatureDesiredPropertyLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(retrieveFeatureDesiredPropertyTwinCommand.getType())
                .withDittoHeaders(retrieveFeatureDesiredPropertyTwinCommand.getDittoHeaders())
                .withId(retrieveFeatureDesiredPropertyTwinCommand.getEntityId())
                .withManifest(retrieveFeatureDesiredPropertyTwinCommand.getManifest())
                .withResourcePath(retrieveFeatureDesiredPropertyTwinCommand.getResourcePath());
        assertThat(underTest.getFeatureId()).isEqualTo(retrieveFeatureDesiredPropertyTwinCommand.getFeatureId());
        assertThat(underTest.getDesiredPropertyPointer())
                .isEqualTo(retrieveFeatureDesiredPropertyTwinCommand.getDesiredPropertyPointer());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final RetrieveFeatureDesiredPropertyLiveCommand newRetrieveFeatureDesiredPropertyLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newRetrieveFeatureDesiredPropertyLiveCommand).withDittoHeaders(emptyDittoHeaders);
    }

    @Test
    public void answerReturnsNotNull() {
        Assertions.assertThat(underTest.answer()).isNotNull();
    }

    @Test
    public void toStringReturnsExpected() {
        assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains("command=")
                .contains(retrieveFeatureDesiredPropertyTwinCommand.toString());
    }

}
