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
package org.eclipse.ditto.client.twin;

import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.client.management.CommonManagement;
import org.eclipse.ditto.client.options.Option;

/**
 * Twin API of Eclipse Ditto acting as the entry point for managing and monitoring <em>Twin Things</em>, which are the
 * digital representations (digital twins) of the actual devices being managed.
 *
 * @since 1.0.0
 */
public interface Twin extends CommonManagement<TwinThingHandle, TwinFeatureHandle> {

    /**
     * Start consuming changes on this {@code twin()} channel.
     *
     * @return a CompletionStage that terminates when the start operation was successful or fails with
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException} if the client
     * is in a reconnecting state.
     */
    @Override
    // overwritten in order to display a better suiting javadoc for the user
    CompletionStage<Void> startConsumption();

    /**
     * Start consuming changes on this {@code twin()} channel with the passed {@code consumptionOptions}.
     *
     * @param consumptionOptions specifies the {@link org.eclipse.ditto.client.options.Options.Consumption
     * ConsumptionOptions} to apply. Pass them in via:
     * <pre>{@code Options.Consumption.namespaces("org.eclipse.ditto.namespace1","org.eclipse.ditto.namespace2");
     * Options.Consumption.filter("gt(attributes/counter,42)");}
     * </pre>
     * @return a CompletionStage that terminates when the start operation was successful or fails with
     * {@link org.eclipse.ditto.client.management.ClientReconnectingException} if the client
     * is in a reconnecting state.
     */
    @Override
    // overwritten in order to display a better suiting javadoc for the user
    CompletionStage<Void> startConsumption(Option<?>... consumptionOptions);

    /**
     * Retrieve the handle for search operations.
     *
     * @return the search handle.
     * @since 1.1.0
     */
    TwinSearchHandle search();

}
