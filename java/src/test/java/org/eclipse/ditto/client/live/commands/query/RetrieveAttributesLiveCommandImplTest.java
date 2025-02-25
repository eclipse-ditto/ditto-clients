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
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveAttributes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.query.RetrieveAttributesLiveCommandImpl}.
 */
public final class RetrieveAttributesLiveCommandImplTest {

    private RetrieveAttributes retrieveAttributesTwinCommand;
    private RetrieveAttributesLiveCommand underTest;

    @Before
    public void setUp() {
        retrieveAttributesTwinCommand = RetrieveAttributes.of(TestConstants.Thing.THING_ID,
                TestConstants.JSON_FIELD_SELECTOR_ATTRIBUTES, DittoHeaders.empty());
        underTest = RetrieveAttributesLiveCommandImpl.of(retrieveAttributesTwinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(RetrieveAttributesLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingQueryCommand")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetRetrieveAttributesLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> RetrieveAttributesLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetRetrieveAttributesLiveCommandForCreateAttributeCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> RetrieveAttributesLiveCommandImpl.of(commandMock))
                .withMessageContaining(
                        RetrieveAttributes.class.getName())
                .withNoCause();
    }

    @Test
    public void getRetrieveAttributesLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(retrieveAttributesTwinCommand.getType())
                .withDittoHeaders(retrieveAttributesTwinCommand.getDittoHeaders())
                .withId(retrieveAttributesTwinCommand.getEntityId())
                .withManifest(retrieveAttributesTwinCommand.getManifest())
                .withResourcePath(retrieveAttributesTwinCommand.getResourcePath());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final RetrieveAttributesLiveCommand newRetrieveAttributesLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newRetrieveAttributesLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
                .contains(retrieveAttributesTwinCommand.toString());
    }

}
