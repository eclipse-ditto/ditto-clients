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

import java.util.function.Consumer;

import org.eclipse.ditto.model.query.criteria.CriteriaFactory;
import org.eclipse.ditto.model.query.expression.SortFieldExpression;
import org.eclipse.ditto.model.query.expression.ThingsFieldExpressionFactory;
import org.eclipse.ditto.model.thingsearch.SizeOption;
import org.eclipse.ditto.model.thingsearch.SortOption;

/**
 * Factory for search filter criteria.
 *
 * @since 1.1.0
 */
public interface SearchFactory extends CriteriaFactory, ThingsFieldExpressionFactory {

    /**
     * Create a sort option.
     *
     * @param sortOptionCreator creator of a sort option.
     * @return a sort option.
     */
    SortOption sortOption(Consumer<SortBuilder> sortOptionCreator);

    /**
     * Specify the page size of a search request. A bigger page size reduces the number of incoming
     * messages but requires the underlying messaging provider to support a bigger message size.
     *
     * @param pageSize the page size.
     * @return the size option.
     */
    SizeOption sizeOption(final int pageSize);

    /**
     * Builder of a sort option.
     */
    interface SortBuilder {

        /**
         * Add another ascending dimension to the sort option.
         *
         * @param sortFieldExpression the sort field.
         * @return this builder.
         */
        SortBuilder asc(final SortFieldExpression sortFieldExpression);

        /**
         * Add another descending dimension to the sort option.
         *
         * @param sortFieldExpression the sort field.
         * @return this builder.
         */
        SortBuilder desc(final SortFieldExpression sortFieldExpression);
    }
}
