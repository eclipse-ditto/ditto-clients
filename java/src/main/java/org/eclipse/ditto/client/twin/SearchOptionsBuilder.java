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

/**
 * Builder to set the options for search queries.
 *
 * @since 1.1.0
 */
public interface SearchOptionsBuilder {

    /**
     * Set the page size.
     *
     * @param n the page size.
     * @return This builder.
     */
    SearchOptionsBuilder size(int n);

    /**
     * Set the sort options.
     *
     * @param settings Consumer to set the sort options.
     * @return This builder.
     */
    SearchOptionsBuilder sort(Consumer<SortSearchOptionBuilder> settings);

    /**
     * Builder to set sort options for search queries.
     */
    interface SortSearchOptionBuilder {

        /**
         * Add a sort field in the ascending order.
         *
         * @param field JSON pointer to the field being sorted.
         * @return This builder.
         */
        SortSearchOptionBuilder asc(CharSequence field);

        /**
         * Add a sort field in the descending order.
         *
         * @param field JSON pointer to the field being sorted.
         * @return This builder.
         */
        SortSearchOptionBuilder desc(CharSequence field);
    }
}
