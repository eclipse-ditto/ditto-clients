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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.client.internal.AbstractDittoClientThingsTest;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.messages.model.MessageDirection;
import org.eclipse.ditto.messages.model.MessageHeaders;
import org.eclipse.ditto.messages.model.MessagesModelFactory;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;
import org.eclipse.ditto.things.model.signals.commands.exceptions.ThingNotAccessibleException;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttributeResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttributesResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThingResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttributeResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttributesResponse;
import org.eclipse.ditto.things.model.signals.events.AttributeCreated;
import org.eclipse.ditto.things.model.signals.events.AttributeModified;
import org.eclipse.ditto.things.model.signals.events.ThingEvent;
import org.junit.Test;

/**
 * Test attribute-related operations of the {@link DittoClient}.
 */
public final class DittoClientAttributesTest extends AbstractDittoClientThingsTest {

    private static final String CONDITION = "ne(attributes/test)";
    private static final JsonPointer ATTRIBUTE_KEY_NEW = JsonFactory.newPointer("new");
    private static final JsonPointer ATTRIBUTE_KEY_REALLY_NEW = JsonFactory.newPointer("reallyNew");
    private static final JsonPointer ATTRIBUTE_KEY_OLD = JsonFactory.newPointer("old");
    private static final String ATTRIBUTE_VALUE = "value";
    private static final JsonObject ATTRIBUTES = JsonFactory.newObjectBuilder()
            .set(ATTRIBUTE_KEY_NEW, 42)
            .set(ATTRIBUTE_KEY_REALLY_NEW, true)
            .build();

    @Test
    public void testAddStringAttributeWithExistsOptionFalse() {
        assertEventualCompletion(
                getManagement()
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
                getManagement()
                        .forId(THING_ID)
                        .putAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.Modify.exists(true))
        );
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        assertOnlyIfMatchHeader(command);
        reply(ModifyAttributeResponse.modified(THING_ID, ATTRIBUTE_KEY_NEW, command.getDittoHeaders()));
    }

    @Test
    public void testMergeStringAttributeWithExistsOptionFalse() {
        final JsonPointer absolutePath = Thing.JsonFields.ATTRIBUTES.getPointer().append(ATTRIBUTE_KEY_NEW);

        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .mergeAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.Modify.exists(false))
        );
        final Signal<?> command = expectMsgClass(MergeThing.class);
        assertOnlyIfNoneMatchHeader(command);
        reply(MergeThingResponse.of(THING_ID, absolutePath, command.getDittoHeaders()));
    }

    @Test
    public void testMergeStringAttributeWithExistsOptionTrue() {
        final JsonPointer absolutePath =
                Thing.JsonFields.ATTRIBUTES.getPointer().append(ATTRIBUTE_KEY_NEW);

        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .mergeAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.Modify.exists(true))
        );
        final Signal<?> command = expectMsgClass(MergeThing.class);
        assertOnlyIfMatchHeader(command);
        reply(MergeThingResponse.of(THING_ID, absolutePath, command.getDittoHeaders()));
    }

    @Test
    public void testAddBooleanAttribute() {
        assertEventualCompletion(
                getManagement()
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
                getManagement()
                        .forId(THING_ID)
                        .putAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.newObject("{\"id\": 42, \"name\": \"someName\"}"))
        );
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        reply(ModifyAttributeResponse.modified(THING_ID, ATTRIBUTE_KEY_NEW, command.getDittoHeaders()));
    }

    @Test
    public void testMergeAttribute() {
        final JsonPointer absolutePath = Thing.JsonFields.ATTRIBUTES.getPointer().append(ATTRIBUTE_KEY_NEW);
        final JsonObject value = JsonFactory.newObject("{\"id\": 42, \"name\": " +
                "\"someName\"}");

        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .mergeAttribute(ATTRIBUTE_KEY_NEW, value)
        );
        final Signal<?> command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID, absolutePath, command.getDittoHeaders()));
    }

    @Test
    public void testAddAttributeFailureDueToThingErrorResponse() throws Exception {
        final CompletableFuture<Void> resultFuture = getManagement()
                .forId(THING_ID)
                .putAttribute(ATTRIBUTE_KEY_NEW, true)
                .toCompletableFuture();
        final Signal<?> command = expectMsgClass(ModifyAttribute.class);
        reply(ThingErrorResponse.of(ThingNotAccessibleException.newBuilder(THING_ID).build(),
                command.getDittoHeaders()));
        resultFuture.exceptionally(error -> null).get(1L, TimeUnit.SECONDS);
        assertThat(resultFuture).hasFailedWithThrowableThat().isInstanceOf(ThingNotAccessibleException.class);
    }

    @Test
    public void testMergeAttributeFailureDueToThingErrorResponse() throws Exception {
        final CompletableFuture<Void> resultFuture = getManagement()
                .forId(THING_ID)
                .mergeAttribute(ATTRIBUTE_KEY_NEW, true).toCompletableFuture();
        final Signal<?> command = expectMsgClass(MergeThing.class);
        reply(ThingErrorResponse.of(ThingNotAccessibleException.newBuilder(THING_ID).build(),
                command.getDittoHeaders()));
        resultFuture.exceptionally(error -> null).get(1L, TimeUnit.SECONDS);
        assertThat(resultFuture).hasFailedWithThrowableThat().isInstanceOf(ThingNotAccessibleException.class);
    }

    @Test
    public void testDeleteAttribute() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .deleteAttribute(ATTRIBUTE_KEY_OLD)
        );
        final Signal<?> command = expectMsgClass(DeleteAttribute.class);
        reply(DeleteAttributeResponse.of(THING_ID, ATTRIBUTE_KEY_NEW, command.getDittoHeaders()));
    }

    @Test
    public void testDeleteAttributes() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .deleteAttributes()
        );
        final Signal<?> command = expectMsgClass(DeleteAttributes.class);
        reply(DeleteAttributesResponse.of(THING_ID, command.getDittoHeaders()));
    }

    @Test
    public void testDeleteAttributesFailureDueToUnexpectedResponse() throws Exception {
        final CompletableFuture<Void> resultFuture = getManagement()
                .forId(THING_ID)
                .deleteAttributes()
                .toCompletableFuture();
        final Signal<?> command = expectMsgClass(DeleteAttributes.class);
        reply(command);
        resultFuture.exceptionally(error -> null).get(1, TimeUnit.SECONDS);
        assertThat(resultFuture).hasFailedWithThrowableThat().isInstanceOf(ClassCastException.class);
    }

    @Test
    public void testReceiveAttributeModifiedEvent() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        getManagement().startConsumption();
        getManagement().registerForAttributeChanges("test", ATTRIBUTE_KEY_NEW, attributeChange -> {
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
                .payload(AttributeModified.of(THING_ID,
                        ATTRIBUTE_KEY_NEW,
                        JsonFactory.newValue("value"),
                        1,
                        Instant.now(),
                        headersWithChannel(),
                        null))
                .build();

        messaging.receiveEvent(attributeModified);

        assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testReceiveAttributeModifiedEventWithActionAdded() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        getManagement().startConsumption();
        getManagement().registerForAttributeChanges("test", ATTRIBUTE_KEY_REALLY_NEW, attributeChange -> {
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
                .payload(AttributeCreated.of(THING_ID,
                        ATTRIBUTE_KEY_REALLY_NEW,
                        JsonFactory.newValue("value"),
                        1,
                        Instant.now(),
                        headersWithChannel(),
                        null))
                .build();

        messaging.receiveEvent(attributeCreated);

        assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAttribute_attributesRootPath() {
        getManagement()
                .forId(THING_ID)
                .deleteAttribute(JsonFactory.emptyPointer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAttribute_attributesEmptyPath() {
        getManagement()
                .forId(THING_ID)
                .deleteAttribute(JsonFactory.emptyPointer());
    }

    @Test
    public void testModifyAttributes_null() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .setAttributes(JsonFactory.nullObject())
        );
        final ModifyAttributes modifyAttributes = expectMsgClass(ModifyAttributes.class);
        assertThat(modifyAttributes.getAttributes()).isEqualTo(ThingsModelFactory.nullAttributes());
        reply(ModifyAttributesResponse.modified(THING_ID, modifyAttributes.getDittoHeaders()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeAttributeWithEmptyPointerThrowsException() {
        getManagement()
                .forId(THING_ID)
                .putAttribute(JsonFactory.emptyPointer(), "it should fail");
    }

    @Test
    public void testPutAttributeWithConditionOption() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .putAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.condition(CONDITION))
        );
        final ModifyAttribute command = expectMsgClass(ModifyAttribute.class);
        reply(ModifyAttributeResponse.created(THING_ID, ATTRIBUTE_KEY_NEW, JsonValue.of(ATTRIBUTE_VALUE),
                command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void testMergeAttributeWithConditionOption() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .mergeAttribute(ATTRIBUTE_KEY_NEW, ATTRIBUTE_VALUE, Options.condition(CONDITION))
        );
        final MergeThing command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID, JsonFactory.newPointer("/attributes"), command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void testMergeAttributesWithConditionOption() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .mergeAttributes(ATTRIBUTES, Options.condition(CONDITION))
        );
        final MergeThing command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID, JsonFactory.newPointer("/attributes"), command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void testDeleteAttributeWithConditionOption() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .deleteAttribute(ATTRIBUTE_KEY_NEW, Options.condition(CONDITION))
        );
        final DeleteAttribute command = expectMsgClass(DeleteAttribute.class);
        reply(DeleteAttributeResponse.of(THING_ID, ATTRIBUTE_KEY_NEW, command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void testDeleteAttributesWithConditionOption() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .deleteAttributes(Options.condition(CONDITION))
        );
        final DeleteAttributes command = expectMsgClass(DeleteAttributes.class);
        reply(DeleteAttributesResponse.of(THING_ID, command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

}
