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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;

public interface PolicyHandle {

    CompletableFuture<Policy> create(Option<?>... options);

    CompletableFuture<Policy> create(Policy policy, Option<?>... options);

    CompletableFuture<Policy> create(PolicyId policyId, Option<?>... options);

    CompletableFuture<Void> update(Option<?>... options);

    CompletableFuture<Optional<Policy>> put(Option<?>... options);

    CompletableFuture<Void> delete(Option<?>... options);

    CompletableFuture<Policy> retrieve();

    CompletableFuture<Policy> retrieve(JsonFieldSelector fieldSelector);

    PolicyId getPolicyEntityId();
}
