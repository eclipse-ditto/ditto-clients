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
package org.eclipse.ditto.client.management.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.internal.SendTerminator;
import org.eclipse.ditto.client.management.PolicyHandle;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;

public class PolicyHandleImpl implements PolicyHandle {

    private OutgoingMessageFactory outgoingMessageFactory;
    private PolicyId policyId;
    private MessagingProvider messagingProvider;
    private ResponseForwarder responseForwarder;

    public PolicyHandleImpl(
            final OutgoingMessageFactory outgoingMessageFactory,
            final PolicyId policyId,
            final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder) {
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.policyId = policyId;
        this.messagingProvider = messagingProvider;
        this.responseForwarder = responseForwarder;
    }

    @Override
    public CompletableFuture<Void> delete(final Option<?>... options) {
        final PolicyCommand command = outgoingMessageFactory.deletePolicy(policyId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, command).applyVoid();
    }

    @Override
    public CompletableFuture<Policy> retrieve() {
        final PolicyCommand command = outgoingMessageFactory.retrievePolicy(policyId);
        return new SendTerminator<Policy>(messagingProvider, responseForwarder, command).applyView(pvr ->
        {
            if (pvr != null) {
                return PoliciesModelFactory.newPolicy(pvr.getEntity(pvr.getImplementedSchemaVersion()).asObject());
            } else {
                return null;
            }
        });
    }

}
