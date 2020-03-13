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
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.internal.bus.Classifiers;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocoladapter.HeaderTranslator;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyErrorResponse;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;

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
    protected AbstractHandle(final MessagingProvider messagingProvider,
            final TopicPath.Channel channel) {
        this.messagingProvider = messagingProvider;
        this.channel = channel;
    }

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
    protected Signal<?> signalFromAdaptable(final Adaptable adaptable) {
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
     */
    protected <T extends PolicyCommand<T>, S extends PolicyCommandResponse<?>, R> CompletionStage<R> askPolicyCommand(
            final T command,
            final Class<S> expectedResponse,
            final Function<S, R> onSuccess) {
        return sendSignalAndExpectResponse(command, expectedResponse, onSuccess, PolicyErrorResponse.class,
                errorResponse -> {
                    throw errorResponse.getDittoRuntimeException();
                });
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
     */
    protected <T extends ThingCommand<T>, S extends ThingCommandResponse<?>, R> CompletionStage<R> askThingCommand(
            final T command,
            final Class<S> expectedResponse,
            final Function<S, R> onSuccess) {
        final ThingCommand<?> commandWithChannel = setChannel(command, channel);
        return sendSignalAndExpectResponse(commandWithChannel, expectedResponse, onSuccess, ThingErrorResponse.class,
                errorResponse -> {
                    throw errorResponse.getDittoRuntimeException();
                });
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
     * @return future of the result.
     */
    protected <S, E, R> CompletionStage<R> sendSignalAndExpectResponse(final Signal signal,
            final Class<S> expectedResponseClass,
            final Function<S, R> onSuccess,
            final Class<E> expectedErrorResponseClass,
            final Function<E, R> onError) {

        final CompletionStage<Adaptable> responseFuture = messagingProvider.getAdaptableBus()
                .subscribeOnceForAdaptable(Classifiers.forCorrelationId(signal), getTimeout());

        messagingProvider.emit(signalToJsonString(signal));
        return responseFuture.thenApply(responseAdaptable -> {
            final Signal<?> response = signalFromAdaptable(responseAdaptable);
            if (expectedResponseClass.isInstance(response)) {
                return onSuccess.apply(expectedResponseClass.cast(response));
            } else if (expectedErrorResponseClass.isInstance(response)) {
                return onError.apply(expectedErrorResponseClass.cast(response));
            } else {
                throw new ClassCastException("Expect " + expectedResponseClass.getSimpleName() + ", got: " + response);
            }
        });
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

    protected <T extends WithDittoHeaders<T>> T setChannel(final T signal, final TopicPath.Channel channel) {
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

    @SuppressWarnings("unchecked")
    protected static Signal<?> adjustHeadersForLiveSignal(final Signal<?> signal) {
        return adjustHeadersForLive((Signal) signal);
    }

    private static <T extends WithDittoHeaders<T>> T adjustHeadersForLive(final T signal) {
        return signal.setDittoHeaders(
                signal.getDittoHeaders()
                        .toBuilder()
                        .channel(TopicPath.Channel.LIVE.getName())
                        .removeHeader(DittoHeaderDefinition.READ_SUBJECTS.getKey())
                        .removeHeader(DittoHeaderDefinition.AUTHORIZATION_SUBJECTS.getKey())
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
