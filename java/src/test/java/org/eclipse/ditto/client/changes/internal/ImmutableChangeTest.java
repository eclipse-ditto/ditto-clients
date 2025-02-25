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

import org.eclipse.ditto.base.model.entity.id.EntityId;
import org.eclipse.ditto.things.model.ThingId;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableChange}.
 */
public final class ImmutableChangeTest {

    @Test
    public void testHashCodeAndEquals() {
        final ThingId red = ThingId.generateRandom();
        final ThingId black = ThingId.generateRandom();

        EqualsVerifier.forClass(ImmutableChange.class)
                .usingGetClass()
                .withIgnoredFields("acknowledgementPublisher")
                .withPrefabValues(EntityId.class, red, black)
                .verify();
    }

}
