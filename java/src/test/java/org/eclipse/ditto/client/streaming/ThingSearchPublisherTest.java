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

import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.CreateSubscription;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.RequestFromSubscription;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionComplete;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionCreated;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionHasNextPage;
import org.junit.Test;
import org.reactivestreams.Publisher;

/**
 * Run a simple interaction test for {@link ThingSearchPublisher}.
 */
public final class ThingSearchPublisherTest extends AbstractDittoClientTest {

    @Test
    public void run() throws Exception {
        final Publisher<SubscriptionHasNextPage> underTest =
                ThingSearchPublisher.of(CreateSubscription.of(DittoHeaders.empty()), PROTOCOL_ADAPTER, messaging);
        final SpliteratorSubscriber<SubscriptionHasNextPage> subscriber = SpliteratorSubscriber.of();
        final CompletableFuture<List<SubscriptionHasNextPage>> subscriberFuture =
                CompletableFuture.supplyAsync(() -> subscriber.asStream().collect(Collectors.toList()),
                        Executors.newSingleThreadExecutor());
        underTest.subscribe(subscriber);
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = "subscription1234";
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        final RequestFromSubscription requestFromSubscription = expectMsgClass(RequestFromSubscription.class);
        final List<SubscriptionHasNextPage> expectedResult = new ArrayList<>();
        for (int i = 0; i < requestFromSubscription.getDemand(); ++i) {
            final SubscriptionHasNextPage hasNext =
                    SubscriptionHasNextPage.of(subscriptionId, JsonArray.of(JsonObject.empty()),
                            requestFromSubscription.getDittoHeaders());
            reply(hasNext);
            expectedResult.add(hasNext);
        }
        final RequestFromSubscription futileRequest = expectMsgClass(RequestFromSubscription.class);
        reply(SubscriptionComplete.of(subscriptionId, futileRequest.getDittoHeaders()));
        subscriberFuture.get(1L, TimeUnit.SECONDS);
        assertThat(subscriberFuture).isCompletedWithValue(expectedResult);
    }
}
