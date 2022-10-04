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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.headers.DittoHeadersSettable;
import org.eclipse.ditto.base.model.headers.translator.HeaderTranslator;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgement;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgements;
import org.eclipse.ditto.base.model.signals.commands.CommandResponse;
import org.eclipse.ditto.base.model.signals.commands.ErrorResponse;
import org.eclipse.ditto.client.ack.internal.AcknowledgementRequestsValidator;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.management.AcknowledgementsFailedException;
import org.eclipse.ditto.client.management.ClientReconnectingException;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.policies.model.signals.commands.PolicyCommand;
import org.eclipse.ditto.policies.model.signals.commands.PolicyCommandResponse;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.protocol.adapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocol.adapter.ProtocolAdapter;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.ThingCommand;
import org.eclipse.ditto.things.model.signals.commands.modify.ThingModifyCommandResponse;

/**
 * Super class of API handles including common methods for request-response handling.
 */
public abstract class AbstractHandle {

    /**
     * Protocol adapter without header translation for the client side.
     */
    protected static final ProtocolAdapter PROTOCOL_ADAPTER = DittoProtocolAdapter.of(HeaderTranslator.empty());

    /**
     * The messaging provider.
     */
    protected final MessagingProvider messagingProvider;

    /**
     * The channel of this handle.
     */
    protected final TopicPath.Channel channel;

    /**
     * Create a handle.
     *
     * @param messagingProvider the messaging provider.
     * @param channel the channel of this handle.
     */
    protected AbstractHandle(final MessagingProvider messagingProvider, final TopicPath.Channel channel) {
        this.messagingProvider = messagingProvider;
        this.channel = channel;
    }

    /**
     * Get the label of built-in acknowledgements for this channel.
     *
     * @return the label.
     */
    protected abstract AcknowledgementLabel getThingResponseAcknowledgementLabel();

    /**
     * Convenience method to turn anything into {@code Void} due to ubiquitous use of {@code CompletableFuture<Void>}
     * in the existing API.
     *
     * @param ignored the ignored result.
     * @return {@code null}.
     */
    protected Void toVoid(final Object ignored) {
        return null;
    }

    /**
     * Adapt a signal into a JSON string.
     *
     * @param signal the signal.
     * @return JSON string of the corresponding Ditto protocol message.
     */
    protected String signalToJsonString(final Signal<?> signal) {
        return ProtocolFactory.wrapAsJsonifiableAdaptable(PROTOCOL_ADAPTER.toAdaptable(signal)).toJsonString();
    }

    /**
     * Convert an adaptable into a signal.
     *
     * @param adaptable the adaptable.
     * @return the signal.
     */
    @SuppressWarnings({"rawtypes", "java:S3740"})
    protected Signal signalFromAdaptable(final Adaptable adaptable) {
        return PROTOCOL_ADAPTER.fromAdaptable(adaptable);
    }

    /**
     * Specialization of {@code this#sendSignalAndExpectResponse(Signal,Class,Function,Class,Function)}
     * for policy commands.
     *
     * @param command the policy command.
     * @param expectedResponse expected response class.
     * @param onSuccess what happens if the expected response arrives.
     * @param <T> type of the command.
     * @param <S> type of the expected response.
     * @param <R> type of the result.
     * @return future of the result if the expected response arrives or a failed future on error.
     * Type is {@code CompletionStage} to signify that the future will complete or fail without caller intervention.
     * If the client is reconnecting while this method is called the future fails with a
     * {@link ClientReconnectingException}.
     */
    protected <T extends PolicyCommand<T>, S extends PolicyCommandResponse<?>, R> CompletionStage<R> askPolicyCommand(
            final T command,
            final Class<S> expectedResponse,
            final Function<S, R> onSuccess) {
        return sendSignalAndExpectResponse(command, expectedResponse, onSuccess, ErrorResponse.class,
                ErrorResponse::getDittoRuntimeException);
    }

    /**
     * Specialization of {@code this#sendSignalAndExpectResponse(Signal,Class,Function,Class,Function)}
     * for thing commands.
     *
     * @param command the thing command.
     * @param expectedResponse expected response class.
     * @param onSuccess what happens if the expected response arrives.
     * @param <T> type of the command.
     * @param <S> type of the expected response.
     * @param <R> type of the result.
     * @return future of the result if the expected response arrives or a failed future on error.
     * Type is {@code CompletionStage} to signify that the future will complete or fail without caller intervention.
     * If the client is reconnecting while this method is called the future fails with a
     * {@link ClientReconnectingException}.
     */
    protected <T extends ThingCommand<T>, S extends CommandResponse<?>, R> CompletionStage<R> askThingCommand(
            final T command,
            final Class<S> expectedResponse,
            final Function<S, R> onSuccess) {

        final ThingCommand<?> commandWithChannel = validateAckRequests(setChannel(command, channel));
        return sendSignalAndExpectResponse(commandWithChannel, expectedResponse, onSuccess, ErrorResponse.class,
                ErrorResponse::getDittoRuntimeException);
    }

    /**
     * Send a request and expect a response.
     *
     * @param signal the request to send.
     * @param expectedResponseClass the expected success response class.
     * @param onSuccess what to do on the expected success response.
     * @param expectedErrorResponseClass the expected error response class.
     * @param onError what to do on the expected error response.
     * @param <S> type of the expected success response.
     * @param <E> type of the expected error response.
     * @param <R> type of the result.
     * @return future of the result. The future can be exceptional with a {@link ClientReconnectingException} if the
     * client is reconnecting while this method is called.
     */
    protected <S, E, R> CompletionStage<R> sendSignalAndExpectResponse(final Signal<?> signal,
            final Class<S> expectedResponseClass,
            final Function<S, R> onSuccess,
            final Class<E> expectedErrorResponseClass,
            final Function<E, ? extends RuntimeException> onError) {

        try {
            final CompletionStage<Adaptable> responseFuture = messagingProvider.getAdaptableBus()
                    .subscribeOnceForAdaptable(Classification.forCorrelationId(signal), getTimeout());

            messagingProvider.emit(signalToJsonString(signal));
            return responseFuture.thenApply(responseAdaptable -> {
                final Signal<?> response = signalFromAdaptable(responseAdaptable);
                if (expectedErrorResponseClass.isInstance(response)) {
                    // extracted runtime exception will be wrapped in CompletionException.
                    throw onError.apply(expectedErrorResponseClass.cast(response));
                } else if (response instanceof Acknowledgements) {
                    final CommandResponse<?> commandResponse =
                            extractCommandResponseFromAcknowledgements(signal, (Acknowledgements) response);
                    return onSuccess.apply(expectedResponseClass.cast(commandResponse));
                } else if (expectedResponseClass.isInstance(response)) {
                    return onSuccess.apply(expectedResponseClass.cast(response));
                } else {
                    throw new ClassCastException(
                            "Expect " + expectedResponseClass.getSimpleName() + ", got: " + response);
                }
            });
        } catch (final ClientReconnectingException cre) {
            return CompletableFuture.supplyAsync(() -> {
                throw cre;
            });
        }

    }

    /**
     * Get the timeout in the messaging configuration.
     *
     * @return the timeout.
     */
    protected Duration getTimeout() {
        return messagingProvider.getMessagingConfiguration().getTimeout();
    }

    /**
     * Build a protocol command together with parameters.
     *
     * @param protocolCmd the protocol command.
     * @param parameters the parameters.
     * @return the string to send.
     */
    protected String buildProtocolCommand(final String protocolCmd, final Map<String, String> parameters) {
        final String paramsString = parameters.entrySet()
                .stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
        final String toSend;
        if (paramsString.isEmpty()) {
            toSend = protocolCmd;
        } else {
            toSend = protocolCmd + "?" + paramsString;
        }
        return toSend;
    }

    protected <T extends DittoHeadersSettable<T>> T setChannel(final T signal, final TopicPath.Channel channel) {
        switch (channel) {
            case LIVE:
                return adjustHeadersForLive(signal);
            case TWIN:
            case NONE:
            default:
                return signal;
        }
    }

    protected Adaptable adaptOutgoingLiveSignal(final Signal<?> liveSignal) {
        return PROTOCOL_ADAPTER.toAdaptable(adjustHeadersForLiveSignal(liveSignal));
    }

    @SuppressWarnings({"unchecked", "rawtypes", "java:S3740"})
    protected static Signal adjustHeadersForLiveSignal(final Signal<?> signal) {
        return adjustHeadersForLive((Signal) signal);
    }

    private ThingCommand<?> validateAckRequests(final ThingCommand<?> thingCommand) {
        AcknowledgementRequestsValidator.validate(thingCommand.getDittoHeaders().getAcknowledgementRequests(),
                getThingResponseAcknowledgementLabel());
        return thingCommand;
    }

    private CommandResponse<?> extractCommandResponseFromAcknowledgements(final Signal<?> signal,
            final Acknowledgements acknowledgements) {

        if (areFailedAcknowledgements(acknowledgements.getHttpStatus())) {
            throw AcknowledgementsFailedException.of(acknowledgements);
        } else {
            final AcknowledgementLabel expectedLabel = getThingResponseAcknowledgementLabel();
            return acknowledgements.stream()
                    .filter(ack -> ack.getLabel().equals(expectedLabel))
                    .findFirst()
                    .map(ack -> createThingModifyCommandResponseFromAcknowledgement(signal, ack))
                    .orElseThrow(() -> AcknowledgementRequestsValidator.didNotReceiveAcknowledgement(expectedLabel));
        }
    }

    private static boolean areFailedAcknowledgements(final HttpStatus httpStatus) {
        return httpStatus.isClientError() || httpStatus.isServerError();
    }

    private static <T extends ThingModifyCommandResponse<T>> ThingModifyCommandResponse<T> createThingModifyCommandResponseFromAcknowledgement(
            final Signal<?> signal,
            final Acknowledgement ack) {

        return new ThingModifyCommandResponse<T>() {
            @Override
            public JsonPointer getResourcePath() {
                return signal.getResourcePath();
            }

            @Override
            public String getType() {
                return signal.getType().replace(".commands", ".responses");
            }

            @Nonnull
            @Override
            public String getManifest() {
                return getType();
            }

            @Override
            public ThingId getEntityId() {
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
            public T setDittoHeaders(final DittoHeaders dittoHeaders) {
                return (T) this;
            }

            @Override
            public HttpStatus getHttpStatus() {
                return ack.getHttpStatus();
            }

            @Override
            public JsonObject toJson(final JsonSchemaVersion schemaVersion, final Predicate<JsonField> predicate) {
                return JsonObject.empty();
            }
        };
    }

    private static <T extends DittoHeadersSettable<T>> T adjustHeadersForLive(final T signal) {
        return signal.setDittoHeaders(
                signal.getDittoHeaders()
                        .toBuilder()
                        .channel(TopicPath.Channel.LIVE.getName())
                        .removeHeader(DittoHeaderDefinition.READ_SUBJECTS.getKey())
                        .removeHeader(DittoHeaderDefinition.AUTHORIZATION_CONTEXT.getKey())
                        .removeHeader(DittoHeaderDefinition.RESPONSE_REQUIRED.getKey())
                        .build()
        );
    }

    private static String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Missing standard charset UTF 8 for encoding.", e);
        }
    }

}
