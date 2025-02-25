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
package org.eclipse.ditto.client.live.commands.modify;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.base.model.signals.commands.assertions.CommandAssertions.assertThat;

import java.text.MessageFormat;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.client.live.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.modify.DeleteFeaturePropertiesLiveCommandImpl}.
 */
public final class DeleteFeaturePropertiesLiveCommandImplTest {

    private DeleteFeatureProperties twinCommand;
    private DeleteFeaturePropertiesLiveCommand underTest;

    @Before
    public void setUp() {
        twinCommand = DeleteFeatureProperties.of(TestConstants.Thing.THING_ID,
                TestConstants.Feature.FLUX_CAPACITOR_ID, DittoHeaders.empty());
        underTest = DeleteFeaturePropertiesLiveCommandImpl.of(twinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DeleteFeaturePropertiesLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingModifyCommand", "featureId")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetDeleteFeaturePropertiesLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> DeleteFeaturePropertiesLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetDeleteFeaturePropertiesLiveCommandForCreateFeaturePropertiesCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> DeleteFeaturePropertiesLiveCommandImpl.of(commandMock))
                .withMessageContaining(DeleteFeatureProperties.class.getName())
                .withNoCause();
    }

    @Test
    public void getDeleteFeaturePropertiesLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(twinCommand.getType())
                .withDittoHeaders(twinCommand.getDittoHeaders())
                .withId(twinCommand.getEntityId())
                .withManifest(twinCommand.getManifest())
                .withResourcePath(twinCommand.getResourcePath());
        assertThat(underTest.getFeatureId()).isEqualTo(twinCommand.getFeatureId());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final DeleteFeaturePropertiesLiveCommand newDeleteFeaturePropertiesLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newDeleteFeaturePropertiesLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
                .contains(twinCommand.toString());
    }

}
