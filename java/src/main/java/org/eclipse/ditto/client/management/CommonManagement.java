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
import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.registration.FeatureChangeRegistration;
import org.eclipse.ditto.client.registration.ThingAttributeChangeRegistration;
import org.eclipse.ditto.client.registration.ThingChangeRegistration;
import org.eclipse.ditto.client.registration.ThingFeaturePropertiesChangeRegistration;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;

/**
 * A {@code CommonManagement} provides the basic functionality, which can be used to manage (i.e., create and delete)
 * {@link Thing}s.
 * <p>
 * Additionally, It provides aggregated functionality to monitor <em>all</em> Things by allowing the user register
 * handlers to be notified about {@link Change}s and {@link ThingChange}s.
 * </p>
 * <p>
 * Note: All methods returning a {@link CompletionStage} are executed non-blocking and asynchronously. Therefore,
 * these methods return a {@code CompletionStage} object that will complete either successfully if the operation was
 * executed and confirmed, or exceptionally with a specific {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException}
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
public interface CommonManagement<T extends ThingHandle<F>, F extends FeatureHandle>
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
     * Parameter used for adding extra fields when subscribing for messages/events at the backend.
     */
    String CONSUMPTION_PARAM_EXTRA_FIELDS = "extraFields";

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
     * org.eclipse.ditto.things.model.Feature Feature} specified by the given Thing plus Feature identifiers.
     *
     * @param thingId the identifier of the Thing containing the Feature to create the handle for.
     * @param featureId the identifier of the Feature to create the handle for.
     * @return the FeatureHandle for the provided {@code thingId}, {@code featureId} combination.
     * @throws IllegalArgumentException if {@code thingId} or {@code featureId} is {@code null}.
     */
    F forFeature(ThingId thingId, String featureId);

    /**
     * Start consuming changes (for {@code twin()} and additionally messages and commands (for {@code live()}.
     *
     * @return a CompletionStage that terminates when the start operation was successful or fails with
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException} if the client
     * is in a reconnecting state.
     */
    CompletionStage<Void> startConsumption();

    /**
     * Start consuming changes (for {@code twin()} and additionally messages and commands (for {@code live()} with the
     * passed {@code consumptionOptions}.
     *
     * @param consumptionOptions specifies the {@link org.eclipse.ditto.client.options.Options.Consumption
     * ConsumptionOptions} to apply. Pass them in via:
     * <pre>{@code Options.Consumption.namespaces("org.eclipse.ditto.namespace1","org.eclipse.ditto.namespace2");
     * Options.Consumption.filter("gt(attributes/counter,42)");}
     * </pre>
     * @return a CompletionStage that terminates when the start operation was successful or fails with
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException} if the client
     * is in a reconnecting state.
     */
    CompletionStage<Void> startConsumption(Option<?>... consumptionOptions);

    /**
     * Suspend consuming events from Eclipse Ditto.
     *
     * @return a CompletionStage that terminates when the suspend operation was successful or fails with
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException} if the client
     * is in a reconnecting state.
     */
    CompletionStage<Void> suspendConsumption();

    /**
     * Creates an empty {@link Thing} with an auto-generated identifier.
     *
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code options} contains an option
     * that is not allowed for creating a thing.
     */
    CompletionStage<Thing> create(Option<?>... options);

    /**
     * Creates an empty {@link Thing} for the given identifier.
     *
     * @param thingId the identifier of the Thing to be created. It must conform to the namespaced
     * entity ID notation (see Ditto documentation).
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thingId} is {@code null} or empty
     * or if {@code options} contains an option that is not allowed for
     * creating a thing.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     */
    CompletionStage<Thing> create(ThingId thingId, Option<?>... options);

    /**
     * Creates the given {@link Thing}.
     *
     * @param thing the Thing to be created.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific {@link
     * org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier
     * or if {@code options} contains an option that is not allowed for
     * creating a thing.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     */
    CompletionStage<Thing> create(Thing thing, Option<?>... options);

    /**
     * Creates a {@link Thing} based on the given {@link JsonObject}.
     *
     * @param thing a JSON object representation of the Thing to be created. The provided JSON object is required to
     * contain a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing
     * to be created. It must conform to the namespaced entity ID notation (see Ditto documentation).
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"} or if {@code options} contains an option that is not allowed for creating a thing.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code thing} cannot be parsed to a
     * {@link Thing}.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     * @since 1.0.0
     */
    CompletionStage<Thing> create(JsonObject thing, Option<?>... options);

    /**
     * Creates an empty {@link Thing} with an auto-generated identifier as well as an initial Policy.
     *
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * @throws IllegalArgumentException if {@code options} contains an option
     * that is not allowed for creating a thing.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * @since 1.1.0
     */
    CompletionStage<Thing> create(Policy initialPolicy, Option<?>... options);

    /**
     * Creates the given {@link Thing}.
     *
     * @param thing the Thing to be created.
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier, or if
     * {@code initialPolicy} is {@code null} or if {@code options} contains an option
     * that is not allowed for creating a thing.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     * @since 1.1.0
     */
    CompletionStage<Thing> create(Thing thing, JsonObject initialPolicy, Option<?>... options);

    /**
     * Creates an empty {@link Thing} for the given identifier.
     *
     * @param thingId the identifier of the Thing to be created. It must conform to the namespaced
     * entity ID notation (see Ditto documentation).
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thingId} is {@code null} or empty, or if {@code initialPolicy} is
     * {@code null} or if {@code options} contains an option that is not allowed for creating a thing.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     * @since 1.1.0
     */
    CompletionStage<Thing> create(ThingId thingId, JsonObject initialPolicy, Option<?>... options);

    /**
     * Creates a {@link Thing} based on the given {@link JsonObject}.
     *
     * @param thing a JSON object representation of the Thing to be created. The provided JSON object is required to
     * contain a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing
     * to be created. It must conform to the namespaced entity ID notation (see Ditto documentation).
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"}, or if {@code initialPolicy} is {@code null} or if {@code options} contains an option that
     * is not allowed for creating a thing.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code thing} cannot be parsed to a
     * {@link Thing}.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     * @since 1.1.0
     */
    CompletionStage<Thing> create(JsonObject thing, JsonObject initialPolicy, Option<?>... options);

    /**
     * Creates the given {@link Thing}.
     *
     * @param thing the Thing to be created.
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier, or if
     * {@code initialPolicy} is {@code null} or if {@code options} contains an option that
     * is not allowed for creating a thing.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     * @since 1.1.0
     */
    CompletionStage<Thing> create(Thing thing, Policy initialPolicy, Option<?>... options);

    /**
     * Creates an empty {@link Thing} for the given identifier.
     *
     * @param thingId the identifier of the Thing to be created. It must conform to the namespaced
     * entity ID notation (see Ditto documentation).
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thingId} is {@code null} or empty, or if {@code initialPolicy} is
     * {@code null} or if {@code options} contains an option that is not allowed for creating a thing.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     * @since 1.1.0
     */
    CompletionStage<Thing> create(ThingId thingId, Policy initialPolicy, Option<?>... options);

    /**
     * Creates a {@link Thing} based on the given {@link JsonObject}.
     *
     * @param thing a JSON object representation of the Thing to be created. The provided JSON object is required to
     * contain a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing
     * to be created. It must conform to the namespaced entity ID notation (see Ditto documentation).
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Thing object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"}, or if {@code initialPolicy} is {@code null} or if {@code options} contains an option that
     * is not allowed for creating a thing.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code thing} cannot be parsed to a
     * {@link Thing}.
     * @throws org.eclipse.ditto.things.model.ThingIdInvalidException if the {@code thingId} was invalid.
     * @since 1.1.0
     */
    CompletionStage<Thing> create(JsonObject thing, Policy initialPolicy, Option<?>... options);

    /**
     * Merge a {@link Thing} if it does exist based on the given {@link Thing}
     *
     * @param thingId the Thing to be merged.
     * @param thing which should be used for merged.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return completable future providing {@code null} in case of success or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code argument} is {@code null} or if {@code options} contains an option
     * that is not allowed for merging a thing.
     * @since 2.0.0
     */
    CompletionStage<Void> merge(ThingId thingId, Thing thing, Option<?>... options);

    /**
     * Merges a {@link Thing} if it does exist based on the given {@link JsonObject}.
     *
     * @param thingId the Thing to be merged.
     * @param thing a JSON object representation of the Thing which should be used for merged.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return completable future providing {@code null} in case of success or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code argument} is {@code null} or if {@code options} contains an option
     * that is not allowed for merging a thing.
     * @since 2.0.0
     */
    CompletionStage<Void> merge(ThingId thingId, JsonObject thing, Option<?>... options);

    /**
     * Puts the given {@link Thing}, which means that the Thing might be created or updated. The behaviour can be
     * restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing the Thing to be put.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier or if {@code options}
     * contains an option that is not allowed for putting a thing.
     * @since 1.0.0
     */
    CompletionStage<Optional<Thing>> put(Thing thing, Option<?>... options);

    /**
     * Puts a {@link Thing} based on the given {@link JsonObject}, which means that the Thing might be created or
     * updated. The behaviour can be restricted with option
     * {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing a JSON object representation of the Thing to be put. The provided JSON object is required to contain
     * a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing to be
     * put.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"} or if {@code options} contains an option that is not allowed for putting a thing.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code thing} cannot be parsed to a
     * {@link Thing}.
     * @since 1.0.0
     */
    CompletionStage<Optional<Thing>> put(JsonObject thing, Option<?>... options);

    /**
     * Puts the given {@link Thing}, which means that the Thing might be created or updated. The behaviour can be
     * restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing the Thing to be put.
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy. This will only apply if
     * the Thing does not already exist.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier, or if
     * {@code initialPolicy} is {@code null} or if {@code options} contains an option that is not allowed for putting
     * a thing.
     * @since 1.1.0
     */
    CompletionStage<Optional<Thing>> put(Thing thing, JsonObject initialPolicy, Option<?>... options);

    /**
     * Puts a {@link Thing} based on the given {@link JsonObject}, which means that the Thing might be created or updated.
     * The behaviour can be restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing a JSON object representation of the Thing to be put. The provided JSON object is required to contain
     * a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing to be
     * put.
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy. This will only apply if
     * the Thing does not already exist.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"}, or if {@code initialPolicy} is {@code null} or if {@code options} contains an option that is
     * not allowed for putting a thing.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code thing} cannot be parsed to a
     * {@link Thing}.
     * @since 1.1.0
     */
    CompletionStage<Optional<Thing>> put(JsonObject thing, JsonObject initialPolicy, Option<?>... options);

    /**
     * Puts the given {@link Thing}, which means that the Thing might be created or updated. The behaviour can be
     * restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing the Thing to be put.
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy. This will only apply if
     * the Thing does not already exist.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier, or if
     * {@code initialPolicy} is {@code null} or if {@code options} contains an option that is
     * not allowed for putting a thing.
     * @since 1.1.0
     */
    CompletionStage<Optional<Thing>> put(Thing thing, Policy initialPolicy, Option<?>... options);

    /**
     * Puts a {@link Thing} based on the given {@link JsonObject}, which means that the Thing might be created or
     * updated. The behaviour can be restricted with option
     * {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param thing a JSON object representation of the Thing to be put. The provided JSON object is required to contain
     * a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing to be
     * put.
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy. This will only apply if
     * the Thing does not already exist.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link Optional} containing the created Thing object, in case the Thing
     * has been created, or an empty Optional, in case the Thing has been updated. Provides a
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"}, or if {@code initialPolicy} is {@code null} or if {@code options} contains an option that is
     * not allowed for putting a thing.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code thing} cannot be parsed to a
     * {@link Thing}.
     * @since 1.1.0
     */
    CompletionStage<Optional<Thing>> put(JsonObject thing, Policy initialPolicy, Option<?>... options);

    /**
     * Updates the given {@link Thing} if it does exist.
     *
     * @param thing the Thing to be updated.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing {@code null} in case of success or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or has no identifier
     * or if {@code options} contains an option that is not allowed for updating a thing.
     */
    CompletionStage<Void> update(Thing thing, Option<?>... options);

    /**
     * Updates a {@link Thing} if it does exist based on the given {@link JsonObject}.
     *
     * @param thing a JSON object representation of the Thing to be updated. The provided JSON object is required to
     * contain a field named {@code "thingId"} of the basic JSON type String which contains the identifier of the Thing
     * to be updated.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing {@code null} in case of success or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thing} is {@code null} or if it does not contain the field named
     * {@code "thingId"} or if {@code options} contains an option that is not allowed for updating a thing.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code thing} cannot be parsed to a
     * {@link Thing}.
     */
    CompletionStage<Void> update(JsonObject thing, Option<?>... options);

    /**
     * Deletes the {@link Thing} specified by the given identifier.
     *
     * @param thingId the identifier of the Thing to be deleted.
     * @param options options to be applied configuring behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of deletion or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thingId} is {@code null} or if {@code options} contains an option
     * that is not allowed for updating a thing.
     */
    CompletionStage<Void> delete(ThingId thingId, Option<?>... options);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things. If only some identifiers are not existing or if they are not readable the other ones will be returned.
     *
     * @param thingId the first identifier of the Thing to be retrieved.
     * @param thingIds additional identifiers of Things to be retrieved.
     * @return CompletionStage providing the requested Things, an empty list or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    CompletionStage<List<Thing>> retrieve(ThingId thingId, ThingId... thingIds);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things.
     *
     * @param fieldSelector a field selector allowing to select a subset of fields on the Things to be retrieved.
     * @param thingId the first identifier of the Thing to be retrieved.
     * @param thingIds additional identifiers of Things to be retrieved.
     * @return CompletionStage providing the requested Things, an empty list or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    CompletionStage<List<Thing>> retrieve(JsonFieldSelector fieldSelector, ThingId thingId, ThingId... thingIds);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things.
     *
     * @param thingIds the identifiers of the Things to be retrieved.
     * @return CompletionStage providing the requested Things, an empty list or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if {@code thingIds} is {@code null}.
     */
    CompletionStage<List<Thing>> retrieve(Iterable<ThingId> thingIds);

    /**
     * Gets a list of {@link Thing}s specified by the given identifiers. The result contains only existing and readable
     * Things.
     *
     * @param fieldSelector a field selector allowing to select a subset of fields on the Things to be retrieved.
     * @param thingIds the identifiers of the Things to be retrieved.
     * @return CompletionStage providing the requested Things, an empty list or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * The CompletionStage fails with a {@link org.eclipse.ditto.client.management.ClientReconnectingException} if
     * the client is in a reconnecting state.
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    CompletionStage<List<Thing>> retrieve(JsonFieldSelector fieldSelector, Iterable<ThingId> thingIds);

}
