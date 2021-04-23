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

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.client.messaging.internal.MockMessagingProvider;
import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.protocol.adapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocol.HeaderTranslator;
import org.eclipse.ditto.protocol.adapter.ProtocolAdapter;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.thingsearch.model.signals.commands.ThingSearchCommand;
import org.eclipse.ditto.thingsearch.model.signals.commands.exceptions.InvalidOptionException;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.CancelSubscription;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.CreateSubscription;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.RequestFromSubscription;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionComplete;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionCreated;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionFailed;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionHasNextPage;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;

/**
 * Verify reactive-streams compatibility of {@link ThingSearchPublisher}.
 */
public final class ThingSearchPublisherVerificationTest extends PublisherVerification<SubscriptionHasNextPage> {

    private static final ProtocolAdapter PROTOCOL_ADAPTER = DittoProtocolAdapter.of(HeaderTranslator.empty());

    public ThingSearchPublisherVerificationTest() {
        // use new TestEnvironment(true) to debug
        super(new TestEnvironment(false), 1000L);
    }

    @Override
    public Publisher<SubscriptionHasNextPage> createPublisher(final long l) {
        final CreateSubscription createSubscription = CreateSubscription.of(DittoHeaders.empty());
        final MockMessagingProvider messaging = new MockMessagingProvider();
        final Publisher<SubscriptionHasNextPage> underTest =
                ThingSearchPublisher.of(createSubscription, PROTOCOL_ADAPTER, messaging);
        mockSearchBackEnd(messaging, l);
        return underTest;
    }

    @Override
    public Publisher<SubscriptionHasNextPage> createFailedPublisher() {
        // ThingSearchPublisher cannot fail before the back-end signals failure,
        // which only happens after the first Subscription.request.
        return null;
    }

    private static void mockSearchBackEnd(final MockMessagingProvider messaging, final long numberOfElements) {
        Executors.newSingleThreadExecutor().submit(() -> {
            // wait for CreateSubscription
            final CreateSubscription createSubscription = awaitUntilSent(messaging, CreateSubscription.class);
            // start send elements
            final String subscriptionId = UUID.randomUUID().toString();
            reply(messaging, SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
            if (numberOfElements < 0) {
                reply(messaging, SubscriptionFailed.of(subscriptionId, InvalidOptionException.newBuilder().build(),
                        createSubscription.getDittoHeaders()));
            } else {
                for (long i = 0; i < numberOfElements; ) {
                    final ThingSearchCommand<?> command = awaitUntilSent(messaging, ThingSearchCommand.class);
                    if (command instanceof CancelSubscription) {
                        break;
                    } else if (command instanceof RequestFromSubscription) {
                        final RequestFromSubscription requestFromSubscription = (RequestFromSubscription) command;
                        long after = Math.min(i + requestFromSubscription.getDemand(), numberOfElements);
                        // defend against overflow
                        if (after <= 0) {
                            after = numberOfElements;
                        }
                        final long actualDemand = after - i;
                        if (actualDemand <= 0L) {
                            reply(messaging, SubscriptionFailed.of(subscriptionId,
                                    InvalidOptionException.newBuilder().build(),
                                    command.getDittoHeaders()));
                            break;
                        } else {
                            for (long j = 0; j < actualDemand; ++j) {
                                reply(messaging, subscriptionHasNext(requestFromSubscription, i, j));
                            }
                            i = after;
                            if (i >= numberOfElements) {
                                reply(messaging, SubscriptionComplete.of(subscriptionId, command.getDittoHeaders()));
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        });
    }

    private static SubscriptionHasNextPage subscriptionHasNext(final RequestFromSubscription request,
            final long i,
            final long j) {
        return SubscriptionHasNextPage.of(request.getSubscriptionId(),
                JsonArray.of(JsonObject.newBuilder()
                        .set("thingId", "x:" + (i + j))
                        .build()),
                request.getDittoHeaders()
        );
    }

    private static <T> T awaitUntilSent(final MockMessagingProvider messaging, final Class<T> clazz) {
        T result;
        while (true) {
            try {
                result = expectMsgClass(messaging, clazz);
                break;
            } catch (final AssertionError e) {
                if (!(e.getCause() instanceof TimeoutException)) {
                    throw e;
                }
            }
        }
        return result;
    }

    private static <T> T expectMsgClass(final MockMessagingProvider messaging, final Class<T> clazz) {
        final String nextMessage = messaging.expectEmitted();
        final Signal<?> signal = PROTOCOL_ADAPTER.fromAdaptable(
                ProtocolFactory.jsonifiableAdaptableFromJson(JsonObject.of(nextMessage)));
        if (clazz.isInstance(signal)) {
            return clazz.cast(signal);
        } else {
            throw new AssertionError("Expect " + clazz + ", got " + signal);
        }
    }

    private static void reply(final MockMessagingProvider mock, final Signal<?> signal) {
        mock.getAdaptableBus().publish(toAdaptableJsonString(signal));
    }

    private static String toAdaptableJsonString(final Signal<?> signal) {
        return ProtocolFactory.wrapAsJsonifiableAdaptable(PROTOCOL_ADAPTER.toAdaptable(signal)).toJsonString();
    }
}
