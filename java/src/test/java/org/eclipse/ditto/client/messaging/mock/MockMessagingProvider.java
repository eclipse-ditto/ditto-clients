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
package org.eclipse.ditto.client.messaging.mock;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.internal.TwinImpl;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageBuilder;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessageHeadersBuilder;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.JsonifiableAdaptable;
import org.eclipse.ditto.protocoladapter.Payload;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.WithFeatureId;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.events.base.Event;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.eclipse.ditto.utils.jsr305.annotations.AllParametersAndReturnValuesAreNonnullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mocks messaging for unit tests.
 */
@AllParametersAndReturnValuesAreNonnullByDefault
public class MockMessagingProvider implements MessagingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockMessagingProvider.class);

    private final AuthenticationConfiguration authenticationConfiguration =
            BasicAuthenticationConfiguration.newBuilder()
                    .username("hans")
                    .password("dampf")
                    .build();
    private final MessagingConfiguration messagingConfiguration = WebSocketMessagingConfiguration.newBuilder()
            .endpoint("ws://localhost:8080")
            .jsonSchemaVersion(JsonSchemaVersion.V_2)
            .build();
    private final AtomicReference<Consumer<Message<?>>> in = new AtomicReference<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, new MockThreadFactory());
    private final BlockingQueue<Message<?>> eventQueue = new LinkedBlockingQueue<>();
    private final JsonifiableAdaptable dummyAdaptable =
            ProtocolFactory.wrapAsJsonifiableAdaptable(Adaptable.newBuilder(TopicPath.newBuilder(ThingId.dummy())
                    .twin()
                    .commands()
                    .modify()
                    .build())
                    .withHeaders(DittoHeaders.empty())
                    .withPayload(Payload.newBuilder().build())
                    .build());

    private Consumer<Message<?>> out = message -> {
        throw new IllegalStateException("Unhandled out-message: " + message);
    };
    private Consumer<PolicyCommand> outgoingPolicyCommand = command -> {
        throw new IllegalStateException("Unhandled outgoing command: " + command);
    };

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
    public void initialize() {
        // noop
    }

    @Override
    public CompletableFuture<Adaptable> sendAdaptable(final Adaptable adaptable) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void send(final Message<?> message, final TopicPath.Channel channel) {
        Objects.requireNonNull(out);
        out.accept(message);
    }

    private void sendPolicyCommand(final Command policyCommand, final TopicPath.Channel channel) {
        outgoingPolicyCommand.accept((PolicyCommand) policyCommand);
    }

    @Override
    public void sendCommand(final Command<?> command, final TopicPath.Channel channel) {
        //Check if command is a PolicyCommand. Else its a ThingCommand
        if (command instanceof PolicyCommand) {
            sendPolicyCommand(command, channel);
        } else {
            sendThingCommand(command, channel);
        }
    }

    private void sendThingCommand(final Command<?> command, final TopicPath.Channel channel) {
        Objects.requireNonNull(out);
        final DittoHeaders dittoHeaders = command.getDittoHeaders();

        MessageHeadersBuilder headersBuilder =
                MessageHeaders.newBuilder(MessageDirection.FROM, command.getEntityId(), command.getType())
                        .putHeaders(command.getDittoHeaders())
                        .timestamp(OffsetDateTime.now());

        if (command instanceof WithFeatureId) {
            headersBuilder.featureId(((WithFeatureId) command).getFeatureId());
        }
        dittoHeaders.getCorrelationId().ifPresent(headersBuilder::correlationId);

        final MessageBuilder<ThingCommand> messageBuilder = Message.<ThingCommand>newBuilder(headersBuilder.build())
                .payload((ThingCommand) command);

        final Message<ThingCommand> message = messageBuilder.build();
        out.accept(message);
    }

    @Override
    public void sendCommandResponse(final CommandResponse<?> commandResponse, final TopicPath.Channel channel) {
        throw new UnsupportedOperationException("MockMessagingProvider is not able to sendCommandResponse()");
    }

    @Override
    public void emitEvent(final Event<?> event, final TopicPath.Channel channel) {
        throw new UnsupportedOperationException("MockMessagingProvider is not able to emitEvent()");
    }

    public void onCommandSend(final Consumer<PolicyCommand> outgoingCommand) {
        Objects.requireNonNull(outgoingCommand);
        this.outgoingPolicyCommand = outgoingCommand;
    }

    public void onSend(final Consumer<Message<?>> out) {
        Objects.requireNonNull(out);
        this.out = out;
    }

    public void receiveEvent(final Message<ThingEvent> message) {
        LOGGER.debug("Manually receiving Event: {}", message);
        eventQueue.add(message);
    }

    @Override
    public void registerReplyHandler(final Consumer<CommandResponse> commandResponseConsumer) {
        // noop
    }

    @Override
    public boolean registerMessageHandler(final String name, final Map<String, String> registrationConfig,
            final Consumer<Message<?>> handler, final CompletableFuture<Void> future) {
        if (name.equals(TwinImpl.CONSUME_TWIN_EVENTS_HANDLER)) {
            Objects.requireNonNull(handler);
            in.set(handler);
            executor.execute(() ->
            {
                try {
                    while (in.get() != null) {
                        in.get().accept(eventQueue.take());
                        LOGGER.debug("Took one message from eventQueue, waiting for next..");
                    }
                } catch (final InterruptedException e) {
                    LOGGER.debug("eventQueue.take was interrupted");
                    Thread.currentThread().interrupt();
                }
            });
        }
        return true;
    }

    @Override
    public void deregisterMessageHandler(final String name, final CompletableFuture<Void> future) {
        in.set(null);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private static class MockThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "mock-provider-" + count.incrementAndGet());
        }
    }
}
