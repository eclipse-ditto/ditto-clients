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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.registration.FeatureChangeRegistration;
import org.eclipse.ditto.client.registration.ThingAttributeChangeRegistration;
import org.eclipse.ditto.client.registration.ThingChangeRegistration;
import org.eclipse.ditto.client.registration.ThingFeaturePropertiesChangeRegistration;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;

/**
 * A {@code CommonManagement} provides the basic functionality, which can be used to manage (i.e., create and delete)
 * {@link Thing}s.
 * <p>
 * Additionally, It provides aggregated functionality to monitor <em>all</em> Things by allowing the user register
 * handlers to be notified about {@link Change}s and {@link ThingChange}s.
 * </p>
 * <p>
 * Note: All methods returning a {@link CompletableFuture} are executed non-blocking and asynchronously. Therefore,
 * these methods return a {@code CompletableFuture} object that will complete either successfully if the operation was
 * executed and confirmed, or exceptionally with a specific {@link org.eclipse.ditto.model.base.exceptions.DittoRuntimeException}
 * if it was executed but has failed.
 * </p>
 * Example:
 * <pre>
 * DittoClient client = ... ;
 *
 * // Create a new thing, define handler for success, and wait for completion
 * client.twin().create("myThing").thenAccept(thing -&gt;
 *    LOGGER.info("Thing created: {}", thing)
 * ).get(1, TimeUnit.SECONDS); // this will block the current thread!
 *
 * // Register for changes of *all* things
 * client.twin().registerForThingChanges("allThingsReg", change -&gt;
 *    LOGGER.info("change received: {}", change));
 * </pre>
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Thing}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public interface CommonManagement<T extends ThingHandle, F extends FeatureHandle>
        extends ThingAttributeChangeRegistration, ThingChangeRegistration, FeatureChangeRegistration,
        ThingFeaturePropertiesChangeRegistration {

    /**
     * Parameter used for only subscribing for changes/messages/commands of specific namespaces at the backend.
     */
    String CONSUMPTION_PARAM_NAMESPACES = "namespaces";

    /**
     * Parameter used for only subscribing for changes matching an RQL filter at the backend.
     */
    String CONSUMPTION_PARAM_FILTER = "filter";

    /**
     * Creates a new instance of {@link ThingHandle} which aggregates all operations of an already existing {@link
     * Thing} specified by the given identifier.
     *
     * @param thingId the identifier of the Thing to create the handle for.
     * @return the ThingHandle for the provided {@code thingId}.
     * @throws IllegalArgumentException if {@code thingId} is {@code null}.
     */
    T forId(ThingId thingId);

    /**
     * Creates a new instance of {@link FeatureHandle} which aggregates all operations of an already existing {@link
     * org.eclipse.ditto.model.things.Feature Feature} specified by the given Thing plus Feature identifiers.
     *
     * @param thingId the identifier of the Thing containing the Feature to create the handle for.
     * @param featureId the identifier of the Feature to create the handle for.
     * @return the FeatureHandle for the provided {@code thingId}, {@code featureId} combination.
     * @throws IllegalArgumentException if {@code thingId} or {@code featureId} is {@code null}.
     */
    F forFeature(ThingId thingId, String featureId);

    /**
     * Creates a new instance of {@link PolicyHandle} which aggregates all operations of an already existing {@link
     * org.eclipse.ditto.model.policies.Policy}.
     *
     * @param policyId the identifier of the Policy.
     * @return the PolicyHandle for the provided {@code policyId}.
     * @throws IllegalArgumentException if {@code policyId} is {@code null}.
     */
    PolicyHandle forPolicy(PolicyId policyId);
    /**
     * Start consuming changes (for {@code twin()} and additionally messages and commands (for {@code live()}.
     *
     * @return a CompletableFuture that terminates when the start operation was successful.
     */
    CompletableFuture<Void> startConsumption();

    /**
     * Start consuming changes (for {@code twin()} and additionally messages and commands (for {@code live()} with the
     * passed {@code consumptionOptions}.
     *
     * @param consumptionOptions specifies the {@link org.eclipse.ditto.client.options.Options.Consumption
     * ConsumptionOptions} to apply. Pass them in via:
     * <pre>{@code Options.Consumption.namespaces("org.eclipse.ditto.namespace1","org.eclipse.ditto.namespace2");
     * Options.Consumption.filter("gt(attributes/counter,42)");}
     * </pre>
     * @return a CompletableFuture that terminates when the start operation was successful.
     */
    CompletableFuture<Void> startConsumption(Option<?>... consumptionOptions);

    /**
     * Suspend consuming events from Eclipse Ditto.
     *
     * @return a CompletableFuture that terminates when the suspend operation was successful.
     */
    CompletableFuture<Void> suspendConsumption();

    /**
     * Creates an empty {@link Thing} with an auto-generated identifier.
     *
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing the created Thing object or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     */
    CompletableFuture<Thing> create(Option<?>... options);

    /**
     * Creates an empty {@link Thing} for the given identifier.
     *
     * @param thingId the identifier of the Thing to be created. It must conform to the namespaced
     * entity ID notation (see Ditto documentation).
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing the created Thing object or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code thingId} is {@code null} or empty.
     * @throws org.eclipse.ditto.model.things.ThingIdInvalidException if the {@code thingId} was invalid.
     */
    CompletableFuture<Thing> create(ThingId thingId, Option<?>... options);

    /**
     * Creates the given {@link Thing}.
     *
     * @param thing the Thing to be created.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing the created Thing object or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier.
     * @throws org.eclipse.ditto.model.things.ThingIdInvalidException if the {@code thingId} was invalid.
     */
    CompletableFuture<Thing> create(Thing thing, Option<?>... options);

    /**
     * Creates a {@link Thing} based on the given {@link JsonObject}.
     *
     * @param thing a JSON object representation of the Thing to be created. The provided JSON object is required to
     * contain a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing
     * to be created. It must conform to the namespaced entity ID notation (see Ditto documentation).
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing the created Thing object or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"}.
     * @throws org.eclipse.ditto.model.base.exceptions.DittoJsonException if {@code thing} cannot be parsed to a {@link
     * Thing}.
     * @throws org.eclipse.ditto.model.things.ThingIdInvalidException if the {@code thingId} was invalid.
     */
    CompletableFuture<Thing> create(JsonObject thing, Option<?>... options);

    /**
     * Puts the given {@link Thing}, which means that the Thing might be created or updated. The behaviour can be
     * restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing the Thing to be put.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier.
     * @since 1.0.0
     */
    CompletableFuture<Optional<Thing>> put(Thing thing, Option<?>... options);

    /**
     * Puts a {@link Thing} based on the given {@link JsonObject}, which means that the Thing might be created or
     * updated. The behaviour can be restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing a JSON object representation of the Thing to be put. The provided JSON object is required to contain
     * a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing to be
     * put.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"}.
     * @throws org.eclipse.ditto.model.base.exceptions.DittoJsonException if {@code thing} cannot be parsed to a {@link
     * Thing}.
     */
    CompletableFuture<Optional<Thing>> put(JsonObject thing, Option<?>... options);

    /**
     * Updates the given {@link Thing} if it does exist.
     *
     * @param thing the Thing to be updated.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing {@code null} in case of success or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier.
     */
    CompletableFuture<Void> update(Thing thing, Option<?>... options);

    /**
     * Updates a {@link Thing} if it does exist based on the given {@link JsonObject}.
     *
     * @param thing a JSON object representation of the Thing to be updated. The provided JSON object is required to
     * contain a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing
     * to be updated.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing {@code null} in case of success or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"}.
     * @throws org.eclipse.ditto.model.base.exceptions.DittoJsonException if {@code thing} cannot be parsed to a {@link
     * Thing}.
     */
    CompletableFuture<Void> update(JsonObject thing, Option<?>... options);

    /**
     * Deletes the {@link Thing} specified by the given identifier.
     *
     * @param thingId the identifier of the Thing to be deleted.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future for handling the result of deletion or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code thingId} is {@code null}.
     */
    CompletableFuture<Void> delete(ThingId thingId, Option<?>... options);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things. If only some identifiers are not existing or if they are not readable the other ones will be returned.
     *
     * @param thingId the first identifier of the Thing to be retrieved.
     * @param thingIds additional identifiers of Things to be retrieved.
     * @return completable future providing the requested Things, an empty list or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    CompletableFuture<List<Thing>> retrieve(ThingId thingId, ThingId... thingIds);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things.
     *
     * @param fieldSelector a field selector allowing to select a subset of fields on the Things to be retrieved.
     * @param thingId the first identifier of the Thing to be retrieved.
     * @param thingIds additional identifiers of Things to be retrieved.
     * @return completable future providing the requested Things, an empty list or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    CompletableFuture<List<Thing>> retrieve(JsonFieldSelector fieldSelector, ThingId thingId, ThingId... thingIds);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things.
     *
     * @param thingIds the identifiers of the Things to be retrieved.
     * @return completable future providing the requested Things, an empty list or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code thingIds} is {@code null}.
     */
    CompletableFuture<List<Thing>> retrieve(Iterable<ThingId> thingIds);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things.
     *
     * @param fieldSelector a field selector allowing to select a subset of fields on the Things to be retrieved.
     * @param thingIds the identifiers of the Things to be retrieved.
     * @return completable future providing the requested Things, an empty list or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    CompletableFuture<List<Thing>> retrieve(JsonFieldSelector fieldSelector, Iterable<ThingId> thingIds);

}
