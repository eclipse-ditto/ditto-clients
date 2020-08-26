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
package org.eclipse.ditto.client.live.messages.internal;

import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.function.Consumer;

import org.eclipse.ditto.model.messages.Message;
import org.junit.Test;
import org.mutabilitydetector.unittesting.MutabilityAssert;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit tests for {@link ImmutableRepliableMessage}.
 */
public class ImmutableRepliableMessageTest {


    @Test
    public void assertImmutability() {
        MutabilityAssert.assertInstancesOf(ImmutableRepliableMessage.class, areImmutable(), provided(
                Message.class, Consumer.class).areAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableRepliableMessage.class)
                .withIgnoredFields("acknowledgementPublisher")
                .verify();
    }
}
