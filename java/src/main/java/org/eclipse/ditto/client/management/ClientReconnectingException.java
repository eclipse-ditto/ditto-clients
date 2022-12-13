/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import javax.annotation.concurrent.Immutable;

/**
 * This exception is thrown in the Ditto client if the client is in a reconnecting state and thus can't send messages
 * to the backend.
 *
 * @since 3.0.0
 */
@Immutable
public class ClientReconnectingException extends RuntimeException {

    private static final long serialVersionUID = -4578923424099138760L;

    private static final String MESSAGE = "Message could not be sent, because the client is currently " +
            "reconnecting.";

    private ClientReconnectingException() {
        super(MESSAGE);
    }

    public static ClientReconnectingException newInstance() {
        return new ClientReconnectingException();
    }

}