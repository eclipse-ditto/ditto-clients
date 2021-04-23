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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.streaming.SpliteratorSubscriber;
import org.eclipse.ditto.client.twin.SearchQueryBuilder;
import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.base.model.signals.commands.exceptions.GatewayInternalErrorException;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.CancelSubscription;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.CreateSubscription;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.RequestFromSubscription;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionComplete;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionCreated;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionFailed;
import org.eclipse.ditto.thingsearch.model.signals.events.SubscriptionHasNextPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.reactivestreams.Publisher;

/**
 * Test the search interface.
 */
@RunWith(Parameterized.class)
public final class DittoClientTwinSearchTest extends AbstractDittoClientTest {

    @Parameterized.Parameters(name = "method={0}")
    public static Method[] methods() {
        return Method.values();
    }

    @Parameterized.Parameter
    public Method method;

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Test
    public void emptyResults() {
        final String filter = "not(or(exists(/features/f1/properties/p1),eq(/attributes/a/b/c,5),exists(thingId)))";
        final String options = "size(5),sort(+/thingId,-/attributes/x)";
        final Stream<Thing> searchResultStream = createStreamUnderTest(search ->
                search.filter(filter)
                        .fields("thingId,policyId")
                        .options(options)
                        .namespace("hello.world")
                        .initialDemand(55)
                        .demand(22)
        );
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = disambiguate("my-empty-subscription");
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        expectMsgClass(RequestFromSubscription.class);
        reply(SubscriptionComplete.of(subscriptionId, createSubscription.getDittoHeaders()));
        assertThat(createSubscription.getFilter()).contains(filter);
        assertThat(createSubscription.getOptions()).contains(options);
        assertThat(createSubscription.getSelectedFields().map(JsonFieldSelector::getPointers).orElse(null))
                .contains(JsonPointer.of("thingId"), JsonPointer.of("policyId"));
        assertThat(searchResultStream).isEmpty();
    }

    @Test
    public void someResults() {
        final Stream<Thing> searchResults = createStreamUnderTest(q -> q.initialDemand(2).demand(1));
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = disambiguate("my-nonempty-subscription");
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        assertThat(expectMsgClass(RequestFromSubscription.class).getDemand()).isEqualTo(2L);
        reply(hasNext(subscriptionId, 0, 5));
        reply(hasNext(subscriptionId, 5, 10));
        reply(SubscriptionComplete.of(subscriptionId, DittoHeaders.empty()));
        assertThat(searchResults.map(thing -> thing.getEntityId().orElseThrow(AssertionError::new)))
                .contains(IntStream.range(0, 10).mapToObj(i -> ThingId.of("x:" + i)).toArray(ThingId[]::new));
    }

    @Test
    public void exceptionInHandlerCancelsStream() {
        final Stream<Thing> searchResults = createStreamUnderTest(q -> q.initialDemand(2).demand(2));
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = disambiguate("my-cancelled-subscription");
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        assertThat(expectMsgClass(RequestFromSubscription.class).getDemand()).isEqualTo(2L);
        reply(hasNext(subscriptionId, 0, 1));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                searchResults.forEach(page -> {
                    throw new IllegalArgumentException("expected exception");
                }));
        assertThat(expectMsgClass(CancelSubscription.class).getSubscriptionId()).isEqualTo(subscriptionId);
    }

    @Test
    public void partialFailure() {
        final Spliterator<Thing> searchResultSpliterator = createStreamUnderTest(q -> {}).spliterator();
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = disambiguate("my-failed-subscription");
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        expectMsgClass(RequestFromSubscription.class);
        reply(hasNext(subscriptionId, 0, 1));
        reply(SubscriptionFailed.of(subscriptionId, GatewayInternalErrorException.newBuilder().message("sorry").build(),
                DittoHeaders.empty()));
        final AtomicReference<Thing> thingBox = new AtomicReference<>();
        assertThat(searchResultSpliterator.tryAdvance(thingBox::set)).isTrue();
        assertThat(thingBox).hasValue(Thing.newBuilder().setId(ThingId.of("x:0")).build());
        assertThatExceptionOfType(GatewayInternalErrorException.class)
                .isThrownBy(() -> searchResultSpliterator.forEachRemaining(thing -> {}));
    }

    private SubscriptionHasNextPage hasNext(final String subscriptionId, final int start, final int end) {
        final JsonArray things = IntStream.range(start, end)
                .mapToObj(i -> JsonObject.newBuilder().set("thingId", "x:" + i).build())
                .collect(JsonCollectors.valuesToArray());
        return SubscriptionHasNextPage.of(subscriptionId, things, DittoHeaders.empty());
    }

    private String disambiguate(final String prefix) {
        return prefix + "-" + COUNTER.getAndIncrement();
    }

    private Stream<Thing> createStreamUnderTest(final Consumer<SearchQueryBuilder> querySpecifier) {
        switch (method) {
            case PUBLISHER:
                final Publisher<List<Thing>> listPublisher = client.twin().search().publisher(querySpecifier);
                final SpliteratorSubscriber<List<Thing>> subscriber =
                        SpliteratorSubscriber.of(messaging.getMessagingConfiguration().getTimeout(), 2, 1);
                listPublisher.subscribe(subscriber);
                return StreamSupport.stream(subscriber, false).flatMap(List::stream);
            case STREAM:
                return client.twin().search().stream(querySpecifier);
            default:
                throw new IllegalArgumentException("Unknown method: " + method);
        }
    }

    enum Method {
        PUBLISHER,
        STREAM
    }
}
