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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_ID;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_JSON_OBJECT;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID_COPY_POLICY;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_WITH_INLINE_POLICY;
import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientThingsTest;
import org.eclipse.ditto.client.management.AcknowledgementsFailedException;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.client.registration.DuplicateRegistrationIdException;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.auth.AuthorizationModelFactory;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.acks.base.Acknowledgements;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingPreconditionFailedException;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.CreateThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.MergeThing;
import org.eclipse.ditto.signals.commands.things.modify.MergeThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyId;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyIdResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingResponse;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.junit.Test;

/**
 * Test top-level (i.e. Thing) operations of the {@link DittoClient}.
 */
public final class DittoClientThingTest extends AbstractDittoClientThingsTest {

    private static final String FEATURE_ID = "someFeature";
    private static final JsonPointer ATTRIBUTE_KEY_NEW = JsonFactory.newPointer("new");
    private static final String ATTRIBUTE_VALUE = "value";
    private static final Feature FEATURE = ThingsModelFactory.newFeatureBuilder()
            .properties(ThingsModelFactory.newFeaturePropertiesBuilder()
                    .set("propertyPointer", "propertyValue")
                    .build())
            .withId(FEATURE_ID)
            .build();

    private static final Thing THING = ThingsModelFactory.newThingBuilder()
            .setId(THING_ID)
            .setPolicyId(POLICY_ID)
            .setAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.newValue(ATTRIBUTE_VALUE))
            .setFeature(FEATURE)
            .build();

    @Test
    public void testCreateThing() {
        assertEventualCompletion(getManagement().create(THING_ID));
        reply(CreateThingResponse.of(Thing.newBuilder().setId(THING_ID).build(),
                expectMsgClass(CreateThing.class).getDittoHeaders()));
    }

    @Test
    public void testMergeThing() {
        assertEventualCompletion(getManagement().merge(THING_ID, THING));
        final MergeThing command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(command.getThingEntityId(), command.getPath(), command.getDittoHeaders()));
        assertOnlyIfMatchHeader(command);
    }


    @Test
    public void testCreateThingWithCustomAcknowledgementsOnly() {
        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement()
                        .create(THING_ID, Options.headers(DittoHeaders.newBuilder()
                                .acknowledgementRequest(AcknowledgementRequest.of(label1))
                                .build()))
                );
    }

    @Test
    public void testCreateThingWith2Acknowledgements() {
        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        final AcknowledgementLabel label2 = getChannelAcknowledgementLabel();
        assertEventualCompletion(getManagement()
                .create(THING_ID, Options.headers(DittoHeaders.newBuilder()
                        .acknowledgementRequest(
                                AcknowledgementRequest.of(label1),
                                AcknowledgementRequest.of(label2))
                        .build()))
        );

        final DittoHeaders sentDittoHeaders = expectMsgClass(CreateThing.class).getDittoHeaders();
        reply(Acknowledgements.of(
                Arrays.asList(
                        Acknowledgement.of(label1, THING_ID, HttpStatusCode.OK, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatusCode.ACCEPTED, DittoHeaders.empty())
                ),
                sentDittoHeaders
        ));

        assertThat(sentDittoHeaders.getAcknowledgementRequests())
                .containsExactly(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2));
    }

    @Test
    public void testUpdateThingWithFailedAcknowledgements() {
        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        final AcknowledgementLabel label2 = getChannelAcknowledgementLabel();
        final Acknowledgements expectedAcknowledgements = Acknowledgements.of(
                Arrays.asList(
                        Acknowledgement.of(label1, THING_ID, HttpStatusCode.FORBIDDEN, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatusCode.ACCEPTED, DittoHeaders.empty())
                ),
                DittoHeaders.empty()
        );
        assertEventualCompletion(getManagement()
                .update(THING, Options.headers(DittoHeaders.newBuilder()
                        .acknowledgementRequest(
                                AcknowledgementRequest.of(label1),
                                AcknowledgementRequest.of(label2))
                        .build()))
                .exceptionally(error -> {
                    assertThat(error).isInstanceOf(CompletionException.class)
                            .hasCauseInstanceOf(AcknowledgementsFailedException.class);
                    final AcknowledgementsFailedException cause = (AcknowledgementsFailedException) error.getCause();
                    assertThat(cause.getAcknowledgements().setDittoHeaders(DittoHeaders.empty()))
                            .isEqualTo(expectedAcknowledgements);
                    return null;
                })
        );

        final DittoHeaders sentDittoHeaders = expectMsgClass(ModifyThing.class).getDittoHeaders();
        reply(expectedAcknowledgements.setDittoHeaders(sentDittoHeaders));

        assertThat(sentDittoHeaders.getAcknowledgementRequests())
                .containsExactly(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2));
    }

    @Test
    public void createThingFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(
                        () -> getManagement().create(THING_ID, Options.Modify.exists(false)).get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testPutThingWithExistsOptionFalse() {
        assertEventualCompletion(getManagement().put(THING, Options.Modify.exists(false)));
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.created(Thing.newBuilder().setId(THING_ID).build(), command.getDittoHeaders()));
        assertOnlyIfNoneMatchHeader(command);
    }

    @Test
    public void testPutThingWithExistsOptionTrue() {
        assertEventualCompletion(getManagement().put(THING, Options.Modify.exists(true)));
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.modified(THING_ID, command.getDittoHeaders()));
        assertOnlyIfMatchHeader(command);
    }

    @Test
    public void testPutThingWithUnsatisfiedPrecondition() {
        assertEventualCompletion(getManagement().put(THING, Options.Modify.exists(true))
                .handle((response, error) -> {
                    assertThat(error)
                            .describedAs("Expect failure with %s, got response=%s, error=%s",
                                    ThingPreconditionFailedException.class.getSimpleName(), response, error)
                            .isInstanceOf(CompletionException.class)
                            .hasCauseInstanceOf(ThingPreconditionFailedException.class);
                    return null;
                }));
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        final DittoRuntimeException error =
                ThingPreconditionFailedException.newBuilder("if-match", "\"*\"", "").build();
        reply(ThingErrorResponse.of(error, command.getDittoHeaders()));
    }

    @Test
    public void testUpdateThing() {
        assertEventualCompletion(getManagement().update(THING));
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.modified(THING_ID, command.getDittoHeaders()));
        assertOnlyIfMatchHeader(command);
    }

    @Test
    public void updateThingFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().update(THING, Options.Modify.exists(false)).get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testDeleteThing() {
        assertEventualCompletion(getManagement().forId(THING_ID).delete());
        reply(DeleteThingResponse.of(THING_ID, expectMsgClass(DeleteThing.class).getDittoHeaders()));
    }

    @Test
    public void deleteThingFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(
                        () -> getManagement().delete(THING_ID, Options.Modify.exists(false)).get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testReceiveCreatedEvent() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        getManagement().startConsumption();
        getManagement().registerForThingChanges("test", change -> {
            assertThat(change)
                    .hasThingId(THING_ID)
                    .isAdded();

            latch.countDown();
        });

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, THING_ID, ThingCreated.TYPE).build();

        final Message<ThingEvent> thingCreated = MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders)
                .payload(ThingCreated.of(Thing.newBuilder().setId(THING_ID).build(), 1, headersWithChannel()))
                .build();

        messaging.receiveEvent(thingCreated);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testReceiveDeletedEvent() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        getManagement().startConsumption();
        getManagement().registerForThingChanges("test", change -> {
            assertThat(change)
                    .hasThingId(THING_ID)
                    .isDeleted();

            latch.countDown();
        });

        final Message<ThingEvent> thingDeleted = createThingDeletedMessage();
        messaging.receiveEvent(thingDeleted);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    private Message<ThingEvent> createThingDeletedMessage() {
        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, THING_ID, ThingDeleted.TYPE).build();

        return MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders)
                .payload(ThingDeleted.of(THING_ID, 1, headersWithChannel()))
                .build();
    }

    @Test
    public void testSetPolicyId() {
        assertEventualCompletion(getManagement().forId(THING_ID).setPolicyId(POLICY_ID));
        reply(ModifyPolicyIdResponse.modified(THING_ID, expectMsgClass(ModifyPolicyId.class).getDittoHeaders()));
    }

    @Test
    public void testMergePolicyId() {
        assertEventualCompletion(getManagement().forId(THING_ID).mergePolicyId(POLICY_ID));
        final MergeThing mergeThing = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID, Thing.JsonFields.POLICY_ID.getPointer(),
                mergeThing.getDittoHeaders()));
    }

    @Test
    public void testRegisterTwoHandlersWithSameSelector() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);

        getManagement().startConsumption();
        getManagement().registerForThingChanges("test", change -> latch.countDown());
        getManagement().registerForThingChanges("test2", change -> latch.countDown());

        final Message<ThingEvent> thingDeleted = createThingDeletedMessage();
        messaging.receiveEvent(thingDeleted);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test(expected = DuplicateRegistrationIdException.class)
    public void testRegisterTwoHandlersWithSameRegistrationId() {
        getManagement().registerForThingChanges("test", change -> {
        });
        getManagement().registerForThingChanges("test", change -> {
        });
    }

    @Test
    public void testDeregisterEventHandler() throws Exception {
        // prepare
        final Semaphore sem = new Semaphore(0);
        final String registrationId = "test";

        getManagement().startConsumption();
        getManagement().registerForThingChanges(registrationId, change -> sem.release());

        final Message<ThingEvent> thingDeleted = createThingDeletedMessage();
        messaging.receiveEvent(thingDeleted);
        Assertions.assertThat(sem.tryAcquire(1, TIMEOUT, TIME_UNIT)).isTrue();

        // test
        final boolean unregistered = getManagement().deregister(registrationId);
        Assertions.assertThat(unregistered).isTrue();

        messaging.receiveEvent(thingDeleted);

        // verify: handler must not have been called
        Assertions.assertThat(sem.tryAcquire(1, TIMEOUT, TIME_UNIT)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateThingWithMissingId() {
        getManagement().create(JsonFactory.newObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateThingWithMissingId() {
        getManagement().update(JsonFactory.newObject());
    }

    @Test
    public void testCreateThingWithoutFeatures() {
        final ThingId thingIdWithoutFeatures = ThingId.of("demo:mything1");
        final AuthorizationSubject authorizationSubject = AuthorizationModelFactory.newAuthSubject("someSubject");
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingIdWithoutFeatures)
                .setPermissions(authorizationSubject, ThingsModelFactory.allPermissions())
                .build();
        assertEventualCompletion(getManagement().create(thing));
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(command.getThing(), command.getDittoHeaders()));
        assertThat((CharSequence) command.getThingEntityId()).isEqualTo(thingIdWithoutFeatures);
    }

    @Test
    public void testCreateThingWithInlinePolicy() {
        assertEventualCompletion(getManagement().create(THING_WITH_INLINE_POLICY));
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(ThingsModelFactory.newThing(THING_WITH_INLINE_POLICY), command.getDittoHeaders()));
        assertThat(command.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testCreateThingWithInitialJSONPolicy() {
        getManagement().create(THING_ID, POLICY_JSON_OBJECT);
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(ThingsModelFactory.newThing(THING_WITH_INLINE_POLICY), command.getDittoHeaders()));
        Assertions.assertThat(command.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testCreateThingWithInitialPolicy() {
        getManagement().create(THING_ID, POLICY);
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(ThingsModelFactory.newThing(THING_WITH_INLINE_POLICY), command.getDittoHeaders()));
        Assertions.assertThat(command.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testPutThingWithInlinePolicy() {
        getManagement().put(THING_WITH_INLINE_POLICY);
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.created(ThingsModelFactory.newThing(THING_WITH_INLINE_POLICY),
                command.getDittoHeaders()));
        Assertions.assertThat(command.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testPutThingWithInitialJSONPolicy() {
        assertEventualCompletion(getManagement().put(THING, POLICY_JSON_OBJECT));
        final ModifyThing createThing = expectMsgClass(ModifyThing.class);
        reply(CreateThingResponse.of(Thing.newBuilder().setId(THING_ID).build(), createThing.getDittoHeaders()));
        assertThat((CharSequence) createThing.getThingEntityId()).isEqualTo(THING_ID);
        assertThat(createThing.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testPutThingWithInitialPolicy() {
        getManagement().put(THING, POLICY);
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.created(THING, command.getDittoHeaders()));
        assertThat(command.getInitialPolicy()).contains(POLICY_JSON_OBJECT);
    }

    @Test
    public void testCreateThingWithInitialPolicyJsonNullable() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().create(THING_ID, (JsonObject) null));
    }

    @Test
    public void testCreateThingWithInitialPolicyNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().create(THING_ID, (Policy) null));
    }

    @Test
    public void testPutThingWithInitialPolicyJsonNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().put(THING, (JsonObject) null));
    }

    @Test
    public void testPutThingWithInitialPolicyNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().put(THING, (Policy) null));
    }

    @Test
    public void testCreateThingWithOptionCopyPolicy() {
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);
        assertEventualCompletion(getManagement().create(THING_ID_COPY_POLICY, copyPolicy));
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(THING, command.getDittoHeaders()));
        assertThat(command.getPolicyIdOrPlaceholder()).contains(POLICY_ID.toString());
    }

    @Test
    public void testCreateThingWithOptionCopyPolicyFromThing() {
        final Option<ThingId> copyPolicy = Options.Modify.copyPolicyFromThing(THING_ID);
        assertEventualCompletion(getManagement().create(THING_ID_COPY_POLICY, copyPolicy));
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(THING, command.getDittoHeaders()));
        assertThat(command.getPolicyIdOrPlaceholder()).contains("{{ ref:things/" + THING_ID + "/policyId }}");
    }

    @Test
    public void testCreateThingWithJsonInlinePolicyAndOptionCopyPolicy() {
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().create(THING_ID_COPY_POLICY, POLICY_JSON_OBJECT, copyPolicy));
    }

    @Test
    public void testCreateThingWithAllOptionCopyPolicy() {
        final Option<ThingId> copyPolicyFromThing = Options.Modify.copyPolicyFromThing(THING_ID);
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().create(THING_ID_COPY_POLICY, copyPolicy, copyPolicyFromThing));
    }

    @Test
    public void testPutThingWithOptionCopyPolicy() {
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);
        assertEventualCompletion(getManagement().put(THING, copyPolicy));
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.created(THING, command.getDittoHeaders()));
        assertThat(command.getPolicyIdOrPlaceholder()).contains(POLICY_ID.toString());
    }

    @Test
    public void testPutThingWithJsonInlinePolicyAndOptionCopyPolicy() {
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().put(THING, POLICY_JSON_OBJECT, copyPolicy));
    }

    @Test
    public void testPutThingWithAllOptionCopyPolicy() {
        final Option<ThingId> copyPolicyFromThing = Options.Modify.copyPolicyFromThing(THING_ID);
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getManagement().put(THING, copyPolicy, copyPolicyFromThing));
    }

    @Test
    public void testPutThingWithoutPolicy() {
        assertEventualCompletion(getManagement().put(THING)
                .thenAccept(result -> assertThat(result).isEmpty())
        );

        reply(ModifyThingResponse.modified(THING_ID, expectMsgClass(ModifyThing.class).getDittoHeaders()));
    }
}
