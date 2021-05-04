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
 * Provides the necessary functionality for registering handlers to receive {@link RepliableMessage}s that are sent
 * to/from {@link org.eclipse.ditto.things.model.Thing}s or {@link org.eclipse.ditto.things.model.Feature}s.
 *
 * @since 1.0.0
 */
public interface MessageRegistration extends HandlerDeregistration {

    /**
     * Registers a {@link Consumer} to receive {@link RepliableMessage}s with the specified {@code subject} and
     * specified payload Java {@code type}. <p> If registered for a specific {@code Thing}, it will only receive
     * messages for that Thing and that Thing's {@code Feature}s. Otherwise, it will receive messages for <em>all</em>
     * Things and all Thing's Features. <p> Example: </p>
     * <pre>
     * DittoClient client = ...
     * String subject = "theSubjectOfInterest";
     *
     * client.live().forId("myThing").registerForMessage(HANDLER_ID, subject, JsonValue.class, message -&gt;
     *    LOGGER.info("Json payload received: {}", message.getPayload()));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * The {@code registrationId} needs to be unique per {@link DittoClient} instance.
     * @param subject the subject of the messages to be received; if the subject {@code *} is used all messages will be
     * received independently from their subjects.
     * @param type the type of the expected message.
     * @param handler the {@code Consumer} to handle messages.
     * @param <T> the type of the Message's payload.
     * @param <U> the type of the Response's payload.
     * @throws MessageSerializationException if no MessageDeserializer for the given {@code subject} + {@code type} pair
     * is registered. Register a {@code MessageSerializer} by configuring the Client at startup with a customized {@code
     * MessageSerializerConfiguration}.
     * @throws org.eclipse.ditto.client.registration.DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    <T, U> void registerForMessage(String registrationId, String subject, Class<T> type,
            Consumer<RepliableMessage<T, U>> handler);

    /**
     * Registers a {@link Consumer} to receive messages with the specified {@code subject} regardless of the payload
     * Java {@code type}. As a consequence, the Consumer will only be provided with the raw {@link ByteBuffer} payload
     * of the Message. <p> If registered for a specific {@code Thing}, it will only receive messages for that Thing and
     * that Thing's {@code Feature}s. Otherwise, it will receive messages for <em>all</em> Things and all Thing's
     * Features. <p> Example: </p>
     * <pre>
     * DittoClient client = ...
     * String subject = "theSubjectOfInterest";
     *
     * client.live().forId("myThing").registerForMessage(HANDLER_ID, subject, message -&gt;
     *    LOGGER.info("Raw Message received: {}", message.getRawPayload()));
     * </pre>
     *
     * @param registrationId an arbitrary ID provided by the user which can be used to cancel the registration later on.
     * The {@code registrationId} needs to be unique per {@link DittoClient} instance.
     * @param subject the subject of the messages to be received; if the subject {@code *} is used all messages will be
     * received independently from their subjects.
     * @param handler the {@code Consumer} to handle messages with raw payload.
     * @param <U> the type of the Response's payload.
     * @throws org.eclipse.ditto.client.registration.DuplicateRegistrationIdException if a handler is already registered for the given {@code
     * registrationId}.
     */
    <U> void registerForMessage(String registrationId, String subject, Consumer<RepliableMessage<?, U>> handler);

}
