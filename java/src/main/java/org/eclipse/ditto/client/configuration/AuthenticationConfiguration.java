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
package org.eclipse.ditto.client.configuration;

import java.util.Map;

/**
 * Interface for configuration provider specifying the necessary information for authenticating a client at Eclipse
 * Ditto.
 *
 * @since 1.0.0
 */
public interface AuthenticationConfiguration {

    /**
     * Returns the session identifier for this client - has to be unique for each newly instantiated client.
     *
     * @return the session identifier for this client
     */
    String getClientSessionId();

    /**
     * Returns additional header fields which should be used during authentication.
     *
     * @return additional header fields which should be used during authentication.
     */
    Map<String, String> getAdditionalHeaders();
}
