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

import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.model.policies.Policy;

/**
 * A {@code PolicyHandle} is the entry point to managing and monitoring a <em>specific</em> {@code Policy}. It can be used
 * to manage (create, modify and delete) a Policy.
 * <p>
 * * Note: All methods returning a {@link java.util.concurrent.CompletableFuture} are executed non-blocking and asynchronously. Therefore,
 * * these methods return a {@code CompletableFuture} object that will complete either successfully if the operation was
 * * executed and confirmed, or exceptionally with a specific {@link org.eclipse.ditto.model.base.exceptions.DittoRuntimeException}
 * * if it was executed but has failed.
 * * </p>
 * * Example:
 * * <pre>
 *  * DittoClient client = ... ;
 *  * PolicyHandle myPolicy = client.policies.forId("myPolicy");
 *  *
 *  *
 * // Update a Policy, define handler for success, and wait for completion
 * myPolicy.update(policy)
 *    .thenAccept(_void -&gt; LOGGER.info("Policy updated successfully."))
 *    .get(1, TimeUnit.SECONDS); // this will block the current thread!
 * </pre>
 *
 * @since 1.0.0
 */
public interface PolicyHandle {

    /**
     * Deletes the {@code Policy} object being handled by this {@code PolicyHandle}.
     *
     * @param options options to be applied configuring behaviour of this method, see {@link
     * org.eclipse.ditto.client.options.Options}.
     * @return completable future providing the created Thing object or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     */
    CompletableFuture<Void> delete(Option<?>... options);

    /**
     * Retrieve the {@code Policy} object being handled by this {@code PolicyHandle}.
     *
     * @return completable future providing the requested {@link org.eclipse.ditto.model.policies.Policy} or a specific {@link
     * org.eclipse.ditto.model.base.exceptions.DittoRuntimeException} if the operation failed
     */
    CompletableFuture<Policy> retrieve();
}
