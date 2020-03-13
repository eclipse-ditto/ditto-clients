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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classifier;
import org.eclipse.ditto.client.internal.bus.Classifiers;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CancelSubscription;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.RequestSubscription;
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
 */
public final class ThingSearchSubscription implements Subscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingSearchSubscription.class);

    private final String subscriptionId;
    private final ProtocolAdapter protocolAdapter;
    private final MessagingProvider messagingProvider;
    private final Subscriber<? super SubscriptionHasNext> subscriber;
    private final AtomicBoolean cancelled;
    private final AtomicBoolean started;
    private final ConcurrentLinkedQueue<Adaptable> stash;
    private final AtomicReference<Throwable> timeoutErrorStash;
    private final AdaptableBus.SubscriptionId busSubscription;

    private ThingSearchSubscription(final String subscriptionId,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider,
            final Subscriber<? super SubscriptionHasNext> subscriber) {
        this.subscriptionId = subscriptionId;
        this.protocolAdapter = protocolAdapter;
        this.messagingProvider = messagingProvider;
        this.subscriber = subscriber;
        cancelled = new AtomicBoolean(false);
        started = new AtomicBoolean(false);
        stash = new ConcurrentLinkedQueue<>();
        timeoutErrorStash = new AtomicReference<>();

        final AdaptableBus bus = messagingProvider.getAdaptableBus();
        final Duration timeout = messagingProvider.getMessagingConfiguration().getTimeout();
        final Classifier.Classification tag = Classifiers.forThingsSearch(subscriptionId);
        this.busSubscription =
                bus.subscribeForAdaptableWithTimeout(tag, timeout, this::onNext, this::isTermination, this::onTimeout);
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
        if (n <= 0) {
            cancel();
            subscriber.onError(new IllegalArgumentException("Expect positive demand, got: " + n));
        } else if (!cancelled.get()) {
            final Signal<?> requestSubscription = RequestSubscription.of(subscriptionId, n, DittoHeaders.empty());
            messagingProvider.emitAdaptable(protocolAdapter.toAdaptable(requestSubscription));
        }
    }

    // called by subscriber and self
    @Override
    public void cancel() {
        if (!cancelled.getAndSet(true)) {
            sendCancelSubscription();
        }
        cancelBusSubscriptionAndClearStash();
    }

    private void sendCancelSubscription() {
        final Signal<?> cancelSubscription = CancelSubscription.of(subscriptionId, DittoHeaders.empty());
        messagingProvider.emitAdaptable(protocolAdapter.toAdaptable(cancelSubscription));
    }

    // called by bus and self
    private void onTimeout(final Throwable timeoutError) {
        if (!cancelled.getAndSet(true)) {
            // send cancellation to back-end just in case
            sendCancelSubscription();
            // bus subscription is already cancelled; call this method anyway so that nowhere else clears the stash
            cancelBusSubscriptionAndClearStash();
            if (started.get()) {
                // notify subscriber of timeout unless already cancelled
                subscriber.onError(timeoutError);
            } else {
                // stash timeout error in case it arrives before subscriber.onSubscribe() returns
                timeoutErrorStash.set(timeoutError);
            }
        }
    }

    // called by bus
    private void onNext(final Adaptable adaptable) {
        stash.add(adaptable);
        handleStashedMessagesIfStarted();
    }

    // called by bus
    private boolean isTermination(final Adaptable adaptable) {
        return adaptable.getTopicPath().getSearchAction().filter(this::isTerminationAction).isPresent();
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

    private void handleStashedAdaptable(final Adaptable adaptable) {
        final Signal<?> signal = protocolAdapter.fromAdaptable(adaptable);
        if (signal instanceof SubscriptionHasNext) {
            LOGGER.trace("Notifying subscriber of: <{}>", signal);
            subscriber.onNext((SubscriptionHasNext) signal);
        } else if (signal instanceof SubscriptionComplete) {
            LOGGER.trace("Notifying subscriber of: <{}>", signal);
            cancelBusSubscriptionAndClearStash();
            subscriber.onComplete();
        } else if (signal instanceof SubscriptionFailed) {
            LOGGER.trace("Notifying subscriber of: <{}>", signal);
            cancelBusSubscriptionAndClearStash();
            subscriber.onError(((SubscriptionFailed) signal).getError());
        } else {
            cancel();
            subscriber.onError(new ClassCastException("Expect SubscriptionEvent, got " + signal));
        }
    }

    private void cancelBusSubscriptionAndClearStash() {
        messagingProvider.getAdaptableBus().unsubscribe(busSubscription);
        stash.clear();
    }

    private void handleStashedMessagesIfStarted() {
        final boolean isStarted = started.get();
        if (isStarted && !cancelled.get()) {
            handleStashedAdaptables();
        } else if (isStarted) {
            handleStashedTimeoutErrorIfAny();
        }
    }

    private void handleStashedAdaptables() {
        Adaptable nextMessage;
        boolean hasNextMessage;
        do {
            nextMessage = stash.poll();
            hasNextMessage = nextMessage != null;
            if (hasNextMessage) {
                handleStashedAdaptable(nextMessage);
            }
        } while (hasNextMessage && !cancelled.get());
    }

    private void handleStashedTimeoutErrorIfAny() {
        final Throwable stashedTimeoutError = timeoutErrorStash.getAndSet(null);
        if (stashedTimeoutError != null) {
            onTimeout(stashedTimeoutError);
        }
    }

    private void startForwarding() {
        LOGGER.trace("Returned from subscriber.onSubscribe(), start forwarding messages");
        started.set(true);
        handleStashedMessagesIfStarted();
    }
}
