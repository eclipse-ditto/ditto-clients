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
package org.eclipse.ditto.client.live.events;

import java.util.UUID;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

/**
 * Factory for {@link org.eclipse.ditto.signals.events.base.Event Event}s which are emitted by live client "from
 * scratch", that is when something happened on a device, e.g. a property was modified. <p> Base interface for Factories
 * with a more specific scope, e.g. Things or Features. </p>
 *
 * @since 1.0.0
 */
public interface EventFactory {

    /**
     * Provides default {@link DittoHeaders} with a random {@code UUID} as {@code correlation-id} and the configured
     * {@code schemaVersion} and {@code source} of the Client instance.
     *
     * @return the built default {@code DittoHeaders}
     */
    default DittoHeaders getDefaultDittoHeaders() {
        return DittoHeaders.newBuilder()
                .correlationId(String.valueOf(UUID.randomUUID()))
                .schemaVersion(getSchemaVersion())
                .build();
    }

    /**
     * Returns the JsonSchemaVersion this Client was configured to handle.
     *
     * @return the JsonSchemaVersion this Client was configured to handle.
     */
    JsonSchemaVersion getSchemaVersion();

}
