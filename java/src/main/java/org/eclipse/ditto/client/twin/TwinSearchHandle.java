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
package org.eclipse.ditto.client.twin;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.ditto.model.things.Thing;
import org.reactivestreams.Publisher;

/**
 * Search API for <em>Twin Things</em>.
 *
 * @since 1.1.0
 */
public interface TwinSearchHandle {

    /**
     * Search for things and publish each page of search result as a reactive-streams publisher.
     *
     * @param querySpecifier the consumer to specify the search query.
     * @return a publisher of things matching the query.
     */
    Publisher<List<Thing>> publisher(Consumer<SearchQueryBuilder> querySpecifier);

    /**
     * Search for things and iterate over results with a spliterator.
     * The stream tries its best not to block via an internal buffer.
     * However, it must wait for the initial response before the first result can be delivered.
     *
     * @param querySpecifier the consumer to specify the search query.
     * @return a spliterator over things matching the query.
     */
    Stream<Thing> stream(Consumer<SearchQueryBuilder> querySpecifier);
}
