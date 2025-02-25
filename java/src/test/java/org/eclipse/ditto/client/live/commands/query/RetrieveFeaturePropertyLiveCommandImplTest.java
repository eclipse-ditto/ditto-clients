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
package org.eclipse.ditto.client.live.commands.query;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.base.model.signals.commands.assertions.CommandAssertions.assertThat;

import java.text.MessageFormat;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.client.live.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.query.RetrieveFeaturePropertyLiveCommandImpl}.
 */
public final class RetrieveFeaturePropertyLiveCommandImplTest {

    private RetrieveFeatureProperty retrieveFeaturePropertyTwinCommand;
    private RetrieveFeaturePropertyLiveCommand underTest;

    @Before
    public void setUp() {
        retrieveFeaturePropertyTwinCommand = RetrieveFeatureProperty.of(TestConstants.Thing.THING_ID,
                TestConstants.Feature.FLUX_CAPACITOR_ID, TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_POINTER,
                DittoHeaders.empty());
        underTest = RetrieveFeaturePropertyLiveCommandImpl.of(retrieveFeaturePropertyTwinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveFeaturePropertyLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingQueryCommand", "featureId", "propertyPointer")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetRetrieveFeaturePropertyLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> RetrieveFeaturePropertyLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetRetrieveFeaturePropertyLiveCommandForCreateAttributeCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> RetrieveFeaturePropertyLiveCommandImpl.of(commandMock))
                .withMessageContaining(RetrieveFeatureProperty.class.getName())
                .withNoCause();
    }

    @Test
    public void getRetrieveFeaturePropertyLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(retrieveFeaturePropertyTwinCommand.getType())
                .withDittoHeaders(retrieveFeaturePropertyTwinCommand.getDittoHeaders())
                .withId(retrieveFeaturePropertyTwinCommand.getEntityId())
                .withManifest(retrieveFeaturePropertyTwinCommand.getManifest())
                .withResourcePath(retrieveFeaturePropertyTwinCommand.getResourcePath());
        assertThat(underTest.getFeatureId()).isEqualTo(retrieveFeaturePropertyTwinCommand.getFeatureId());
        assertThat(underTest.getPropertyPointer())
                .isEqualTo(retrieveFeaturePropertyTwinCommand.getPropertyPointer());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final RetrieveFeaturePropertyLiveCommand newRetrieveFeaturePropertyLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newRetrieveFeaturePropertyLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
                .contains(retrieveFeaturePropertyTwinCommand.toString());
    }

}
