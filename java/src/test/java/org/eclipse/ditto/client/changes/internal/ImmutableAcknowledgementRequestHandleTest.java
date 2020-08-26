/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import org.eclipse.ditto.client.ack.internal.ImmutableAcknowledgementRequestHandle;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.entity.id.EntityIdWithType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit tests for {@link org.eclipse.ditto.client.ack.internal.ImmutableAcknowledgementRequestHandle}.
 */
public final class ImmutableAcknowledgementRequestHandleTest {

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableAcknowledgementRequestHandle.class, areImmutable(),
                provided(AcknowledgementLabel.class, EntityIdWithType.class, DittoHeaders.class, Consumer.class)
                        .isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        final ThingId red = ThingId.generateRandom();
        final ThingId black = ThingId.generateRandom();

        EqualsVerifier.forClass(ImmutableAcknowledgementRequestHandle.class)
                .usingGetClass()
                .withIgnoredFields("acknowledgementPublisher")
                .withPrefabValues(EntityIdWithType.class, red, black)
                .verify();
    }

}
