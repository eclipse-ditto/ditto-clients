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
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;

/**
 * A {@code Policy} provides the basic functionality, which can be used to manage (i.e. create and delete)
 * {@link org.eclipse.ditto.model.policies.Policy}.
 * <p>
 * Note: All methods returning a {@link java.util.concurrent.CompletableFuture} are executed non-blocking and asynchronously. Therefore,
 * these methods return a {@code CompletableFuture} object that will complete either successfully if the operation was
 * executed and confirmed, or exceptionally with a specific {@link org.eclipse.ditto.model.base.exceptions.DittoRuntimeException}
 * if it was executed but has failed.
 * </p>
 * Example:
 * <pre>
 * DittoClient client = ... ;
 *
 * // Create a new Policy, define handler for success, and wait for completion
 * client.policies().create(myPolicyId).thenAccept(policy -&gt;
 *    LOGGER.info("Policy created: {}", policy)
 * ).get(1, TimeUnit.SECONDS); // this will block the current thread!
 * </pre>
 *
 * @since 1.1.0
 */
public interface Policies {

    /**
     * Creates the given {@link org.eclipse.ditto.model.policies.Policy}.
     *
     * @param policy the Thing to be created.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing the created Thing object or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code policy} is {@code null} or has no identifier.
     * @throws org.eclipse.ditto.model.policies.PolicyIdInvalidException if the {@code policyId} was invalid.
     */
    CompletableFuture<Policy> create(Policy policy, Option<?>... options);

    /**
     * Creates a {@link org.eclipse.ditto.model.policies.Policy} based on the given {@link org.eclipse.ditto.json.JsonObject}.
     *
     * @param jsonObject a JSON object representation of the Policy to be created. The provided JSON object is required to
     * contain a field named {@code "policyId"} of the basic JSON type String which contains the identifier of the Policy
     * to be created. It must conform to the namespaced entity ID notation (see Ditto documentation).
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing the created Policy object or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code policy} is {@code null} or if it does not contain the field named
     * {@code "policyId"}.
     * @throws org.eclipse.ditto.model.base.exceptions.DittoJsonException if {@code policy} cannot be parsed to a {@link
     * org.eclipse.ditto.model.policies.Policy}.
     * @throws org.eclipse.ditto.model.policies.PolicyIdInvalidException if the {@code policyId} was invalid.
     */
    CompletableFuture<Policy> create(JsonObject jsonObject, Option<?>... options);

    /**
     * Puts the given {@link org.eclipse.ditto.model.policies.Policy}, which means that the Thing might be created or updated. The behaviour can be
     * restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param policy the Policy to be put.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing an {@link java.util.Optional} containing the created Policy object, in case the Policy
     * has been created, or an empty Optional, in case the Policy has been updated. Provides a {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code policy} is {@code null} or has no identifier.
     * @since 1.0.0
     */
    CompletableFuture<Optional<Policy>> put(Policy policy, Option<?>... options);

    /**
     * Puts a {@link org.eclipse.ditto.model.policies.Policy} based on the given {@link JsonObject}, which means that the Policy might be created or
     * updated. The behaviour can be restricted with option {@link org.eclipse.ditto.client.options.Options.Modify#exists(boolean)}.
     *
     * @param jsonObject a JSON object representation of the Policy to be put. The provided JSON object is required to contain
     * a field named {@code "policyId"} of the basic JSON type String which contains the identifier of the Policy to be
     * put.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing an {@link Optional} containing the created Policy object, in case the Policy
     * has been created, or an empty Optional, in case the Policy has been updated. Provides a {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code policy} is {@code null} or if it does not contain the field named
     * {@code "policyId"}.
     * @throws org.eclipse.ditto.model.base.exceptions.DittoJsonException if {@code policy} cannot be parsed to a {@link
     * org.eclipse.ditto.model.policies.Policy}.
     */
    CompletableFuture<Optional<Policy>> put(JsonObject jsonObject, Option<?>... options);

    /**
     * Updates the given {@link org.eclipse.ditto.model.policies.Policy} if it does exist.
     *
     * @param policy the Policy to be updated.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing {@code null} in case of success or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code policy} is {@code null} or has no identifier.
     */
    CompletableFuture<Void> update(Policy policy, Option<?>... options);

    /**
     * Updates a {@link org.eclipse.ditto.model.policies.Policy} if it does exist based on the given {@link JsonObject}.
     *
     * @param jsonObject a JSON object representation of the Policy to be updated. The provided JSON object is required to
     * contain a field named {@code "policyId"} of the basic JSON type String which contains the identifier of the Policy
     * to be updated.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing {@code null} in case of success or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed.
     * @throws IllegalArgumentException if {@code policy} is {@code null} or if it does not contain the field named
     * {@code "policyId"}.
     * @throws org.eclipse.ditto.model.base.exceptions.DittoJsonException if {@code policy} cannot be parsed to a {@link
     * org.eclipse.ditto.model.policies.Policy}.
     */
    CompletableFuture<Void> update(JsonObject jsonObject, Option<?>... options);

    /**
     * Deletes the {@link org.eclipse.ditto.model.policies.Policy} specified by the given identifier.
     *
     * @param policyId the identifier of the Policy to be deleted.
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future for handling the result of deletion or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if {@code policyId} is {@code null}.
     */
    CompletableFuture<Void> delete(PolicyId policyId, Option<?>... options);

    /**
     * Gets a {@link org.eclipse.ditto.model.policies.Policy}s specified by the given identifier. The result contains only existing and readable
     * Policies.
     *
     * @param policyId the identifier of the Policy to be retrieved.
     * @return completable future providing the requested Policies, an empty list or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    CompletableFuture<Policy> retrieve(PolicyId policyId);
}
