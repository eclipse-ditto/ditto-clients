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

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CancelSubscription;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.RequestFromSubscription;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionComplete;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionCreated;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionFailed;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionHasNext;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscription of search results of a query.
 *
 * @since 1.1.0
 */
public final class ThingSearchSubscription implements Subscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingSearchSubscription.class);

    private final String subscriptionId;
    private final ProtocolAdapter protocolAdapter;
    private final MessagingProvider messagingProvider;
    private final Subscriber<? super SubscriptionHasNext> subscriber;
    private final AtomicBoolean cancelled;
    private final AtomicReference<AdaptableBus.SubscriptionId> busSubscription;
    private final ExecutorService singleThreadedExecutorService;

    private ThingSearchSubscription(final String subscriptionId,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider,
            final Subscriber<? super SubscriptionHasNext> subscriber) {
        this.subscriptionId = subscriptionId;
        this.protocolAdapter = protocolAdapter;
        this.messagingProvider = messagingProvider;
        this.subscriber = subscriber;
        cancelled = new AtomicBoolean(false);
        busSubscription = new AtomicReference<>();

        // not shutdown to handle queued messages; will be shutdown by garbage collector
        singleThreadedExecutorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Start running a search subscription.
     *
     * @param event the event informing the existence of the subscription on the backend.
     * @param protocolAdapter the protocol adapter.
     * @param messagingProvider the messaging provider.
     */
    public static void start(final SubscriptionCreated event,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider,
            final Subscriber<? super SubscriptionHasNext> subscriber) {
        // start getting search events from adaptable bus
        final ThingSearchSubscription thingSearchSubscription =
                new ThingSearchSubscription(event.getSubscriptionId(), protocolAdapter, messagingProvider, subscriber);
        // notify subscriber of thingSearchSubscription
        subscriber.onSubscribe(thingSearchSubscription);
        // start forwarding messages to subscriber
        thingSearchSubscription.startForwarding();
    }

    // called by subscriber
    @Override
    public void request(final long n) {
        singleThreadedExecutorService.submit(() -> {
            if (n <= 0) {
                doCancel();
                subscriber.onError(new IllegalArgumentException("Expect positive demand, got: " + n));
            } else if (!cancelled.get()) {
                ensureBusSubscription();
                final Signal<?> requestSubscription =
                        RequestFromSubscription.of(subscriptionId, n, DittoHeaders.newBuilder()
                                .randomCorrelationId()
                                .build());
                messagingProvider.emitAdaptable(protocolAdapter.toAdaptable(requestSubscription));
            }
        });
    }

    // called by subscriber
    @Override
    public void cancel() {
        singleThreadedExecutorService.submit(this::doCancel);
    }

    private void doCancel() {
        if (!cancelled.getAndSet(true)) {
            cancelBusSubscription();
            sendCancelSubscription();
        }
    }

    // called by bus
    private void onTimeout(final Throwable timeoutError) {
        singleThreadedExecutorService.submit(() -> {
            if (!cancelled.getAndSet(true)) {
                // bus subscription already cancelled
                // trust back-end to free resources on its own
                subscriber.onError(timeoutError);
            }
        });
    }

    // called by bus
    private void onNext(final Adaptable adaptable) {
        singleThreadedExecutorService.submit(() -> {
            LOGGER.trace("Received from bus: <{}>", adaptable);
            handleAdaptable(adaptable);
        });
    }

    // called by bus
    private boolean isTermination(final Adaptable adaptable) {
        return adaptable.getTopicPath().getSearchAction().filter(this::isTerminationAction).isPresent();
    }

    private void sendCancelSubscription() {
        final Signal<?> cancelSubscription = CancelSubscription.of(subscriptionId, DittoHeaders.empty());
        messagingProvider.emitAdaptable(protocolAdapter.toAdaptable(cancelSubscription));
    }

    private boolean isTerminationAction(final TopicPath.SearchAction searchAction) {
        switch (searchAction) {
            case COMPLETE:
            case FAILED:
                return true;
            default:
                return false;
        }
    }

    private void handleAdaptable(final Adaptable adaptable) {
        final Signal<?> signal = protocolAdapter.fromAdaptable(adaptable);
        LOGGER.trace("Notifying subscriber of: <{}>", signal);
        if (signal instanceof SubscriptionHasNext) {
            subscriber.onNext((SubscriptionHasNext) signal);
        } else if (signal instanceof SubscriptionComplete) {
            cancelDueToUpstreamTermination();
            subscriber.onComplete();
        } else if (signal instanceof SubscriptionFailed) {
            cancelDueToUpstreamTermination();
            subscriber.onError(((SubscriptionFailed) signal).getError());
        } else {
            doCancel();
            subscriber.onError(new ClassCastException("Expect SubscriptionEvent, got " + signal));
        }
    }

    private void cancelDueToUpstreamTermination() {
        cancelled.set(true);
        cancelBusSubscription();
        // upstream already considers itself cancelled
    }

    private void cancelBusSubscription() {
        final AdaptableBus.SubscriptionId busSubId = busSubscription.get();
        if (busSubId != null) {
            messagingProvider.getAdaptableBus().unsubscribe(busSubId);
        }
    }

    private void startForwarding() {
        LOGGER.trace("Returned from subscriber.onSubscribe()");
        ensureBusSubscription();
    }

    private void ensureBusSubscription() {
        synchronized (busSubscription) {
            if (busSubscription.get() == null) {
                final AdaptableBus bus = messagingProvider.getAdaptableBus();
                final Duration timeout = messagingProvider.getMessagingConfiguration().getTimeout();
                final Classification tag = Classification.forThingsSearch(subscriptionId);
                final AdaptableBus.SubscriptionId busSubscriptionId =
                        bus.subscribeForAdaptableWithTimeout(tag, timeout, this::onNext, this::isTermination,
                                this::onTimeout);
                busSubscription.set(busSubscriptionId);
            }
        }
    }
}
