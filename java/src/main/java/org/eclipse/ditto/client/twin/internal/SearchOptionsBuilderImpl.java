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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.ditto.client.twin.SearchOptionsBuilder;

/**
 * Internal implementation of SearchOptionsBuilder.
 *
 * @since 1.1.0
 */
final class SearchOptionsBuilderImpl implements SearchOptionsBuilder {

    private static final String COMMA = ",";
    private static final BinaryOperator<String> JOIN_WITH_COMMA = (s, t) -> s + COMMA + t;

    private Integer size;
    private SortSearchOptionBuilderImpl sortBuilder = new SortSearchOptionBuilderImpl();

    @Override
    public SearchOptionsBuilder size(final int n) {
        size = n;
        return this;
    }

    @Override
    public SearchOptionsBuilder sort(final Consumer<SortSearchOptionBuilder> settings) {
        settings.accept(sortBuilder);
        return this;
    }

    Optional<String> build() {
        final Stream<String> sizeOption = size == null
                ? Stream.empty()
                : Stream.of(String.format("size(%d)", size));
        final Stream<String> sortOption = sortBuilder.dimensions.stream()
                .reduce(JOIN_WITH_COMMA)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .map(fields -> String.format("sort(%s)", fields));
        return Stream.concat(sizeOption, sortOption).reduce(JOIN_WITH_COMMA);
    }

    private static final class SortSearchOptionBuilderImpl implements SortSearchOptionBuilder {

        private final List<String> dimensions = new LinkedList<>();

        @Override
        public SortSearchOptionBuilder asc(final CharSequence field) {
            dimensions.add("+" + field);
            return this;
        }

        @Override
        public SortSearchOptionBuilder desc(final CharSequence field) {
            dimensions.add("-" + field);
            return this;
        }
    }
}
