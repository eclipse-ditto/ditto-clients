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

import java.util.function.Consumer;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.ThingChange;

/**
 * Provides the functionality for registering handlers which are notified about {@link
 * org.eclipse.ditto.things.model.Thing} changes.
 *
 * @since 1.0.0
 */
public interface ThingChangeRegistration extends HandlerDeregistration {

    /**
     * Registers a {@link Consumer} which is notified about {@link ThingChange}s. <p> If registered for a specific
     * Thing, it will only be notified of changes of that specific Thing. Otherwise, it will receive changes of
     * <em>all</em> Things. </p> Example:
     * <pre>
     * DittoClient client = ...
     * ThingHandle myThing = client.twin().forId("myThing");
     *
     * myThing.registerForThingChanges(HANDLER_ID, thingChange -&gt; LOGGER.info("change received: {}", thingChange));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * The {@code registrationId} needs to be unique per {@link DittoClient} instance.
     * @param handler the {@code Consumer} to handle change notifications.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForThingChanges(String registrationId, Consumer<ThingChange> handler);

}
