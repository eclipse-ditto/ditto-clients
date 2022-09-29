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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.ditto.client.ack.ResponseConsumer;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.things.model.ThingId;

/**
 * Builder for instances of {@link Message} which uses Object Scoping and Method Chaining to provide a convenient usage
 * experience.
 *
 * @param <T> the type of the Message's payload.
 * @since 1.0.0
 */
public interface MessageSender<T> {

    /**
     * Sets the message as being sent <em>FROM</em> a {@code Thing} (or a Thing's {@code Feature}).
     *
     * @param sendConsumer a Consumer which will at the end of the builder when invoking {@code send()} be called with
     * the built Message and any callback for the message response.
     * @return fluent api builder that provides the functionality to set the id of the Thing from which the Message will
     * be sent.
     */
    SetThingId<T> from(BiConsumer<Message<T>, ResponseConsumer<?>> sendConsumer);

    /**
     * Sets the message as being sent <em>TO</em> a {@code Thing} (or a Thing's {@code Feature}).
     *
     * @param sendConsumer a Consumer which will at the end of the builder when invoking {@code send()} be called with
     * the built Message and any callback of the response.
     * @return fluent api builder that provides the functionality to set the id of the Thing to which the Message will
     * be sent.
     */
    SetThingId<T> to(BiConsumer<Message<T>, ResponseConsumer<?>> sendConsumer);

    /**
     * Sets the message as being sent <em>FROM</em> a {@code Thing} (or a Thing's {@code Feature}).
     * No response is expected for the message.
     *
     * @param sendConsumer a Consumer which will at the end of the builder when invoking {@code send()} be called with
     * the built Message.
     * @return fluent api builder that provides the functionality to set the id of the Thing from which the Message will
     * be sent.
     */
    SetThingId<T> from(Consumer<Message<T>> sendConsumer);

    /**
     * Sets the message as being sent <em>TO</em> a {@code Thing} (or a Thing's {@code Feature}).
     * No response is expected for the message.
     *
     * @param sendConsumer a Consumer which will at the end of the builder when invoking {@code send()} be called with
     * the built Message.
     * @return fluent api builder that provides the functionality to set the id of the Thing to which the Message will
     * be sent.
     */
    SetThingId<T> to(Consumer<Message<T>> sendConsumer);

    /**
     * Fluent api builder that provides the functionality to set the id of the {@code Thing} from/to which the message
     * will be sent.
     *
     * @param <T> the type of the Message's payload.
     * @since 1.0.0
     */
    interface SetThingId<T> {

        /**
         * Sets the id of the {@code Thing} from/to which the message will be sent.
         *
         * @param thingId the id of the Thing from/to which the Message will be sent
         * @return fluent api builder that provides the functionality to <em>optionally</em> set the id of the {@code
         * Feature} from/to which the Message will be sent, or to leave the featureId empty and set the subject of the
         * Message.
         */
        SetFeatureIdOrSubject<T> thingId(ThingId thingId);
    }

    /**
     * Fluent api builder that provides the functionality to set the subject of the message.
     *
     * @param <T> the type of the Message's payload.
     * @since 1.0.0
     */
    interface SetSubject<T> {

        /**
         * Sets the subject of the message.
         *
         * @param subject the subject of the Message
         * @return fluent api builder that provides the functionality to set <em>optionally</em> fields of the message
         * or send the message.
         */
        SetPayloadOrSend<T> subject(String subject);
    }

    /**
     * Fluent api builder that provides the functionality to <em>optionally</em> set the id of the {@code Feature}
     * from/to which the Message will be sent, or to leave the featureId empty and set the subject of the Message.
     *
     * @param <T> the type of the Message's payload.
     * @since 1.0.0
     */
    interface SetFeatureIdOrSubject<T> extends SetSubject<T> {

        /**
         * Sets the id of the {@code Feature} from/to which the message will be sent.
         *
         * @param featureId the id of the Feature from/to which the Message will be sent
         * @return fluent api builder that provides the functionality to set the subject of the Message.
         */
        SetSubject<T> featureId(String featureId);
    }

    /**
     * Fluent api builder that provides the functionality to <em>optionally</em> set the payload of the message, or to
     * leave the payload empty and build the massage.
     *
     * @param <T> the type of the Message's payload.
     * @since 1.0.0
     */
    interface SetPayloadOrSend<T> extends MessageSendable<T> {

        /**
         * Sets the timeout of the message.
         *
         * @param timeout the timeout.
         * @return fluent api builder that provides the functionality to set <em>optionally</em> fields of the message
         * or send the message.
         */
        SetPayloadOrSend<T> timeout(Duration timeout);

        /**
         * Sets the timestamp of the message.
         *
         * @param timestamp the timestamp.
         * @return fluent api builder that provides the functionality to set <em>optionally</em> fields of the message
         * or send the message.
         */
        SetPayloadOrSend<T> timestamp(OffsetDateTime timestamp);

        /**
         * Sets the correlationId of the message.
         *
         * @param correlationId the correlationId.
         * @return fluent api builder that provides the functionality to set <em>optionally</em> fields of the message
         * or send the message.
         */
        SetPayloadOrSend<T> correlationId(String correlationId);

        /**
         * Sets the HTTP status of the message.
         *
         * @param httpStatus the HTTP status.
         * @return fluent api builder that provides the functionality to set <em>optionally</em> fields of the message
         * or send the message.
         * @since 2.0.0
         */
        SetPayloadOrSend<T> httpStatus(HttpStatus httpStatus);

        /**
         * Sets additional headers to send in the message.
         *
         * @param additionalHeaders the headers.
         * @return fluent api builder that provides the functionality to set <em>optionally</em> fields of the message
         * or send the message.
         * @since 1.1.0
         */
        SetPayloadOrSend<T> headers(DittoHeaders additionalHeaders);

        /**
         * Sets the payload of the message. NOTE: The maximum payload size is restricted to 10MB.
         *
         * @param payload the payload of the Message
         * @return fluent api builder that provides the functionality to set the MIME contentType of the payload of the
         * Message.
         */
        SetContentType<T> payload(T payload);
    }

    /**
     * Fluent api builder that provides the functionality to set the MIME contentType of the payload of the message.
     *
     * @param <T> the type of the Message's payload.
     * @since 1.0.0
     */
    interface SetContentType<T> extends MessageSendable<T> {

        /**
         * Sets the MIME contentType of the payload of the message.
         *
         * @param contentType the MIME contentType of the payload of the message
         * @return fluent api builder that terminates this builder and sends the message.
         */
        MessageSendable<T> contentType(String contentType);
    }

    /**
     * Fluent api builder that sends the message.
     *
     * @param <T> the type of the Message's payload.
     * @since 1.0.0
     */
    interface MessageSendable<T> {

        /**
         * Terminates this builder, builds the {@code Message} and sends it. <p> NOTE: Sending a message is handled as
         * <em>fire-and-forget</em>. The API does not provide any kind of acknowledgements that the message is received
         * by its potential targets. </p>
         *
         * @throws IllegalStateException if the {@code Message} to be sent is in an invalid state.
         * @throws org.eclipse.ditto.client.management.ClientReconnectingException if the client is in a reconnecting
         * state.
         */
        void send();

        /**
         * Terminates this builder, builds the {@code Message} and sends it providing a callback for the expected
         * response in the passed {@code responseConsumer}. Expects a response message of type {@link ByteBuffer}.
         *
         * @param responseConsumer the Consumer which should be notified with the response ot the Throwable in case of
         * an error.
         * @throws IllegalStateException if the {@code Message} to be sent is in an invalid state.
         * @throws org.eclipse.ditto.client.management.ClientReconnectingException if the client is in a reconnecting
         * state.
         */
        default void send(final BiConsumer<Message<ByteBuffer>, Throwable> responseConsumer) {
            send(ByteBuffer.class, responseConsumer);
        }

        /**
         * Terminates this builder, builds the {@code Message} and sends it providing a callback for the expected
         * response in the passed {@code responseConsumer}. Expects a response message of the passed type {@code
         * responseType}.
         *
         * @param responseType expected type of the response message's payload.
         * @param responseConsumer the Consumer which should be notified with the response ot the Throwable in case of
         * an error.
         * @param <R> the type of the response message's payload.
         * @throws IllegalStateException if the {@code Message} to be sent is in an invalid state.
         * @throws org.eclipse.ditto.client.management.ClientReconnectingException if the client is in a reconnecting
         * state.
         * @since 1.0.0
         */
        <R> void send(Class<R> responseType, BiConsumer<Message<R>, Throwable> responseConsumer);

    }

}
