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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotAccessibleException;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributeResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributesResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributeResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributesResponse;
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
    public void testAddStringAttributeWithExistsOptionFalse() {
        assertEventualCompletion(
                client.twin()
                        .forId(THING_ID)
                        .putAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.Modify.exists(false))
        );
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        assertOnlyIfNoneMatchHeader(command);
        reply(ModifyAttributeResponse.created(THING_ID, ATTRIBUTE_KEY_NEW, JsonValue.of(ATTRIBUTE_VALUE),
                command.getDittoHeaders()));
    }

    @Test
    public void testAddStringAttributeWithExistsOptionTrue() {
        assertEventualCompletion(
                client.twin()
                        .forId(THING_ID)
                        .putAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.Modify.exists(true))
        );
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        assertOnlyIfMatchHeader(command);
        reply(ModifyAttributeResponse.modified(THING_ID, ATTRIBUTE_KEY_NEW, command.getDittoHeaders()));
    }

    @Test
    public void testAddBooleanAttribute() {
        assertEventualCompletion(
                client.twin()
                        .forId(THING_ID)
                        .putAttribute(ATTRIBUTE_KEY_NEW, true)
        );
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        reply(ModifyAttributeResponse.created(THING_ID, ATTRIBUTE_KEY_NEW, JsonValue.of(true),
                command.getDittoHeaders()));
    }

    @Test
    public void testAddObjectAttribute() {
        assertEventualCompletion(
                client.twin()
                        .forId(THING_ID)
                        .putAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.newObject("{\"id\": 42, \"name\": \"someName\"}"))
        );
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        reply(ModifyAttributeResponse.modified(THING_ID, ATTRIBUTE_KEY_NEW, command.getDittoHeaders()));
    }

    @Test
    public void testAddAttributeFailureDueToThingErrorResponse() throws Exception {
        final CompletableFuture<Void> resultFuture = client.twin()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, true);
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        reply(ThingErrorResponse.of(ThingNotAccessibleException.newBuilder(THING_ID).build(),
                command.getDittoHeaders()));
        resultFuture.exceptionally(error -> null).get(1L, TimeUnit.SECONDS);
        assertThat(resultFuture).hasFailedWithThrowableThat().isInstanceOf(ThingNotAccessibleException.class);
    }

    @Test
    public void testDeleteAttribute() {
        assertEventualCompletion(
                client.twin()
                        .forId(THING_ID)
                        .deleteAttribute(ATTRIBUTE_KEY_OLD)
        );
        final Signal<?> command = expectMsgClass(DeleteAttribute.class);
        reply(DeleteAttributeResponse.of(THING_ID, ATTRIBUTE_KEY_NEW, command.getDittoHeaders()));
    }

    @Test
    public void testDeleteAttributes() {
        assertEventualCompletion(
                client.twin()
                        .forId(THING_ID)
                        .deleteAttributes()
        );
        final Signal<?> command = expectMsgClass(DeleteAttributes.class);
        reply(DeleteAttributesResponse.of(THING_ID, command.getDittoHeaders()));
    }

    @Test
    public void testDeleteAttributesFailureDueToUnexpectedResponse() throws Exception {
        final CompletableFuture<Void> resultFuture = client.twin()
                .forId(THING_ID)
                .deleteAttributes();
        final Signal<?> command = expectMsgClass(DeleteAttributes.class);
        reply(command);
        resultFuture.exceptionally(error -> null).get(1, TimeUnit.SECONDS);
        assertThat(resultFuture).hasFailedWithThrowableThat().isInstanceOf(ClassCastException.class);
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
    public void testModifyAttributes_null() {
        assertEventualCompletion(
                client.twin()
                        .forId(THING_ID)
                        .setAttributes(JsonFactory.nullObject())
        );
        final ModifyAttributes modifyAttributes = expectMsgClass(ModifyAttributes.class);
        assertThat(modifyAttributes.getAttributes()).isEqualTo(ThingsModelFactory.nullAttributes());
        reply(ModifyAttributesResponse.modified(THING_ID, modifyAttributes.getDittoHeaders()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeAttributeWithEmptyPointerThrowsException() {
        client.twin()
                .forId(THING_ID)
                .putAttribute(JsonFactory.emptyPointer(), "it should fail");
    }
}
