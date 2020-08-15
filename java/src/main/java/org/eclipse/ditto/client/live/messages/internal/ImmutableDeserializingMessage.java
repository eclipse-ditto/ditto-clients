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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.messages.MessageSerializationException;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.MessageSerializers;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessageResponseConsumer;
import org.eclipse.ditto.client.ack.ResponseConsumer;
import org.eclipse.ditto.model.things.ThingId;

/**
 * A specialized Message implementation which delegates all calls to a passed in {@code delegateMessage} except for the
 * {@link #getPayload()} call which dynamically deserializes the bytes of {@link #getRawPayload()} and creates an
 * instance of type {@code <T>}.
 *
 * @param <T> the type of the message's payload to deserialize.
 * @since 1.0.0
 */
@Immutable
public final class ImmutableDeserializingMessage<T> implements Message<T> {

    private final Message<T> delegateMessage;
    private final Class<T> payloadType;
    private final MessageSerializerRegistry serializerRegistry;

    private ImmutableDeserializingMessage(final Message<T> delegateMessage, final Class<T> payloadType,
            final MessageSerializerRegistry serializerRegistry) {
        this.delegateMessage = delegateMessage;
        this.payloadType = payloadType;
        this.serializerRegistry = serializerRegistry;
    }

    /**
     * Creates a new dynamically deserializing Message.
     *
     * @param delegateMessage the Message to delegate method invocations to.
     * @param payloadType the Java type of the consumed Messages.
     * @param serializerRegistry the MessageSerializerRegistry to find the appropriate {@code MessageSerializer} in.
     * @param <T> the type of the payload to deserialize.
     * @return the new dynamically deserializing Message.
     */
    public static <T> Message<T> of(final Message<T> delegateMessage, final Class<T> payloadType,
            final MessageSerializerRegistry serializerRegistry) {
        return new ImmutableDeserializingMessage<>(delegateMessage, payloadType, serializerRegistry);
    }

    /**
     * {@inheritDoc}
     *
     * @throws MessageSerializationException if no suiting deserializers could be found or deserialization failed
     * exceptionally.
     */
    @Override
    public Optional<T> getPayload() {

        final String subject = delegateMessage.getSubject();
        final Optional<String> optContentType = delegateMessage.getContentType();
        final Optional<Charset> optCharset = MessageSerializers.determineCharsetFromContentType(optContentType);

        final BiFunction<ByteBuffer, Charset, T> deserializer =
                optContentType.map(ct -> serializerRegistry.findSerializerFor(ct, payloadType, subject))
                        .orElseGet(() -> serializerRegistry.findSerializerFor(payloadType, subject))
                        .orElseThrow(() -> optContentType.map(contentType -> new MessageSerializationException(
                                "No deserializer found for contentType '" + contentType + "'" + " and payload type '" +
                                        payloadType + "'"))
                                .orElse(new MessageSerializationException(
                                        "No deserializer found for payload type '" + payloadType + "'")))
                        .getDeserializer();

        if (delegateMessage.getRawPayload().isPresent()) {
            return delegateMessage.getRawPayload().map(body -> {
                try {
                    return deserializer.apply(body, optCharset.orElse(StandardCharsets.UTF_8));
                } catch (final RuntimeException e) {
                    // something went wrong during deserialization
                    throw new MessageSerializationException(
                            "Deserialization of message for subject '" + subject + "' failed", e);
                }
            });
        } else if (delegateMessage.getPayload().isPresent()) {
            return delegateMessage.getPayload().map(payload -> {
                try {
                    return deserializer.apply(ByteBuffer.wrap(
                            payload.toString().getBytes(optCharset.orElse(StandardCharsets.UTF_8))),
                            optCharset.orElse(StandardCharsets.UTF_8));
                } catch (final RuntimeException e) {
                    // something went wrong during deserialization
                    throw new MessageSerializationException(
                            "Deserialization of message for subject '" + subject + "' failed", e);
                }
            });
        } else {
            return Optional.empty();
        }
    }

    @Override
    public MessageDirection getDirection() {
        return delegateMessage.getDirection();
    }

    @Override
    public ThingId getThingEntityId() {
        return delegateMessage.getThingEntityId();
    }

    @Override
    public String getSubject() {
        return delegateMessage.getSubject();
    }

    @Override
    public Optional<String> getFeatureId() {
        return delegateMessage.getFeatureId();
    }

    @Override
    public Optional<String> getContentType() {
        return delegateMessage.getContentType();
    }

    @Override
    public Optional<ByteBuffer> getRawPayload() {
        return delegateMessage.getRawPayload();
    }

    @Override
    public Optional<JsonObject> getExtra() {
        return delegateMessage.getExtra();
    }

    @Override
    public MessageHeaders getHeaders() {
        return delegateMessage.getHeaders();
    }

    @Override
    public Optional<MessageResponseConsumer<?>> getResponseConsumer() {
        return delegateMessage.getResponseConsumer();
    }

    @Override
    public Optional<Duration> getTimeout() {
        return delegateMessage.getTimeout();
    }

    @Override
    public Optional<OffsetDateTime> getTimestamp() {
        return delegateMessage.getTimestamp();
    }

    @Override
    public Optional<String> getCorrelationId() {
        return delegateMessage.getCorrelationId();
    }

    @Override
    public AuthorizationContext getAuthorizationContext() {
        return delegateMessage.getAuthorizationContext();
    }

    @Override
    public Optional<HttpStatusCode> getStatusCode() {
        return delegateMessage.getStatusCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableDeserializingMessage<?> that = (ImmutableDeserializingMessage<?>) o;
        return Objects.equals(delegateMessage, that.delegateMessage) && Objects.equals(payloadType, that.payloadType)
                && Objects.equals(serializerRegistry, that.serializerRegistry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegateMessage, payloadType, serializerRegistry);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "direction=" + getDirection() +
                ", thingId=" + getThingEntityId() +
                ", featureId=" + getFeatureId().orElse(null) +
                ", subject=" + getSubject() +
                ", contentType=" + getContentType().orElse(null) +
                ", rawPayload=" + getRawPayload().orElse(null) +
                ", payload=" + getPayload().orElse(null) +
                ", timeout=" + getTimeout().orElse(null) +
                ", timestamp=" + getTimestamp().orElse(null) +
                ", correlationId=" + getCorrelationId().orElse(null) +
                "]";
    }
}
