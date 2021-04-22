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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CancelSubscription;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CreateSubscription;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.RequestFromSubscription;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionCreated;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Test interactions only possible via the publisher interface.
 */
public final class DittoClientTwinSearchPublisherTest extends AbstractDittoClientTest {

    @Test
    public void cancellation() throws Exception {
        final Publisher<List<Thing>> underTest = client.twin().search().publisher(s -> {});
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = "my-cancelled-subscription";
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        final TestSubscriber<List<Thing>> testSubscriber = new TestSubscriber<>();
        underTest.subscribe(testSubscriber);
        testSubscriber.subscriptionLatch.await(TIMEOUT, TIME_UNIT);
        final Subscription subscription = checkNotNull(testSubscriber.subscriptions.peek());
        subscription.request(99L);
        expectMsgClass(RequestFromSubscription.class);
        subscription.cancel();
        expectMsgClass(CancelSubscription.class);
    }

    private static final class TestSubscriber<T> implements Subscriber<T> {

        private final Queue<Subscription> subscriptions = new ConcurrentLinkedQueue<>();
        private final Queue<T> elements = new ConcurrentLinkedQueue<>();
        private final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();
        private final AtomicInteger completeCounter = new AtomicInteger(0);
        private final CountDownLatch subscriptionLatch = new CountDownLatch(1);

        @Override
        public void onSubscribe(final Subscription s) {
            subscriptions.add(s);
            subscriptionLatch.countDown();
        }

        @Override
        public void onNext(final T t) {
            elements.add(t);
        }

        @Override
        public void onError(final Throwable t) {
            errors.add(t);
        }

        @Override
        public void onComplete() {
            completeCounter.incrementAndGet();
        }
    }

}
