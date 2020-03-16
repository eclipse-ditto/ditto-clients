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
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.ditto.client.twin.SearchFactory;
import org.eclipse.ditto.model.query.criteria.Criteria;
import org.eclipse.ditto.model.query.criteria.CriteriaFactory;
import org.eclipse.ditto.model.query.criteria.CriteriaFactoryImpl;
import org.eclipse.ditto.model.query.criteria.Predicate;
import org.eclipse.ditto.model.query.expression.ExistsFieldExpression;
import org.eclipse.ditto.model.query.expression.FilterFieldExpression;
import org.eclipse.ditto.model.query.expression.SortFieldExpression;
import org.eclipse.ditto.model.query.expression.ThingsFieldExpressionFactory;
import org.eclipse.ditto.model.query.things.ModelBasedThingsFieldExpressionFactory;
import org.eclipse.ditto.model.thingsearch.SearchModelFactory;
import org.eclipse.ditto.model.thingsearch.SizeOption;
import org.eclipse.ditto.model.thingsearch.SortOption;
import org.eclipse.ditto.model.thingsearch.SortOptionEntry;

/**
 * Implements {@link org.eclipse.ditto.client.twin.SearchFactory}.
 */
final class SearchFactoryImpl implements SearchFactory {

    private final CriteriaFactory cf;
    private final ThingsFieldExpressionFactory tf;

    SearchFactoryImpl() {
        cf = new CriteriaFactoryImpl();
        tf = new ModelBasedThingsFieldExpressionFactory();
    }

    @Override
    public Criteria any() {
        return cf.any();
    }

    @Override
    public Criteria and(final List<Criteria> criterias) {
        return cf.and(criterias);
    }

    @Override
    public Criteria or(final List<Criteria> criterias) {
        return cf.or(criterias);
    }

    @Override
    public Criteria nor(final List<Criteria> criterias) {
        return cf.nor(criterias);
    }

    @Override
    public Criteria fieldCriteria(final FilterFieldExpression fieldExpression, final Predicate predicate) {
        return cf.fieldCriteria(fieldExpression, predicate);
    }

    @Override
    public Criteria existsCriteria(final ExistsFieldExpression fieldExpression) {
        return cf.existsCriteria(fieldExpression);
    }

    @Override
    public Predicate eq(final Object value) {
        return cf.eq(value);
    }

    @Override
    public Predicate ne(final Object value) {
        return cf.ne(value);
    }

    @Override
    public Predicate gt(final Object value) {
        return cf.gt(value);
    }

    @Override
    public Predicate ge(final Object value) {
        return cf.ge(value);
    }

    @Override
    public Predicate lt(final Object value) {
        return cf.lt(value);
    }

    @Override
    public Predicate le(final Object value) {
        return cf.le(value);
    }

    @Override
    public Predicate like(final Object value) {
        return cf.like(value);
    }

    @Override
    public Predicate in(final List<?> values) {
        return cf.in(values);
    }

    @Override
    public FilterFieldExpression filterBy(final String propertyName) {
        return tf.filterBy(propertyName);
    }

    @Override
    public ExistsFieldExpression existsBy(final String propertyName) {
        return tf.existsBy(propertyName);
    }

    @Override
    public SortFieldExpression sortBy(final String propertyName) {
        return tf.sortBy(propertyName);
    }

    @Override
    public SortOption sortOption(final Consumer<SortBuilder> sortOptionCreator) {
        final SortBuilderImpl sortBuilder = new SortBuilderImpl();
        sortOptionCreator.accept(sortBuilder);
        if (sortBuilder.entries.isEmpty()) {
            throw new IllegalArgumentException("sort option must have 1 or more dimensions.");
        }
        return SearchModelFactory.newSortOption(sortBuilder.entries);
    }

    @Override
    public SizeOption sizeOption(final int pageSize) {
        return SearchModelFactory.newSizeOption(pageSize);
    }

    private static final class SortBuilderImpl implements SortBuilder {

        private final List<SortOptionEntry> entries = new ArrayList<>();

        @Override
        public SortBuilder asc(final SortFieldExpression sortFieldExpression) {
            entries.add(SearchModelFactory.newSortOptionEntry(
                    sortFieldExpression.accept(ToStringVisitors.FIELD_EXPRESSION),
                    SortOptionEntry.SortOrder.ASC
            ));
            return this;
        }

        @Override
        public SortBuilder desc(final SortFieldExpression sortFieldExpression) {
            entries.add(SearchModelFactory.newSortOptionEntry(
                    sortFieldExpression.accept(ToStringVisitors.FIELD_EXPRESSION),
                    SortOptionEntry.SortOrder.DESC
            ));
            return this;
        }
    }
}
