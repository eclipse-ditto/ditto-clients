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

import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributeModified;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.junit.Test;

/**
 * Test attribute-related operations of the {@link DittoClient}.
 */
public final class DittoClientAttributesTest extends AbstractDittoClientTest {

    private static final JsonPointer ATTRIBUTE_KEY_NEW = JsonFactory.newPointer("new");
    private static final JsonPointer ATTRIBUTE_KEY_REALLY_NEW = JsonFactory.newPointer("reallyNew");
    private static final JsonPointer ATTRIBUTE_KEY_OLD = JsonFactory.newPointer("old");
    private static final String ATTRIBUTE_VALUE = "value";


    @Test
    public void testAddStringAttributeWithoutExistsOption() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE)
                    .hasNoConditionalHeaders();

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddStringAttributeWithExistsOptionFalse() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE)
                    .hasOnlyIfNoneMatchHeader();

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.Modify.exists(false));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddStringAttributeWithExistsOptionTrue() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE)
                    .hasOnlyIfMatchHeader();

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.Modify.exists(true));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddBooleanAttribute() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, true);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddObjectAttribute() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.newObject("{\"id\": 42, \"name\": \"someName\"}"));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddArrayAttribute() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.newArray("[1, \"two\"]"));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddObjectAttributePartial() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW.addLeaf(JsonFactory.newKey("name")), "someOtherName");

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testDeleteAttribute() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(DeleteAttribute.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .deleteAttribute(ATTRIBUTE_KEY_OLD);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testDeleteAttributes() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(DeleteAttributes.TYPE);
            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .deleteAttributes();

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testReceiveAttributeModifiedEvent() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        client.twin().startConsumption();
        client.twin().registerForAttributeChanges("test", ATTRIBUTE_KEY_NEW, attributeChange -> {
            assertThat(attributeChange)
                    .hasThingId(THING_ID)
                    .hasPath(ATTRIBUTE_KEY_NEW)
                    .hasAttributeValue(JsonFactory.newValue("value"))
                    .isUpdated();

            latch.countDown();
        });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, THING_ID, AttributeModified.TYPE)
                        .build();

        final Message<ThingEvent> attributeModified = MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders)
                .payload(AttributeModified.of(THING_ID, ATTRIBUTE_KEY_NEW, JsonFactory.newValue("value"), 1,
                        DittoHeaders.empty()))
                .build();

        messaging.receiveEvent(attributeModified);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testReceiveAttributeModifiedEventWithActionAdded() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        client.twin().startConsumption();
        client.twin().registerForAttributeChanges("test", ATTRIBUTE_KEY_REALLY_NEW, attributeChange -> {
            assertThat(attributeChange)
                    .hasThingId(THING_ID)
                    .hasPath(ATTRIBUTE_KEY_REALLY_NEW)
                    .hasAttributeValue(JsonFactory.newValue("value"))
                    .isAdded();

            latch.countDown();
        });


        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, THING_ID, AttributeCreated.TYPE).build();

        final Message<ThingEvent> attributeCreated = MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders)
                .payload(AttributeCreated.of(THING_ID, ATTRIBUTE_KEY_REALLY_NEW, JsonFactory.newValue("value"), 1,
                        DittoHeaders.empty()))
                .build();

        messaging.receiveEvent(attributeCreated);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAttribute_attributesRootPath() {
        client.twin()
                .forId(THING_ID)
                .deleteAttribute(JsonFactory.emptyPointer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAttribute_attributesEmptyPath() {
        client.twin()
                .forId(THING_ID)
                .deleteAttribute(JsonFactory.emptyPointer());
    }

    @Test
    public void testAddNullAttribute() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.nullLiteral()); // or "null"

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddArrayAttributeObject() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttribute.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.newArrayBuilder()
                        .add(1)
                        .add("two")
                        .build());

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddAttributesStructure() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttributes.TYPE);

            latch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .setAttributes(JsonFactory.newObjectBuilder()
                        .set("att1", 34)
                        .set("att2", "someValue")
                        .build());

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testAddAttributesStructure_null() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyAttributes.TYPE);

            latch.countDown();

        });

        client.twin()
                .forId(THING_ID)
                .setAttributes(JsonFactory.nullObject());

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeAttributeWithEmptyPointerThrowsException() {
        client.twin()
                .forId(THING_ID)
                .putAttribute(JsonFactory.emptyPointer(), "it should fail");
    }

}
