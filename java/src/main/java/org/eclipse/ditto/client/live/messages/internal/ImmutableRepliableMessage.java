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

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessageResponseConsumer;
import org.eclipse.ditto.model.things.ThingId;

/**
 * Immutable implementation for {@link RepliableMessage}.
 *
 * @param <T> the type of the message's payload.
 * @param <U> the type of the response's payload.
 * @since 1.0.0
 */
@Immutable
public final class ImmutableRepliableMessage<T, U> implements RepliableMessage<T, U> {

    private final Message<T> message;
    private final Consumer<Message<U>> responseConsumer;

    private ImmutableRepliableMessage(final Message<T> message, final Consumer<Message<U>> responseConsumer) {
        this.message = message;
        this.responseConsumer = responseConsumer;
    }

    /**
     * Returns a new {@code RepliableMessage} instance for the given {@code configuration}, {@code replyTo} and {@code
     * message}.
     *
     * @param message the message to delegate/wrap.
     * @param responseConsumer the consumer which gets notified about the response message.
     * @param <T> the type of the message's payload.
     * @param <U> the type of the response's payload.
     * @return the new {@code RepliableMessage} instance.
     */
    public static <T, U> RepliableMessage<T, U> of(final Message<T> message,
            final Consumer<Message<U>> responseConsumer) {
        argumentNotNull(message, "Message");
        argumentNotNull(responseConsumer, "ResponseConsumer");

        return new ImmutableRepliableMessage<>(message, responseConsumer);
    }

    @Override
    public MessageDirection getDirection() {
        return message.getDirection();
    }

    @Override
    public String getSubject() {
        return message.getSubject();
    }

    @Override
    public ThingId getThingEntityId() {
        return message.getThingEntityId();
    }

    @Override
    public Optional<String> getFeatureId() {
        return message.getFeatureId();
    }

    @Override
    public Optional<T> getPayload() {
        return message.getPayload();
    }

    @Override
    public Optional<ByteBuffer> getRawPayload() {
        return message.getRawPayload();
    }

    @Override
    public MessageHeaders getHeaders() {
        return message.getHeaders();
    }

    @Override
    public Optional<MessageResponseConsumer<?>> getResponseConsumer() {
        return message.getResponseConsumer();
    }

    @Override
    public Optional<String> getContentType() {
        return message.getContentType();
    }

    @Override
    public Optional<Duration> getTimeout() {
        return message.getTimeout();
    }

    @Override
    public Optional<OffsetDateTime> getTimestamp() {
        return message.getTimestamp();
    }

    @Override
    public Optional<String> getCorrelationId() {
        return message.getCorrelationId();
    }

    @Override
    public AuthorizationContext getAuthorizationContext() {
        return message.getAuthorizationContext();
    }

    @Override
    public Optional<HttpStatusCode> getStatusCode() {
        return message.getStatusCode();
    }

    @Override
    public MessageSender.SetPayloadOrSend<U> reply() {
        return ImmutableMessageSender.<U>response().from(responseConsumer)
                .thingId(message.getThingEntityId())
                .featureId(message.getFeatureId().orElse(null))
                .subject(message.getSubject())
                .correlationId(message.getCorrelationId().orElseThrow(
                        () -> DittoRuntimeException.newBuilder("correlation.missing", HttpStatusCode.BAD_REQUEST)
                                .build()));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableRepliableMessage<?, ?> that = (ImmutableRepliableMessage<?, ?>) o;
        return Objects.equals(message, that.message) && Objects.equals(responseConsumer, that.responseConsumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, responseConsumer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "message=" + message +
                ", responseConsumer=" + responseConsumer +
                "]";
    }
}
