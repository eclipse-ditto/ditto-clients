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
package org.eclipse.ditto.client.internal;

import static java.util.Objects.requireNonNull;
import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.ditto.client.exceptions.DuplicateRegistrationIdException;
import org.eclipse.ditto.client.internal.bus.JsonPointerSelector;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.internal.bus.PointerWithData;
import org.eclipse.ditto.client.internal.bus.Registration;
import org.eclipse.ditto.client.management.FeatureHandle;
import org.eclipse.ditto.client.management.ThingHandle;

/**
 * Manages handlers on an {@link PointerBus}. Allows registration and deregistration of consumers based on a {@code
 * registrationId}.
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Things}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public final class HandlerRegistry<T extends ThingHandle, F extends FeatureHandle> {

    private final PointerBus bus;
    private final ConcurrentHashMap<String, Registration<Consumer<PointerWithData>>> registry;
    private final Map<String, T> thingHandles;
    private final Map<String, F> featureHandles;

    /**
     * Constructor.
     *
     * @param bus the Bus
     */
    public HandlerRegistry(final PointerBus bus) {
        this.bus = requireNonNull(bus);
        registry = new ConcurrentHashMap<>();
        thingHandles = new ConcurrentHashMap<>();
        featureHandles = new ConcurrentHashMap<>();
    }

    private static void checkRegistrationId(final String registrationId) {
        argumentNotNull(registrationId, "registrationId");
    }

    /**
     * Returns the {@link Executor} of the {@code bus} this registry manages.
     *
     * @return the {@link Executor} of the {@code bus} this registry manages.
     */
    public Executor getBusExecutor() {
        return bus.getExecutor();
    }

    /**
     * Register a Consumer to be triggered when a notification matches the given JsonPointerSelector.
     *
     * @param registrationId the registration id
     * @param selector the JsonPointerSelector
     * @param consumer the Consumer
     * @throws IllegalArgumentException if any parameter is {@code null}
     * @throws DuplicateRegistrationIdException if a consumer is already registered for the given {@code
     * registrationId}
     */
    public void register(final String registrationId, final JsonPointerSelector selector,
            final Consumer<PointerWithData> consumer) {
        checkRegistrationId(registrationId);

        registry.compute(registrationId, (k, v) -> {
            if (v != null) {
                throw new DuplicateRegistrationIdException(registrationId);
            }
            return bus.on(selector, consumer);
        });
    }

    /**
     * Deregisters the consumer which has been registered with the given registration id.
     *
     * @param registrationId the registration id
     * @return {@code true}, if the consumer has been deregistered; {@code false}, if no consumer for the given {@code
     * registrationId} exists
     * @throws IllegalArgumentException if parameter {@code registrationId} is {@code null}
     */
    public boolean deregister(final String registrationId) {
        checkRegistrationId(registrationId);

        final Registration<Consumer<PointerWithData>> registration = registry.remove(registrationId);
        if (registration == null) {
            return false;
        }

        registration.cancel();
        return true;
    }

    /**
     * Retrieves a for the passed {@code thingId} already registered {@link ThingHandle} or if not yet present, creates
     * one by invoking the passed {@code thingHandleSupplier}, stores that with the {@code thingId} and returns it.
     *
     * @param thingId the Thing ID to look for in the already created ThingHandles
     * @param thingHandleSupplier the Supplier which can create a new ThingHandle
     * @return the looked up or created thing handle
     */
    public T thingHandleForThingId(final String thingId, final Supplier<T> thingHandleSupplier) {
        if (!thingHandles.containsKey(thingId)) {
            thingHandles.put(thingId, thingHandleSupplier.get());
        }
        return thingHandles.get(thingId);
    }

    /**
     * Returns the created {@link ThingHandle} for the passed {@code thingId} if one was created.
     *
     * @param thingId the Thing ID to look for in the already created ThingHandles
     * @return the looked up thing handle
     */
    public Optional<T> getThingHandle(final String thingId) {
        return Optional.ofNullable(thingHandles.get(thingId));
    }

    /**
     * Retrieves a for the passed {@code thingId} and {@code featureod} already registered {@link FeatureHandle} or if
     * not yet present, creates one by invoking the passed {@code featureHandleSupplier}, stores that with the {@code
     * thingId} and {@code featureId} and returns it.
     *
     * @param thingId the Thing ID to look for in the already created FeatureHandles
     * @param featureId the Feature ID to look for in the already created FeatureHandles
     * @param featureHandleSupplier the Supplier which can create a new ThingHandle
     * @return the looked up or created feature handle
     */
    public F featureHandleForFeatureId(final String thingId, final String featureId,
            final Supplier<F> featureHandleSupplier) {
        final String key = thingId + "&&" + featureId;
        if (!featureHandles.containsKey(key)) {
            featureHandles.put(key, featureHandleSupplier.get());
        }
        return featureHandles.get(key);
    }

    /**
     * Returns the created {@link FeatureHandle} for the passed {@code thingId} and {@code featureId} if one was
     * created.
     *
     * @param thingId the Thing ID to look for in the already created FeatureHandles
     * @param featureId the Feature ID to look for in the already created FeatureHandles
     * @return the looked up feature handle
     */
    protected Optional<F> getFeatureHandle(final String thingId, final String featureId) {
        final String key = thingId + "&&" + featureId;
        return Optional.ofNullable(featureHandles.get(key));
    }
}
