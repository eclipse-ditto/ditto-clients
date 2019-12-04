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
package org.eclipse.ditto.client.management.internal;

import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.management.PolicyHandle;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.model.policies.Policy;

public class PolicyHandleImpl implements PolicyHandle {

    @Override
    public CompletableFuture<Void> delete(final Option<?>... options) {
        return null;
    }

    @Override
    public CompletableFuture<Policy> retrieve() {
        return null;
    }

    @Override
    public CompletableFuture<Policy> retrieve(final JsonFieldSelector fieldSelector) {
        return null;
    }

}
