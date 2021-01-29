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

import static org.eclipse.ditto.client.internal.BusAddressPatterns.ACL_ENTRY_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.ACL_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.ATTRIBUTES_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.ATTRIBUTE_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.FEATURES_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.FEATURE_DEFINITION_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.FEATURE_DESIRED_PROPERTIES_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.FEATURE_DESIRED_PROPERTY_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.FEATURE_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.FEATURE_PROPERTIES_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.FEATURE_PROPERTY_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.POLICY_ID_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.THING_PATTERN;

import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.things.ThingResourceMapper;

/**
 * Factory creates bus addresses for different parts of a thing.
 */
class BusAddressFactory {

    private static final ThingResourceMapper<ThingId, String> RESOURCE_PATH_MAPPER =
            ThingResourceMapper.from(PathToBusAddressVisitor.getInstance());

    static String forThing(final ThingId thingId) {
        return THING_PATTERN.format(thingId);
    }

    @Deprecated
    static String forAcl(final ThingId thingId) {
        return ACL_PATTERN.format(thingId);
    }

    @Deprecated
    static String forAclEntry(final ThingId thingId, final String subjectId) {
        return ACL_ENTRY_PATTERN.format(thingId, subjectId);
    }

    static String forThingDefinition(final ThingId thingId, final String subjectId) {
        return ACL_PATTERN.format(thingId, subjectId);
    }

    static String forPolicyId(final ThingId thingId) {
        return POLICY_ID_PATTERN.format(thingId);
    }

    static String forAttributes(final ThingId thingId) {
        return ATTRIBUTES_PATTERN.format(thingId);
    }

    static String forAttribute(final ThingId thingId, final JsonPointer attributePath) {
        return ATTRIBUTE_PATTERN.format(thingId, attributePath);
    }

    static String forFeature(final ThingId thingId, final String featureId) {
        return FEATURE_PATTERN.format(thingId, featureId);
    }

    static String forFeatures(final ThingId thingId) {
        return FEATURES_PATTERN.format(thingId);
    }

    static String forFeatureDefinition(final ThingId thingId, final String featureId) {
        return FEATURE_DEFINITION_PATTERN.format(thingId, featureId);
    }

    static String forFeatureProperties(final ThingId thingId, final String featureId) {
        return FEATURE_PROPERTIES_PATTERN.format(thingId, featureId);
    }

    static String forFeatureProperty(final ThingId thingId, final String featureId, final JsonPointer propertyPath) {
        return FEATURE_PROPERTY_PATTERN.format(thingId, featureId, propertyPath);
    }

    static String forFeatureDesiredProperties(final ThingId thingId, final String featureId) {
        return FEATURE_DESIRED_PROPERTIES_PATTERN.format(thingId, featureId);
    }

    static String forFeatureDesiredProperty(final ThingId thingId, final String featureId,
            final JsonPointer propertyPath) {
        return FEATURE_DESIRED_PROPERTY_PATTERN.format(thingId, featureId, propertyPath);
    }

    static String forThingMergedEvent(final ThingId thingId, final JsonPointer path) {
        return RESOURCE_PATH_MAPPER.map(path, thingId);
    }

}
