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

import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.function.Consumer;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.id.EntityIdWithType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableChange}.
 */
public final class ImmutableChangeTest {

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableChange.class, areImmutable(),
                provided(JsonValue.class, JsonPointer.class, EntityIdWithType.class, JsonObject.class,
                        DittoHeaders.class, Consumer.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        final ThingId red = ThingId.generateRandom();
        final ThingId black = ThingId.generateRandom();

        EqualsVerifier.forClass(ImmutableChange.class)
                .usingGetClass()
                .withIgnoredFields("acknowledgementPublisher")
                .withPrefabValues(EntityIdWithType.class, red, black)
                .verify();
    }

}
