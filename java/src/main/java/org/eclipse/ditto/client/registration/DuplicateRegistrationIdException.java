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
package org.eclipse.ditto.client.registration;

import static java.util.Objects.requireNonNull;

/**
 * Thrown when a message handler is tried to be registered with an already existing {@code registrationId}.
 *
 * @since 1.0.0
 */
public class DuplicateRegistrationIdException extends RuntimeException {

    private static final long serialVersionUID = 5847631638229404998L;

    /**
     * Constructs a new {@link DuplicateRegistrationIdException} object.
     *
     * @param registrationId the registration id
     */
    public DuplicateRegistrationIdException(final String registrationId) {
        super(String.format("Registration id already exists: '%s'", requireNonNull(registrationId)));
    }
}
