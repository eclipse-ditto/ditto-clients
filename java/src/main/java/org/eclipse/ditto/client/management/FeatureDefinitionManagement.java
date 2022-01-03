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
package org.eclipse.ditto.client.management;

import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.things.model.FeatureDefinition;

/**
 * {@code FeatureDefinitionManagement} provides create, update and delete functionality for managing {@link
 * org.eclipse.ditto.things.model.FeatureDefinition}s.
 * <p>
 * All the methods are executed non-blocking and asynchronously.
 * Therefore, the methods return a {@code CompletionStage} object that will complete either successfully
 * if the operation was executed and confirmed, or exceptionally with a specific
 * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if it was executed but has failed.
 * </p>
 *
 * @since 1.0.0
 */
public interface FeatureDefinitionManagement {

    /**
     * Sets the given definition to the Feature.
     *
     * @param definition the FeatureDefinition to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for setting
     * a feature definition.
     */
    CompletionStage<Void> setDefinition(FeatureDefinition definition, Option<?>... options);

    /**
     * Merge the given definition to the Feature.
     *
     * @param definition the FeatureDefinition to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for merging
     * a feature definition.
     * @since 2.0.0
     */
    CompletionStage<Void> mergeDefinition(FeatureDefinition definition, Option<?>... options);

    /**
     * Deletes the definition of the Feature.
     *
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for deleting
     * a feature definition.
     */
    CompletionStage<Void> deleteDefinition(Option<?>... options);

}
