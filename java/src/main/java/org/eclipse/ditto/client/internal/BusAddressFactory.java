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

import static org.eclipse.ditto.client.internal.BusAddressPatterns.ATTRIBUTES_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.ATTRIBUTE_PATTERN;
import static org.eclipse.ditto.client.internal.BusAddressPatterns.DEFINITION_PATTERN;
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
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.ThingResourceMapper;

/**
 * Factory creates bus addresses for different parts of a thing.
 */
class BusAddressFactory {

    private BusAddressFactory() {
        throw new AssertionError();
    }

    private static final ThingResourceMapper<ThingId, String> RESOURCE_PATH_MAPPER =
            ThingResourceMapper.from(PathToBusAddressVisitor.getInstance());

    /**
     * @param thingId the thingId that is part of the pattern
     * @return bus pattern for the thing resource
     */
    static String forThing(final ThingId thingId) {
        return THING_PATTERN.format(thingId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @return bus pattern for the thing definition resource
     */
    static String forThingDefinition(final ThingId thingId) {
        return DEFINITION_PATTERN.format(thingId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @return bus pattern for the policy id resource
     */
    static String forPolicyId(final ThingId thingId) {
        return POLICY_ID_PATTERN.format(thingId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @return bus pattern for the attributes resource
     */
    static String forAttributes(final ThingId thingId) {
        return ATTRIBUTES_PATTERN.format(thingId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @param attributePath the attributePath that is part of the pattern
     * @return bus pattern for an attribute resource
     */
    static String forAttribute(final ThingId thingId, final JsonPointer attributePath) {
        return ATTRIBUTE_PATTERN.format(thingId, attributePath);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @param featureId the feature that is part of the pattern
     * @return bus pattern for the feature resource
     */
    static String forFeature(final ThingId thingId, final String featureId) {
        return FEATURE_PATTERN.format(thingId, featureId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @return bus pattern for the features resource
     */
    static String forFeatures(final ThingId thingId) {
        return FEATURES_PATTERN.format(thingId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @param featureId the feature that is part of the pattern
     * @return bus pattern for the feature definition resource
     */
    static String forFeatureDefinition(final ThingId thingId, final String featureId) {
        return FEATURE_DEFINITION_PATTERN.format(thingId, featureId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @param featureId the feature that is part of the pattern
     * @return bus pattern for the feature properties resource
     */
    static String forFeatureProperties(final ThingId thingId, final String featureId) {
        return FEATURE_PROPERTIES_PATTERN.format(thingId, featureId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @param featureId the feature that is part of the pattern
     * @param propertyPath the propertyPath that is part of the pattern
     * @return bus pattern for the feature property resource
     */
    static String forFeatureProperty(final ThingId thingId, final String featureId, final JsonPointer propertyPath) {
        return FEATURE_PROPERTY_PATTERN.format(thingId, featureId, propertyPath);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @param featureId the feature that is part of the pattern
     * @return bus pattern for the desired properties resource
     */
    static String forFeatureDesiredProperties(final ThingId thingId, final String featureId) {
        return FEATURE_DESIRED_PROPERTIES_PATTERN.format(thingId, featureId);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @param featureId the feature that is part of the pattern
     * @param desiredPropertyPath the desiredPropertyPath that is part of the pattern
     * @return bus pattern for the desired property resource
     */
    static String forFeatureDesiredProperty(final ThingId thingId, final String featureId,
            final JsonPointer desiredPropertyPath) {
        return FEATURE_DESIRED_PROPERTY_PATTERN.format(thingId, featureId, desiredPropertyPath);
    }

    /**
     * @param thingId the thingId that is part of the pattern
     * @return bus pattern for a ThingMerged event
     */
    static String forThingMergedEvent(final ThingId thingId, final JsonPointer path) {
        return RESOURCE_PATH_MAPPER.map(path, thingId);
    }

}
