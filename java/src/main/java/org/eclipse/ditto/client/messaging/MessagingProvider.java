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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.JsonifiableAdaptable;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
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
     * Send Ditto Protocol {@link Adaptable} using the underlying connection.
     *
     * @param adaptable the adaptable to be sent
     * @return a CompletableFuture containing the correlated response to the sent {@code dittoProtocolAdaptable}
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send Messages
     */
    CompletableFuture<Adaptable> sendAdaptable(Adaptable adaptable);

    /**
     * Send message using the underlying connection.
     *
     * @param message the message to be sent
     * @param channel the Channel to use for sending the message (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send Messages
     */
    void send(Message<?> message, TopicPath.Channel channel);

    /**
     * Send command using the underlying connection.
     *
     * @param command the command to be sent
     * @param channel the Channel to use for sending the command (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send Commands
     */
    void sendCommand(Command<?> command, TopicPath.Channel channel);

    /**
     * Send CommandResponse using the underlying connection.
     *
     * @param commandResponse the CommandResponse to be sent
     * @param channel the Channel to use for sending the commandResponse (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to send CommandResponses
     */
    void sendCommandResponse(CommandResponse<?> commandResponse, TopicPath.Channel channel);

    /**
     * Emits Event using the underlying connection.
     *
     * @param event the Event to be emitted
     * @param channel the Channel to use for emitting the event (Live/Twin)
     * @throws UnsupportedOperationException if the MessagingProvider is not able to emit Events
     */
    void emitEvent(Event<?> event, TopicPath.Channel channel);

    /**
     * Register handler for ThingCommandResponse.
     *
     * @param commandResponseHandler the consumer that is called for incoming command response messages
     */
    void registerReplyHandler(Consumer<ThingCommandResponse> commandResponseHandler);

    /**
     * Register a named message handler and receipt handler.
     *
     * @param name name of the message handler
     * @param registrationConfig optional configuration for this registration
     * @param handler the handler invoked when new message are received
     * @param receiptFuture the future that takes responses to this register call
     * @return the {@code true} if handler was registered, {@code false} otherwise (e.g. handler was already registered)
     */
    boolean registerMessageHandler(String name,
            Map<String, String> registrationConfig,
            BiConsumer<Message<?>, JsonifiableAdaptable> handler,
            CompletableFuture<Void> receiptFuture);

    /**
     * Remove a previously registered message handler.
     *
     * @param name name of the handler to deregister
     * @param future the future to handle the response
     */
    void deregisterMessageHandler(String name, CompletableFuture<Void> future);

    /**
     * Close the underlying connection.
     */
    void close();

}
