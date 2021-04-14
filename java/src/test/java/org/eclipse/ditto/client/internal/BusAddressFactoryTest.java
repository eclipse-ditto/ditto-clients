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
package org.eclipse.ditto.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.client.TestConstants.Feature.FLUX_CAPACITOR_ID;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;

import org.eclipse.ditto.json.JsonPointer;
import org.junit.Test;

/**
 * Tests {@link org.eclipse.ditto.client.internal.BusAddressFactory}.
 */
public class BusAddressFactoryTest {

    private static final String THING_PATTERN = "/things/" + THING_ID;

    @Test
    public void forThing() {
        assertThat(BusAddressFactory.forThing(THING_ID)).isEqualTo(THING_PATTERN);
    }

    @Test
    public void forAcl() {
        assertThat(BusAddressFactory.forAcl(THING_ID)).isEqualTo(THING_PATTERN + "/acl");
    }

    @Test
    public void forAclEntry() {
        assertThat(BusAddressFactory.forAclEntry(THING_ID, "subjectId"))
                .isEqualTo(THING_PATTERN + "/acl/subjectId");
    }

    @Test
    public void forThingDefinition() {
        assertThat(BusAddressFactory.forThingDefinition(THING_ID))
                .isEqualTo(THING_PATTERN + "/definition");
    }

    @Test
    public void forPolicyId() {
        assertThat(BusAddressFactory.forPolicyId(THING_ID))
                .isEqualTo(THING_PATTERN + "/policyId");
    }

    @Test
    public void forAttributes() {
        assertThat(BusAddressFactory.forAttributes(THING_ID))
                .isEqualTo(THING_PATTERN + "/attributes");
    }

    @Test
    public void forAttribute() {
        assertThat(BusAddressFactory.forAttribute(THING_ID, JsonPointer.of("location/latitude")))
                .isEqualTo(THING_PATTERN + "/attributes/location/latitude");
    }

    @Test
    public void forFeature() {
        assertThat(BusAddressFactory.forFeature(THING_ID, FLUX_CAPACITOR_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID);
    }

    @Test
    public void forFeatures() {
        assertThat(BusAddressFactory.forFeatures(THING_ID)).isEqualTo(THING_PATTERN + "/features");
    }

    @Test
    public void forFeatureDefinition() {
        assertThat(BusAddressFactory.forFeatureDefinition(THING_ID, FLUX_CAPACITOR_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/definition");
    }

    @Test
    public void forFeatureProperties() {
        assertThat(BusAddressFactory.forFeatureProperties(THING_ID, FLUX_CAPACITOR_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/properties");
    }

    @Test
    public void forFeatureProperty() {
        assertThat(BusAddressFactory.forFeatureProperty(THING_ID, FLUX_CAPACITOR_ID,
                JsonPointer.of("status/temperature")))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/properties/status/temperature");
    }

    @Test
    public void forFeatureDesiredProperties() {
        assertThat(BusAddressFactory.forFeatureDesiredProperties(THING_ID, FLUX_CAPACITOR_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/desiredProperties");
    }

    @Test
    public void forFeatureDesiredProperty() {
        assertThat(BusAddressFactory.forFeatureDesiredProperty(THING_ID, FLUX_CAPACITOR_ID,
                JsonPointer.of("status/temperature")))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/desiredProperties/status/temperature");
    }

    @Test
    public void forThingMergedEvent() {
        assertThat(BusAddressFactory.forThingMergedEvent(THING_ID, JsonPointer.of("attributes/location/latitude")))
                .isEqualTo(THING_PATTERN + "/attributes/location/latitude");
    }
}
