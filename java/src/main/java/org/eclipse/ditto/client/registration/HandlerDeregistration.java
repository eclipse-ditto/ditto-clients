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

/**
 * Defines the functionality for unregistering arbitrary handlers, e.g. handlers for receiving messages or changes.
 *
 * @since 1.0.0
 */
public interface HandlerDeregistration {

    /**
     * Unregisters the handler which has been registered with the given {@code registrationId}.
     *
     * @param registrationId the identifier of the handler to be unregistered.
     * @return {@code true}, if the handler has been unregistered; {@code false}, if no handler for the given {@code
     * registrationId} exists.
     * @throws IllegalArgumentException if parameter {@code registrationId} is {@code null}.
     */
    boolean deregister(String registrationId);

}
