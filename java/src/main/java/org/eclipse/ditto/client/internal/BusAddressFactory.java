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
import org.eclipse.ditto.protocoladapter.UnknownPathException;
import org.eclipse.ditto.signals.commands.common.ThingMergePathMatcher;

/**
 * Factory creates bus addresses for different parts of a thing.
 */
class BusAddressFactory {

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

    static String forMergeThingEvent(final ThingId thingId, final JsonPointer path) {
        final ThingMergePathMatcher payloadPathMatcher = ThingMergePathMatcher.getInstance();
        switch (payloadPathMatcher.match(path)) {
            case THING_PATH:
                return BusAddressFactory.forThing(thingId);
            case ATTRIBUTE_PATH:
                return BusAddressFactory.forAttribute(thingId, extractAttributePath(path));
            case ATTRIBUTES_PATH:
                return BusAddressFactory.forAttributes(thingId);
            case FEATURE_PATH:
                return BusAddressFactory.forFeature(thingId, extractFeatureId(path));
            case FEATURES_PATH:
                return BusAddressFactory.forFeatures(thingId);
            case FEATURE_PROPERTY_PATH:
                return BusAddressFactory.forFeatureProperty(thingId, extractFeatureId(path),
                        extractFeaturePropertyPath(path));
            case FEATURE_PROPERTIES_PATH:
                return BusAddressFactory.forFeatureProperties(thingId, extractFeatureId(path));
            case FEATURE_DESIRED_PROPERTY_PATH:
                return BusAddressFactory.forFeatureDesiredProperty(thingId, extractFeatureId(path),
                        extractFeaturePropertyPath(path));
            case FEATURE_DESIRED_PROPERTIES_PATH:
                return BusAddressFactory.forFeatureDesiredProperties(thingId, extractFeatureId(path));
            default:
                throw new UnknownPathException.Builder(path).build();
        }
    }

    private static JsonPointer extractFeaturePropertyPath(final JsonPointer path) {
        return path.getSubPointer(3)
                .orElseThrow(() -> UnknownPathException.newBuilder(path).build());
    }

    private static JsonPointer extractAttributePath(final JsonPointer path) {
        return path.getSubPointer(1)
                .orElseThrow(() -> UnknownPathException.newBuilder(path).build());
    }

    private static String extractFeatureId(final JsonPointer path) {
        return path.get(1).map(CharSequence::toString)
                .orElseThrow(() -> UnknownPathException.newBuilder(path).build());
    }
}
