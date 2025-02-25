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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.eclipse.ditto.base.model.signals.commands.assertions.CommandAssertions.assertThat;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.client.live.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.modify.DeleteFeatureDefinitionLiveCommandImpl}.
 */
public final class DeleteFeatureDefinitionLiveCommandImplTest {

    private DeleteFeatureDefinition twinCommand;
    private DeleteFeatureDefinitionLiveCommand underTest;

    @Before
    public void setUp() {
        twinCommand = DeleteFeatureDefinition.of(TestConstants.Thing.THING_ID,
                TestConstants.Feature.FLUX_CAPACITOR_ID, DittoHeaders.empty());
        underTest = DeleteFeatureDefinitionLiveCommandImpl.of(twinCommand);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DeleteFeatureDefinitionLiveCommandImpl.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("thingModifyCommand", "featureId")
                .verify();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetDeleteFeatureDefinitionLiveCommandForNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> DeleteFeatureDefinitionLiveCommandImpl.of(null))
                .withMessage("The %s must not be null!", "command")
                .withNoCause();
    }

    @Test
    public void tryToGetDeleteFeatureDefinitionLiveCommandForCreateFeatureDefinitionCommand() {
        final Command<?> commandMock = Mockito.mock(Command.class);

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> DeleteFeatureDefinitionLiveCommandImpl.of(commandMock))
                .withMessageContaining(DeleteFeatureDefinition.class.getName())
                .withNoCause();
    }

    @Test
    public void getDeleteFeatureDefinitionLiveCommandReturnsExpected() {
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
        final DeleteFeatureDefinitionLiveCommand newDeleteFeatureDefinitionLiveCommand =
                underTest.setDittoHeaders(emptyDittoHeaders);

        assertThat(newDeleteFeatureDefinitionLiveCommand).withDittoHeaders(emptyDittoHeaders);
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
