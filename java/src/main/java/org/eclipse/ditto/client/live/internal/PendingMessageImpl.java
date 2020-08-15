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

import java.time.Duration;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.ack.ResponseConsumer;
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
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.signals.base.Signal;
import org.slf4j.Logger;

final class PendingMessageImpl<T> implements PendingMessage<T> {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1L);

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

    private static void typeCheckAndConsume(final ResponseConsumer<?> responseConsumer, final Signal<?> response) {
        try {
            responseConsumer.accept(response);
        } catch (final Throwable e) {
            responseConsumer.getResponseConsumer().accept(null, e);
        }
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

    private void sendMessage(final Message<T> message, @Nullable final ResponseConsumer<?> responseConsumer) {
        final Message<?> toBeSentMessage =
                outgoingMessageFactory.sendMessage(messageSerializerRegistry, message);
        logger.trace("Message about to send: {}", toBeSentMessage);
        if (responseConsumer != null) {
            toBeSentMessage.getCorrelationId().ifPresent(correlationId ->
                    messagingProvider.getAdaptableBus().subscribeOnceForAdaptable(
                            Classification.forCorrelationId(correlationId),
                            getCallbackTTL(message)
                    ).handle((responseAdaptable, error) -> {
                        typeCheckAndConsume(responseConsumer, protocolAdapter.fromAdaptable(responseAdaptable));
                        return null;
                    })
            );
        }
        messagingProvider.emitAdaptable(
                LiveMessagesUtil.constructAdaptableFromMessage(toBeSentMessage, protocolAdapter));
    }

    private static Duration getCallbackTTL(final Message<?> message) {
        // set handler timeout to some time after actual timeout to account for delay and latency in both directions.
        return message.getTimeout().orElse(DEFAULT_TIMEOUT).plus(Duration.ofSeconds(10L));
    }

}
