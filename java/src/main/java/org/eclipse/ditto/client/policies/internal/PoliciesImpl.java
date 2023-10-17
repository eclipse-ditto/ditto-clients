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
package org.eclipse.ditto.client.policies.internal;

import static org.eclipse.ditto.base.model.common.ConditionChecker.argumentNotNull;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nonnull;

import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.client.internal.AbstractHandle;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.policies.Policies;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.policies.model.PoliciesModelFactory;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.policies.model.signals.commands.modify.CreatePolicy;
import org.eclipse.ditto.policies.model.signals.commands.modify.CreatePolicyResponse;
import org.eclipse.ditto.policies.model.signals.commands.modify.DeletePolicy;
import org.eclipse.ditto.policies.model.signals.commands.modify.DeletePolicyResponse;
import org.eclipse.ditto.policies.model.signals.commands.modify.ModifyPolicyResponse;
import org.eclipse.ditto.policies.model.signals.commands.modify.PolicyModifyCommandResponse;
import org.eclipse.ditto.policies.model.signals.commands.query.RetrievePolicy;
import org.eclipse.ditto.policies.model.signals.commands.query.RetrievePolicyResponse;
import org.eclipse.ditto.protocol.TopicPath;

/**
 * Default implementation for {@link Policies}.
 *
 * @since 1.1.0
 */
public final class PoliciesImpl extends AbstractHandle implements Policies {

    private final OutgoingMessageFactory outgoingMessageFactory;
    private final PointerBus bus;

    public PoliciesImpl(final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        super(messagingProvider, TopicPath.Channel.NONE);
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.bus = bus;
    }

    /**
     * Creates a new {@code PoliciesImpl} instance.
     *
     * @param messagingProvider implementation of underlying messaging provider.
     * @param outgoingMessageFactory a factory for messages.
     * @param bus the bus for message routing.
     * @return the new {@code PoliciesImpl} instance.
     */
    public static PoliciesImpl newInstance(final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        return new PoliciesImpl(messagingProvider, outgoingMessageFactory, bus);
    }

    @Override
    public CompletionStage<Policy> create(final Policy policy, final Option<?>... options) {
        argumentNotNull(policy);
        assertThatPolicyHasId(policy);

        final CreatePolicy command = outgoingMessageFactory.createPolicy(policy, options);
        return askPolicyCommand(command, CreatePolicyResponse.class,
                response -> response.getPolicyCreated().orElse(null));
    }

    @Override
    public CompletionStage<Policy> create(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Policy policy = PoliciesModelFactory.newPolicy(jsonObject);
        return create(policy, options);
    }

    @Override
    public CompletionStage<Optional<Policy>> put(final Policy policy, final Option<?>... options) {
        argumentNotNull(policy);
        assertThatPolicyHasId(policy);

        return askPolicyCommand(outgoingMessageFactory.putPolicy(policy, options),
                // response could be either CreatePolicyResponse or ModifyPolicyResponse.
                PolicyModifyCommandResponse.class,
                response -> {
                    final Optional<JsonValue> entity = response.getEntity(response.getImplementedSchemaVersion());
                    return entity
                            .map(JsonValue::asObject)
                            .map(PoliciesModelFactory::newPolicy);
                }
        );
    }

    @Override
    public CompletionStage<Optional<Policy>> put(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Policy policy = PoliciesModelFactory.newPolicy(jsonObject);
        return put(policy, options);
    }

    @Override
    public CompletionStage<Void> update(final Policy policy, final Option<?>... options) {
        argumentNotNull(policy);
        assertThatPolicyHasId(policy);

        return askPolicyCommand(outgoingMessageFactory.updatePolicy(policy, options), ModifyPolicyResponse.class,
                this::toVoid);
    }

    @Override
    public CompletionStage<Void> update(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Policy policy = PoliciesModelFactory.newPolicy(jsonObject);
        return update(policy, options);
    }

    @Override
    public CompletionStage<Void> delete(final PolicyId policyId, final Option<?>... options) {
        argumentNotNull(policyId);

        final DeletePolicy command = outgoingMessageFactory.deletePolicy(policyId, options);
        return askPolicyCommand(command, DeletePolicyResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Policy> retrieve(final PolicyId policyId) {
        final RetrievePolicy command = outgoingMessageFactory.retrievePolicy(policyId);
        return askPolicyCommand(command, RetrievePolicyResponse.class, RetrievePolicyResponse::getPolicy);
    }

    @Override
    public CompletionStage<Policy> retrieve(final PolicyId policyId, final Option<?>... options) {
        final RetrievePolicy command = outgoingMessageFactory.retrievePolicy(policyId, options);
        return askPolicyCommand(command, RetrievePolicyResponse.class, RetrievePolicyResponse::getPolicy);
    }

    @Override
    public CompletionStage<Policy> retrieve(final PolicyId policyId, final JsonFieldSelector fieldSelector) {
        final RetrievePolicy command = outgoingMessageFactory.retrievePolicy(policyId, fieldSelector);
        return askPolicyCommand(command, RetrievePolicyResponse.class, RetrievePolicyResponse::getPolicy);
    }

    @Override
    public CompletionStage<Policy> retrieve(final PolicyId policyId, final JsonFieldSelector fieldSelector,
            final Option<?>... options) {

        final RetrievePolicy command = outgoingMessageFactory.retrievePolicy(policyId, fieldSelector, options);
        return askPolicyCommand(command, RetrievePolicyResponse.class, RetrievePolicyResponse::getPolicy);
    }

    private static void assertThatPolicyHasId(final Policy policy) {
        if (!policy.getEntityId().isPresent()) {
            final String msgPattern = "Mandatory field <{0}> is missing!";
            throw new IllegalArgumentException(MessageFormat.format(msgPattern, Policy.JsonFields.ID.getPointer()));
        }
    }

    /**
     * Returns the MessagingProvider this Policy uses.
     *
     * @return the MessagingProvider this Policy uses.
     */
    public MessagingProvider getMessagingProvider() {
        return messagingProvider;
    }

    /**
     * Returns the Bus this Policy uses.
     *
     * @return the Bus this Policy uses.
     */
    public PointerBus getBus() {
        return bus;
    }

    @Override
    @Nonnull
    protected AcknowledgementLabel getThingResponseAcknowledgementLabel() {
        return DittoAcknowledgementLabel.TWIN_PERSISTED;
    }

}
