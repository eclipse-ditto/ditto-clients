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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.management.ClientReconnectingException;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;

/**
 * Interface to be used when implementing a messaging provider for the Things client.
 *
 * @since 1.0.0
 */
public interface MessagingProvider {

    /**
     * Perform initialization asynchronously.
     *
     * @return a future that completes after initialization completed.
     * @since 1.3.0
     */
    CompletionStage<?> initializeAsync();

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
     * Returns the {@code AdaptableBus} to which all incoming messages are published.
     *
     * @return the adaptable bus.
     * @since 1.1.0
     */
    AdaptableBus getAdaptableBus();

    /**
     * Register a subscription message by key to send on reconnect.
     * Replace previously registered subscription messages with the same key.
     * It should be a no-op for messaging providers over channels where no subscription message is allowed,
     * e. g., MQTT.
     *
     * @param key the key of the subscription message.
     * @param message the subscription message.
     * @return this object.
     */
    MessagingProvider registerSubscriptionMessage(Object key, String message);

    /**
     * Remove a subscription message to send on reconnect by its key.
     * It should be a no-opo for messaging providers over channels where no subscription message is allowed,
     * e. g., MQTT.
     *
     * @param key the key with which the subscription message is registered.
     * @return this object.
     */
    MessagingProvider unregisterSubscriptionMessage(Object key);

    /**
     * Send a message into the channel provided by this provider.
     *
     * @param message the message to emit.
     * @throws org.eclipse.ditto.client.management.ClientReconnectingException if the client is reconnecting and thus
     * can't emit the message.
     * @since 1.1.0
     */
    void emit(String message);

    /**
     * Emit an adaptable message in a fire-and-forget manner.
     *
     * @param message the message to emit.
     * @throws org.eclipse.ditto.client.management.ClientReconnectingException if the client is reconnecting and thus
     * can't emit the message.
     * @since 1.1.0
     */
    default void emitAdaptable(Adaptable message) {
        emit(ProtocolFactory.wrapAsJsonifiableAdaptable(message).toJsonString());
    }

    /**
     * Send Ditto Protocol {@link Adaptable} using the underlying connection and expect a response.
     *
     * @param adaptable the adaptable to be sent
     * @return a CompletionStage containing the correlated response to the sent {@code dittoProtocolAdaptable} or is
     * failed with a {@link org.eclipse.ditto.client.management.ClientReconnectingException}, when the client is in a
     * reconnecting state.
     */
    default CompletionStage<Adaptable> sendAdaptable(final Adaptable adaptable) {
        try {
            final String correlationId = adaptable.getDittoHeaders()
                    .getCorrelationId()
                    .orElseGet(() -> UUID.randomUUID().toString());
            final Adaptable adaptableToSend = adaptable.getDittoHeaders()
                    .getCorrelationId()
                    .map(cid -> adaptable)
                    .orElseGet(() -> adaptable.setDittoHeaders(
                            adaptable.getDittoHeaders().toBuilder().correlationId(correlationId).build())
                    );
            final Duration timeout = getMessagingConfiguration().getTimeout();
            final CompletionStage<Adaptable> result = getAdaptableBus()
                    .subscribeOnceForAdaptable(Classification.forCorrelationId(correlationId), timeout);
            emitAdaptable(adaptableToSend);
            return result;
        } catch (final ClientReconnectingException cre) {
            return CompletableFuture.supplyAsync(() -> {
                throw cre;
            });
        }
    }

    /**
     * Close the underlying connection.
     */
    void close();

    /**
     * Registers a {@code Runnable} to run when the user code performs a {@code closeChannel} in the disconnection
     * handler of a registered disconnectionListener.
     *
     * @param channelCloser the runnable to run
     * @since 2.1.0
     */
    void registerChannelCloser(Runnable channelCloser);

    /**
     * Injects an error provided via Ditto Protocol message into the messaging provider in order to make it available
     * to a user code provided {@code DisconnectedContext}.
     *
     * @param throwable the {@code DittoRuntimeException} or another throwable providing the error.
     * @since 2.1.0
     */
    void onDittoProtocolError(Throwable throwable);

}
