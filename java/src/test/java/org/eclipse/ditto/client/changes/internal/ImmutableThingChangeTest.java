/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.changes.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.time.Instant;
import java.util.Optional;

import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.things.Thing;
import org.junit.Test;
import org.mockito.Mockito;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableThingChange}.
 */
public final class ImmutableThingChangeTest {

    private static final ChangeAction KNOWN_ACTION = ChangeAction.DELETED;
    private static final Thing THING_MOCK = Mockito.mock(Thing.class);
    private static final JsonPointer POINTER_MOCK = Mockito.mock(JsonPointer.class);
    private static final long KNOWN_REVISION = 34L;
    private static final Instant KNOWN_TIMESTAMP = Instant.now();

    @Test
    public void constructWithValidThingIdAndType() {
        final ThingChange thingChange =
                new ImmutableThingChange(THING_ID, KNOWN_ACTION, THING_MOCK, POINTER_MOCK, KNOWN_REVISION,
                        KNOWN_TIMESTAMP, null);

        assertThat(thingChange).isNotNull();
    }

    @Test
    public void constructWithNullThingId() {
        assertThatNullPointerException()
                .isThrownBy(() -> new ImmutableThingChange(null, KNOWN_ACTION, THING_MOCK, POINTER_MOCK, KNOWN_REVISION,
                        KNOWN_TIMESTAMP, null))
                .withMessage("The %s must not be null!", "Thing ID")
                .withNoCause();
    }

    @Test
    public void constructWithChangeAction() {
        assertThatNullPointerException()
                .isThrownBy(
                        () -> new ImmutableThingChange(THING_ID, null, THING_MOCK, POINTER_MOCK, KNOWN_REVISION,
                                KNOWN_TIMESTAMP, null))
                .withMessage("The %s must not be null!", "change action")
                .withNoCause();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableThingChange.class,
                areImmutable(),
                provided(Thing.class, ImmutableChange.class, JsonObject.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableThingChange.class)
                .usingGetClass()
                .verify();
    }

    @Test
    public void getHashCodeForChangeWithNullThing() {
        final ImmutableThingChange underTest =
                new ImmutableThingChange(THING_ID, KNOWN_ACTION, null, POINTER_MOCK, KNOWN_REVISION,
                        KNOWN_TIMESTAMP, null);

        final int hashCode = underTest.hashCode();

        assertThat(hashCode).isNotZero();
    }

    @Test
    public void gettersReturnExpected() {
        final ThingChange underTest =
                new ImmutableThingChange(THING_ID, KNOWN_ACTION, THING_MOCK, POINTER_MOCK, KNOWN_REVISION,
                        KNOWN_TIMESTAMP, null);

        assertThat((CharSequence) underTest.getEntityId()).isEqualTo(THING_ID);
        assertThat(underTest.getAction()).isSameAs(KNOWN_ACTION);
        assertThat(underTest.getThing()).contains(THING_MOCK);
    }

    @Test
    public void createEventWithThing() {
        final ThingChange underTest =
                new ImmutableThingChange(THING_ID, KNOWN_ACTION, THING_MOCK, POINTER_MOCK, KNOWN_REVISION,
                        KNOWN_TIMESTAMP, null);

        final Optional<Thing> thing = underTest.getThing();

        assertThat(thing).contains(THING_MOCK);
    }

}
