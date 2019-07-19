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
package org.eclipse.ditto.client.messaging.websocket;

import org.eclipse.ditto.client.exceptions.ClientAuthenticationException;

/**
 * Exception thrown when there were errors during Ditto Websocket authentication.
 *
 * @since 1.0.0
 */
public final class WsAuthenticationException extends ClientAuthenticationException {

    private static final long serialVersionUID = 8327295757761677998L;

    /**
     * Constructs a new {@code AuthenticationException} instance.
     *
     * @param message the exception message.
     */
    public WsAuthenticationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code AuthenticationException} instance.
     *
     * @param message the exception message.
     * @param cause the cause of the exception.
     */
    public WsAuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
