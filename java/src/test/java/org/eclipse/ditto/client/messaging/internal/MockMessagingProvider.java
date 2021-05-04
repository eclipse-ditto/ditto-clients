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
package org.eclipse.ditto.client.messaging.internal;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.BusFactory;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.adapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocol.HeaderTranslator;
import org.eclipse.ditto.protocol.Payload;
import org.eclipse.ditto.protocol.adapter.ProtocolAdapter;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.things.model.signals.events.ThingEvent;
import org.eclipse.ditto.utils.jsr305.annotations.AllParametersAndReturnValuesAreNonnullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mocks messaging for unit tests.
 */
@AllParametersAndReturnValuesAreNonnullByDefault
public class MockMessagingProvider implements MessagingProvider {

    private static final ProtocolAdapter PROTOCOL_ADAPTER = DittoProtocolAdapter.of(HeaderTranslator.empty());
    private static final Logger LOGGER = LoggerFactory.getLogger(MockMessagingProvider.class);

    private final AuthenticationConfiguration authenticationConfiguration =
            BasicAuthenticationConfiguration.newBuilder()
                    .username("hans")
                    .password("dampf")
                    .build();
    private final AtomicReference<Consumer<Message<?>>> in = new AtomicReference<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, new MockThreadFactory());
    private final MessagingConfiguration messagingConfiguration;
    private final AdaptableBus adaptableBus = BusFactory.createAdaptableBus();
    private final BlockingQueue<String> emittedMessages = new LinkedBlockingQueue<>();
    private final AtomicReference<Consumer<Object>> onSendConsumer = new AtomicReference<>(m -> {});

    public MockMessagingProvider() {
        this(JsonSchemaVersion.LATEST);
    }

    public MockMessagingProvider(final JsonSchemaVersion schemaVersion) {
        this.messagingConfiguration = getDefaultMessagingConfiguration(schemaVersion);
    }

    @Override
    public AuthenticationConfiguration getAuthenticationConfiguration() {
        return authenticationConfiguration;
    }

    @Override
    public MessagingConfiguration getMessagingConfiguration() {
        return messagingConfiguration;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executor;
    }

    @Override
    public AdaptableBus getAdaptableBus() {
        return adaptableBus;
    }

    @Override
    public MessagingProvider registerSubscriptionMessage(final Object key, final String message) {
        // no-op
        return this;
    }

    @Override
    public MessagingProvider unregisterSubscriptionMessage(final Object key) {
        // no-op
        return this;
    }

    @Override
    public CompletionStage<?> initializeAsync() {
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public void emit(final String message) {
        onSendConsumer.get().accept(message);
        emittedMessages.add(message);
    }

    public String expectEmitted() {
        try {
            final String result = emittedMessages.poll(1L, TimeUnit.SECONDS);
            if (result == null) {
                throw new TimeoutException("No message emitted");
            } else {
                return result;
            }
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    public void clearEmitted() {
        emittedMessages.clear();
    }

    public void onSend(final Consumer<Object> out) {
        onSendConsumer.set(Objects.requireNonNull(out));
    }

    public void receiveEvent(final Message<ThingEvent> message) {
        LOGGER.debug("Manually receiving Event: {}", message);
        final Adaptable fromMessagePayload =
                PROTOCOL_ADAPTER.toAdaptable((Signal<?>) message.getPayload().orElseThrow(NoSuchElementException::new));
        final Adaptable adaptable =
                ProtocolFactory.newAdaptableBuilder(fromMessagePayload)
                        .withPayload(Payload.newBuilder(fromMessagePayload.getPayload())
                                .withExtra(message.getExtra().orElse(null))
                                .build())
                        .build();
        receiveAdaptable(adaptable);
    }

    public void receiveAdaptable(final Adaptable adaptable) {
        adaptableBus.publish(ProtocolFactory.wrapAsJsonifiableAdaptable(adaptable).toJsonString());
    }

    @Override
    public void close() {
        adaptableBus.shutdownExecutor();
        executor.shutdownNow();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.info("Waiting for termination was interrupted.");
        }
    }

    private static MessagingConfiguration getDefaultMessagingConfiguration(final JsonSchemaVersion version) {
        return WebSocketMessagingConfiguration.newBuilder()
                .endpoint("ws://localhost:8080")
                .jsonSchemaVersion(version)
                .initialConnectRetryEnabled(true)
                .build();
    }

    private static class MockThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "mock-provider-" + count.incrementAndGet());
        }
    }
}
