/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.streaming;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CancelSubscription;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.RequestSubscription;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionCreated;
import org.reactivestreams.Subscription;

/**
 * TODO
 */
public final class ThingSearchSubscription implements Subscription {

    private final String subscriptionId;
    private final ProtocolAdapter protocolAdapter;
    private final MessagingProvider messagingProvider;
    private final AtomicBoolean cancelled;

    private ThingSearchSubscription(final String subscriptionId,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider) {
        this.subscriptionId = subscriptionId;
        this.protocolAdapter = protocolAdapter;
        this.messagingProvider = messagingProvider;
        cancelled = new AtomicBoolean(false);
    }

    // TODO: javadoc
    public static Subscription of(final SubscriptionCreated event,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider) {
        // create new object each time to trigger rule 2.5
        return new ThingSearchSubscription(event.getSubscriptionId(), protocolAdapter, messagingProvider);
    }

    @Override
    public void request(final long n) {
        if (!cancelled.get()) {
            final Signal<?> requestSubscription = RequestSubscription.of(subscriptionId, n, DittoHeaders.empty());
            messagingProvider.emitAdaptable(protocolAdapter.toAdaptable(requestSubscription));
        }
    }

    @Override
    public void cancel() {
        if (!cancelled.getAndSet(true)) {
            final Signal<?> cancelSubscription = CancelSubscription.of(subscriptionId, DittoHeaders.empty());
            messagingProvider.emitAdaptable(protocolAdapter.toAdaptable(cancelSubscription));
        }
    }
}
