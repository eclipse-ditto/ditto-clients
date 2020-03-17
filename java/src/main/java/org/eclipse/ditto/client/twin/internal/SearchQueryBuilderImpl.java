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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.twin.SearchQueryBuilder;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.commands.thingsearch.subscription.CreateSubscription;

final class SearchQueryBuilderImpl implements SearchQueryBuilder {

    @Nullable private String filter;
    private final List<String> options = new ArrayList<>();
    @Nullable private String fields;
    private final Set<String> namespaces = new HashSet<>();
    private int bufferedPages = 2;
    private int pagesPerBatch = 1;

    SearchQueryBuilderImpl() {}

    @Override
    public SearchQueryBuilder filter(@Nullable String filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public SearchQueryBuilder options(@Nullable final String options) {
        this.options.clear();
        if (options != null) {
            this.options.add(options);
        }
        return this;
    }

    @Override
    public SearchQueryBuilder fields(@Nullable final String fields) {
        this.fields = fields;
        return this;
    }

    @Override
    public SearchQueryBuilder namespace(final String namespace) {
        namespaces.add(namespace);
        return this;
    }

    @Override
    public SearchQueryBuilder namespaces(@Nullable final Set<String> namespaces) {
        this.namespaces.clear();
        if (namespaces != null && !namespaces.isEmpty()) {
            this.namespaces.addAll(namespaces);
        }
        return this;
    }

    @Override
    public SearchQueryBuilder bufferedPages(final int n) {
        bufferedPages = n;
        return this;
    }

    @Override
    public SearchQueryBuilder pagesPerBatch(final int n) {
        pagesPerBatch = n;
        return this;
    }

    CreateSubscription createSubscription() {
        final String optionsString = options.isEmpty() ? null : String.join(",", options);
        final JsonFieldSelector fieldSelector = JsonFactory.parseJsonFieldSelector(fields);
        final Set<String> namespaces = this.namespaces.isEmpty() ? null : this.namespaces;
        return CreateSubscription.of(filter, optionsString, fieldSelector, namespaces, DittoHeaders.empty());
    }

    int getBufferedPages() {
        return bufferedPages;
    }

    int getPagesPerBatch() {
        return pagesPerBatch;
    }
}
