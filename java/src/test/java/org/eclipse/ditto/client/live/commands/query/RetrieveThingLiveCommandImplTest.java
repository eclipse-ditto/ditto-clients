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
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThing;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.query.RetrieveThingLiveCommandImpl}.
 */
public final class RetrieveThingLiveCommandImplTest {

    private RetrieveThing retrieveThingTwinCommand;
    private RetrieveThingLiveCommand underTest;

    @Before
    public void setUp() {
        retrieveThingTwinCommand = RetrieveThing.getBuilder(TestConstants.Thing.THING_ID, DittoHeaders.empty())
                .withSelectedFields(TestConstants.JSON_FIELD_SELECTOR_ATTRIBUTES)
                .build();
        underTest = RetrieveThingLiveCommandImpl.of(retrieveThingTwinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveThingLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingQueryCommand")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetRetrieveThingLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> RetrieveThingLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetRetrieveThingLiveCommandForCreateAttributeCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> RetrieveThingLiveCommandImpl.of(commandMock))
                .withMessageContaining(RetrieveThing.class.getName())
                .withNoCause();
    }

    @Test
    public void getRetrieveThingLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(retrieveThingTwinCommand.getType())
                .withDittoHeaders(retrieveThingTwinCommand.getDittoHeaders())
                .withId(retrieveThingTwinCommand.getEntityId())
                .withManifest(retrieveThingTwinCommand.getManifest())
                .withResourcePath(retrieveThingTwinCommand.getResourcePath());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final RetrieveThingLiveCommand newRetrieveThingLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newRetrieveThingLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
                .contains(retrieveThingTwinCommand.toString());
    }

}
