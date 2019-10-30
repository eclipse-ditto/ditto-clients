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
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;

/**
 * Provides the necessary functionality for registering handlers which are notified about property changes on {@link
 * Twin} level.
 *
 * @since 1.0.0
 */
public interface ThingFeaturePropertiesChangeRegistration extends HandlerDeregistration {

    /**
     * Registers a {@link Consumer} which is notified about <em>all</em> {@code property} changes of the specified
     * {@code featureId}. <p> Example: </p>
     * <pre>
     * DittoClient client = ...
     * client.twin().registerForFeaturePropertyChanges(HANDLER_ID, "smokeDetector", change -&gt;
     *    LOGGER.info("propertyChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param featureId the specific featureId to receive FeatureProperty changes for.
     * @param handler the {@code Consumer} to handle property change notifications.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForFeaturePropertyChanges(String registrationId, String featureId, Consumer<Change> handler);

    /**
     * Registers a {@link Consumer} which is notified about <em>specific</em> {@code property} changes of the specified
     * {@code featureId}. <p> Example: </p>
     * <pre>
     * DittoClient client = ...
     * client.twin().registerForFeaturePropertyChanges(HANDLER_ID, "smokeDetector", "density",
     *    change -&gt; LOGGER.info("propertyChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param featureId the specific Feature ID to receive Feature property changes for.
     * @param path the path to the {@code property} of interest - may contain {@code "/"}for addressing nested paths in
     * a hierarchy.
     * @param handler the {@code Consumer} to handle property change notifications.
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    default void registerForFeaturePropertyChanges(String registrationId, String featureId, CharSequence path,
            Consumer<Change> handler) {
        argumentNotNull(path);
        registerForFeaturePropertyChanges(registrationId, featureId, JsonFactory.newPointer(path), handler);
    }

    /**
     * Registers a {@link Consumer} which is notified about <em>specific</em> {@code property} changes of the specified
     * {@code featureId}. <p> Example: </p>
     * <pre>
     * DittoClient client = ...
     * client.twin().registerForFeaturePropertyChanges(HANDLER_ID, "smokeDetector",
     *    JsonFactory.newPointer("density"), change -&gt;
     *       LOGGER.info("propertyChange received: {}", change));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * It is required to be unique per {@link DittoClient} instance.
     * @param featureId the specific Feature ID to receive Feature property changes for.
     * @param path the path to the {@code property} of interest.
     * @param handler the {@code Consumer} to handle property change notifications.
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty.
     * @throws DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    void registerForFeaturePropertyChanges(String registrationId, String featureId, JsonPointer path,
            Consumer<Change> handler);

}
