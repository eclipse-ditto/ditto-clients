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
package org.eclipse.ditto.client.live.events.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.TestConstants;
import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableThingEventFactory}.
 */
public final class ImmutableThingEventFactoryTest {

    private static final JsonSchemaVersion SCHEMA_VERSION = JsonSchemaVersion.V_2;

    private ImmutableThingEventFactory underTest = null;


    @Before
    public void setUp() {
        underTest = ImmutableThingEventFactory.getInstance(SCHEMA_VERSION, TestConstants.Thing.THING_ID);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableThingEventFactory.class).usingGetClass().verify();
    }

    @Test
    public void toStringReturnsExpected() {
        assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains("globalEventFactory=")
                .contains("thingId=", TestConstants.Thing.THING_ID);
    }

}
