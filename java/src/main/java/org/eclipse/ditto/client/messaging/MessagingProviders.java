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
package org.eclipse.ditto.client.messaging;

import org.eclipse.ditto.client.messaging.websocket.WsProviderConfiguration;
import org.eclipse.ditto.client.messaging.websocket.internal.WsMessagingProviderConfigurationImpl;

/**
 * Contains static methods for instantiating entry points (Builders) to different {@link MessagingProvider}
 * implementations.
 *
 * @since 1.0.0
 */
public final class MessagingProviders {

    private MessagingProviders() {
        // no instantiation
    }

    /**
     * Creates a Builder for direct WebSocket communication Eclipse Ditto.
     *
     * @return the created Ditto WebSocket provider builder.
     */
    public static WsProviderConfiguration.AuthenticationConfigurationSettable
    dittoWebsocketProviderBuilder() {

        return WsMessagingProviderConfigurationImpl.newBuilder();
    }
}
