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
import org.eclipse.ditto.client.registration.FeatureChangeRegistration;
import org.eclipse.ditto.client.registration.ThingAttributeChangeRegistration;
import org.eclipse.ditto.client.registration.ThingChangeRegistration;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.WithThingId;

/**
 * A {@code ThingHandle} is the entry point to managing and monitoring a <em>specific</em> {@code Thing}. It can for
 * example be used to manage (create, modify and delete) a Thing's {@code Attributes} and {@code Features}.
 * <p>
 * Additionally, It provides the possibility to monitor a {@code Thing} by registering handlers to be notified about
 * {@code ThingAttributeChange}s and {@code ThingChange}s.
 * </p>
 * <p>
 * Note: All methods returning a {@link CompletionStage} are executed non-blocking and asynchronously. Therefore,
 * these methods return a {@code CompletionStage} object that will complete either successfully if the operation was
 * executed and confirmed, or exceptionally with a specific
 * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if it was executed but has failed.
 * </p>
 * Example:
 * <pre>
 * DittoClient client = ... ;
 * ThingHandle myThing = client.twin().forId("myThing");
 *
 * // Create a new attribute, define handler for success, and wait for completion
 * myThing.putAttribute(JsonFactory.newPointer("address/city"), "Berlin")
 *    .thenAccept(_void -&gt; LOGGER.info("New attribute created successfully."))
 *    .get(1, TimeUnit.SECONDS); // this will block the current thread!
 *
 * // Register for changes of the Thing
 * myThing.registerForThingChanges("myThingReg", change -&gt; LOGGER.info("change received: {}", change));
 * </pre>
 *
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public interface ThingHandle<F extends FeatureHandle>
        extends WithThingId, ThingAttributeManagement, ThingAttributeChangeRegistration, FeatureChangeRegistration,
        ThingChangeRegistration {

    /**
     * Creates a new instance of {@link FeatureHandle} which aggregates all operations of an already existing
     * {@link Feature} specified by the given identifier.
     *
     * @param featureId the identifier of the Feature to create the handle for.
     * @return the handle for the provided {@code featureId}.
     * @throws IllegalArgumentException if {@code featureId} is {@code null}.
     */
    F forFeature(String featureId);

    /**
     * Deletes the {@code Thing} object being handled by this {@code ThingHandle}.
     *
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of the operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for deleting
     * a thing.
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     */
    CompletionStage<Void> delete(Option<?>... options);

    /**
     * Retrieve the {@code Thing} object being handled by this {@code ThingHandle}.
     *
     * @return CompletionStage providing the requested {@link Thing} or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     */
    CompletionStage<Thing> retrieve();

    /**
     * Retrieves the {@code Thing} object being handled by this {@code ThingHandle}.
     *
     * @param options options that determine the behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the requested {@link Thing} or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws NullPointerException if {@code options} is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for retrieving
     * a thing.
     * @since 2.1.0
     */
    CompletionStage<Thing> retrieve(Option<?>... options);

    /**
     * Retrieve the {@code Thing} object being handled by this {@code ThingHandle}.
     *
     * @param fieldSelector a field selector object allowing to select a subset of fields on the Thing to be retrieved.
     * @return CompletionStage providing the requested {@link Thing} or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     */
    CompletionStage<Thing> retrieve(JsonFieldSelector fieldSelector);

    /**
     * Retrieves the {@code Thing} object being handled by this {@code ThingHandle}.
     *
     * @param fieldSelector a field selector object allowing to select a subset of fields on the Thing to be retrieved.
     * @param options options that determine the behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the requested {@link Thing} or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for retrieving
     * a thing.
     * @since 2.1.0
     */
    CompletionStage<Thing> retrieve(JsonFieldSelector fieldSelector, Option<?>... options);

    /**
     * Sets the given {@code policyId} to this Thing.
     *
     * @param policyId the PolicyId of the Policy to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of the operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code policyId} is {@code null} or if {@code options} contains an option
     * that is not allowed for setting a policy ID to a thing.
     * @since 1.1.0
     */
    CompletionStage<Void> setPolicyId(PolicyId policyId, Option<?>... options);

    /**
     * Merge the given {@code policyId} to this Thing.
     *
     * @param policyId the PolicyId of the Policy to be merged.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of the operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code policyId} is {@code null} or if {@code options} contains an option
     * that is not allowed for merging a policy ID to a thing.
     * @since 2.0.0
     */
    CompletionStage<Void> mergePolicyId(PolicyId policyId, Option<?>... options);

    /**
     * Sets the given {@code Features} to this Thing. All existing Features are replaced.
     *
     * @param features the Features to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of the operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code features} is {@code null} or if {@code options} contains an option
     * that is not allowed for setting features to a thing.
     */
    CompletionStage<Void> setFeatures(Features features, Option<?>... options);

    /**
     * Merges the given {@code Features} to this Thing.
     *
     * @param features the Features to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of the operation or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code features} is {@code null} or if {@code options} contains an option
     * that is not allowed for merging features to a thing.
     * @since 2.0.0
     */
    CompletionStage<Void> mergeFeatures(Features features, Option<?>... options);

    /**
     * Updates the given Feature of this Thing or creates a new one if it does not yet exist.
     *
     * @param feature Feature to be set.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code feature} is {@code null} or if {@code options} contains an option
     * that is not allowed for putting a feature to a thing.
     */
    CompletionStage<Void> putFeature(Feature feature, Option<?>... options);

    /**
     * Merges the given Feature of this Thing or creates a new one if it does not yet exist.
     *
     * @param feature Feature to be merged.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of this operation or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code feature} is {@code null} or if {@code options} contains an option
     * that is not allowed for merging a feature to a thing.
     * @since 2.0.0
     */
    CompletionStage<Void> mergeFeature(Feature feature, Option<?>... options);

    /**
     * Deletes the Feature by the given identifier from this Thing.
     *
     * @param featureId the identifier of the Feature to be deleted.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the deletion or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code featureId} is {@code null} or if {@code options} contains an option
     * that is not allowed for deleting a feature from a thing.
     */
    CompletionStage<Void> deleteFeature(String featureId, Option<?>... options);

    /**
     * Deletes all Features from this Thing.
     *
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the deletion or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException or if {@code options} contains an option that is not allowed for deleting
     * features from a thing.
     */
    CompletionStage<Void> deleteFeatures(Option<?>... options);

}
