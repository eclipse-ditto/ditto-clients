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
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.auth.AuthorizationModelFactory;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.things.AccessControlList;
import org.eclipse.ditto.model.things.AclEntry;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Permission;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.model.things.ThingRevision;
import org.eclipse.ditto.model.things.ThingsModelFactory;

/**
 * Defines constants for testing.
 */
public final class TestConstants {

    private TestConstants() {
        throw new AssertionError();
    }

    public static final String CLIENT_ID = "theClient";

    /**
     * Authorization-related test constants.
     */
    public static final class Authorization {

        /**
         * A known Authorization Subject for testing.
         */
        public static final AuthorizationSubject AUTH_SUBJECT_OLDMAN =
                AuthorizationModelFactory.newAuthSubject("JohnOldman");

        /**
         * Another known AuthorizationSubject for testing.
         */
        public static final AuthorizationSubject AUTH_SUBJECT_GRIMES =
                AuthorizationModelFactory.newAuthSubject("FrankGrimes");

        /**
         * An Authorization Context which contains all known Authorization Subjects.
         */
        public static final AuthorizationContext AUTH_CONTEXT =
                AuthorizationModelFactory.newAuthContext(AUTH_SUBJECT_OLDMAN, AUTH_SUBJECT_GRIMES);

        /**
         * The known ACL entry of John Oldman.
         */
        public static final AclEntry ACL_ENTRY_OLDMAN =
                ThingsModelFactory.newAclEntry(Authorization.AUTH_SUBJECT_OLDMAN, Permission.READ, Permission.WRITE,
                        Permission.ADMINISTRATE);

        /**
         * The known ACL entry of Frank Grimes.
         */
        public static final AclEntry ACL_ENTRY_GRIMES =
                ThingsModelFactory.newAclEntry(Authorization.AUTH_SUBJECT_GRIMES, Permission.READ);

        private Authorization() {
            throw new AssertionError();
        }
    }

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
        public static final org.eclipse.ditto.model.things.Feature FLUX_CAPACITOR =
                org.eclipse.ditto.model.things.Feature.newBuilder()
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
        public static final String NAMEPSACE = "example.com";

        /**
         * A known Thing ID for testing.
         */
        public static final ThingId THING_ID = ThingId.of("example.com:testThing");

        /**
         * A known Policy ID for testing.
         */
        public static final String POLICY_ID = "example.com:testPolicy";

        /**
         * A known lifecycle of a Thing.
         */
        public static final ThingLifecycle LIFECYCLE = ThingLifecycle.ACTIVE;

        /**
         * A known Access Control List of a Thing.
         */
        public static final AccessControlList ACL =
                ThingsModelFactory.newAcl(Authorization.ACL_ENTRY_OLDMAN, Authorization.ACL_ENTRY_GRIMES);

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

        /**
         * A known revision of a Thing.
         */
        public static final ThingRevision REVISION = ThingsModelFactory.newThingRevision(REVISION_NUMBER);

        public static final Instant MODIFIED = Instant.EPOCH;

        /**
         * A known Thing for testing in V1.
         */
        public static final org.eclipse.ditto.model.things.Thing THING_V1 = ThingsModelFactory.newThingBuilder()
                .setAttributes(ATTRIBUTES)
                .setFeatures(Feature.FEATURES)
                .setLifecycle(LIFECYCLE)
                .setPermissions(ACL)
                .setId(THING_ID)
                .setRevision(REVISION_NUMBER)
                .setModified(MODIFIED)
                .build();

        /**
         * A known Thing for testing in V2.
         */
        public static final org.eclipse.ditto.model.things.Thing THING_V2 = ThingsModelFactory.newThingBuilder()
                .setAttributes(ATTRIBUTES)
                .setFeatures(Feature.FEATURES)
                .setLifecycle(LIFECYCLE)
                .setPolicyId(POLICY_ID)
                .setId(THING_ID)
                .setRevision(REVISION_NUMBER)
                .setModified(MODIFIED)
                .build();

        /**
         * A known type for a {@code Resource}.
         */
        public static final String RESOURCE_TYPE = "relation";

        /**
         * A known {@code JsonPointer} for a {@code Resource}.
         */
        public static final JsonPointer RESOURCE_PATH = JsonFactory.newPointer("/attributes/foo");

        /**
         * Known Thing with policy.
         */
        public static final org.eclipse.ditto.model.things.Thing THING_V2_WITH_POLICY =
                ThingsModelFactory.newThingBuilder()
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
        public static final JsonObject INLINE_POLICY_JSON_OBJECT = JsonObject.of(
                "{\n" +
                        "  \"_policy\": {\n" +
                        "    \"entries\": {\n" +
                        "      \"DEVICE\": {\n" +
                        "        \"{{ request:subjectId }}\": {\n" +
                        "          \"type\": \"suite-auth\"\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"resources\": {\n" +
                        "        \"thing:/\": {\n" +
                        "          \"grant\": [\n" +
                        "            \"READ\",\n" +
                        "            \"WRITE\"\n" +
                        "          ],\n" +
                        "          \"revoke\": []\n" +
                        "        },\n" +
                        "        \"message:/\": {\n" +
                        "          \"grant\": [\n" +
                        "            \"READ\",\n" +
                        "            \"WRITE\"\n" +
                        "          ],\n" +
                        "          \"revoke\": []\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}");

        public static final JsonObject INLINE_POLICY = INLINE_POLICY_JSON_OBJECT.toBuilder()
                .set("thingId", THING_ID.toString())
                .set("policyId", POLICY_ID)
                .build();
    }

    /**
     * Policy-related test constants.
     */
    public static final class Policy {

        /**
         * Known PolicyId for testing
         */
        public static final String POLICY_ID = "policy.namespace:policyName";

        /**
         * Known Policy in JsonObject.
         */
        public static final JsonObject POLICY_JSON_OBJECT = JsonObject.of("{\n" +
                "  \"entries\": {\n" +
                "    \"DEVICE\": {\n" +
                "      \"subjects\": {\n" +
                "        \"{{ request:subjectId }}\": {\n" +
                "          \"type\": \"suite-auth\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"resources\": {\n" +
                "        \"policy:/\": {\n" +
                "          \"grant\": [\n" +
                "            \"READ\",\n" +
                "            \"WRITE\"\n" +
                "          ],\n" +
                "          \"revoke\": []\n" +
                "        },\n" +
                "        \"thing:/\": {\n" +
                "          \"grant\": [\n" +
                "            \"READ\",\n" +
                "            \"WRITE\"\n" +
                "          ],\n" +
                "          \"revoke\": []\n" +
                "        },\n" +
                "        \"message:/\": {\n" +
                "          \"grant\": [\n" +
                "            \"READ\",\n" +
                "            \"WRITE\"\n" +
                "          ],\n" +
                "          \"revoke\": []\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}");


        /**
         * Known Policy created from known jsonObject.
         */
        public static final org.eclipse.ditto.model.policies.Policy POLICY =
                PoliciesModelFactory.newPolicy(POLICY_JSON_OBJECT);

    }

}
