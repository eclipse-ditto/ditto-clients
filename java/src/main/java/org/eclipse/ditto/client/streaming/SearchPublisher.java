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

import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionCreated;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionHasNext;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * TODO
 */
public final class SearchPublisher implements Publisher<SubscriptionHasNext> {

    private final ProtocolAdapter protocolAdapter;
    private final MessagingProvider messagingProvider;
    private final CompletionStage<SubscriptionCreated> subscriptionFuture;
    private final AtomicBoolean subscribed;

    private SearchPublisher(final MessagingProvider messagingProvider, final Signal<?> createSubscription,
            final ProtocolAdapter protocolAdapter) {
        this.messagingProvider = messagingProvider;
        this.protocolAdapter = protocolAdapter;
        subscribed = new AtomicBoolean(false);
        subscriptionFuture = messagingProvider.sendAdaptable(protocolAdapter.toAdaptable(createSubscription))
                .thenApply(answer -> {
                    final Signal<?> signal = protocolAdapter.fromAdaptable(answer);
                    if (signal instanceof SubscriptionCreated) {
                        return (SubscriptionCreated) signal;
                    } else {
                        throw new IllegalStateException("Expect SubscriptionCreated, got: " + signal);
                    }
                });
    }

    @Override
    public void subscribe(final Subscriber<? super SubscriptionHasNext> s) {
        if (subscribed.getAndSet(true)) {
            // Subscribed more than once. Deliver dummy subscription per
            // https://github.com/reactive-streams/reactive-streams-jvm/issues/364
            s.onSubscribe(FailedSubscription.of());
            s.onError(new IllegalStateException("SearchPublisher supports at most 1 subscriber."));
        } else {
            subscriptionFuture.handle((subscriptionCreated, error) -> {
                if (subscriptionCreated != null) {
                    // TODO: implement.
                    throw new IllegalStateException("TODO: Not implemented yet");
                } else {
                    s.onSubscribe(FailedSubscription.of());
                    s.onError(error);
                }
                return null;
            });
        }
    }
}
