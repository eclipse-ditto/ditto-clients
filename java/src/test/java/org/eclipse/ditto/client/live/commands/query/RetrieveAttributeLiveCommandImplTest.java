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
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.query.RetrieveAttributeLiveCommandImpl}.
 */
public final class RetrieveAttributeLiveCommandImplTest {

    private RetrieveAttribute retrieveAttributeTwinCommand;
    private RetrieveAttributeLiveCommand underTest;

    @Before
    public void setUp() {
        retrieveAttributeTwinCommand = RetrieveAttribute.of(TestConstants.Thing.THING_ID,
                TestConstants.Thing.LOCATION_ATTRIBUTE_POINTER, DittoHeaders.empty());
        underTest = RetrieveAttributeLiveCommandImpl.of(retrieveAttributeTwinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveAttributeLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingQueryCommand", "attributePointer")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetRetrieveAttributeLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> RetrieveAttributeLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetRetrieveAttributeLiveCommandForCreateAttributeCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> RetrieveAttributeLiveCommandImpl.of(commandMock))
                .withMessageContaining(RetrieveAttribute.class.getName())
                .withNoCause();
    }

    @Test
    public void getRetrieveAttributeLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(retrieveAttributeTwinCommand.getType())
                .withDittoHeaders(retrieveAttributeTwinCommand.getDittoHeaders())
                .withId(retrieveAttributeTwinCommand.getEntityId())
                .withManifest(retrieveAttributeTwinCommand.getManifest())
                .withResourcePath(retrieveAttributeTwinCommand.getResourcePath());
        assertThat(underTest.getAttributePointer()).isEqualTo(retrieveAttributeTwinCommand.getAttributePointer());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final RetrieveAttributeLiveCommand newRetrieveAttributeLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newRetrieveAttributeLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
                .contains(retrieveAttributeTwinCommand.toString());
    }

}
