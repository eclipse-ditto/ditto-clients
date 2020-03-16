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

import java.util.Arrays;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayInternalErrorException;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CreateSubscription;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.RequestSubscription;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionComplete;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionCreated;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionFailed;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionHasNext;
import org.junit.Test;

/**
 * Test the search interface.
 */
public final class DittoClientTwinSearchTest extends AbstractDittoClientTest {

    @Test
    public void emptyResults() {
        final Stream<Thing> searchResultStream = client.twin().search().stream(search ->
                search.filter(sf ->
                        sf.nor(Arrays.asList(
                                sf.existsCriteria(sf.existsByFeatureProperty("f1", "p1")),
                                sf.fieldCriteria(sf.filterByAttribute("a/b/c"), sf.eq(5)),
                                sf.any()
                        )))
                        .fields("thingId,policyId")
                        .option(sf -> sf.sizeOption(5))
                        .option(sf -> sf.sortOption(sort -> sort.asc(sf.sortByThingId()).desc(sf.sortByAttribute("x"))))
                        .namespace("hello.world")
                        .bufferedPages(55)
                        .pagesPerBatch(22)
        );
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = "my-empty-subscription";
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        reply(SubscriptionComplete.of(subscriptionId, createSubscription.getDittoHeaders()));
        assertThat(createSubscription.getFilter())
                .contains("not(or(exists(/features/f1/properties/p1),eq(/attributes/a/b/c,5),exists(thingId)))");
        assertThat(createSubscription.getOptions()).contains("size(5),sort(+/thingId,-/attributes/x)");
        assertThat(createSubscription.getSelectedFields().map(JsonFieldSelector::getPointers).orElse(null))
                .contains(JsonPointer.of("thingId"), JsonPointer.of("policyId"));
        assertThat(searchResultStream).isEmpty();
    }

    @Test
    public void someResults() {
        final Stream<Thing> searchResults = client.twin().search().stream(q -> q.bufferedPages(2).pagesPerBatch(1));
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = "my-nonempty-subscription";
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        expectMsgClass(RequestSubscription.class);
        reply(hasNext(subscriptionId, 0, 5));
        expectMsgClass(RequestSubscription.class);
        reply(hasNext(subscriptionId, 5, 10));
        reply(SubscriptionComplete.of(subscriptionId, DittoHeaders.empty()));
        assertThat(searchResults.map(thing -> thing.getEntityId().orElseThrow(AssertionError::new)))
                .contains(IntStream.range(0, 10).mapToObj(i -> ThingId.of("x:" + i)).toArray(ThingId[]::new));
    }

    @Test
    public void partialFailure() {
        final Spliterator<Thing> searchResultSpliterator = client.twin().search().stream(q -> {}).spliterator();
        final CreateSubscription createSubscription = expectMsgClass(CreateSubscription.class);
        final String subscriptionId = "my-failed-subscription";
        reply(SubscriptionCreated.of(subscriptionId, createSubscription.getDittoHeaders()));
        expectMsgClass(RequestSubscription.class);
        reply(hasNext(subscriptionId, 0, 1));
        reply(SubscriptionFailed.of(subscriptionId, GatewayInternalErrorException.newBuilder().message("sorry").build(),
                DittoHeaders.empty()));
        final AtomicReference<Thing> thingBox = new AtomicReference<>();
        assertThat(searchResultSpliterator.tryAdvance(thingBox::set)).isTrue();
        assertThat(thingBox).hasValue(Thing.newBuilder().setId(ThingId.of("x:0")).build());
        assertThatExceptionOfType(DittoRuntimeException.class)
                .isThrownBy(() -> searchResultSpliterator.forEachRemaining(thing -> {}));
    }

    private static SubscriptionHasNext hasNext(final String subscriptionId, final int start, final int end) {
        final JsonArray things = IntStream.range(start, end)
                .mapToObj(i -> JsonObject.newBuilder().set("thingId", "x:" + i).build())
                .collect(JsonCollectors.valuesToArray());
        return SubscriptionHasNext.of(subscriptionId, things, DittoHeaders.empty());
    }
}
