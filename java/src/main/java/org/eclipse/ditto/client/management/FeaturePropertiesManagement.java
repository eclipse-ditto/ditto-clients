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

import static org.eclipse.ditto.base.model.common.ConditionChecker.argumentNotNull;

import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;

/**
 * {@code FeaturePropertiesManagement} provides create, update and delete functionality for managing {@link
 * org.eclipse.ditto.things.model.FeatureProperties}. <p> All the methods are executed non-blocking and asynchronously.
 * Therefore, the methods return a {@code CompletionStage} object that will complete either successfully if the
 * operation was executed and confirmed, or exceptionally with a specific
 * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if it was executed but has failed. </p>
 *
 * @since 1.0.0
 */
public interface FeaturePropertiesManagement {

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletionStage<Void> putProperty(final CharSequence path, final boolean value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletionStage<Void> putProperty(JsonPointer path, boolean value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletionStage<Void> putProperty(final CharSequence path, final double value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletionStage<Void> putProperty(JsonPointer path, double value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletionStage<Void> putProperty(final CharSequence path, final int value, final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletionStage<Void> putProperty(JsonPointer path, int value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletionStage<Void> putProperty(final CharSequence path, final long value, final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletionStage<Void> putProperty(JsonPointer path, long value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletionStage<Void> putProperty(final CharSequence path, final String value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletionStage<Void> putProperty(JsonPointer path, String value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletionStage<Void> putProperty(final CharSequence path, final JsonValue value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletionStage<Void> putProperty(JsonPointer path, JsonValue value, Option<?>... options);

    /**
     * Merge the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be merged - may contain {@code "/"} for addressing nested
     * paths in a hierarchy.
     * @param value the value to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @since 2.0.0
     */
    default CompletionStage<Void> mergeProperty(final CharSequence path, final boolean value,
            final Option<?>... options) {
        argumentNotNull(path);
        return mergeProperty(JsonFactory.newPointer(path), JsonFactory.newValue(value), options);
    }

    /**
     * Merge the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be merged - may contain {@code "/"} for addressing nested
     * paths in a hierarchy.
     * @param value the value to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @since 2.0.0
     */
    default CompletionStage<Void> mergeProperty(final CharSequence path, final double value,
            final Option<?>... options) {
        argumentNotNull(path);
        return mergeProperty(JsonFactory.newPointer(path), JsonFactory.newValue(value), options);
    }

    /**
     * Merge the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be merged - may contain {@code "/"} for addressing nested
     * paths
     * in a hierarchy.
     * @param value the value to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @since 2.0.0
     */
    default CompletionStage<Void> mergeProperty(final CharSequence path, final int value,
            final Option<?>... options) {
        argumentNotNull(path);
        return mergeProperty(JsonFactory.newPointer(path), JsonFactory.newValue(value), options);
    }

    /**
     * Merge the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be merged - may contain {@code "/"} for addressing nested
     * paths
     * in a hierarchy.
     * @param value the value to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @since 2.0.0
     */
    default CompletionStage<Void> mergeProperty(final CharSequence path, final long value,
            final Option<?>... options) {
        argumentNotNull(path);
        return mergeProperty(JsonFactory.newPointer(path), JsonFactory.newValue(value), options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be merged - may contain {@code "/"} for addressing nested
     * paths
     * in a hierarchy.
     * @param value the value to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @since 2.0.0
     */
    default CompletionStage<Void> mergeProperty(final CharSequence path, final String value,
            final Option<?>... options) {
        argumentNotNull(path);
        return mergeProperty(JsonFactory.newPointer(path), JsonFactory.newValue(value), options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @since 2.0.0
     */
    default CompletionStage<Void> mergeProperty(final CharSequence path, final JsonValue value,
            final Option<?>... options) {
        argumentNotNull(path);
        return mergeProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Merge the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be merged.
     * @param value the value to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @since 2.0.0
     */
    CompletionStage<Void> mergeProperty(JsonPointer path, JsonValue value, Option<?>... options);

    /**
     * Sets the given properties of the Feature.
     *
     * @param value the properties to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     */
    CompletionStage<Void> setProperties(JsonObject value, Option<?>... options);

    /**
     * Merge the given properties of the Feature.
     *
     * @param value the properties to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @since 2.0.0
     */
    CompletionStage<Void> mergeProperties(JsonObject value, Option<?>... options);

    /**
     * Deletes the property specified by the given path.
     *
     * @param path the hierarchical path to the property to be deleted.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletionStage<Void> deleteProperty(final CharSequence path, final Option<?>... options) {
        argumentNotNull(path);
        return deleteProperty(JsonFactory.newPointer(path), options);
    }

    /**
     * Deletes the property specified by the given path.
     *
     * @param path the hierarchical path to the property to be deleted.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletionStage<Void> deleteProperty(JsonPointer path, Option<?>... options);

    /**
     * Deletes the properties of the Feature.
     *
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return a CompletionStage providing the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed
     */
    CompletionStage<Void> deleteProperties(Option<?>... options);

}
