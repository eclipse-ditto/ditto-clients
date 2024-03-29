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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_ID;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_JSON_OBJECT;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID_COPY_POLICY;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_WITH_INLINE_POLICY;
import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.acks.AcknowledgementRequest;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.exceptions.DittoRuntimeException;
import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgement;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgements;
import org.eclipse.ditto.client.internal.AbstractDittoClientThingsTest;
import org.eclipse.ditto.client.management.AcknowledgementsFailedException;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.client.registration.DuplicateRegistrationIdException;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.messages.model.MessageDirection;
import org.eclipse.ditto.messages.model.MessageHeaders;
import org.eclipse.ditto.messages.model.MessagesModelFactory;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;
import org.eclipse.ditto.things.model.signals.commands.exceptions.ThingPreconditionFailedException;
import org.eclipse.ditto.things.model.signals.commands.modify.CreateThing;
import org.eclipse.ditto.things.model.signals.commands.modify.CreateThingResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteThing;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteThingResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThingResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyPolicyId;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyPolicyIdResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyThing;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyThingResponse;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThingResponse;
import org.eclipse.ditto.things.model.signals.events.ThingCreated;
import org.eclipse.ditto.things.model.signals.events.ThingDeleted;
import org.eclipse.ditto.things.model.signals.events.ThingEvent;
import org.junit.Test;

/**
 * Test top-level (i.e. Thing) operations of the {@link DittoClient}.
 */
public final class DittoClientThingTest extends AbstractDittoClientThingsTest {

    private static final String FEATURE_ID = "someFeature";
    private static final String CONDITION = "ne(attributes/test)";
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
        reply(MergeThingResponse.of(command.getEntityId(), command.getPath(), command.getDittoHeaders()));
        assertOnlyIfMatchHeader(command);
    }

    @Test
    public void testCreateThingWithCustomAcknowledgementsOnly() {
        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        assertThatIllegalArgumentException()
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
                        Acknowledgement.of(label1, THING_ID, HttpStatus.OK, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatus.ACCEPTED, DittoHeaders.empty())
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
                        Acknowledgement.of(label1, THING_ID, HttpStatus.FORBIDDEN, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatus.ACCEPTED, DittoHeaders.empty())
                ),
                DittoHeaders.empty()
        );
        assertEventualCompletion(getManagement()
                .update(THING, Options.headers(DittoHeaders.newBuilder()
                        .acknowledgementRequest(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2))
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
        assertThatIllegalArgumentException()
                .isThrownBy(() -> getManagement()
                        .create(THING_ID, Options.Modify.exists(false))
                        .toCompletableFuture()
                        .get(TIMEOUT, TIME_UNIT));
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
        assertThatIllegalArgumentException()
                .isThrownBy(() -> getManagement()
                        .update(THING, Options.Modify.exists(false))
                        .toCompletableFuture()
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testDeleteThing() {
        assertEventualCompletion(getManagement().forId(THING_ID).delete());
        reply(DeleteThingResponse.of(THING_ID, expectMsgClass(DeleteThing.class).getDittoHeaders()));
    }

    @Test
    public void deleteThingFailsWithExistsOption() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> getManagement()
                        .delete(THING_ID, Options.Modify.exists(false))
                        .toCompletableFuture()
                        .get(TIMEOUT, TIME_UNIT));
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
                .payload(ThingCreated.of(Thing.newBuilder().setId(THING_ID).build(),
                        1,
                        Instant.now(),
                        headersWithChannel(),
                        null))
                .build();

        messaging.receiveEvent(thingCreated);

        assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
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

        assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    private Message<ThingEvent> createThingDeletedMessage() {
        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, THING_ID, ThingDeleted.TYPE).build();

        return MessagesModelFactory.<ThingEvent>newMessageBuilder(messageHeaders)
                .payload(ThingDeleted.of(THING_ID, 1, Instant.now(), headersWithChannel(), null))
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
        reply(MergeThingResponse.of(THING_ID, Thing.JsonFields.POLICY_ID.getPointer(), mergeThing.getDittoHeaders()));
    }

    @Test
    public void testRegisterTwoHandlersWithSameSelector() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);

        getManagement().startConsumption();
        getManagement().registerForThingChanges("test", change -> latch.countDown());
        getManagement().registerForThingChanges("test2", change -> latch.countDown());

        final Message<ThingEvent> thingDeleted = createThingDeletedMessage();
        messaging.receiveEvent(thingDeleted);

        assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
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
        assertThat(sem.tryAcquire(1, TIMEOUT, TIME_UNIT)).isTrue();

        // test
        final boolean unregistered = getManagement().deregister(registrationId);
        assertThat(unregistered).isTrue();

        messaging.receiveEvent(thingDeleted);

        // verify: handler must not have been called
        assertThat(sem.tryAcquire(1, TIMEOUT, TIME_UNIT)).isFalse();
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
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingIdWithoutFeatures)
                .build();
        assertEventualCompletion(getManagement().create(thing));
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(command.getThing(), command.getDittoHeaders()));
        assertThat((CharSequence) command.getEntityId()).isEqualTo(thingIdWithoutFeatures);
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
        assertThat(command.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testCreateThingWithInitialPolicy() {
        getManagement().create(THING_ID, POLICY);
        final CreateThing command = expectMsgClass(CreateThing.class);
        reply(CreateThingResponse.of(ThingsModelFactory.newThing(THING_WITH_INLINE_POLICY), command.getDittoHeaders()));
        assertThat(command.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testPutThingWithInlinePolicy() {
        getManagement().put(THING_WITH_INLINE_POLICY);
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.created(ThingsModelFactory.newThing(THING_WITH_INLINE_POLICY),
                command.getDittoHeaders()));
        assertThat(command.getInitialPolicy()).isNotEmpty();
    }

    @Test
    public void testPutThingWithInitialJSONPolicy() {
        assertEventualCompletion(getManagement().put(THING, POLICY_JSON_OBJECT));
        final ModifyThing createThing = expectMsgClass(ModifyThing.class);
        reply(CreateThingResponse.of(Thing.newBuilder().setId(THING_ID).build(), createThing.getDittoHeaders()));
        assertThat((CharSequence) createThing.getEntityId()).isEqualTo(THING_ID);
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
        assertThatIllegalArgumentException().isThrownBy(() -> getManagement().create(THING_ID, (JsonObject) null));
    }

    @Test
    public void testCreateThingWithInitialPolicyNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> getManagement().create(THING_ID, (Policy) null));
    }

    @Test
    public void testPutThingWithInitialPolicyJsonNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> getManagement().put(THING, (JsonObject) null));
    }

    @Test
    public void testPutThingWithInitialPolicyNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> getManagement().put(THING, (Policy) null));
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

        assertThatIllegalArgumentException()
                .isThrownBy(() -> getManagement().create(THING_ID_COPY_POLICY, POLICY_JSON_OBJECT, copyPolicy));
    }

    @Test
    public void testCreateThingWithAllOptionCopyPolicy() {
        final Option<ThingId> copyPolicyFromThing = Options.Modify.copyPolicyFromThing(THING_ID);
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);

        assertThatIllegalArgumentException()
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

        assertThatIllegalArgumentException()
                .isThrownBy(() -> getManagement().put(THING, POLICY_JSON_OBJECT, copyPolicy));
    }

    @Test
    public void testPutThingWithAllOptionCopyPolicy() {
        final Option<ThingId> copyPolicyFromThing = Options.Modify.copyPolicyFromThing(THING_ID);
        final Option<PolicyId> copyPolicy = Options.Modify.copyPolicy(POLICY_ID);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> getManagement().put(THING, copyPolicy, copyPolicyFromThing));
    }

    @Test
    public void testPutThingWithoutPolicy() {
        assertEventualCompletion(getManagement().put(THING)
                .thenAccept(result -> assertThat(result).isEmpty())
        );

        reply(ModifyThingResponse.modified(THING_ID, expectMsgClass(ModifyThing.class).getDittoHeaders()));
    }

    @Test
    public void createThingFailsWithConditionOption() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> getManagement().create(THING, Options.condition(CONDITION))
                        .toCompletableFuture()
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testPutThingWithConditionOption() {
        assertEventualCompletion(getManagement().put(THING, Options.condition(CONDITION)));
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.modified(THING_ID, command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void testUpdateThingWithConditionOption() {
        assertEventualCompletion(getManagement().update(THING, Options.condition(CONDITION)));
        final ModifyThing command = expectMsgClass(ModifyThing.class);
        reply(ModifyThingResponse.modified(THING_ID, command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void testMergeThingWithConditionOption() {
        assertEventualCompletion(getManagement().merge(THING_ID, THING, Options.condition(CONDITION)));
        final MergeThing command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID, JsonPointer.empty(), command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void testDeleteThingWithConditionOption() {
        assertEventualCompletion(getManagement().delete(THING_ID, Options.condition(CONDITION)));
        final DeleteThing command = expectMsgClass(DeleteThing.class);
        reply(DeleteThingResponse.of(THING_ID, command.getDittoHeaders()));
        assertThat(command.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void retrieveThingWithConditionOption() {
        assertEventualCompletion(getManagement().forId(THING_ID).retrieve(Options.condition(CONDITION)));
        final RetrieveThing retrieveThing = expectMsgClass(RetrieveThing.class);
        reply(RetrieveThingResponse.of(THING_ID,
                TestConstants.Thing.THING_V2.toJson(),
                retrieveThing.getDittoHeaders()));
        assertThat(retrieveThing.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

    @Test
    public void retrieveThingWithFieldSelectorAndConditionOption() {
        final JsonFieldSelector jsonFieldSelector = JsonFieldSelector.newInstance("attributes/manufacturer");

        assertEventualCompletion(getManagement()
                .forId(THING_ID)
                .retrieve(jsonFieldSelector, Options.condition(CONDITION)));
        final RetrieveThing retrieveThing = expectMsgClass(RetrieveThing.class);
        reply(RetrieveThingResponse.of(THING_ID,
                TestConstants.Thing.THING_V2,
                jsonFieldSelector,
                null,
                retrieveThing.getDittoHeaders()));
        assertThat(retrieveThing.getDittoHeaders()).containsEntry(DittoHeaderDefinition.CONDITION.getKey(), CONDITION);
    }

}
