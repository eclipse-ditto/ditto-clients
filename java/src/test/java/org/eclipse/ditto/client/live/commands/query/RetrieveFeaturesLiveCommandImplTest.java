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
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatures;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.query.RetrieveFeaturesLiveCommandImpl}.
 */
public final class RetrieveFeaturesLiveCommandImplTest {

    private RetrieveFeatures retrieveFeaturesTwinCommand;
    private RetrieveFeaturesLiveCommand underTest;

    @Before
    public void setUp() {
        retrieveFeaturesTwinCommand = RetrieveFeatures.of(TestConstants.Thing.THING_ID,
                TestConstants.JSON_FIELD_SELECTOR_ATTRIBUTES, DittoHeaders.empty());
        underTest = RetrieveFeaturesLiveCommandImpl.of(retrieveFeaturesTwinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveFeaturesLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingQueryCommand")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetRetrieveFeaturesLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> RetrieveFeaturesLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetRetrieveFeaturesLiveCommandForCreateAttributeCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> RetrieveFeaturesLiveCommandImpl.of(commandMock))
                .withMessageContaining(RetrieveFeatures.class.getName())
                .withNoCause();
    }

    @Test
    public void getRetrieveFeaturesLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(retrieveFeaturesTwinCommand.getType())
                .withDittoHeaders(retrieveFeaturesTwinCommand.getDittoHeaders())
                .withId(retrieveFeaturesTwinCommand.getEntityId())
                .withManifest(retrieveFeaturesTwinCommand.getManifest())
                .withResourcePath(retrieveFeaturesTwinCommand.getResourcePath());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final RetrieveFeaturesLiveCommand newRetrieveFeaturesLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newRetrieveFeaturesLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
                .contains(retrieveFeaturesTwinCommand.toString());
    }

}
