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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.util.function.Consumer;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;

/**
 * Provides the necessary functionality for registering handlers which are notified about attribute changes.
 *
 * @since 1.0.0
 */
public interface ThingAttributeChangeRegistration extends HandlerDeregistration {

    /**
     * Registers a {@link Consumer} which is notified about <em>all</em> {@code attribute} changes. <p> If registered
     * for a <em>specific</em> Thing, it will be notified of <em>all</em> attribute changes of that Thing. Otherwise, it
     * will receive <em>all</em> attribute changes of <em>all</em> Things. </p> Example:
     * <pre>
     * DittoClient client = ...
     * ThingHandle myThing = client.twin().forId("myThing");
     *
     * myThing.registerForAttributeChanges(HANDLER_ID, change -&gt; LOGGER.info("attributeChange received: {}",
     * change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param handler the {@code Consumer} to handle attribute change notifications.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForAttributesChanges(String registrationId, Consumer<Change> handler);

    /**
     * Registers a {@link Consumer} which is notified about <em>specific</em> attribute changes. <p> If registered for
     * a
     * <em>specific</em> Thing, it will be notified of attribute changes of that Thing. Otherwise, it will receive
     * attribute changes of <em>all</em> Things. </p> Example:
     * <pre>
     * DittoClient client = ...
     * ThingHandle myThing = client.twin().forId("myThing");
     *
     * myThing.registerForAttributeChanges(HANDLER_ID, "address/city",
     *          change -&gt; LOGGER.info("attributeChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param path the path to the {@code attribute} entry of interest - may contain {@code "/"} for addressing nested
     * paths in a hierarchy.
     * @param handler the {@code Consumer} to handle attribute change notifications.
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    default void registerForAttributeChanges(String registrationId, CharSequence path, Consumer<Change> handler) {
        argumentNotNull(path);
        registerForAttributeChanges(registrationId, JsonFactory.newPointer(path), handler);
    }

    /**
     * Registers a {@link Consumer} which is notified about <em>specific</em> attribute changes. <p> If registered for
     * a
     * <em>specific</em> Thing, it will be notified of attribute changes of that Thing. Otherwise, it will receive
     * attribute changes of <em>all</em> Things. </p> Example:
     * <pre>
     * DittoClient client = ...
     * ThingHandle myThing = client.twin().forId("myThing");
     *
     * myThing.registerForAttributeChanges(HANDLER_ID, JsonFactory.newPointer("address/city"),
     *          change -&gt; LOGGER.info("attributeChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param path the path to the {@code attribute} of interested.
     * @param handler the {@code Consumer} to handle attribute change notifications.
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForAttributeChanges(String registrationId, JsonPointer path, Consumer<Change> handler);

}
