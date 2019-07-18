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

import org.eclipse.ditto.client.live.commands.FeaturesCommandHandling;
import org.eclipse.ditto.client.live.events.EventEmitter;
import org.eclipse.ditto.client.live.events.FeatureEventFactory;
import org.eclipse.ditto.client.live.messages.MessageRegistration;
import org.eclipse.ditto.client.live.messages.PendingMessageWithFeatureId;
import org.eclipse.ditto.client.management.FeatureHandle;

/**
 * A {@code LiveFeatureHandle} provides management and registration functionality for specific {@code Live Thing}
 * features.
 *
 * @since 1.0.0
 */
public interface LiveFeatureHandle extends FeatureHandle, MessageRegistration, FeaturesCommandHandling,
        EventEmitter<FeatureEventFactory> {

    /**
     * Provides the functionality to create and send a new {@link org.eclipse.ditto.model.messages.Message} <em>TO</em>
     * or <em>FROM</em> the {@code Feature} handled by this {@code LiveFeatureHandle}. <p> Example: </p>
     * <pre>
     * client.live()
     *    .forId("org.eclipse.ditto:fireDetectionDevice")
     *    .forFeature("smokeDetector")
     *    .message()
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
    <T> PendingMessageWithFeatureId<T> message();

}
