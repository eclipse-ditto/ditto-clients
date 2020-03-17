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
    SearchQueryBuilder filter(@Nullable String filter);

    /**
     * Set the options as string.
     *
     * @param options options as comma-separated string, or null to remove all options.
     * @return this builder.
     */
    SearchQueryBuilder options(@Nullable String options);

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
     * Set the number of pages to buffer at the client side to minimize blocking.
     * Must be 1 or more.
     *
     * @param n the number of pages to buffer at the client side.
     * @return this builder.
     */
    SearchQueryBuilder bufferedPages(int n);

    /**
     * Set the number of pages to request in one batch.
     * A bigger batch size reduces the number of outgoing messages but increases the risk of blocking.
     * Must be between 1 and buffered pages.
     *
     * @param n the number of pages to request in one batch.
     * @return this builder.
     */
    SearchQueryBuilder pagesPerBatch(int n);
}
