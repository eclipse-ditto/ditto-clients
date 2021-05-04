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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.adapter.ProtocolAdapter;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.base.model.signals.commands.ErrorResponse;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionCreated;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionHasNextPage;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * Publisher of search results.
 *
 * @since 1.1.0
 */
public final class ThingSearchPublisher implements Publisher<SubscriptionHasNextPage> {

    private final ProtocolAdapter protocolAdapter;
    private final MessagingProvider messagingProvider;
    private final CompletionStage<SubscriptionCreated> subscriptionFuture;
    private final AtomicBoolean subscribed;

    private ThingSearchPublisher(final Signal<?> createSubscription,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider) {
        this.protocolAdapter = protocolAdapter;
        this.messagingProvider = messagingProvider;
        subscribed = new AtomicBoolean(false);
        subscriptionFuture = messagingProvider.sendAdaptable(protocolAdapter.toAdaptable(createSubscription))
                .thenApply(this::expectSubscriptionCreated);
    }

    /**
     * Create a single-use publisher for search results.
     *
     * @param createSubscription the command to create a stream of search results.
     * @param protocolAdapter the protocol adapter.
     * @param messagingProvider the messaging provider.
     * @return the single-use publisher.
     */
    public static Publisher<SubscriptionHasNextPage> of(final Signal<?> createSubscription,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider) {

        return new ThingSearchPublisher(createSubscription, protocolAdapter, messagingProvider);
    }

    // subscriber should not be null but still marked as nullable to it possible to trigger reactive-streams rule 1.9
    @Override
    public void subscribe(final Subscriber<? super SubscriptionHasNextPage> subscriber) {
        checkNotNull(subscriber, "subscriber");
        if (subscribed.getAndSet(true)) {
            // Subscribed more than once. Deliver dummy subscription per
            // https://github.com/reactive-streams/reactive-streams-jvm/issues/364
            subscriber.onSubscribe(FailedSubscription.of());
            subscriber.onError(new IllegalStateException("SearchPublisher supports at most 1 subscriber."));
        } else {
            subscriptionFuture.handle((subscriptionCreated, error) -> {
                if (subscriptionCreated != null) {
                    ThingSearchSubscription.start(subscriptionCreated, protocolAdapter, messagingProvider, subscriber);
                } else {
                    subscriber.onSubscribe(FailedSubscription.of());
                    subscriber.onError(error);
                }
                return null;
            });
        }
    }

    // Precondition: protocolAdapter != null
    private SubscriptionCreated expectSubscriptionCreated(final Adaptable answer) {
        final Signal<?> signal = protocolAdapter.fromAdaptable(answer);
        if (signal instanceof SubscriptionCreated) {
            return (SubscriptionCreated) signal;
        } else if (signal instanceof ErrorResponse) {
            throw ((ErrorResponse<?>) signal).getDittoRuntimeException();
        } else {
            throw new IllegalStateException("Expect SubscriptionCreated, got: " + signal);
        }
    }
}
