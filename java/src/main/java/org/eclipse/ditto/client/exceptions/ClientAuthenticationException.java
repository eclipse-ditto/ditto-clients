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
package org.eclipse.ditto.client.exceptions;

/**
 * This exception is thrown if an error occurred during authentication process of the Client.
 *
 * @since 1.0.0
 */
public class ClientAuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 4405868587578425044L;

    /**
     * Constructs a new {@code ClientAuthenticationException} object.
     *
     * @param message the detail message.
     */
    public ClientAuthenticationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ClientAuthenticationException} object.
     *
     * @param message the detail message.
     * @param cause the cause of the exception. A {@code null} value indicates that no cause exists or is unknown.
     */
    public ClientAuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
