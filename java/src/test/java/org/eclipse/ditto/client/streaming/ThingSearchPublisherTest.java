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
import java.util.stream.Collectors;

import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CreateSubscription;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.RequestSubscription;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionComplete;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionCreated;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionHasNext;
import org.junit.Test;
import org.reactivestreams.Publisher;

/**
 * Run a simple interaction test for {@link ThingSearchPublisher}.
 */
public final class ThingSearchPublisherTest extends AbstractDittoClientTest {

    @Test
    public void run() {
        final Publisher<SubscriptionHasNext> underTest =
                ThingSearchPublisher.of(CreateSubscription.of(DittoHeaders.empty()), PROTOCOL_ADAPTER, messaging);
        final SpliteratorSubscriber<SubscriptionHasNext> subscriber = SpliteratorSubscriber.of();
        underTest.subscribe(subscriber);
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = "subscription1234";
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        final RequestSubscription requestSubscription = expectMsgClass(RequestSubscription.class);
        final List<SubscriptionHasNext> expectedResult = new ArrayList<>();
        for (int i = 0; i < requestSubscription.getDemand(); ++i) {
            final SubscriptionHasNext hasNext =
                    SubscriptionHasNext.of(subscriptionId, JsonArray.of(JsonObject.empty()),
                            requestSubscription.getDittoHeaders());
            reply(hasNext);
            expectedResult.add(hasNext);
        }
        final RequestSubscription futileRequest = expectMsgClass(RequestSubscription.class);
        reply(SubscriptionComplete.of(subscriptionId, futileRequest.getDittoHeaders()));
        assertThat(subscriber.asStream().collect(Collectors.toList())).isEqualTo(expectedResult);
    }
}
