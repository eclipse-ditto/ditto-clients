/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.live.internal;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.PendingMessage;
import org.eclipse.ditto.client.live.messages.PendingMessageWithFeatureId;
import org.eclipse.ditto.client.live.messages.PendingMessageWithThingId;
import org.eclipse.ditto.client.live.messages.internal.ImmutableMessageSender;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageResponseConsumer;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.base.ErrorResponse;
import org.eclipse.ditto.signals.commands.messages.MessageCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;
import org.slf4j.Logger;

final class PendingMessageImpl<T> implements PendingMessage<T> {

    private final Logger logger;
    private final OutgoingMessageFactory outgoingMessageFactory;
    private final MessageSerializerRegistry messageSerializerRegistry;
    private final ProtocolAdapter protocolAdapter;
    private final MessagingProvider messagingProvider;

    private PendingMessageImpl(final Logger logger,
            final OutgoingMessageFactory outgoingMessageFactory,
            final MessageSerializerRegistry messageSerializerRegistry,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider) {
        this.logger = logger;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.messageSerializerRegistry = messageSerializerRegistry;
        this.protocolAdapter = protocolAdapter;
        this.messagingProvider = messagingProvider;
    }

    static <T> PendingMessageImpl<T> of(final Logger logger,
            final OutgoingMessageFactory outgoingMessageFactory,
            final MessageSerializerRegistry messageSerializerRegistry,
            final ProtocolAdapter protocolAdapter,
            final MessagingProvider messagingProvider) {

        return new PendingMessageImpl<>(logger, outgoingMessageFactory, messageSerializerRegistry, protocolAdapter,
                messagingProvider);
    }

    PendingMessageWithThingId<T> withThingId(final ThingId thingId) {
        return new PendingMessageWithThingId<T>() {
            @Override
            public MessageSender.SetSubject<T> from() {
                return getSelf().from(thingId);
            }

            @Override
            public MessageSender.SetSubject<T> to() {
                return getSelf().to(thingId);
            }
        };
    }

    PendingMessageWithFeatureId<T> withThingAndFeatureIds(final ThingId thingId, final String featureId) {
        return new PendingMessageWithFeatureId<T>() {
            @Override
            public MessageSender.SetSubject<T> from() {
                return getSelf().from(thingId).featureId(featureId);
            }

            @Override
            public MessageSender.SetSubject<T> to() {
                return getSelf().to(thingId).featureId(featureId);
            }
        };
    }

    private PendingMessageImpl<T> getSelf() {
        return this;
    }

    @SuppressWarnings("unchecked")
    private static void typeCheckAndConsume(final MessageResponseConsumer<?> responseConsumer,
            final Signal<?> response) {

        final BiConsumer uncheckedResponseConsumer = responseConsumer.getResponseConsumer();
        final Class<?> responseType = responseConsumer.getResponseType();

        // throw ClassCastException if response has incorrect type
        final Message<?> responseMessage;
        if (response instanceof MessageCommand) {
            responseMessage = ((MessageCommand) response).getMessage();
        } else if (response instanceof MessageCommandResponse) {
            responseMessage = ((MessageCommandResponse) response).getMessage();
        } else if (response instanceof ErrorResponse) {
            uncheckedResponseConsumer.accept(null, ((ErrorResponse<?>) response).getDittoRuntimeException());
            return;
        } else {
            uncheckedResponseConsumer.accept(null, classCastException(responseType, response));
            return;
        }

        if (responseConsumer.getResponseType().isAssignableFrom(ByteBuffer.class)) {
            uncheckedResponseConsumer.accept(asByteBufferMessage(responseMessage), null);
        } else {
            final Optional<?> payloadOptional = responseMessage.getPayload();
            if (payloadOptional.isPresent()) {
                final Object payload = payloadOptional.get();
                if (responseConsumer.getResponseType().isInstance(payload)) {
                    uncheckedResponseConsumer.accept(payload, null);
                } else {
                    // response has unexpected type
                    uncheckedResponseConsumer.accept(setMessagePayload(responseMessage, null),
                            classCastException(responseType, payload));
                }
            } else {
                // response has no payload; regard it as any message type
                uncheckedResponseConsumer.accept(responseMessage, null);
            }
        }
    }

    private static ClassCastException classCastException(final Class<?> expectedClass, final Object actual) {
        return new ClassCastException(
                "Expected: " + expectedClass.getCanonicalName() +
                        "; Actual: " + actual.getClass().getCanonicalName() +
                        " (" + actual + ")"
        );
    }

    @Override
    public MessageSender.SetFeatureIdOrSubject<T> from(final ThingId thingId) {
        return ImmutableMessageSender.<T>newInstance()
                .from(this::sendMessage)
                .thingId(thingId);
    }

    @Override
    public MessageSender.SetFeatureIdOrSubject<T> to(final ThingId thingId) {
        return ImmutableMessageSender.<T>newInstance()
                .to(this::sendMessage)
                .thingId(thingId);
    }

    private void sendMessage(final Message<T> message) {
        final Message<?> toBeSentMessage =
                outgoingMessageFactory.sendMessage(messageSerializerRegistry, message);
        final String correlationId = toBeSentMessage.getCorrelationId().orElse(null);
        logger.trace("Message about to send: {}", toBeSentMessage);
        message.getResponseConsumer().ifPresent(consumer ->
                messagingProvider.getAdaptableBus().subscribeOnceForAdaptable(
                        Classification.forCorrelationId(correlationId),
                        Duration.ofSeconds(60)
                ).handle((responseAdaptable, error) -> {
                    typeCheckAndConsume(consumer, protocolAdapter.fromAdaptable(responseAdaptable));
                    return null;
                })
        );
        messagingProvider.emitAdaptable(
                LiveMessagesUtil.constructAdaptableFromMessage(toBeSentMessage, protocolAdapter));
    }

    private static Message<ByteBuffer> asByteBufferMessage(final Message<?> message) {
        final ByteBuffer byteBufferPayload = message.getRawPayload()
                .orElseGet(() -> message.getPayload()
                        .map(object -> ByteBuffer.wrap(object.toString().getBytes()))
                        .orElse(ByteBuffer.allocate(0))
                );
        return setMessagePayload(message, byteBufferPayload);
    }

    private static <S, T> Message<T> setMessagePayload(final Message<S> message, @Nullable final T payload) {
        return Message.<T>newBuilder(message.getHeaders())
                .payload(payload)
                .rawPayload(message.getRawPayload().orElse(null))
                .extra(message.getExtra().orElse(null))
                .responseConsumer(message.getResponseConsumer().orElse(null))
                .build();
    }

}
