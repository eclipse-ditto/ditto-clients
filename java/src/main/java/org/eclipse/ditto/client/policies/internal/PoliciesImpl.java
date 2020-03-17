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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.internal.SendTerminator;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.policies.Policies;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.commands.policies.modify.CreatePolicy;
import org.eclipse.ditto.signals.commands.policies.modify.DeletePolicy;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicy;

/**
 * Default implementation for {@link Policies}.
 *
 * @since 1.1.0
 */
public final class PoliciesImpl implements Policies {

    private final MessagingProvider messagingProvider;
    private final ResponseForwarder responseForwarder;
    private final OutgoingMessageFactory outgoingMessageFactory;
    private final PointerBus bus;

    public PoliciesImpl(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        this.messagingProvider = messagingProvider;
        this.responseForwarder = responseForwarder;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.bus = bus;
    }

    /**
     * Creates a new {@code PoliciesImpl} instance.
     *
     * @param messagingProvider implementation of underlying messaging provider.
     * @param responseForwarder response forwarder.
     * @param outgoingMessageFactory a factory for messages.
     * @param bus the bus for message routing.
     * @return the new {@code PoliciesImpl} instance.
     */
    public static PoliciesImpl newInstance(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        return new PoliciesImpl(messagingProvider, responseForwarder, outgoingMessageFactory, bus);
    }

    @Override
    public CompletableFuture<Policy> create(final Policy policy, final Option<?>... options) {
        argumentNotNull(policy);
        assertThatPolicyHasId(policy);

        final CreatePolicy command = outgoingMessageFactory.createPolicy(policy, options);

        return new SendTerminator<Policy>(messagingProvider, responseForwarder, command)
                .applyModifyPolicy(response -> {
                    if (response != null) {
                        return PoliciesModelFactory.newPolicy(
                                response.getEntity(response.getImplementedSchemaVersion())
                                        .orElse(JsonFactory.nullObject())
                                        .asObject());
                    } else {
                        return null;
                    }
                });
    }

    @Override
    public CompletableFuture<Policy> create(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Policy policy = PoliciesModelFactory.newPolicy(jsonObject);
        return create(policy, options);
    }

    @Override
    public CompletableFuture<Optional<Policy>> put(final Policy policy, final Option<?>... options) {
        argumentNotNull(policy);
        assertThatPolicyHasId(policy);

        return new SendTerminator<Optional<Policy>>(messagingProvider, responseForwarder,
                outgoingMessageFactory.putPolicy(policy, options))
                .applyModifyPolicy(response -> {
                    if (response != null) {
                        return response.getEntity(response.getImplementedSchemaVersion())
                                .map(JsonValue::asObject)
                                .map(PoliciesModelFactory::newPolicy);
                    } else {
                        throw new IllegalStateException("Response is always expected!");
                    }
                });
    }

    @Override
    public CompletableFuture<Optional<Policy>> put(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Policy policy = PoliciesModelFactory.newPolicy(jsonObject);
        return put(policy, options);
    }

    @Override
    public CompletableFuture<Void> update(final Policy policy, final Option<?>... options) {
        argumentNotNull(policy);
        assertThatPolicyHasId(policy);

        return new SendTerminator<Void>(messagingProvider, responseForwarder,
                outgoingMessageFactory.updatePolicy(policy, options)).applyVoid();
    }

    @Override
    public CompletableFuture<Void> update(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Policy policy = PoliciesModelFactory.newPolicy(jsonObject);
        return update(policy, options);
    }

    @Override
    public CompletableFuture<Void> delete(final PolicyId policyId, final Option<?>... options) {
        argumentNotNull(policyId);

        final DeletePolicy command = outgoingMessageFactory.deletePolicy(policyId, options);
        return new SendTerminator<Void>(messagingProvider, responseForwarder, command).applyVoid();
    }

    @Override
    public CompletableFuture<Policy> retrieve(PolicyId policyId) {
        final RetrievePolicy command = outgoingMessageFactory.retrievePolicy(policyId);
        return new SendTerminator<Policy>(messagingProvider, responseForwarder, command)
                .applyViewWithPolicyResponse(response -> {
                    if (response != null) {
                        return PoliciesModelFactory.newPolicy(
                                response.getEntity(response.getImplementedSchemaVersion()).asObject());
                    } else {
                        return null;
                    }
                });
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

}
