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

import static org.eclipse.ditto.base.model.common.ConditionChecker.argumentNotNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.ack.ResponseConsumer;
import org.eclipse.ditto.client.ack.internal.AcknowledgementRequestsValidator;
import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.management.AcknowledgementsFailedException;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageBuilder;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessageHeadersBuilder;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgement;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgements;
import org.eclipse.ditto.base.model.signals.commands.CommandResponse;
import org.eclipse.ditto.base.model.signals.commands.ErrorResponse;
import org.eclipse.ditto.model.messages.signals.commands.MessageCommandResponse;
import org.eclipse.ditto.model.messages.signals.commands.MessagePayloadSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mutable builder with a fluent API for building and sending an immutable {@link Message}.
 * This is ImmutableMessage-Sender, not Immutable-MessageSender.
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
    private HttpStatus messageStatus;
    private DittoHeaders messageAdditionalHeaders;
    private BiConsumer<Message<T>, ResponseConsumer<?>> sendConsumer;

    private ImmutableMessageSender(final boolean isResponse) {
        this.isResponse = isResponse;
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
    public SetThingId<T> from(final BiConsumer<Message<T>, ResponseConsumer<?>> sendConsumer) {
        this.sendConsumer = argumentNotNull(sendConsumer, "sendConsumer");
        messageDirection = MessageDirection.FROM;
        return new SetThingIdImpl();
    }

    @Override
    public SetThingId<T> to(final BiConsumer<Message<T>, ResponseConsumer<?>> sendConsumer) {
        this.sendConsumer = argumentNotNull(sendConsumer, "sendConsumer");
        messageDirection = MessageDirection.TO;
        return new SetThingIdImpl();
    }

    @Override
    public SetThingId<T> from(final Consumer<Message<T>> sendConsumer) {
        return from(ignoreResponse(sendConsumer));
    }

    @Override
    public SetThingId<T> to(final Consumer<Message<T>> sendConsumer) {
        return to(ignoreResponse(sendConsumer));
    }

    private void buildAndSendMessage(final T payload) {
        buildAndSendMessage(payload, null);
    }

    private void buildAndSendMessage(final T payload, final ResponseConsumer<?> responseConsumer) {
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

        if (null != messageStatus) {
            if (HttpStatus.NO_CONTENT.equals(messageStatus) && payload != null) {
                final String warnMessage = "HTTP status '" + HttpStatus.NO_CONTENT + "' cannot be used in "
                        + "combination with a set message payload. Message with subject '" + messageSubject +
                        "' was NOT sent!";
                LOGGER.warn(warnMessage);
                throw new IllegalStateException(warnMessage);
            }
            messageHeadersBuilder.httpStatus(messageStatus);
        } else if (isResponse) {
            final String warnMessage =
                    "HTTP status has to be set for response messages. Response message with subject '" +
                            messageSubject + "' was NOT sent!";
            LOGGER.warn(warnMessage);
            throw new IllegalStateException(warnMessage);
        }

        final MessageBuilder<T> messageBuilder =
                MessagesModelFactory.<T>newMessageBuilder(messageHeadersBuilder.build())
                        .payload(payload);

        sendConsumer.accept(messageBuilder.build(), responseConsumer);
    }

    private BiConsumer<Message<T>, ResponseConsumer<?>> ignoreResponse(final Consumer<Message<T>> sendConsumer) {
        return (message, responseConsumer) -> sendConsumer.accept(message);
    }

    private static <T> ResponseConsumer<?> createCommandResponseConsumer(final Class<T> clazz,
            final BiConsumer<Message<T>, Throwable> responseMessageHandler) {

        return new ResponseConsumerImpl<>(CommandResponse.class, (response, error) -> {
            final Message<?> message;
            final Throwable errorToPublish;
            if (response instanceof Acknowledgements) {
                if (!((Acknowledgements) response).getFailedAcknowledgements().isEmpty()) {
                    message = null;
                    errorToPublish = AcknowledgementsFailedException.of((Acknowledgements) response);
                } else {
                    final AcknowledgementLabel expectedLabel = DittoAcknowledgementLabel.LIVE_RESPONSE;
                    final Optional<Message<?>> messageOptional =
                            ((Acknowledgements) response).getAcknowledgement(expectedLabel)
                                    .map(ImmutableMessageSender::getMessageResponseInAcknowledgement);
                    if (messageOptional.isPresent()) {
                        message = messageOptional.get();
                        errorToPublish = null;
                    } else {
                        message = null;
                        errorToPublish = AcknowledgementRequestsValidator.didNotReceiveAcknowledgement(expectedLabel);
                    }
                }
            } else if (response instanceof MessageCommandResponse) {
                message = ((MessageCommandResponse<?, ?>) response).getMessage();
                errorToPublish = null;
            } else if (response instanceof ErrorResponse) {
                message = null;
                errorToPublish = ((ErrorResponse<?>) response).getDittoRuntimeException();
            } else if (response == null) {
                message = null;
                errorToPublish = error;
            } else {
                message = null;
                final String errorMessage = String.format(
                        "Expected received response to be instance of either <%s> or <%s> but found <%s>.",
                        Acknowledgements.class,
                        MessageCommandResponse.class,
                        response.getClass());
                errorToPublish = new ClassCastException(errorMessage);
            }
            checkPayloadTypeAndAccept(clazz, responseMessageHandler, message, errorToPublish);
        });
    }

    private static Message<?> getMessageResponseInAcknowledgement(final Acknowledgement ack) {
        final MessageHeaders messageHeaders = MessageHeaders.of(ack.getDittoHeaders()).toBuilder()
                .httpStatus(ack.getHttpStatus())
                .build();
        final MessageBuilder<Object> messageBuilder = MessagesModelFactory.newMessageBuilder(messageHeaders);
        @Nullable final JsonValue payload = ack.getEntity().orElse(null);
        MessagePayloadSerializer.deserialize(payload, messageBuilder, messageHeaders);
        return messageBuilder.build();
    }

    private static <T> void checkPayloadTypeAndAccept(final Class<T> clazz,
            final BiConsumer<Message<T>, Throwable> responseMessageHandler,
            final Message<?> message,
            final Throwable error) {

        if (message != null && clazz.isAssignableFrom(ByteBuffer.class)) {
            responseMessageHandler.accept(
                    withPayload(message, message.getRawPayload().map(clazz::cast).orElse(null)),
                    error);
        } else {
            final Optional<?> payloadOptional = getMessagePayload(message);
            if (payloadOptional.isPresent()) {
                final Object payload = payloadOptional.get();
                if (clazz.isInstance(payload)) {
                    responseMessageHandler.accept(withPayload(message, clazz.cast(payload)), error);
                } else {
                    responseMessageHandler.accept(null, new ClassCastException(
                            "Expected: " + clazz.getCanonicalName() +
                                    "; Actual: " + payload.getClass().getCanonicalName() +
                                    " (" + payload + ")"
                    ));
                }
            } else if (message != null) {
                responseMessageHandler.accept(null, new NoSuchElementException("No payload"));
            } else {
                responseMessageHandler.accept(null, error);
            }
        }
    }

    private static Optional<?> getMessagePayload(@Nullable final Message<?> message) {
        if (null != message) {
            return message.getPayload();
        } else {
            return Optional.empty();
        }
    }

    private static <T> Message<T> withPayload(final Message<?> message, final T payload) {
        return MessagesModelFactory.<T>newMessageBuilder(message.getHeaders())
                .payload(payload)
                .rawPayload(message.getRawPayload().orElse(null))
                .extra(message.getExtra().orElse(null))
                .build();
    }

    private final class SetThingIdImpl implements SetThingId<T> {

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
        public SetPayloadOrSend<T> httpStatus(final HttpStatus httpStatus) {
            messageStatus = httpStatus;
            return this;
        }

        @Override
        public SetPayloadOrSend<T> headers(final DittoHeaders additionalHeaders) {
            AcknowledgementRequestsValidator.validate(additionalHeaders.getAcknowledgementRequests(),
                    DittoAcknowledgementLabel.LIVE_RESPONSE);
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
            buildAndSendMessage(null, createCommandResponseConsumer(responseType, responseConsumer));
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
            buildAndSendMessage(payload, createCommandResponseConsumer(responseType, responseConsumer));
        }

    }

    private final class MessageSendableImpl implements MessageSendable<T> {

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
            buildAndSendMessage(payload, createCommandResponseConsumer(responseType, responseConsumer));
        }

    }

    private static final class ResponseConsumerImpl<T> implements ResponseConsumer<T> {

        private final Class<T> clazz;
        private final BiConsumer<T, Throwable> consumer;

        private ResponseConsumerImpl(final Class<T> clazz, final BiConsumer<T, Throwable> consumer) {
            this.clazz = clazz;
            this.consumer = consumer;
        }

        @Override
        public Class<T> getResponseType() {
            return clazz;
        }

        @Override
        public BiConsumer<T, Throwable> getResponseConsumer() {
            return consumer;
        }

    }

}
