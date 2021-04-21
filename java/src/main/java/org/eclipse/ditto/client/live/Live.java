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
package org.eclipse.ditto.client.live;

import java.util.concurrent.CompletionStage;

import org.eclipse.ditto.client.live.commands.FeaturePropertiesCommandHandling;
import org.eclipse.ditto.client.live.commands.FeaturesCommandHandling;
import org.eclipse.ditto.client.live.commands.ThingAttributesCommandHandling;
import org.eclipse.ditto.client.live.commands.ThingCommandHandling;
import org.eclipse.ditto.client.live.commands.ThingsCommandHandling;
import org.eclipse.ditto.client.live.events.EventEmitter;
import org.eclipse.ditto.client.live.events.GlobalEventFactory;
import org.eclipse.ditto.client.live.messages.ClaimMessageRegistration;
import org.eclipse.ditto.client.live.messages.MessageRegistration;
import org.eclipse.ditto.client.live.messages.PendingMessage;
import org.eclipse.ditto.client.management.CommonManagement;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.things.model.Thing;

/**
 * Live API acting as the entry point for directly managing and monitoring <em>Live Things</em>. Those are not managed
 * by Eclipse Ditto. When using the Live API, two Ditto Clients can directly interact with each other, via Eclipse Ditto
 * in between.
 *
 * @since 1.0.0
 */
public interface Live extends CommonManagement<LiveThingHandle, LiveFeatureHandle>, MessageRegistration,
        ClaimMessageRegistration, ThingsCommandHandling, ThingCommandHandling, ThingAttributesCommandHandling,
        FeaturesCommandHandling, FeaturePropertiesCommandHandling, EventEmitter<GlobalEventFactory> {

    /**
     * Provides the functionality to create and send a new {@link org.eclipse.ditto.model.messages.Message}
     * <em>FROM</em> or <em>TO</em> a "Live" {@link Thing} or a "Live" Thing's {@link
     * org.eclipse.ditto.things.model.Feature Feature}. <p> Example: </p>
     * <pre>
     * client.live().message()
     *    .from("org.eclipse.ditto:fireDetectionDevice")
     *    .featureId("smokeDetector")
     *    .subject("fireAlert")
     *    .payload(JsonFactory.newObject("{\"action\" : \"call fire department\"}"))
     *    .send();
     * </pre>
     *
     * @param <T> the type of the Message's payload.
     * @return a new message builder that offers the functionality to create and send the message.
     */
    <T> PendingMessage<T> message();

    /**
     * Start consuming changes, messages and commands on this {@code live()} channel.
     *
     * @return a CompletionStage that terminates when the start operation was successful.
     */
    @Override
    // overwritten in order to display a better suiting javadoc for the user
    CompletionStage<Void> startConsumption();

    /**
     * Start consuming changes, messages and commands on this {@code live()} channel with the passed {@code
     * consumptionOptions}.
     *
     * @param consumptionOptions specifies the {@link org.eclipse.ditto.client.options.Options.Consumption
     * ConsumptionOptions} to apply. Pass them in via:
     * <pre>{@code Options.Consumption.namespaces("org.eclipse.ditto.namespace1","org.eclipse.ditto.namespace2");
     * Options.Consumption.filter("gt(attributes/counter,42)");}
     * </pre>
     * @return a CompletionStage that terminates when the start operation was successful.
     */
    @Override
    // overwritten in order to display a better suiting javadoc for the user
    CompletionStage<Void> startConsumption(Option<?>... consumptionOptions);

}
