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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;

/**
 * {@code FeaturePropertiesManagement} provides create, update and delete functionality for managing {@link
 * org.eclipse.ditto.model.things.FeatureProperties}. <p> All the methods are executed non-blocking and asynchronously.
 * Therefore, the methods return a {@code CompletableFuture} object that will complete either successfully if the
 * operation was executed and confirmed, or exceptionally with a specific {@link org.eclipse.ditto.model.base.exceptions.DittoRuntimeException}
 * if it was executed but has failed. </p>
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
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletableFuture<Void> putProperty(final CharSequence path, final boolean value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletableFuture<Void> putProperty(JsonPointer path, boolean value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletableFuture<Void> putProperty(final CharSequence path, final double value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletableFuture<Void> putProperty(JsonPointer path, double value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletableFuture<Void> putProperty(final CharSequence path, final int value, final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletableFuture<Void> putProperty(JsonPointer path, int value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletableFuture<Void> putProperty(final CharSequence path, final long value, final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletableFuture<Void> putProperty(JsonPointer path, long value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletableFuture<Void> putProperty(final CharSequence path, final String value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletableFuture<Void> putProperty(JsonPointer path, String value, Option<?>... options);

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set - may contain {@code "/"} for addressing nested paths
     * in a hierarchy.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletableFuture<Void> putProperty(final CharSequence path, final JsonValue value,
            final Option<?>... options) {
        argumentNotNull(path);
        return putProperty(JsonFactory.newPointer(path), value, options);
    }

    /**
     * Sets the given property of the Feature.
     *
     * @param path the hierarchical path to the property to be set.
     * @param value the value to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletableFuture<Void> putProperty(JsonPointer path, JsonValue value, Option<?>... options);

    /**
     * Sets the given properties of the Feature.
     *
     * @param value the properties to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     */
    CompletableFuture<Void> setProperties(JsonObject value, Option<?>... options);

    /**
     * Deletes the property specified by the given path.
     *
     * @param path the hierarchical path to the property to be deleted.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    default CompletableFuture<Void> deleteProperty(final CharSequence path, final Option<?>... options) {
        argumentNotNull(path);
        return deleteProperty(JsonFactory.newPointer(path), options);
    }

    /**
     * Deletes the property specified by the given path.
     *
     * @param path the hierarchical path to the property to be deleted.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     */
    CompletableFuture<Void> deleteProperty(JsonPointer path, Option<?>... options);

    /**
     * Deletes the properties of the Feature.
     *
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return a completable future providing the result of this operation or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     */
    CompletableFuture<Void> deleteProperties(Option<?>... options);

}
