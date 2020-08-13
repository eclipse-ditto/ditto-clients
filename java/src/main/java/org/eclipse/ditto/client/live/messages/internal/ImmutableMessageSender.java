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
package org.eclipse.ditto.client.live.messages.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageBuilder;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessageHeadersBuilder;
import org.eclipse.ditto.model.messages.MessageResponseConsumer;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.model.messages.ResponseConsumer;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.acks.base.Acknowledgements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mutable builder with a fluent API for building and sending an immutable {@link Message}.
 *
 * @param <T> the type of the payload of the Messages this builder builds and sends.
 * @since 1.0.0
 */
@Immutable
public final class ImmutableMessageSender<T> implements MessageSender<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImmutableMessageSender.class);

    private final boolean isResponse;
    private MessageDirection messageDirection;
    private ThingId messageThingId;
    private String messageFeatureId;
    private String messageSubject;
    private Duration messageTimeout;
    private OffsetDateTime messageTimestamp;
    private String messageCorrelationId;
    private String messageContentType;
    private HttpStatusCode messageStatusCode;
    private DittoHeaders messageAdditionalHeaders;
    private Consumer<Message<T>> sendConsumer;

    private ImmutableMessageSender(final boolean isResponse) {
        this.isResponse = isResponse;
        messageDirection = null;
        messageThingId = null;
        messageFeatureId = null;
        messageSubject = null;
        messageContentType = null;
        messageTimeout = null;
        messageTimestamp = null;
        messageCorrelationId = null;
        messageStatusCode = null;
        messageAdditionalHeaders = null;
    }

    /**
     * Returns a new instance of {@code ImmutableMessageBuilder}.
     *
     * @param <T> the type of the payload of the Messages this builder builds and sends.
     * @return the new builder.
     */
    public static <T> MessageSender<T> newInstance() {
        return new ImmutableMessageSender<>(false);
    }

    /**
     * Returns a new instance of {@code ImmutableMessageBuilder} which is a response message.
     *
     * @param <T> the type of the payload of the Messages this builder builds and sends.
     * @return the new builder.
     */
    public static <T> MessageSender<T> response() {
        return new ImmutableMessageSender<>(true);
    }

    @Override
    public SetThingId<T> from(final Consumer<Message<T>> sendConsumer) {
        this.sendConsumer = argumentNotNull(sendConsumer, "sendConsumer");
        messageDirection = MessageDirection.FROM;
        return new SetThingIdImpl();
    }

    @Override
    public SetThingId<T> to(final Consumer<Message<T>> sendConsumer) {
        this.sendConsumer = argumentNotNull(sendConsumer, "sendConsumer");
        messageDirection = MessageDirection.TO;
        return new SetThingIdImpl();
    }

    private <R, C> ResponseConsumer<R, C> createResponseConsumer(final Class<R> expectedResponseType,
            final BiConsumer<C, Throwable> responseConsumer) {
        return new ResponseConsumer<R, C>() {
            @Override
            @Nonnull
            public Class<R> getResponseType() {
                return expectedResponseType;
            }

            @Override
            @Nonnull
            public BiConsumer<C, Throwable> getResponseConsumer() {
                return responseConsumer;
            }
        };
    }

    private void buildAndSendMessage(final T payload) {
        buildAndSendMessage(payload, null);
    }

    private void buildAndSendMessage(final T payload, final ResponseConsumer<?, ?> responseConsumer) {
        final MessageHeadersBuilder messageHeadersBuilder =
                MessageHeaders.newBuilder(messageDirection, messageThingId, messageSubject);

        if (null != messageAdditionalHeaders) {
            // put additionalHeaders first, so that custom "contentType", "timeout", etc. still overwrites the values:
            messageHeadersBuilder.putHeaders(messageAdditionalHeaders);
        }

        messageHeadersBuilder
                .contentType(messageContentType)
                .featureId(messageFeatureId)
                .timeout(messageTimeout)
                .timestamp(messageTimestamp)
                .correlationId(messageCorrelationId);

        if (null != messageStatusCode) {
            if (messageStatusCode == HttpStatusCode.NO_CONTENT && payload != null) {
                final String warnMessage = "StatusCode '" + HttpStatusCode.NO_CONTENT + "' cannot be used in "
                        + "combination with a set message payload. Message with subject '" + messageSubject +
                        "' was NOT sent!";
                LOGGER.warn(warnMessage);
                throw new IllegalStateException(warnMessage);
            }
            messageHeadersBuilder.statusCode(messageStatusCode);
        } else if (isResponse) {
            final String warnMessage = "StatusCode has to be set for response messages. Response message with subject '"
                    + messageSubject + "' was NOT sent!";
            LOGGER.warn(warnMessage);
            throw new IllegalStateException(warnMessage);
        }

        final MessageBuilder<T> messageBuilder =
                MessagesModelFactory.<T>newMessageBuilder(messageHeadersBuilder.build())
                        .responseConsumer(responseConsumer)
                        .payload(payload);

        sendConsumer.accept(messageBuilder.build());
    }

    private class SetThingIdImpl implements SetThingId<T> {

        @Override
        public SetFeatureIdOrSubject<T> thingId(final ThingId thingId) {
            messageThingId = thingId;
            return new SetFeatureIdOrSubjectImpl();
        }
    }

    private final class SetFeatureIdOrSubjectImpl implements SetFeatureIdOrSubject<T> {

        @Override
        public SetSubject<T> featureId(final String featureId) {
            messageFeatureId = featureId;
            return new SetSubjectImpl();
        }

        @Override
        public SetPayloadOrSend<T> subject(final String subject) {
            messageSubject = subject;
            return new SetPayloadOrSendImpl();
        }
    }

    private final class SetSubjectImpl implements SetSubject<T> {

        @Override
        public SetPayloadOrSend<T> subject(final String subject) {
            messageSubject = subject;
            return new SetPayloadOrSendImpl();
        }
    }

    private final class SetPayloadOrSendImpl implements SetPayloadOrSend<T> {

        @Override
        public SetPayloadOrSend<T> timeout(final Duration timeout) {
            messageTimeout = timeout;
            return this;
        }

        @Override
        public SetPayloadOrSend<T> timestamp(final OffsetDateTime timestamp) {
            messageTimestamp = timestamp;
            return this;
        }

        @Override
        public SetPayloadOrSend<T> correlationId(final String correlationId) {
            messageCorrelationId = correlationId;
            return this;
        }

        @Override
        public SetPayloadOrSend<T> statusCode(final HttpStatusCode statusCode) {
            messageStatusCode = statusCode;
            return this;
        }

        @Override
        public SetPayloadOrSend<T> headers(final DittoHeaders additionalHeaders) {
            messageAdditionalHeaders = additionalHeaders;
            return this;
        }

        @Override
        public SetContentType<T> payload(final T payload) {
            return new SetContentTypeImpl(payload);
        }

        @Override
        public void send() {
            buildAndSendMessage(null);
        }

        @Override
        public <R> void send(final Class<R> responseType, final BiConsumer<Message<R>, Throwable> responseConsumer) {
            buildAndSendMessage(null, createResponseConsumer(responseType, responseConsumer));
        }

        @Override
        public void sendWithExpectedAcknowledgement(final BiConsumer<Acknowledgements, Throwable> responseConsumer) {
            buildAndSendMessage(null, createResponseConsumer(Acknowledgements.class, responseConsumer));
        }
    }

    private final class SetContentTypeImpl implements SetContentType<T> {

        private final T payload;

        private SetContentTypeImpl(final T payload) {
            this.payload = payload;
        }

        @Override
        public MessageSendable<T> contentType(final String contentType) {
            messageContentType = contentType;
            return new MessageSendableImpl(payload);
        }

        @Override
        public void send() {
            buildAndSendMessage(payload);
        }

        @Override
        public <R> void send(final Class<R> responseType, final BiConsumer<Message<R>, Throwable> responseConsumer) {
            buildAndSendMessage(payload, createResponseConsumer(responseType, responseConsumer));
        }

        @Override
        public void sendWithExpectedAcknowledgement(final BiConsumer<Acknowledgements, Throwable> responseConsumer) {
            buildAndSendMessage(payload, createResponseConsumer(Acknowledgements.class, responseConsumer));
        }
    }

    private class MessageSendableImpl implements MessageSendable<T> {

        private final T payload;

        MessageSendableImpl(final T payload) {
            this.payload = payload;
        }

        @Override
        public void send() {
            buildAndSendMessage(payload);
        }

        @Override
        public <R> void send(final Class<R> responseType, final BiConsumer<Message<R>, Throwable> responseConsumer) {
            buildAndSendMessage(payload, createResponseConsumer(responseType, responseConsumer));
        }

        @Override
        public void sendWithExpectedAcknowledgement(final BiConsumer<Acknowledgements, Throwable> responseConsumer) {
            buildAndSendMessage(payload, createResponseConsumer(Acknowledgements.class, responseConsumer));
        }
    }

}
