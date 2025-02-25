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

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.client.live.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatures;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.modify.ModifyFeaturesLiveCommandImpl}.
 */
public final class ModifyFeaturesLiveCommandImplTest {

    private ModifyFeatures twinCommand;
    private ModifyFeaturesLiveCommand underTest;

    @Before
    public void setUp() {
        twinCommand = ModifyFeatures.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FEATURES,
                DittoHeaders.empty());
        underTest = ModifyFeaturesLiveCommandImpl.of(twinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ModifyFeaturesLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingModifyCommand", "features")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetModifyFeaturesLiveCommandForNull() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> ModifyFeaturesLiveCommandImpl.of(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetModifyFeaturesLiveCommandForCreateThingCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> ModifyFeaturesLiveCommandImpl.of(commandMock))
                .withMessageContaining(ModifyFeatures.class.getName())
                .withNoCause();
    }

    @Test
    public void getModifyFeaturesLiveCommandReturnsExpected() {
        assertThat(underTest)
                .withType(twinCommand.getType())
                .withDittoHeaders(twinCommand.getDittoHeaders())
                .withId(twinCommand.getEntityId())
                .withManifest(twinCommand.getManifest())
                .withResourcePath(twinCommand.getResourcePath());
        assertThat(underTest.getFeatures()).isEqualTo(twinCommand.getFeatures());
    }

    @Test
    public void setDittoHeadersReturnsExpected() {
        final DittoHeaders emptyDittoHeaders = DittoHeaders.empty();
        final ModifyFeaturesLiveCommand newModifyFeaturesLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newModifyFeaturesLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
                .contains(twinCommand.toString());
    }

}
