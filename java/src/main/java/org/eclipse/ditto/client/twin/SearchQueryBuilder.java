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

import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Builder of thing-search queries.
 *
 * @since 1.1.0
 */
public interface SearchQueryBuilder {

    /**
     * Set or remove the filter for the search query.
     *
     * @param filter the filter as string, or null to remove the filter.
     * @return this builder.
     */
    SearchQueryBuilder filter(@Nullable CharSequence filter);

    /**
     * Set the options as string.
     *
     * @param options options as comma-separated string, or null to remove all options.
     * @return this builder.
     */
    SearchQueryBuilder options(@Nullable String options);

    /**
     * Set the options with a builder consumer.
     *
     * @param settings options as comma-separated string, or null to remove all options.
     * @return this builder.
     */
    SearchQueryBuilder options(Consumer<SearchOptionsBuilder> settings);

    /**
     * Set the fields to select in search results.
     * Select only {@code "thingId"} to speed up the query and reduce the size of incoming messages.
     *
     * @param fields the fields to select.
     * @return this builder.
     */
    SearchQueryBuilder fields(@Nullable String fields);

    /**
     * Add a namespace in which to search for things.
     * Not specifying a namespace implies searching over all things visible to the client.
     *
     * @param namespace the namespace to search.
     * @return this builder.
     */
    SearchQueryBuilder namespace(String namespace);

    /**
     * Restrict search to the given namespaces to improve query performance.
     * When given null or an empty set of namespaces, the search is performed over all things visible to the client.
     *
     * @param namespaces the namespaces to restrict.
     * @return this builder.
     */
    SearchQueryBuilder namespaces(@Nullable Set<String> namespaces);

    /**
     * Set the number of pages to demand in the first request message to the back-end,
     * which is equal to the number of pages to buffer at the client side.
     * A bigger value reduces the risk of blocking at the cost of memory at the client side.
     * Must be 1 or more.
     *
     * @param n the number of pages to buffer at the client side.
     * @return this builder.
     */
    SearchQueryBuilder initialDemand(int n);

    /**
     * Set the number of pages to demand in one request message to the back-end.
     * A bigger value reduces the number of outgoing messages but increases the risk of blocking.
     * Must be between 1 and the initial demand.
     *
     * @param n the number of pages to demand in one request message.
     * @return this builder.
     */
    SearchQueryBuilder demand(int n);
}
