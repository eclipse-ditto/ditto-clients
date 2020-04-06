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
package org.eclipse.ditto.client.internal;

import static java.util.Objects.requireNonNull;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.acks.base.Acknowledgements;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.policies.modify.PolicyModifyCommandResponse;
import org.eclipse.ditto.signals.commands.policies.query.PolicyQueryCommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommandResponse;
import org.eclipse.ditto.signals.commands.things.query.ThingQueryCommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terminates a command i.e. sends the according message(s). Provides methods to register callbacks for result and
 * exceptions that occurred during execution.
 *
 * @param <T> The type of the message.
 * @since 1.0.0
 */
public final class SendTerminator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendTerminator.class);
    private static final String COMMAND_ARGUMENT_NAME = "command";

    private final Command<?> command;
    private final Message<T> message;
    private final MessagingProvider messagingProvider;
    private final ResponseForwarder responseForwarder;
    private final TopicPath.Channel channel;

    /**
     * Constructs a new {@code SendTerminator} object.
     *
     * @param messagingProvider the messaging provider.
     * @param responseForwarder the response forwarder.
     * @param channel the Channel to use for sending the command (Live/Twin).
     * @param command the outgoing message.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public SendTerminator(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final TopicPath.Channel channel,
            final ThingCommand<?> command) {

        this(messagingProvider, responseForwarder, channel, checkNotNull(command, COMMAND_ARGUMENT_NAME), null);
    }

    /**
     * Constructs a new {@code SendTerminator} object.
     *
     * @param messagingProvider the messaging provider.
     * @param responseForwarder the response forwarder.
     * @param message the outgoing message.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public SendTerminator(final MessagingProvider messagingProvider, final ResponseForwarder responseForwarder,
            final Message<T> message) {

        // messages are always sent on "live" channel
        this(messagingProvider, responseForwarder, TopicPath.Channel.LIVE, null,
                checkNotNull(message, "message to be sent"));
    }

    /**
     * Constructs a new {@code SendTerminator} object.
     *
     * @param messagingProvider the messaging provider.
     * @param responseForwarder the response forwarder.
     * @param policyCommand the outgoing message.
     * @throws NullPointerException if any argument is {@code null}.
     * @since 1.1.0
     */
    public SendTerminator(final MessagingProvider messagingProvider, final ResponseForwarder responseForwarder,
            final PolicyCommand<?> policyCommand) {

        // commands are always sent on "NONE" channel, to modify Policies.
        this(messagingProvider, responseForwarder, TopicPath.Channel.NONE,
                checkNotNull(policyCommand, COMMAND_ARGUMENT_NAME), null);
    }


    private SendTerminator(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final TopicPath.Channel channel,
            @Nullable final Command<?> command,
            @Nullable final Message<T> message) {

        this.messagingProvider = checkNotNull(messagingProvider, "messagingProvider");
        this.responseForwarder = checkNotNull(responseForwarder, "responseForwarder");
        this.channel = checkNotNull(channel, "channel");
        this.command = command;
        this.message = message;
    }

    /**
     * Sends the {@code message} without expecting a response.
     */
    public void send() {
        requireNonNull(message, "A message must be present in order to call send!");
        LOGGER.trace("Sending message <{}>.", message);
        messagingProvider.send(message, channel);
    }

    /**
     * Applies the {@code command} of this SendTerminator with "modify" behavior - expecting a {@link
     * CommandResponse} as result and returning a Future of the type {@code <T>}.
     *
     * @param function the function to apply to extract an instance of type {@code <T>} from the returned
     * {@link ThingModifyCommandResponse}.
     * @return a CompletableFuture of type {@code <T>}.
     * @throws NullPointerException if {@code function} is {@code null}.
     */
    public CompletableFuture<T> applyModify(final Function<CommandResponse, T> function) {
        checkNotNull(function, "function");
        final CompletableFuture<CommandResponse> intermediaryResult = createIntermediaryResult();
        LOGGER.trace("Sending modify command <{}>.", command);
        messagingProvider.sendCommand(command, channel);

        return intermediaryResult.thenApply(result -> {
            // also handle Acknowledgements which are a CommandResponse as well:
            if (result instanceof Acknowledgements) {
                // from the "twin-persisted" acknowledgement, create the actual CommandResponse and feed it back
                return ((Acknowledgements) result).stream()
                        .filter(ack -> ack.getLabel().equals(DittoAcknowledgementLabel.PERSISTED))
                        .findFirst()
                        .map(this::createThingModifyCommandResponseFromAcknowledgement)
                        .map(signal -> (CommandResponse) signal)
                        .orElseThrow(() -> new IllegalStateException("Didn't receive an Acknowledgement for label '" +
                                DittoAcknowledgementLabel.PERSISTED + "'. Please make sure to always request the '"  +
                                DittoAcknowledgementLabel.PERSISTED + "' Acknowledgement if you need to process the " +
                                "response in the client."));
            }
            return result;
        }).thenApply(function);
    }

    /**
     * Applies the {@code command} of this SendTerminator with "modify" behavior - expecting a {@link
     * PolicyModifyCommandResponse} as result and returning a Future of the type {@code <T>}.
     *
     * @param function the function to apply to extract an instance of type {@code <T>} from the returned
     * {@link PolicyModifyCommandResponse}.
     * @return a CompletableFuture of type {@code <T>}.
     * @throws NullPointerException if {@code function} is {@code null}.
     * @since 1.1.0
     */
    public CompletableFuture<T> applyModifyPolicy(final Function<PolicyModifyCommandResponse<?>, T> function) {
        checkNotNull(function, "function");
        final CompletableFuture<CommandResponse> intermediaryResult = createIntermediaryResult();
        LOGGER.trace("Sending modify command <{}>.", command);
        messagingProvider.sendCommand(command, channel);

        return intermediaryResult.thenApply(pcr -> (PolicyModifyCommandResponse<?>) pcr).thenApply(function);
    }

    private CompletableFuture<CommandResponse> createIntermediaryResult() {
        checkNotNull(command, COMMAND_ARGUMENT_NAME);

        final CompletableFuture<CommandResponse> intermediaryResult = new CompletableFuture<>();

        final DittoHeaders dittoHeaders = command.getDittoHeaders();
        final boolean responseRequired = dittoHeaders.isResponseRequired();
        final Optional<String> correlationId = dittoHeaders.getCorrelationId();
        if (responseRequired && correlationId.isPresent()) {
            responseForwarder.put(correlationId.get(), intermediaryResult);
        } else {
            // If no response was required or no correlation-id present: complete the future right away
            // no need to let the caller wait:
            intermediaryResult.complete(null);
        }

        return intermediaryResult;
    }

    /**
     * Applies the {@code command} of this SendTerminator with "void" behavior. If a response is required and received
     * the returned promise is completed with {@code null}. If no response is required the promise is immediately
     * completed with {@code null}. The response itself is not of interest for the caller of this method.
     *
     * @return a CompletableFuture of type {@code Void}.
     */
    public CompletableFuture<Void> applyVoid() {
        // The CompletableFuture is used to wait for a response even though the response itself is not regarded in
        // this case.
        final CompletableFuture<CommandResponse> intermediaryResult = createIntermediaryResult();
        LOGGER.trace("Sending void command <{}>.", command);
        messagingProvider.sendCommand(command, channel);

        return intermediaryResult.thenApply(tcr -> null);
    }

    /**
     * Applies the {@code command} of this SendTerminator with "view" behavior - expecting a {@code
     * ThingQueryCommandResponse} as result and returning a Future of the type {@code <T>}.
     *
     * @param function the function to apply to extract an instance of type {@code <T>} from the returned
     * ThingQueryCommandResponse.
     * @return a CompletableFuture of type {@code <T>}.
     * @throws NullPointerException if {@code function} is {@code null}.
     */
    public CompletableFuture<T> applyView(final Function<ThingQueryCommandResponse<?>, T> function) {
        checkNotNull(function, "function");
        final CompletableFuture<CommandResponse> result = createIntermediaryResult();
        LOGGER.trace("Sending view command <{}>.", command);
        messagingProvider.sendCommand(command, channel);

        return result.thenApply(tcr -> (ThingQueryCommandResponse<?>) tcr).thenApply(function);
    }

    /**
     * Applies the {@code command} of this SendTerminator with "view" behavior - expecting a {@link
     * PolicyQueryCommandResponse} as result and returning a Future of the type {@code <T>}.
     *
     * @param function the function to apply to extract an instance of type {@code <T>} from the returned
     * {@link PolicyQueryCommandResponse}.
     * @return a CompletableFuture of type {@code <T>}.
     * @throws NullPointerException if {@code function} is {@code null}.
     * @since 1.1.0
     */
    public CompletableFuture<T> applyViewWithPolicyResponse(final Function<PolicyQueryCommandResponse<?>, T> function) {
        checkNotNull(function, "function");
        final CompletableFuture<CommandResponse> result = createIntermediaryResult();
        LOGGER.trace("Sending view command <{}>.", command);
        messagingProvider.sendCommand(command, channel);

        return result.thenApply(tcr -> (PolicyQueryCommandResponse<?>) tcr).thenApply(function);
    }

    private ThingModifyCommandResponse<ThingModifyCommandResponse> createThingModifyCommandResponseFromAcknowledgement(
            final Acknowledgement ack) {
        return new ThingModifyCommandResponse<ThingModifyCommandResponse>() {
            @Override
            public JsonPointer getResourcePath() {
                return command.getResourcePath();
            }

            @Override
            public String getType() {
                return command.getType().replace(".commands", ".responses");
            }

            @Nonnull
            @Override
            public String getManifest() {
                return getType();
            }

            @Override
            public ThingId getThingEntityId() {
                return (ThingId) ack.getEntityId();
            }

            @Override
            public DittoHeaders getDittoHeaders() {
                return ack.getDittoHeaders();
            }

            @Override
            public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
                return ack.getEntity(schemaVersion);
            }

            @Override
            public ThingModifyCommandResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
                return this;
            }

            @Override
            public HttpStatusCode getStatusCode() {
                return ack.getStatusCode();
            }

            @Override
            public JsonObject toJson(final JsonSchemaVersion schemaVersion, final Predicate<JsonField> predicate) {
                return JsonObject.empty();
            }
        };
    }
}
