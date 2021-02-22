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

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.junit.Test;

/**
 * Tests {@link org.eclipse.ditto.client.internal.PathToBusAddressVisitor}.
 */
public class PathToBusAddressVisitorTest {

    private static final String THING_PATTERN = "/things/" + THING_ID;
    private static final PathToBusAddressVisitor UNDER_TEST = PathToBusAddressVisitor.getInstance();

    @Test
    public void visitThing() {
        assertThat(UNDER_TEST.visitThing(JsonPointer.empty(), THING_ID)).isEqualTo(THING_PATTERN);
    }

    @Test
    public void visitThingDefinition() {
        assertThat(UNDER_TEST.visitThingDefinition(newPointer("definition"), THING_ID))
                .isEqualTo(THING_PATTERN + "/definition");
    }

    @Test
    public void visitPolicyId() {
        assertThat(UNDER_TEST.visitPolicyId(newPointer("policyId"), THING_ID))
                .isEqualTo(THING_PATTERN + "/policyId");
    }

    @Test
    public void visitAttributes() {
        assertThat(UNDER_TEST.visitAttributes(newPointer("attributes"), THING_ID))
                .isEqualTo(THING_PATTERN + "/attributes");
    }

    @Test
    public void visitAttribute() {
        assertThat(UNDER_TEST.visitAttribute(newPointer("attributes", "location", "latitude"), THING_ID))
                .isEqualTo(THING_PATTERN + "/attributes/location/latitude");
    }

    @Test
    public void visitFeature() {
        assertThat(UNDER_TEST.visitFeature(newPointer("features", FLUX_CAPACITOR_ID), THING_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID);
    }

    @Test
    public void visitFeatures() {
        assertThat(UNDER_TEST.visitFeatures(newPointer("features"), THING_ID)).isEqualTo(THING_PATTERN + "/features");
    }

    @Test
    public void visitFeatureDefinition() {
        assertThat(UNDER_TEST.visitFeatureDefinition(newPointer("features", FLUX_CAPACITOR_ID, "definition"), THING_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/definition");
    }

    @Test
    public void visitFeatureProperties() {
        assertThat(UNDER_TEST.visitFeatureProperties(newPointer("features", FLUX_CAPACITOR_ID, "properties"), THING_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/properties");
    }

    @Test
    public void visitFeatureProperty() {
        assertThat(UNDER_TEST.visitFeatureProperty(
                newPointer("features", FLUX_CAPACITOR_ID, "properties", "status", "temperature"), THING_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/properties/status/temperature");
    }

    @Test
    public void visitFeatureDesiredProperties() {
        final JsonPointer path = newPointer("features", FLUX_CAPACITOR_ID, "desiredProperties");
        assertThat(UNDER_TEST.visitFeatureDesiredProperties(path, THING_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/desiredProperties");
    }

    @Test
    public void visitFeatureDesiredProperty() {
        assertThat(UNDER_TEST.visitFeatureDesiredProperty(newPointer("features", FLUX_CAPACITOR_ID,
                "desiredProperties", "status", "temperature"), THING_ID))
                .isEqualTo(THING_PATTERN + "/features/" + FLUX_CAPACITOR_ID + "/desiredProperties/status/temperature");
    }

    private static JsonPointer newPointer(final String... elements) {
        return JsonFactory.newPointer(String.join("/", elements));
    }
}
