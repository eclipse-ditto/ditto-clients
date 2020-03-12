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
package org.eclipse.ditto.client.messaging;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classifiers;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.events.base.Event;

/**
 * Interface to be used when implementing a messaging provider for the Things client.
 *
 * @since 1.0.0
 */
public interface MessagingProvider {

    /**
     * Initializes the Messaging Provider by opening the underlying connections, etc.
     */
    void initialize();

    /**
     * Returns the {@code AuthenticationConfiguration} of this provider.
     *
     * @return the configuration.
     */
    AuthenticationConfiguration getAuthenticationConfiguration();

    /**
     * Returns the {@code MessagingConfiguration} of this provider.
     *
     * @return the configuration.
     */
    MessagingConfiguration getMessagingConfiguration();

    /**
     * Returns the {@code ExecutorService} of this provider.
     *
     * @return the executor service.
     */
    ExecutorService getExecutorService();

    /**
     * Returns the {@code AdaptableBus} to which incoming messages are published.
     *
     * @return the adaptable bus.
     */
    AdaptableBus getAdaptableBus();

    /**
     * Emit a message in a fire-and-forget manner.
     *
     * @param message the message to emit.
     */
    void emit(String message);

    /**
     * Emit an adaptable message in a fire-and-forget manner.
     *
     * @param message the message to emit.
     */
    default void emitAdaptable(Adaptable message) {
        emit(ProtocolFactory.wrapAsJsonifiableAdaptable(message).toJsonString());
    }

    /**
     * Send Ditto Protocol {@link Adaptable} using the underlying connection.
     * TODO: does this method belong here?
     *
     * @param adaptable the adaptable to be sent
     * @return a CompletableFuture containing the correlated response to the sent {@code dittoProtocolAdaptable}
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send Messages
     */
    default CompletableFuture<Adaptable> sendAdaptable(Adaptable adaptable) {
        final String correlationId = adaptable.getDittoHeaders()
                .getCorrelationId()
                .orElseGet(() -> UUID.randomUUID().toString());
        final Adaptable adaptableToSend = adaptable.getDittoHeaders()
                .getCorrelationId()
                .map(cid -> adaptable)
                .orElseGet(() -> adaptable.setDittoHeaders(
                        adaptable.getDittoHeaders().toBuilder().correlationId(correlationId).build())
                );
        final CompletableFuture<Adaptable> result = getAdaptableBus()
                .subscribeOnceForAdaptable(Classifiers.forCorrelationId(correlationId), Duration.ofSeconds(60L))
                .toCompletableFuture();
        emitAdaptable(adaptableToSend);
        return result;
    }

    /**
     * Send message using the underlying connection.
     *
     * @param message the message to be sent
     * @param channel the Channel to use for sending the message (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send Messages
     */
    @Deprecated
    default void send(Message<?> message, TopicPath.Channel channel) {
        throw new UnsupportedOperationException();
    }

    /**
     * Send command using the underlying connection.
     *
     * @param command the command to be sent
     * @param channel the Channel to use for sending the command (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send Commands
     */
    @Deprecated
    default void sendCommand(Command<?> command, TopicPath.Channel channel) {
        throw new UnsupportedOperationException();
    }

    /**
     * Send CommandResponse using the underlying connection.
     *
     * @param commandResponse the CommandResponse to be sent
     * @param channel the Channel to use for sending the commandResponse (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send CommandResponses
     */
    @Deprecated
    default void sendCommandResponse(CommandResponse<?> commandResponse, TopicPath.Channel channel) {
        throw new UnsupportedOperationException();
    }

    /**
     * Emits Event using the underlying connection.
     *
     * @param event the Event to be emitted
     * @param channel the Channel to use for emitting the event (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to emit Events
     */
    @Deprecated
    default void emitEvent(Event<?> event, TopicPath.Channel channel) {
        throw new UnsupportedOperationException();
    }

    /**
     * Register handler for ThingCommandResponse.
     *
     * @param commandResponseHandler the consumer that is called for incoming command response messages
     */
    @Deprecated
    default void registerReplyHandler(Consumer<CommandResponse<?>> commandResponseHandler) {
        throw new UnsupportedOperationException();
    }

    /**
     * Register a named message handler and receipt handler.
     *
     * @param name name of the message handler
     * @param registrationConfig optional configuration for this registration
     * @param handler the handler invoked when new message are received
     * @param receiptFuture the future that takes responses to this register call
     * @return the {@code true} if handler was registered, {@code false} otherwise (e.g. handler was already registered)
     */
    @Deprecated
    default boolean registerMessageHandler(String name,
            Map<String, String> registrationConfig,
            Consumer<Message<?>> handler,
            CompletableFuture<Void> receiptFuture) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove a previously registered message handler.
     *
     * @param name name of the handler to deregister
     * @param future the future to handle the response
     */
    @Deprecated
    default void deregisterMessageHandler(String name, CompletableFuture<Void> future) {
        throw new UnsupportedOperationException();
    }

    /**
     * Close the underlying connection.
     */
    void close();

}
