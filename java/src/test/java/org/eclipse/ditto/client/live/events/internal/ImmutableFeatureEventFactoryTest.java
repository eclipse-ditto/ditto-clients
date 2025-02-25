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
import static org.eclipse.ditto.client.TestConstants.Feature.FLUX_CAPACITOR_ID;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableFeatureEventFactory}.
 */
public final class ImmutableFeatureEventFactoryTest {

    private static final JsonSchemaVersion SCHEMA_VERSION = JsonSchemaVersion.V_2;

    private ImmutableFeatureEventFactory underTest = null;


    @Before
    public void setUp() {
        underTest = ImmutableFeatureEventFactory.getInstance(SCHEMA_VERSION, THING_ID, FLUX_CAPACITOR_ID);
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableFeatureEventFactory.class).usingGetClass().verify();
    }

    @Test
    public void toStringReturnsExpected() {
        assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains("thingEventFactory=")
                .contains("featureId=", FLUX_CAPACITOR_ID);
    }

}
