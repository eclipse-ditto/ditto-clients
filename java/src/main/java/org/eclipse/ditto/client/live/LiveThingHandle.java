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

import org.eclipse.ditto.client.live.commands.FeaturePropertiesCommandHandling;
import org.eclipse.ditto.client.live.commands.FeaturesCommandHandling;
import org.eclipse.ditto.client.live.commands.ThingAttributesCommandHandling;
import org.eclipse.ditto.client.live.commands.ThingCommandHandling;
import org.eclipse.ditto.client.live.events.EventEmitter;
import org.eclipse.ditto.client.live.events.ThingEventFactory;
import org.eclipse.ditto.client.live.messages.ClaimMessageRegistration;
import org.eclipse.ditto.client.live.messages.MessageRegistration;
import org.eclipse.ditto.client.live.messages.PendingMessageWithThingId;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.client.options.Option;

/**
 * A {@code LiveThingHandle} provides management and registration functionality for specific <em>Live Things</em>.
 *
 * @since 1.0.0
 */
public interface LiveThingHandle
        extends ThingHandle<LiveFeatureHandle>, MessageRegistration,
        ClaimMessageRegistration, ThingCommandHandling, ThingAttributesCommandHandling,
        FeaturesCommandHandling, FeaturePropertiesCommandHandling, EventEmitter<ThingEventFactory> {

    /**
     * Provides the functionality to create and send a new {@link org.eclipse.ditto.messages.model.Message}
     * <em>FROM</em> or <em>TO</em> the {@code Thing} handled by this {@code LiveThingHandle}. <p> Example: </p>
     * <pre>
     * client.live().forId("org.eclipse.ditto:fireDetectionDevice").message()
     *    .from()
     *    .subject("fireAlert")
     *    .payload("{\"action\" : \"call fire department\"}")
     *    .contentType("application/json")
     *    .send();
     * </pre>
     *
     * @param <T> the type of the Message's payload.
     * @return a new message builder that offers the functionality to create and send the message.
     */
    <T> PendingMessageWithThingId<T> message();

    /**
     * Provides the functionality to create and send a new {@link org.eclipse.ditto.messages.model.Message}
     * <em>FROM</em> or <em>TO</em> the {@code Thing} handled by this {@code LiveThingHandle}. <p> Example: </p>
     * <pre>
     * client.live().forId("org.eclipse.ditto:fireDetectionDevice").message()
     *    .from()
     *    .subject("fireAlert")
     *    .payload("{\"action\" : \"call fire department\"}")
     *    .contentType("application/json")
     *    .send();
     * </pre>
     *
     * @param <T> the type of the Message's payload.
     * @param options options sent to the outbound message.
     * @return a new message builder that offers the functionality to create and send the message.
     * @since 3.1.0
     */
    <T> PendingMessageWithThingId<T> message(Option<?>... options);

}
