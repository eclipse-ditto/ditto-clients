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
package org.eclipse.ditto.client.live.commands.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.ditto.things.model.signals.commands.ThingCommandResponse;
import org.eclipse.ditto.things.model.signals.events.ThingEvent;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.base.ImmutableLiveCommandAnswer}.
 */
public final class ImmutableLiveCommandAnswerTest {

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableLiveCommandAnswer.class) //
                .usingGetClass() //
                .verify();
    }

    @Test
    public void getResponseReturnsEmptyOptionalForNullResponse() {
        final ImmutableLiveCommandAnswer underTest = ImmutableLiveCommandAnswer.newInstance(null, null);

        assertThat(underTest.getResponse()).isEmpty();
    }

    @Test
    public void getEventReturnsEmptyOptionalForNullEvent() {
        final ImmutableLiveCommandAnswer underTest = ImmutableLiveCommandAnswer.newInstance(null, null);

        assertThat(underTest.getEvent()).isEmpty();
    }

    @Test
    public void getResponseReturnsExpected() {
        final ThingCommandResponse<?> responseMock = Mockito.mock(ThingCommandResponse.class);
        final ImmutableLiveCommandAnswer underTest = ImmutableLiveCommandAnswer.newInstance(responseMock, null);

        assertThat(underTest.getResponse()).contains(responseMock);
    }

    @Test
    public void getEventReturnsExpected() {
        final ThingEvent<?> eventMock = Mockito.mock(ThingEvent.class);
        final ImmutableLiveCommandAnswer underTest = ImmutableLiveCommandAnswer.newInstance(null, eventMock);

        assertThat(underTest.getEvent()).contains(eventMock);
    }

}
