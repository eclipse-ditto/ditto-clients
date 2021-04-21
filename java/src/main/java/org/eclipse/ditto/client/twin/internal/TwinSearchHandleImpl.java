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
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.ditto.client.internal.AbstractHandle;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.streaming.MapPublisher;
import org.eclipse.ditto.client.streaming.SpliteratorSubscriber;
import org.eclipse.ditto.client.streaming.ThingSearchPublisher;
import org.eclipse.ditto.client.twin.SearchQueryBuilder;
import org.eclipse.ditto.client.twin.TwinSearchHandle;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CreateSubscription;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionHasNextPage;
import org.reactivestreams.Publisher;

/**
 * Implements {@link org.eclipse.ditto.client.twin.TwinSearchHandle}.
 */
final class TwinSearchHandleImpl extends AbstractHandle implements TwinSearchHandle {

    TwinSearchHandleImpl(final MessagingProvider messagingProvider) {
        super(messagingProvider, TopicPath.Channel.TWIN);
    }

    @Override
    public Publisher<List<Thing>> publisher(final Consumer<SearchQueryBuilder> querySpecifier) {
        final SearchQueryBuilderImpl builder = new SearchQueryBuilderImpl();
        querySpecifier.accept(builder);
        final Publisher<SubscriptionHasNextPage> thingSearchPublisher =
                ThingSearchPublisher.of(builder.createSubscription(), PROTOCOL_ADAPTER, messagingProvider);
        return MapPublisher.of(thingSearchPublisher, TwinSearchHandleImpl::pageToThingList);
    }

    @Override
    public Stream<Thing> stream(final Consumer<SearchQueryBuilder> querySpecifier) {
        return internalSpliterator(querySpecifier).asStream()
                .flatMap(TwinSearchHandleImpl::streamAsThings);
    }

    private SpliteratorSubscriber<SubscriptionHasNextPage> internalSpliterator(
            final Consumer<SearchQueryBuilder> querySpecifier) {

        final SearchQueryBuilderImpl builder = new SearchQueryBuilderImpl();
        querySpecifier.accept(builder);
        final CreateSubscription createSubscription = builder.createSubscription();
        final Duration timeout = messagingProvider.getMessagingConfiguration().getTimeout();
        final int bufferedPages = builder.getInitialDemand();
        final int pagesPerBatch = builder.getDemand();
        final Publisher<SubscriptionHasNextPage> publisher =
                ThingSearchPublisher.of(createSubscription, PROTOCOL_ADAPTER, messagingProvider);
        final SpliteratorSubscriber<SubscriptionHasNextPage> subscriber =
                SpliteratorSubscriber.of(timeout, bufferedPages, pagesPerBatch);
        publisher.subscribe(subscriber);
        return subscriber;
    }

    private static Stream<Thing> streamAsThings(final SubscriptionHasNextPage page) {
        return page.getItems()
                .stream()
                .map(JsonValue::asObject)
                .map(ThingsModelFactory::newThing);
    }

    private static List<Thing> pageToThingList(final SubscriptionHasNextPage page) {
        return page.getItems()
                .stream()
                .map(JsonValue::asObject)
                .map(ThingsModelFactory::newThing)
                .collect(Collectors.toList());
    }

    @Override
    protected AcknowledgementLabel getThingResponseAcknowledgementLabel() {
        return DittoAcknowledgementLabel.TWIN_PERSISTED;
    }
}
