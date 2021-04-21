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
package org.eclipse.ditto.client.live.messages;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.registration.HandlerDeregistration;

/**
 * Provides the necessary functionality for registering handlers to receive {@link RepliableMessage}s that are sent to
 * {@link org.eclipse.ditto.things.model.Thing}s to claim these.
 *
 * @since 1.0.0
 */
public interface ClaimMessageRegistration extends HandlerDeregistration {

    /**
     * Registers a {@link Consumer} to receive {@link RepliableMessage}s with the {@link
     * org.eclipse.ditto.model.messages.KnownMessageSubjects#CLAIM_SUBJECT} and the specified payload Java {@code type}
     * . <p> Example: </p>
     * <pre>
     * DittoClient client = ...
     * client.live().forId("myThing").registerForClaimMessage(HANDLER_ID, JsonValue.class, message -&gt;
     *    LOGGER.info("Claim Message received: {}", message));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * The {@code registrationId} needs to be unique per {@link DittoClient} instance.
     * @param type the type of the expected message.
     * @param handler the {@code Consumer} to handle claim messages.
     * @param <T> the type of the Message's payload.
     * @param <U> the type of the Response's payload.
     * @throws MessageSerializationException if no MessageSerializer for the given {@code type} is registered. Register
     * a {@code MessageSerializer} by configuring the Client at startup with a customized {@code
     * MessageSerializerConfiguration}.
     * @throws org.eclipse.ditto.client.registration.DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    <T, U> void registerForClaimMessage(String registrationId, Class<T> type,
            Consumer<RepliableMessage<T, U>> handler);

    /**
     * Registers a {@link Consumer} to receive {@link RepliableMessage}'s with the {@link
     * org.eclipse.ditto.model.messages.KnownMessageSubjects#CLAIM_SUBJECT} regardless of the payload Java {@code type}
     * . As a consequence, the Consumer will only be provided with the raw {@link ByteBuffer} payload of the Message.
     * <p> Example: </p>
     * <pre>
     * DittoClient client = ...
     * client.live().forId("myThing").registerForClaimMessage(HANDLER_ID, message -&gt;
     *    LOGGER.info("Claim Message received: {}", message));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * The {@code registrationId} needs to be unique per {@link DittoClient} instance.
     * @param handler the {@code Consumer} to handle claim messages.
     * @param <U> the type of the Response's payload.
     * @throws MessageSerializationException if no MessageSerializer for the given {@code type} is registered. Register
     * a {@code MessageSerializer} by configuring the Client at startup with a customized {@code
     * MessageSerializerConfiguration}.
     * @throws org.eclipse.ditto.client.registration.DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    <U> void registerForClaimMessage(String registrationId, Consumer<RepliableMessage<?, U>> handler);
}
