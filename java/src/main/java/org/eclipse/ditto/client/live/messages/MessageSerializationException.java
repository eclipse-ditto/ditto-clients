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
package org.eclipse.ditto.client.live.messages;

/**
 * Thrown in case a {@link org.eclipse.ditto.messages.model.Message}'s payload could not be serialized/deserialized
 * appropriately.
 *
 * @since 1.0.0
 */
public class MessageSerializationException extends RuntimeException {

    private static final long serialVersionUID = -8181362444215858266L;

    /**
     * Constructs a new {@code MessageSerializationException} object.
     *
     * @param message the detailed error message.
     */
    public MessageSerializationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code MessageSerializationException} object.
     *
     * @param message the detailed error message.
     * @param cause the cause of the Exception.
     */
    public MessageSerializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
