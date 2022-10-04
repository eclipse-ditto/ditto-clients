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
package org.eclipse.ditto.client.policies;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.things.model.Thing;

/**
 * A {@code Policy} provides the basic functionality, which can be used to manage (i.e. create and delete)
 * a {@link org.eclipse.ditto.policies.model.Policy}.
 * <p>
 * Note: All methods returning a {@link CompletionStage} are executed non-blocking and asynchronously. Therefore,
 * these methods return a {@code CompletionStage} object that will complete either successfully if the operation was
 * executed and confirmed, or exceptionally with a specific
 * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if it was executed but has failed.
 * </p>
 * Example:
 * <pre>
 * DittoClient client = ... ;
 *
 * // Create a new Policy, define handler for success, and wait for completion
 * client.policies().create(myPolicy)
 *    .thenAccept(policy -&gt; LOGGER.info("Policy created: {}", policy))
 *    .get(1, TimeUnit.SECONDS); // this will block the current thread!
 * </pre>
 *
 * @since 1.1.0
 */
public interface Policies {

    /**
     * Creates the given {@link org.eclipse.ditto.policies.model.Policy}.
     *
     * @param policy the Policy to be created.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Policy object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code policy} is {@code null} or has no identifier.
     * @throws org.eclipse.ditto.policies.model.PolicyIdInvalidException if the {@code policyId} was invalid.
     */
    CompletionStage<Policy> create(Policy policy, Option<?>... options);

    /**
     * Creates a {@link org.eclipse.ditto.policies.model.Policy} based on the given
     * {@link org.eclipse.ditto.json.JsonObject}.
     *
     * @param jsonObject a JSON object representation of the Policy to be created.
     * The provided JSON object is required to contain a field named {@code "policyId"} of the basic JSON type String
     * which contains the identifier of the Policy to be created. It must conform to the namespaced entity ID notation
     * (see Ditto documentation).
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the created Policy object or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code jsonObject} is {@code null} or if it does not contain the field named
     * {@code "policyId"}.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code jsonObject} cannot be parsed to a
     * {@link org.eclipse.ditto.policies.model.Policy}.
     * @throws org.eclipse.ditto.policies.model.PolicyIdInvalidException if the {@code policyId} was invalid.
     */
    CompletionStage<Policy> create(JsonObject jsonObject, Option<?>... options);

    /**
     * Puts the given {@link org.eclipse.ditto.policies.model.Policy}, which means that the Policy might be created or
     * updated. The behaviour can be restricted with option
     * {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param policy the Policy to be put.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link java.util.Optional} containing the created Policy object,
     * in case the Policy has been created, or an empty Optional, in case the Policy has been updated.
     * Provides a {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code policy} is {@code null} or has no identifier.
     */
    CompletionStage<Optional<Policy>> put(Policy policy, Option<?>... options);

    /**
     * Puts a {@link org.eclipse.ditto.policies.model.Policy} based on the given {@link JsonObject}, which means that
     * the Policy might be created or updated. The behaviour can be restricted with option
     * {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param jsonObject a JSON object representation of the Policy to be put. The provided JSON object is required
     * to contain a field named {@code "policyId"} of the basic JSON type String which contains the identifier of the
     * Policy to be put.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing an {@link Optional} containing the created Policy object, in case the Policy
     * has been created, or an empty Optional, in case the Policy has been updated.
     * Provides a {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code jsonObject} is {@code null} or if it does not contain the field named
     * {@code "policyId"}.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code jsonObject} cannot be parsed to a
     * {@link org.eclipse.ditto.policies.model.Policy}.
     */
    CompletionStage<Optional<Policy>> put(JsonObject jsonObject, Option<?>... options);

    /**
     * Updates the given {@link org.eclipse.ditto.policies.model.Policy} if it does exist.
     *
     * @param policy the Policy to be updated.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing for handling a successful update or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code policy} is {@code null} or has no identifier.
     */
    CompletionStage<Void> update(Policy policy, Option<?>... options);

    /**
     * Updates a {@link org.eclipse.ditto.policies.model.Policy} if it does exist based on the given {@link JsonObject}.
     *
     * @param jsonObject a JSON object representation of the Policy to be updated.
     * The provided JSON object is required to contain a field named {@code "policyId"} of the basic JSON type String
     * which contains the identifier of the Policy to be updated.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing for handling a successful update or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code jsonObject} is {@code null} or if it does not contain the field named
     * {@code "policyId"}.
     * @throws org.eclipse.ditto.base.model.exceptions.DittoJsonException if {@code jsonObject} cannot be parsed to a
     * {@link org.eclipse.ditto.policies.model.Policy}.
     */
    CompletionStage<Void> update(JsonObject jsonObject, Option<?>... options);

    /**
     * Deletes the {@link org.eclipse.ditto.policies.model.Policy} specified by the given identifier.
     *
     * @param policyId the identifier of the Policy to be deleted.
     * @param options options to be applied configuring behaviour of this method,
     * see {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage for handling the result of deletion or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code policyId} is {@code null}.
     */
    CompletionStage<Void> delete(PolicyId policyId, Option<?>... options);

    /**
     * Gets the {@code Policy} specified by the given identifier.
     *
     * @param policyId the identifier of the Policy to be retrieved.
     * @return CompletionStage providing the requested Policy or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws IllegalArgumentException if {@code policyId} is {@code null}.
     */
    CompletionStage<Policy> retrieve(PolicyId policyId);


    /**
     * Gets the {@code Policy} specified by the given identifier with the given options.
     *
     * @param policyId the policyId to retrieve.
     * @param options options that determine the behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the requested {@link Thing} or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws NullPointerException if {@code options} is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for retrieving
     * a thing.
     * @since 2.4.0
     */
    CompletionStage<Policy> retrieve(PolicyId policyId, Option<?>... options);

    /**
     * Retrieve the {@code Policy} specified by the given identifier, containing the fields specified by
     * the given {@code fieldSelector}.
     *
     * @param policyId the policyId to retrieve.
     * @param fieldSelector a field selector object allowing to select a subset of fields on the Policy to be retrieved.
     * @return CompletionStage providing the requested {@link Policy} or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @since 2.4.0
     */
    CompletionStage<Policy> retrieve(PolicyId policyId, JsonFieldSelector fieldSelector);

    /**
     * Gets the {@code Policy} specified by the given identifier with the given options, containing the fields
     * specified by the given {@code fieldSelector}.
     *
     * @param policyId the policyId to retrieve.
     * @param fieldSelector a field selector object allowing to select a subset of fields on the Policy to be retrieved.
     * @param options options that determine the behaviour of this method, see
     * {@link org.eclipse.ditto.client.options.Options}.
     * @return CompletionStage providing the requested {@link Policy} or a specific
     * {@link org.eclipse.ditto.base.model.exceptions.DittoRuntimeException} if the operation failed.
     * If the client is reconnecting the CompletionStage fails with a
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains an option that is not allowed for retrieving
     * a policy.
     * @since 2.4.0
     */
    CompletionStage<Policy> retrieve(PolicyId policyId, JsonFieldSelector fieldSelector, Option<?>... options);
}
