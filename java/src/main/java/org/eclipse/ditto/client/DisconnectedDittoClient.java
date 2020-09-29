/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client;

import java.util.concurrent.CompletionStage;

/**
 * Client interface before connecting.
 *
 * @since 1.3.0
 */
public interface DisconnectedDittoClient {

    /**
     * Connect the client to the configured Ditto back-end.
     * If this method is called more than once, the result is not defined.
     *
     * @return a future that completes with the connected client.
     */
    CompletionStage<DittoClient> connect();

    /**
     * Release resources held by this client.
     */
    void destroy();
}
