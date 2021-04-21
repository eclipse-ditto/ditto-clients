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

import org.eclipse.ditto.things.model.Thing;
import org.reactivestreams.Publisher;

/**
 * Search API for <em>Twin Things</em>.
 *
 * @since 1.1.0
 */
public interface TwinSearchHandle {

    /**
     * Search for things and publish each page of search result as a reactive-streams publisher.
     * This method offers the full power of a reactive-streams {@code Publisher}.
     * Best used with a reactive-streams library such as Akka Streams or RxJava.
     *
     * @param querySpecifier the consumer to specify the search query.
     * @return a publisher of things matching the query.
     */
    Publisher<List<Thing>> publisher(Consumer<SearchQueryBuilder> querySpecifier);

    /**
     * Search for things and iterate over results with a stream.
     * This method is a wrapper of {@code this#publisher(java.util.function.Consumer)} for ease-of-use,
     * but it is less powerful. In particular, there is no way to terminate the stream early
     * without throwing an exception.
     * The stream tries its best not to block through an internal buffer.
     * However, it must wait for the initial response before the first result can be delivered.
     * <p>
     * <em>Error handling</em>
     * <p>
     * If any {@code RuntimeException} is thrown in the user code processing stream elements, then the stream
     * is cancelled and the exception is rethrown. The reason for this behavior is that any user code catching
     * the {@code RuntimeException} is outside of the consumer of stream elements and should expect
     * that the stream is "used up."
     * <p>
     * While user code may take advantage of this behavior to terminate a stream early,
     * it is recommended to use
     * {@link org.eclipse.ditto.client.twin.TwinSearchHandle#publisher(java.util.function.Consumer)}
     * together with a reactive-streams library instead.
     *
     * @param querySpecifier the consumer to specify the search query.
     * @return a stream over things matching the query.
     */
    Stream<Thing> stream(Consumer<SearchQueryBuilder> querySpecifier);
}
