/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import java.util.Optional;

/**
 * Context provided to registered {@code disconnectedListener}s provided when building {@code MessagingConfiguration}
 * of a new Ditto client instance.
 *
 * @since 2.1.0
 */
public interface DisconnectedContext {

    /**
     * Returns the {@link Source} providing the source of the disconnect.
     *
     * @return the source of the disconnect.
     */
    Source getSource();

    /**
     * Provides the optional cause (e.g. the last {@code DittoRuntimeException} or a websocket exception) before the
     * disconnect.
     *
     * @return the optional cause of the disconnect.
     */
    Optional<Throwable> getCause();

    /**
     * Returns a {@link DisconnectionHandler} providing means to either destroy the client channel, prevent an automatic
     * reconnect or to explicitly perform a reconnect.
     *
     * @return the disconnection handler providing options how to handle the disconnect.
     */
    DisconnectionHandler handleDisconnect();

    /**
     * An enumeration of the possible sources which initiated a disconnect.
     */
    enum Source {
        /**
         * The server closed the connection, potentially providing a cause before
         * (sent as {@code DittoRuntimeException} wrapped in an error {@code Adaptable} before).
         */
        SERVER,

        /**
         * The client closed the connection - without request by the user code.
         * This can e.g. happen if:
         * <ul>
         *   <li>authentication at the Ditto backend failed</li>
         *   <li>an initialization or network error occurred (e.g. unknown/unresolvable host, proxy problems)</li>
         *   <li>the WebSocket handshake with the provided Ditto backend endpoint failed</li>
         * </ul>
         */
        CLIENT,

        /**
         * The user code explicitly disconnected.
         */
        USER_CODE
    }

    /**
     * A handler provided to the user code providing options to handle an encountered disconnect.
     */
    interface DisconnectionHandler {

        /**
         * Closes the underlying channel (e.g. twin, live or policies channel) including their
         * {@code MessagingProvider}.
         *
         * @return this instance for chaining.
         */
        DisconnectionHandler closeChannel();

        /**
         * Prevents the client from doing a (configured) automatic reconnect upon disconnection.
         *
         * @param preventReconnect whether to prevent the automatic reconnect or not.
         * @return this instance for chaining.
         */
        DisconnectionHandler preventConfiguredReconnect(boolean preventReconnect);

        /**
         * Performs a reconnect independent from the configured automatic reconnect upon disconnection.
         * Can e.g. be used to perform a reconnect only for certain disconnection sources or causes.
         *
         * @return this instance for chaining.
         */
        DisconnectionHandler performReconnect();
    }

}
