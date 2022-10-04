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
import org.eclipse.ditto.client.registration.FeaturePropertiesChangeRegistration;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.base.model.signals.WithFeatureId;

/**
 * A {@code FeatureHandle} is the entry point to managing and monitoring a <em>specific</em> {@code Feature}.
 * For example, it can be used to manage (create, modify and delete) a Feature's {@code Properties}.
 * <p>
 * Additionally, it provides the possibility to monitor a {@code Feature} by registering handlers to be notified about
 * {@code FeaturePropertyChange}s.
 * </p>
 * <p>
 * Note: All methods returning a {@link CompletionStage} are executed non-blocking and asynchronously.
 * Therefore, these methods return a {@code CompletionStage} object that will complete either successfully
 * if the operation was executed and confirmed, or exceptionally with a specific
 * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if it was executed but has failed.
 * </p>
 * Example:
 * <pre>
 * DittoClient client = ... ;
 * FeatureHandle myFeature = client.twin().forId("myThing").forFeature("smokeDetector");
 *
 * // Create a new property, define handler for success, and wait for completion
 * myFeature.putProperty(JsonFactory.newPointer("density"), 42)
 *    .thenAccept(_void -&gt; LOGGER.info("New property created successfully."))
 *    .get(1, TimeUnit.SECONDS); // this will block the current thread!
 * </pre>
 *
 * @since 1.0.0
 */
public interface FeatureHandle extends WithFeatureId, FeaturePropertiesManagement, FeatureDefinitionManagement,
        FeaturePropertiesChangeRegistration {

    /**
     * Deletes the {@code Feature} being handled by this {@code FeatureHandle}.
     *
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of the operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for deleting a
     * feature.
     */
    CompletionStage<Void> delete(Option<?>... options);

    /**
     * Retrieve the {@code Feature} being handled by this {@code FeatureHandle}.
     *
     * @return CompletionStage providing the requested Feature object, when completed successfully or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     */
    CompletionStage<Feature> retrieve();

    /**
     * Retrieves the {@code Feature} being handled by this {@code FeatureHandle}.
     *
     * @param options options that determine the behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the requested Feature object, when completed successfully or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws NullPointerException if {@code options} is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for retrieving a
     * feature.
     * @since 2.1.0
     */
    CompletionStage<Feature> retrieve(Option<?>... options);

    /**
     * Retrieve the {@code Feature} being handled by this {@code FeatureHandle}.
     *
     * @param fieldSelector a field selector object allowing to select a subset of fields on the Feature to be
     * retrieved.
     * @return CompletionStage providing the requested Feature object, when completed successfully or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     */
    CompletionStage<Feature> retrieve(JsonFieldSelector fieldSelector);

    /**
     * Retrieves the {@code Feature} being handled by this {@code FeatureHandle}.
     *
     * @param fieldSelector a field selector object allowing to select a subset of fields on the Feature to be
     * retrieved.
     * @param options options that determine the behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the requested Feature object, when completed successfully or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for retrieving a
     * feature.
     * @since 2.1.0
     */
    CompletionStage<Feature> retrieve(JsonFieldSelector fieldSelector, Option<?>... options);

}
