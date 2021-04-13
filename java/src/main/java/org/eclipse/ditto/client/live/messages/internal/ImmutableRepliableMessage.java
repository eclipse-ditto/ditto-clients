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
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.ack.internal.ImmutableAcknowledgementRequestHandle;
import org.eclipse.ditto.client.changes.AcknowledgementRequestHandle;
import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessageResponseConsumer;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;

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
    private final Consumer<Acknowledgement> acknowledgementPublisher;

    private ImmutableRepliableMessage(final Message<T> message, final Consumer<Message<U>> responseConsumer,
            final Consumer<Acknowledgement> acknowledgementPublisher) {

        this.message = message;
        this.responseConsumer = responseConsumer;
        this.acknowledgementPublisher = acknowledgementPublisher;
    }

    /**
     * Returns a new {@code RepliableMessage} instance for the given {@code configuration}, {@code replyTo} and {@code
     * message}.
     *
     * @param message the message to delegate/wrap.
     * @param responseConsumer the consumer which gets notified about the response message.
     * @param acknowledgementPublisher the consumer for publishing built acknowledgements to the Ditto backend.
     * @param <T> the type of the message's payload.
     * @param <U> the type of the response's payload.
     * @return the new {@code RepliableMessage} instance.
     */
    public static <T, U> RepliableMessage<T, U> of(final Message<T> message,
            final Consumer<Message<U>> responseConsumer, final Consumer<Acknowledgement> acknowledgementPublisher) {
        argumentNotNull(message, "Message");
        argumentNotNull(responseConsumer, "ResponseConsumer");

        return new ImmutableRepliableMessage<>(message, responseConsumer, acknowledgementPublisher);
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
    public ThingId getEntityId() {
        return message.getEntityId();
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
    public Optional<JsonObject> getExtra() {
        return message.getExtra();
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
    public Optional<HttpStatus> getHttpStatus() {
        return message.getHttpStatus();
    }

    @Override
    public MessageSender.SetPayloadOrSend<U> reply() {
        return ImmutableMessageSender.<U>response().from(responseConsumer)
                .thingId(message.getEntityId())
                .featureId(message.getFeatureId().orElse(null))
                .subject(message.getSubject())
                .correlationId(message.getCorrelationId()
                        .orElseThrow(() -> MissingCorrelationIdException.newBuilder().build()));
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

    @Override
    public void handleAcknowledgementRequests(
            final Consumer<Collection<AcknowledgementRequestHandle>> acknowledgementHandles) {

        checkNotNull(acknowledgementHandles, "acknowledgementHandles");
        final MessageHeaders headers = message.getHeaders();
        final Set<AcknowledgementRequest> acknowledgementRequests = headers.getAcknowledgementRequests();
        final ThingId thingId = message.getEntityId();
        acknowledgementHandles.accept(
                acknowledgementRequests.stream()
                        .map(request -> new ImmutableAcknowledgementRequestHandle(request.getLabel(), thingId, headers,
                                acknowledgementPublisher))
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

    @Override
    public void handleAcknowledgementRequest(final AcknowledgementLabel acknowledgementLabel,
            final Consumer<AcknowledgementRequestHandle> acknowledgementHandle) {

        checkNotNull(acknowledgementLabel, "acknowledgementLabel");
        checkNotNull(acknowledgementHandle, "acknowledgementHandle");
        final MessageHeaders headers = message.getHeaders();
        final Set<AcknowledgementRequest> acknowledgementRequests = headers.getAcknowledgementRequests();
        final ThingId thingId = message.getEntityId();
        acknowledgementRequests.stream()
                .filter(req -> req.getLabel().equals(acknowledgementLabel))
                .map(request -> new ImmutableAcknowledgementRequestHandle(request.getLabel(), thingId, headers,
                        acknowledgementPublisher))
                .forEach(acknowledgementHandle);
    }

}
