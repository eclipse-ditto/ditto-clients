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
import org.eclipse.ditto.client.changes.FeatureChange;
import org.eclipse.ditto.client.changes.FeaturesChange;

/**
 * Provides the functionality for registering handlers for {@link FeatureChange} events.
 *
 * @since 1.0.0
 */
public interface FeatureChangeRegistration extends HandlerDeregistration {

    /**
     * Registers a {@code Consumer} which is notified about <em>all</em> {@code Feature} changes.
     * <p>
     * If registered for a <em>specific</em> Thing, it will be notified about all Feature changes of <em>that</em>
     * Thing. Otherwise, it will receive <em>all</em> Feature changes of <em>all</em> Things.
     * </p>
     * Example:
     * <pre>
     * DittoClient client = ...
     * ThingHandle myThing = client.twin().forId("myThing");
     *
     * myThing.registerForFeatureChanges(HANDLER_ID, change -&gt; LOGGER.info("featureChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param handler the consumer of the Feature change to be registered.
     * @throws IllegalArgumentException if {@code handler} is {@code null}.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForFeatureChanges(String registrationId, Consumer<FeatureChange> handler);

    /**
     * Registers a {@code Consumer} which is notified about <em>specific</em> {@code Feature} changes.
     * <p>
     * If registered for a <em>specific</em> Thing, it will be notified about Feature changes of that Thing. Otherwise,
     * it will receive Feature changes of <em>all</em> Things.
     * </p>
     * Example:
     * <pre>
     * DittoClient client = ...
     * ThingHandle myThing = client.twin().forId("myThing");
     * String featureId = "myFeature";
     *
     * myThing.registerForFeatureChanges(HANDLER_ID, featureId, change -&gt; LOGGER.info("featureChange received: {}",
     * change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param featureId the identifier of the observed Feature.
     * @param handler the consumer of the Feature change to be registered.
     * @throws IllegalArgumentException if any argument is {@code null}.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForFeatureChanges(String registrationId, String featureId, Consumer<FeatureChange> handler);

    /**
     * Registers a {@code Consumer} which is notified about <em>all</em> changes of Thing {@code Features}.
     * <p>
     * If registered for a <em>specific</em> Thing, it will be notified about the Features object changes of
     * <em>that</em> Thing. Otherwise it will be notified about Features object changes of <em>all</em> Things.
     * </p>
     * Example for listening on Features change on one specific Thing:
     * <pre>
     * DittoClient client = ...
     * ThingHandle myThing = client.twin().forId("myThing");
     *
     * myThing.registerForFeaturesChanges(HANDLER_ID, change -&gt; LOGGER.info("featuresReplacement received: {}",
     * change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param handler the consumer of the Features change.
     * @throws IllegalArgumentException if {@code handler} is {@code null}.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForFeaturesChanges(String registrationId, Consumer<FeaturesChange> handler);

}
