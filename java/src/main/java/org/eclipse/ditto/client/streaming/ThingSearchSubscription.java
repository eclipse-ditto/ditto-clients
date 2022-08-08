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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.protocol.adapter.ProtocolAdapter;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.CancelSubscription;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.RequestFromSubscription;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionComplete;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionCreated;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionFailed;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionHasNextPage;
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
    private final Subscriber<? super SubscriptionHasNextPage> subscriber;
    private final AtomicBoolean cancelled;
    private final AtomicReference<AdaptableBus.SubscriptionId> busSubscription;
    @Nullable private volatile ExecutorService singleThreadedExecutor;

    private ThingSearchSubscription(final String subscriptionId,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider,
            final Subscriber<? super SubscriptionHasNextPage> subscriber) {
        this.subscriptionId = subscriptionId;
        this.protocolAdapter = protocolAdapter;
        this.messagingProvider = messagingProvider;
        this.subscriber = subscriber;
        cancelled = new AtomicBoolean(false);
        busSubscription = new AtomicReference<>();
    }

    /**
     * Start running a search subscription.
     *
     * @param event the event informing the existence of the subscription on the backend.
     * @param protocolAdapter the protocol adapter.
     * @param messagingProvider the messaging provider.
     * @param subscriber subscriber for search results.
     */
    public static void start(final SubscriptionCreated event,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider,
            final Subscriber<? super SubscriptionHasNextPage> subscriber) {
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
        singleThreaded(() -> {
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
        singleThreaded(this::doCancel);
    }

    /**
     * Set the single-threaded executor of this subscription.
     * Subscription methods run in the executor in order to maintain element order.
     * Creating the executor within this class is not possible because the garbage collector may not stop the executor.
     *
     * @param singleThreadedExecutor The single-threaded executor.
     */
    public void setSingleThreadedExecutor(final ExecutorService singleThreadedExecutor) {
        // TODO: After upgrading to Java 9, consider using the Cleaner interface instead.
        this.singleThreadedExecutor = singleThreadedExecutor;
    }

    private void singleThreaded(final Runnable runnable) {
        if (singleThreadedExecutor != null) {
            Objects.requireNonNull(singleThreadedExecutor).submit(runnable);
        } else {
            runnable.run();
        }
    }

    private void doCancel() {
        if (!cancelled.getAndSet(true)) {
            cancelBusSubscription();
            sendCancelSubscription();
        }
    }

    // called by bus
    private void onTimeout(final Throwable timeoutError) {
        singleThreaded(() -> {
            if (!cancelled.getAndSet(true)) {
                // bus subscription already cancelled
                // trust back-end to free resources on its own
                subscriber.onError(timeoutError);
            }
        });
    }

    // called by bus
    private void onNext(final Adaptable adaptable) {
        singleThreaded(() -> {
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
        if (signal instanceof SubscriptionHasNextPage) {
            subscriber.onNext((SubscriptionHasNextPage) signal);
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
