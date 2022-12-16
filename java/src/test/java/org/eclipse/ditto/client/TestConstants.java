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
package org.eclipse.ditto.client;

import java.time.Instant;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.policies.model.PoliciesModelFactory;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.things.model.Attributes;
import org.eclipse.ditto.things.model.FeatureProperties;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.ThingLifecycle;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.things.model.signals.commands.modify.CreateThing;

/**
 * Defines constants for testing.
 */
public final class TestConstants {

    private TestConstants() {
        throw new AssertionError();
    }

    public static final String CLIENT_ID = "theClient";

    /**
     * Feature-related test constants.
     */
    public static final class Feature {

        /**
         * A known ID of a Feature.
         */
        public static final String FLUX_CAPACITOR_ID = "FluxCapacitor";

        /**
         * Properties of a known Feature.
         */
        public static final FeatureProperties FLUX_CAPACITOR_PROPERTIES = FeatureProperties.newBuilder()
                .set("target_year_1", 1955)
                .set("target_year_2", 2015)
                .set("target_year_3", 1885)
                .build();

        /**
         * A known Feature which is required for time travel.
         */
        public static final org.eclipse.ditto.things.model.Feature FLUX_CAPACITOR =
                org.eclipse.ditto.things.model.Feature.newBuilder()
                        .properties(FLUX_CAPACITOR_PROPERTIES)
                        .withId(FLUX_CAPACITOR_ID)
                        .build();

        /**
         * Known features of a Thing.
         */
        public static final Features FEATURES = ThingsModelFactory.newFeatures(Feature.FLUX_CAPACITOR);

        private Feature() {
            throw new AssertionError();
        }

    }

    /**
     * Thing-related test constants.
     */
    public static final class Thing {

        /**
         * A known namespace for testing.
         */
        public static final String NAMESPACE = "example.com";

        /**
         * A known Thing ID for testing.
         */
        public static final ThingId THING_ID = ThingId.of(NAMESPACE, "testThing");

        /**
         * A known Thing ID for testing.
         */
        public static final ThingId THING_ID_COPY_POLICY = ThingId.of(NAMESPACE, "testThingForCopyPolicy");

        /**
         * A known Policy ID for testing.
         */
        public static final PolicyId POLICY_ID = PolicyId.of(NAMESPACE, "testPolicy");

        /**
         * A known lifecycle of a Thing.
         */
        public static final ThingLifecycle LIFECYCLE = ThingLifecycle.ACTIVE;

        /**
         * A known location attribute for testing.
         */
        public static final JsonObject LOCATION_ATTRIBUTE = JsonFactory.newObjectBuilder()
                .set("latitude", 44.673856)
                .set("longitude", 8.261719)
                .build();

        /**
         * Known attributes of a Thing.
         */
        public static final Attributes ATTRIBUTES = ThingsModelFactory.newAttributesBuilder()
                .set("location", LOCATION_ATTRIBUTE)
                .set("maker", "ACME Inc.")
                .build();

        /**
         * A known revision number of a Thing.
         */
        public static final long REVISION_NUMBER = 0;

        public static final Instant MODIFIED = Instant.EPOCH;

        /**
         * A known Thing for testing in V2.
         */
        public static final org.eclipse.ditto.things.model.Thing THING_V2 = ThingsModelFactory.newThingBuilder()
                .setAttributes(ATTRIBUTES)
                .setFeatures(Feature.FEATURES)
                .setLifecycle(LIFECYCLE)
                .setPolicyId(POLICY_ID)
                .setId(THING_ID)
                .setRevision(REVISION_NUMBER)
                .setModified(MODIFIED)
                .build();

        private Thing() {
            throw new AssertionError();
        }

        /**
         * Known inline Policy JsonObject.
         */
        public static final JsonObject INLINE_POLICY_JSON_OBJECT =
                JsonObject.newBuilder()
                        .set(CreateThing.JSON_INLINE_POLICY, Policy.POLICY_JSON_OBJECT)
                        .build();

        public static final JsonObject THING_WITH_INLINE_POLICY = INLINE_POLICY_JSON_OBJECT.toBuilder()
                .set(org.eclipse.ditto.things.model.Thing.JsonFields.ID, THING_ID.toString())
                .set(org.eclipse.ditto.things.model.Thing.JsonFields.POLICY_ID, POLICY_ID.toString())
                .build();

    }

    /**
     * Policy-related test constants.
     */
    public static final class Policy {

        /**
         * Known PolicyId for testing
         */
        public static final PolicyId POLICY_ID = PolicyId.of("policy.namespace:policyName");

        /**
         * Known Policy in JsonObject.
         */
        public static final JsonObject POLICY_JSON_OBJECT = JsonObject.of("{\n" +
                "    \"policyId\": \"" + POLICY_ID + "\",\n" +
                "    \"imports\": {},\n" +
                "    \"entries\": {\n" +
                "        \"maker\": {\n" +
                "            \"subjects\": {\n" +
                "                \"{{ request:subjectId }}\": {\n" +
                "                    \"type\": \"suite-auth\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"resources\": {\n" +
                "                \"policy:/\": {\n" +
                "                    \"grant\": [\n" +
                "                        \"READ\",\n" +
                "                        \"WRITE\"\n" +
                "                    ],\n" +
                "                    \"revoke\": []\n" +
                "                },\n" +
                "                \"thing:/\": {\n" +
                "                    \"grant\": [\n" +
                "                        \"READ\",\n" +
                "                        \"WRITE\"\n" +
                "                    ],\n" +
                "                    \"revoke\": []\n" +
                "                },\n" +
                "                \"message:/\": {\n" +
                "                    \"grant\": [\n" +
                "                        \"READ\",\n" +
                "                        \"WRITE\"\n" +
                "                    ],\n" +
                "                    \"revoke\": []\n" +
                "                }\n" +
                "            },\n" +
                "            \"importable\":\"implicit\"\n" +
                "        }\n" +
                "    }\n" +
                "}");


        /**
         * Known Policy created from known jsonObject.
         */
        public static final org.eclipse.ditto.policies.model.Policy POLICY =
                PoliciesModelFactory.newPolicy(POLICY_JSON_OBJECT);

        public static final JsonObject POLICY_REVISION_ONLY_JSON_OBJECT = JsonObject.of("{\n" +
                "    \"_revision\": " + 1 + "\n" + "}");

        public static final org.eclipse.ditto.policies.model.Policy REVISION_ONLY_POLICY =
                PoliciesModelFactory.newPolicy(POLICY_REVISION_ONLY_JSON_OBJECT);

    }

}
