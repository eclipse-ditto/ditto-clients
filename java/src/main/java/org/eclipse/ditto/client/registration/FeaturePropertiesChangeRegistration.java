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
import org.eclipse.ditto.model.things.WithThingId;

/**
 * Provides the necessary functionality for registering handlers which are notified about property changes.
 *
 * @since 1.0.0
 */
public interface FeaturePropertiesChangeRegistration extends WithThingId, HandlerDeregistration {

    /**
     * Registers a {@link Consumer} which is notified about <em>all</em> {@code property} changes.
     * <p>
     * If registered for a <em>specific</em> Feature, it will be notified of <em>all</em> property changes of that
     * Feature. Otherwise, it will receive <em>all</em> property changes of <em>all</em> Features.
     * </p>
     * Example:
     * <pre>
     * DittoClient client = ...
     * FeatureHandle myFeature = client.twin().forId("org.eclipse.ditto:myThing").forFeature("smokeDetector");
     *
     * myFeature.registerForPropertyChanges(HANDLER_ID, change -&gt; LOGGER.info("propertyChange received: {}",
     * change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param handler the {@code Consumer} to handle property change notifications.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForPropertyChanges(String registrationId, Consumer<Change> handler);

    /**
     * Registers a {@link Consumer} which is notified about <em>specific</em> {@code property} changes.
     * <p>
     * If registered for a <em>specific</em> Feature, it will be notified of property changes of that Feature.
     * Otherwise, it will receive property changes of <em>all</em> Features.
     * </p>
     * Example:
     * <pre>
     * DittoClient client = ...
     * FeatureHandle myFeature = client.twin().forId("org.eclipse.ditto:myThing").forFeature("smokeDetector");
     *
     * myFeature.registerForPropertyChanges(HANDLER_ID, "density",
     *    change -&gt; LOGGER.info("propertyChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param path the path to the {@code property} of interest - may contain {@code "/"}for addressing nested paths in
     * a hierarchy.
     * @param handler the {@code Consumer} to handle property change notifications.
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    default void registerForPropertyChanges(final String registrationId, final CharSequence path,
            final Consumer<Change> handler) {

        argumentNotNull(path);
        registerForPropertyChanges(registrationId, JsonFactory.newPointer(path), handler);
    }

    /**
     * Registers a {@link Consumer} which is notified about <em>specific</em> {@code property} changes.
     * <p>
     * If registered for a <em>specific</em> Feature, it will be notified of property changes of that Feature.
     * Otherwise, it will receive property changes of <em>all</em> Features.
     * </p>
     * Example:
     * <pre>
     * DittoClient client = ...
     * FeatureHandle myFeature = client.twin().forId("org.eclipse.ditto:myThing").forFeature("smokeDetector");
     *
     * myFeature.registerForPropertyChanges(HANDLER_ID, JsonFactory.newPointer("density"),
     *    change -&gt; LOGGER.info("propertyChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param path the path to the {@code property} of interest.
     * @param handler the {@code Consumer} to handle property change notifications.
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForPropertyChanges(String registrationId, JsonPointer path, Consumer<Change> handler);

}
