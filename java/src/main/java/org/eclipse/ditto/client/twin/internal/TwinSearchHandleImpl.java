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
package org.eclipse.ditto.client.twin.internal;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.ditto.client.internal.AbstractHandle;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.streaming.SpliteratorSubscriber;
import org.eclipse.ditto.client.streaming.ThingSearchPublisher;
import org.eclipse.ditto.client.twin.SearchQueryBuilder;
import org.eclipse.ditto.client.twin.TwinSearchHandle;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CreateSubscription;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionHasNext;
import org.reactivestreams.Publisher;

/**
 * Implements {@link org.eclipse.ditto.client.twin.TwinSearchHandle}.
 */
final class TwinSearchHandleImpl extends AbstractHandle implements TwinSearchHandle {

    TwinSearchHandleImpl(final MessagingProvider messagingProvider) {
        super(messagingProvider, TopicPath.Channel.TWIN);
    }

    @Override
    public Publisher<Thing> publisher(final Consumer<SearchQueryBuilder> querySpecifier) {
        // TODO: Map and Spliterator processors with tests
        throw new UnsupportedOperationException("TODO: implement");
    }

    @Override
    public Stream<Thing> stream(final Consumer<SearchQueryBuilder> querySpecifier) {
        return internalSpliterator(querySpecifier).asStream()
                .flatMap(TwinSearchHandleImpl::streamAsThings);
    }

    private SpliteratorSubscriber<SubscriptionHasNext> internalSpliterator(
            final Consumer<SearchQueryBuilder> querySpecifier) {

        final SearchQueryBuilderImpl builder = new SearchQueryBuilderImpl();
        querySpecifier.accept(builder);
        final CreateSubscription createSubscription = builder.createSubscription();
        final Duration timeout = messagingProvider.getMessagingConfiguration().getTimeout();
        final int bufferedPages = builder.getBufferedPages();
        final int pagesPerBatch = builder.getPagesPerBatch();
        final Publisher<SubscriptionHasNext> publisher =
                ThingSearchPublisher.of(createSubscription, PROTOCOL_ADAPTER, messagingProvider);
        final SpliteratorSubscriber<SubscriptionHasNext> subscriber =
                SpliteratorSubscriber.of(timeout, bufferedPages, pagesPerBatch);
        publisher.subscribe(subscriber);
        return subscriber;
    }

    private static Stream<Thing> streamAsThings(final SubscriptionHasNext page) {
        return page.getItems()
                .stream()
                .map(JsonValue::asObject)
                .map(ThingsModelFactory::newThing);
    }
}
