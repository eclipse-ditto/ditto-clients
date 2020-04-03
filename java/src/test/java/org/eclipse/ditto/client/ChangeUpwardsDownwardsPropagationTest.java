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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.ditto.json.JsonFactory.emptyPointer;
import static org.eclipse.ditto.json.JsonFactory.newObjectBuilder;
import static org.eclipse.ditto.json.JsonFactory.newPointer;
import static org.eclipse.ditto.model.base.auth.AuthorizationModelFactory.newAuthSubject;
import static org.junit.Assert.assertEquals;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Permission;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributesCreated;
import org.eclipse.ditto.signals.events.things.AttributesModified;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.things.FeatureModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertyCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tests that change events are propagates upwards and downwards correctly. E.g.: <ul> <li>The Client user adds a
 * registration for {@link ThingChange}s -> he gets notified when: <ul> <li>The complete Thing changes (CREATE, UPDATE,
 * DELETE)</li> <li>All attributes of the Thing changes at once</li> <li>Single attributes change</li> <li>All features
 * of the Thing changes at once</li> <li>A single feature changes</li> <li>Single feature properties change</li> </ul>
 * </li> <li>The Client user adds a registration for {@link Change}s on a specific
 * attribute -> he gets notified when: <ul> <li>This specific attributes changes (CREATE, UPDATE, DELETE)</li> <li>The
 * attribute changes implicitly because a parent JsonObject attribute is changed completely</li> <li>The attribute
 * changes implicitly because all attributes are changed</li> <li>The attribute changes implicitly because the Thing
 * changes</li> </ul> </li> </ul>
 * <p>
 * Test Runtrip:
 * <p>
 * Local run against cloud foundry dev with parameter <b>-Dtest.environment=cloud-dev</b>
 * <p>
 * Client send modify/delete feature(s) --> Client retrieve thing, verify feature(s) correctly modified/deleted --> Rest
 * API of Thing service, check the values of feature(s)
 * <p>
 * Client listen to feature(s) changes --> verify Client get the event feature(s) modified/deleted
 */
public class ChangeUpwardsDownwardsPropagationTest extends AbstractDittoClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeUpwardsDownwardsPropagationTest.class);

    private static final String FEATURE_ID_1 = "feature_id_1";
    private static final String FEATURE_ID_2 = "feature_id_2";

    private static final FeatureProperties FEATURE1_PROPERTIES = ThingsModelFactory.newFeaturePropertiesBuilder()
            .set("one", 1)
            .set("two", 2).build();
    private static final FeatureProperties FEATURE2_PROPERTIES = ThingsModelFactory.newFeaturePropertiesBuilder()
            .set("complex", newObjectBuilder().set("bum", "lux").build())
            .build();

    private static final Feature FEATURE1 =
            ThingsModelFactory.newFeature(FEATURE_ID_1).setProperties(FEATURE1_PROPERTIES);
    private static final Feature FEATURE2 =
            ThingsModelFactory.newFeature(FEATURE_ID_2).setProperties(FEATURE2_PROPERTIES);

    private static final String ATTRIBUTE_ABC = "abc";
    private static final String ATTRIBUTE_ABC_VALUE = "def";
    private static final String ATTRIBUTE_FOO = "foo";
    private static final boolean ATTRIBUTE_FOO_VALUE = false;
    private static final Attributes
            ATTRIBUTES2 = ThingsModelFactory.newAttributesBuilder().set(ATTRIBUTE_ABC, ATTRIBUTE_ABC_VALUE)
            .set(ATTRIBUTE_FOO, ATTRIBUTE_FOO_VALUE).build();


    private static final int TIMEOUT_SECONDS = 5;
    private static final CharSequence TEST_USER_SID = "fooTestUser";

    private ThingId thingId1;
    private Thing thing1;
    private ThingId thingId2;
    private Thing thing2withAttributes;
    private ThingId thingId3;

    @Before
    public void before() {
        super.before();
        thingId1 = newThingId(UUID.randomUUID().toString());
        thing1 = ThingsModelFactory.newThingBuilder().setId(thingId1)
                .setPermissions(
                        ThingsModelFactory.newAclEntry(newAuthSubject(TEST_USER_SID), Permission.READ,
                                Permission.WRITE),
                        ThingsModelFactory.newAclEntry(newAuthSubject(TestConstants.CLIENT_ID),
                                Permission.READ, Permission.WRITE,
                                Permission.ADMINISTRATE))
                .build();

        thingId2 = newThingId(UUID.randomUUID().toString());
        thing2withAttributes = ThingsModelFactory.newThingBuilder().setId(thingId2)
                .setPermissions(
                        ThingsModelFactory.newAclEntry(newAuthSubject(TEST_USER_SID), Permission.READ,
                                Permission.WRITE),
                        ThingsModelFactory.newAclEntry(newAuthSubject(TestConstants.CLIENT_ID),
                                Permission.READ, Permission.WRITE,
                                Permission.ADMINISTRATE))
                .setAttributes(ATTRIBUTES2)
                .build();

        thingId3 = newThingId(UUID.randomUUID().toString());
    }

    @Test
    public void testUpwardsRegisterForThingChangeWhenThingIsCreated() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        client.twin()
                .forId(thingId1)
                .registerForThingChanges("testUpwardsRegisterForThingChangeWhenThingIsCreated", thingChange -> {
                    LOG.info("received ThingChange {}", thingChange);
                    Assertions.assertThat(thingChange.getAction()).isEqualTo(ChangeAction.CREATED);
                    Assertions.assertThat((CharSequence) thingChange.getEntityId()).isEqualTo(thingId1);
                    Assertions.assertThat(thingChange.isPartial()).isFalse();
                    Assertions.assertThat((CharSequence) thingChange.getPath())
                            .isEqualTo(emptyPointer()); // empty path on ThingChange
                    Assertions.assertThat(thingChange.getThing()).hasValue(thing1);
                    Assertions.assertThat(thingChange.getValue())
                            .hasValue(thing1.toJson(thing1.getImplementedSchemaVersion()));

                    latch.countDown();
                });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId1, ThingCreated.TYPE)
                        .build();

        final Message<ThingEvent> thingCreated =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        ThingCreated.of(thing1, 1, DittoHeaders.empty())).build();

        messaging.receiveEvent(thingCreated);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testMultipleChangeHandlersAreInvokedOnSingleChange() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final int amountOfSubscriptions = 7;
        final CountDownLatch latch = new CountDownLatch(amountOfSubscriptions);

        final Consumer<ThingChange> consumer = thingChange -> {
            LOG.info("received ThingChange {}", thingChange);
            Assertions.assertThat(thingChange.getAction()).isEqualTo(ChangeAction.CREATED);
            Assertions.assertThat((CharSequence) thingChange.getEntityId()).isEqualTo(thingId1);
            Assertions.assertThat(thingChange.isPartial()).isFalse();
            Assertions.assertThat((CharSequence) thingChange.getPath())
                    .isEqualTo(emptyPointer()); // empty path on ThingChange
            Assertions.assertThat(thingChange.getThing()).hasValue(thing1);
            Assertions.assertThat(thingChange.getValue()).hasValue(thing1.toJson(thing1.getImplementedSchemaVersion()));

            latch.countDown();
        };

        for (int i = 0; i < amountOfSubscriptions; i++) {
            if (i % 2 == 0) {
                client.twin()
                        .registerForThingChanges("testMultipleChangeHandlersAreInvokedOnSingleChange" + i, consumer);
            } else {
                client.twin()
                        .forId(thingId1)
                        .registerForThingChanges("testMultipleChangeHandlersAreInvokedOnSingleChange" + i, consumer);
            }
        }

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId1, ThingCreated.TYPE)
                        .build();

        final Message<ThingEvent> thingCreated =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        ThingCreated.of(thing1, 1, DittoHeaders.empty())).build();

        // only create the Thing once:
        messaging.receiveEvent(thingCreated);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testUpwardsRegisterForThingChangeWhenThingIsDeleted() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        client.twin()
                .forId(thingId1)
                .registerForThingChanges("testUpwardsRegisterForThingChangeWhenThingIsDeleted", thingChange -> {
                    LOG.info("received ThingChange {}", thingChange);
                    Assertions.assertThat(thingChange.getAction()).isEqualTo(ChangeAction.DELETED);
                    Assertions.assertThat((CharSequence) thingChange.getEntityId()).isEqualTo(thingId1);
                    Assertions.assertThat(thingChange.isPartial()).isFalse();
                    Assertions.assertThat((CharSequence) thingChange.getPath())
                            .isEqualTo(emptyPointer()); // empty path on ThingChange
                    Assertions.assertThat(thingChange.getThing()).isEmpty();
                    Assertions.assertThat(thingChange.getValue()).isEmpty();

                    latch.countDown();
                });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId1, ThingDeleted.TYPE)
                        .build();

        final Message<ThingEvent> thingDeleted =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        ThingDeleted.of(thingId1, 1, DittoHeaders.empty())).build();

        messaging.receiveEvent(thingDeleted);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testUpwardsRegisterForThingChangeWhenAttributesAreModified() throws Exception {
        // prepare: create the thing
        final JsonObject attributesToSet = newObjectBuilder().set("foo", "bar").set("misc", 1).build();

        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        client.twin()
                .forId(thingId1)
                .registerForThingChanges("testUpwardsRegisterForThingChangeWhenAttributesAreModified", thingChange -> {
                    LOG.info("received ThingChange {}", thingChange);
                    Assertions.assertThat(thingChange.getAction()).isEqualTo(ChangeAction.CREATED);
                    Assertions.assertThat((CharSequence) thingChange.getEntityId()).isEqualTo(thingId1);
                    Assertions.assertThat(thingChange.isPartial()).isTrue();
                    Assertions.assertThat((CharSequence) thingChange.getPath())
                            .isEqualTo(newPointer("attributes")); // attributes were changed
                    Assertions.assertThat(thingChange.getValue().get().toString()).isEqualTo(
                            newObjectBuilder().set("attributes", attributesToSet).build().toString());

                    latch.countDown();
                });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId1, AttributesCreated.TYPE)
                        .build();

        // set the attributes
        final Message<ThingEvent> attributesCreated =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        AttributesCreated.of(thingId1, ThingsModelFactory.newAttributes(attributesToSet), 1,
                                DittoHeaders.empty())).build();

        messaging.receiveEvent(attributesCreated);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testUpwardsRegisterForThingChangeWhenSingleAttributeIsModified() throws Exception {
        // prepare: create the thing
        final JsonPointer newAttribute = newPointer("newAttribute");
        final int simpleValue = 42;
        final JsonObject simple = newObjectBuilder().set("simple", simpleValue).build();
        final JsonObject complex = newObjectBuilder().set("complex", simple).build();

        final JsonObject buildObject = newObjectBuilder().set(newAttribute, complex).build();

        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        client.twin()
                .forId(thingId1)
                .registerForThingChanges("testUpwardsRegisterForThingChangeWhenSingleAttributeIsModified",
                        thingChange -> {
                            LOG.info("received ThingChange {}", thingChange);
                            Assertions.assertThat(thingChange.getAction()).isEqualTo(ChangeAction.CREATED);
                            Assertions.assertThat((CharSequence) thingChange.getEntityId()).isEqualTo(thingId1);
                            Assertions.assertThat(thingChange.isPartial()).isTrue();
                            Assertions.assertThat((CharSequence) thingChange.getPath())
                                    .isEqualTo(newPointer("attributes").append(
                                            newAttribute)); // single attributes was changed
                            Assertions.assertThat(thingChange.getValue().get().toString()).isEqualTo(
                                    newObjectBuilder().set("attributes", buildObject).build().toString());

                            latch.countDown();
                        });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId1, AttributeCreated.TYPE)
                        .build();

        // set the attributes
        final Message<ThingEvent> attributeCreated =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        AttributeCreated.of(thingId1, newAttribute, complex, 1, DittoHeaders.empty())).build();

        messaging.receiveEvent(attributeCreated);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testUpwardsRegisterForThingChangeWhenSingleFeatureIsDeleted() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        client.twin()
                .forId(thingId1)
                .registerForThingChanges("testUpwardsRegisterForThingChangeWhenSingleFeatureIsDeleted", thingChange -> {
                    LOG.info("received ThingChange {}", thingChange);
                    Assertions.assertThat(thingChange.getAction()).isEqualTo(ChangeAction.DELETED);
                    Assertions.assertThat((CharSequence) thingChange.getEntityId()).isEqualTo(thingId1);
                    Assertions.assertThat(thingChange.isPartial()).isTrue();
                    Assertions.assertThat((CharSequence) thingChange.getPath())
                            .isEqualTo(newPointer("features/" + FEATURE_ID_1)); // feature was deleted
                    Assertions.assertThat(thingChange.getValue()).isEmpty();

                    latch.countDown();
                });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId1, FeatureDeleted.TYPE)
                        .build();

        // delete the Feature
        final Message<ThingEvent> featureDeleted =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        FeatureDeleted.of(thingId1, FEATURE_ID_1, 1, DittoHeaders.empty())).build();

        messaging.receiveEvent(featureDeleted);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testUpwardsRegisterForFeaturesChangesWhenSingleFeaturePropertyIsCreated() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(2);

        final JsonPointer fooPointer = newPointer("foo");
        final String fooValue = "bar";
        final JsonObject expectedChangedObject = newObjectBuilder().set(FEATURE_ID_1,
                newObjectBuilder().set("properties", newObjectBuilder().set(fooPointer, fooValue).build()).build())
                .build();

        final Consumer<FeaturesChange> featuresChangeConsumer = featuresChange -> {
            LOG.info("received ThingChange {}", featuresChange);
            Assertions.assertThat(featuresChange.getAction()).isEqualTo(ChangeAction.CREATED);
            Assertions.assertThat((CharSequence) featuresChange.getEntityId()).isEqualTo(thingId1);
            Assertions.assertThat(featuresChange.isPartial()).isTrue();
            Assertions.assertThat((CharSequence) featuresChange.getPath())
                    .isEqualTo(newPointer(FEATURE_ID_1).append(newPointer("properties"))
                            .append(fooPointer)); // feature property was created
            Assertions.assertThat(featuresChange.getValue().get().toString())
                    .isEqualTo(expectedChangedObject.toString());

            latch.countDown();
        };

        // register both the "global" consumer and the one for a specific thingId:
        client.twin()
                .registerForFeaturesChanges("testUpwardsRegisterForFeaturesChangesWhenSingleFeaturePropertyIsCreated",
                        featuresChangeConsumer);
        client.twin()
                .forId(thingId1)
                .registerForFeaturesChanges(
                        "testUpwardsRegisterForFeaturesChangesWhenSingleFeaturePropertyIsCreated-specific",
                        featuresChangeConsumer);

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId1, FeaturePropertyCreated.TYPE)
                        .featureId(FEATURE_ID_1)
                        .build();

        // create a Feature property
        final Message<ThingEvent> featurePropertyCreated =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        FeaturePropertyCreated.of(thingId1, FEATURE_ID_1, fooPointer, JsonValue.of(fooValue),
                                1,
                                DittoHeaders.empty())).build();

        messaging.receiveEvent(featurePropertyCreated);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testUpwardsRegisterForFeatureChangesWhenSingleFeaturePropertyIsUpdated() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        final JsonPointer fooPointer = newPointer("complex/bum");
        final String fooValue = "bar";
        final JsonObject expectedChangedObject =
                newObjectBuilder().set("properties", newObjectBuilder().set(fooPointer, fooValue).build()).build();

        client.twin()
                .registerForFeatureChanges("testUpwardsRegisterForFeatureChangesWhenSingleFeaturePropertyIsUpdated",
                        FEATURE_ID_2, featureChange -> {
                            LOG.info("received Change {}", featureChange);
                            Assertions.assertThat(featureChange.getAction()).isEqualTo(ChangeAction.UPDATED);
                            Assertions.assertThat((CharSequence) featureChange.getEntityId()).isEqualTo(thingId3);
                            Assertions.assertThat(featureChange.isPartial()).isTrue();
                            Assertions.assertThat((CharSequence) featureChange.getPath())
                                    .isEqualTo(newPointer("properties")
                                            .append(fooPointer)); // feature property was created
                            Assertions.assertThat(featureChange.getValue().get().toString())
                                    .isEqualTo(expectedChangedObject.toString());

                            latch.countDown();
                        });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId3, FeaturePropertyModified.TYPE)
                        .featureId(FEATURE_ID_2)
                        .build();

        // update a Feature property
        final Message<ThingEvent> featurePropertyModified =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        FeaturePropertyModified.of(thingId3, FEATURE_ID_2, fooPointer, JsonValue.of(fooValue),
                                1,
                                DittoHeaders.empty())).build();

        messaging.receiveEvent(featurePropertyModified);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testDownwardsRegisterForSingleAttributeChangeWhenThingIsCreated() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        client.twin()
                .forId(thingId2)
                .registerForAttributesChanges("testDownwardsRegisterForSingleAttributeChangeWhenThingIsCreated",
                        attrChange -> {
                            LOG.info("received Change {}", attrChange);
                            Assertions.assertThat(attrChange.getAction()).isEqualTo(ChangeAction.CREATED);
                            Assertions.assertThat((CharSequence) attrChange.getEntityId()).isEqualTo(thingId2);
                            Assertions.assertThat(attrChange.isFull()).isTrue();
                            Assertions.assertThat((CharSequence) attrChange.getPath())
                                    .isEqualTo(emptyPointer()); // empty pointer as all attributes were created
                            Assertions.assertThat(attrChange.getValue().get().toString())
                                    .isEqualTo(ATTRIBUTES2.toJsonString());

                            latch.countDown();
                        });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId2, ThingCreated.TYPE)
                        .build();

        // create the thing with attributes
        final Message<ThingEvent> thingCreated =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        ThingCreated.of(thing2withAttributes, 1, DittoHeaders.empty())).build();

        messaging.receiveEvent(thingCreated);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testDownwardsRegisterForSingleAttributeChangeWhenAttributesAreModified() throws Exception {
        final String newAbcValue = "bumlux";
        final Attributes newAttributes =
                ThingsModelFactory.newAttributesBuilder().set(ATTRIBUTE_ABC, newAbcValue).build();

        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        final JsonPointer abcPointer = newPointer(ATTRIBUTE_ABC);

        client.twin()
                .forId(thingId2)
                .registerForAttributeChanges("testDownwardsRegisterForSingleAttributeChangeWhenAttributesAreModified",
                        abcPointer,
                        attrChange -> {
                            LOG.info("received Change {}", attrChange);
                            Assertions.assertThat(attrChange.getAction()).isEqualTo(ChangeAction.UPDATED);
                            Assertions.assertThat((CharSequence) attrChange.getEntityId()).isEqualTo(thingId2);
                            Assertions.assertThat(attrChange.isFull()).isTrue();
                            Assertions.assertThat((CharSequence) attrChange.getPath())
                                    .isEqualTo(emptyPointer()); // attribute "abc" was modified completely
                            Assertions.assertThat(attrChange.getValue().get().asString()).isEqualTo(newAbcValue);

                            latch.countDown();
                        });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId2, AttributesModified.TYPE)
                        .build();

        // change attributes
        final Message<ThingEvent> attributesModified =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        AttributesModified.of(thingId2, newAttributes, 1, DittoHeaders.empty())).build();

        messaging.receiveEvent(attributesModified);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void testDownwardsRegisterForNestedFeaturePropertyChangeWhenFeatureIsModified() throws Exception {
        // start consuming changes:
        client.twin().startConsumption();

        final CountDownLatch latch = new CountDownLatch(1);

        final JsonPointer complexBumPointer = newPointer("complex/bum");

        client.twin()
                .forFeature(thingId3, FEATURE_ID_2)
                .registerForPropertyChanges("testDownwardsRegisterForNestedFeaturePropertyChangeWhenFeatureIsModified",
                        complexBumPointer,
                        propChange -> {
                            LOG.info("received Change {}", propChange);
                            Assertions.assertThat(propChange.getAction()).isEqualTo(ChangeAction.UPDATED);
                            Assertions.assertThat((CharSequence) propChange.getEntityId()).isEqualTo(thingId3);
                            Assertions.assertThat(propChange.isFull()).isTrue();
                            Assertions.assertThat((CharSequence) propChange.getPath())
                                    .isEqualTo(emptyPointer()); // attribute "complex/bum" was modified completely
                            Assertions.assertThat(propChange.getValue().get().asString()).isEqualTo("lux");

                            latch.countDown();
                        });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId3, FeatureModified.TYPE)
                        .featureId(FEATURE_ID_2)
                        .build();

        // modify the feature
        final Message<ThingEvent> featureModified =
                MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders).payload(
                        FeatureModified.of(thingId3, FEATURE2, 1, DittoHeaders.empty())).build();

        messaging.receiveEvent(featureModified);

        latch.await(TIMEOUT_SECONDS, SECONDS);
        assertEquals(0, latch.getCount());
    }

}
