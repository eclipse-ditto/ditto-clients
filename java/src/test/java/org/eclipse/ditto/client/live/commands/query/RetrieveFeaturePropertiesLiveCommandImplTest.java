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

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.client.live.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.query.RetrieveFeaturePropertiesLiveCommandImpl}.
 */
public final class RetrieveFeaturePropertiesLiveCommandImplTest {

    private RetrieveFeatureProperties retrieveFeaturePropertiesTwinCommand;
    private RetrieveFeaturePropertiesLiveCommand underTest;

    @Before
    public void setUp() {
        retrieveFeaturePropertiesTwinCommand = RetrieveFeatureProperties.of(TestConstants.Thing.THING_ID,
                TestConstants.Feature.FLUX_CAPACITOR_ID, TestConstants.JSON_FIELD_SELECTOR_ATTRIBUTES,
                DittoHeaders.empty());
        underTest = RetrieveFeaturePropertiesLiveCommandImpl.of(retrieveFeaturePropertiesTwinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveFeaturePropertiesLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingQueryCommand", "featureId")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetRetrieveFeaturePropertiesLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> RetrieveFeaturePropertiesLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetRetrieveFeaturePropertiesLiveCommandForCreateAttributeCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> RetrieveFeaturePropertiesLiveCommandImpl.of(commandMock))
                .withMessageContaining(RetrieveFeatureProperties.class.getName())
                .withNoCause();
    }

    @Test
    public void getRetrieveFeaturePropertiesLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(retrieveFeaturePropertiesTwinCommand.getType())
                .withDittoHeaders(retrieveFeaturePropertiesTwinCommand.getDittoHeaders())
                .withId(retrieveFeaturePropertiesTwinCommand.getEntityId())
                .withManifest(retrieveFeaturePropertiesTwinCommand.getManifest())
                .withResourcePath(retrieveFeaturePropertiesTwinCommand.getResourcePath());
        assertThat(underTest.getFeatureId()).isEqualTo(retrieveFeaturePropertiesTwinCommand.getFeatureId());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final RetrieveFeaturePropertiesLiveCommand newRetrieveFeaturePropertiesLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newRetrieveFeaturePropertiesLiveCommand).withDittoHeaders(emptyDittoHeaders);
    }

    @Test
    public void answerReturnsNotNull() {
        assertThat(underTest.answer()).isNotNull();
    }

    @Test
    public void toStringReturnsExpected() {
        assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains("command=")
                .contains(retrieveFeaturePropertiesTwinCommand.toString());
    }

}
