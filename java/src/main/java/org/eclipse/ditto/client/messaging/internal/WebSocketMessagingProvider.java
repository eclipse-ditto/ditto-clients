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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.internal.DefaultThreadFactory;
import org.eclipse.ditto.client.internal.VersionReader;
import org.eclipse.ditto.client.live.internal.LiveImpl;
import org.eclipse.ditto.client.messaging.AuthenticationException;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.MessagingException;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.internal.TwinImpl;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonMissingFieldException;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonParseException;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonRuntimeException;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersBuilder;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessageResponseConsumer;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocoladapter.HeaderTranslator;
import org.eclipse.ditto.protocoladapter.JsonifiableAdaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.protocoladapter.UnknownCommandException;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.base.ErrorResponse;
import org.eclipse.ditto.signals.commands.live.LiveCommandFactory;
import org.eclipse.ditto.signals.commands.live.base.LiveCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;
import org.eclipse.ditto.signals.commands.messages.SendFeatureMessage;
import org.eclipse.ditto.signals.commands.messages.SendFeatureMessageResponse;
import org.eclipse.ditto.signals.commands.messages.SendThingMessage;
import org.eclipse.ditto.signals.commands.messages.SendThingMessageResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyErrorResponse;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.events.base.Event;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.StatusLine;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

/**
 * Messaging Provider providing messaging access to Ditto WebSocket which is directly provided by Eclipse Ditto
 * Gateway.
 *
 * @since 1.0.0
 */
public final class WebSocketMessagingProvider extends WebSocketAdapter implements MessagingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMessagingProvider.class);

    private static final int RECONNECTION_TIMEOUT_SECONDS = 5;

    private static final String PROTOCOL_CMD_START_SEND_EVENTS = "START-SEND-EVENTS";
    private static final String PROTOCOL_CMD_STOP_SEND_EVENTS = "STOP-SEND-EVENTS";

    private static final String PROTOCOL_CMD_START_SEND_MESSAGES = "START-SEND-MESSAGES";
    private static final String PROTOCOL_CMD_STOP_SEND_MESSAGES = "STOP-SEND-MESSAGES";

    private static final String PROTOCOL_CMD_START_SEND_LIVE_COMMANDS = "START-SEND-LIVE-COMMANDS";
    private static final String PROTOCOL_CMD_STOP_SEND_LIVE_COMMANDS = "STOP-SEND-LIVE-COMMANDS";

    private static final String PROTOCOL_CMD_START_SEND_LIVE_EVENTS = "START-SEND-LIVE-EVENTS";
    private static final String PROTOCOL_CMD_STOP_SEND_LIVE_EVENTS = "STOP-SEND-LIVE-EVENTS";

    private static final String PROTOCOL_CMD_JWT_TOKEN = "JWT-TOKEN";

    /**
     * The backend sends the protocol message above suffixed by ":ACK" when the subscription was created. E.g.: {@code
     * START-SEND-EVENTS:ACK}
     */
    private static final String PROTOCOL_CMD_ACK_SUFFIX = ":ACK";

    private static final int MAX_OUTSTANDING_MESSAGE_RESPONSES = 250;

    private static final String DITTO_CLIENT_USER_AGENT = "DittoClient/" + VersionReader.determineClientVersion();
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    private final MessagingConfiguration messagingConfiguration;
    private final AuthenticationProvider<WebSocket> authenticationProvider;
    private final ExecutorService callbackExecutor;

    private final String sessionId;
    private final Map<String, CompletableFuture<Void>> subscriptionsAcks;
    private final Map<String, Consumer<Message<?>>> subscriptions;
    private final ScheduledExecutorService reconnectExecutor;
    private final DittoProtocolAdapter protocolAdapter;
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final AtomicBoolean initiallyConnected = new AtomicBoolean(false);
    private final Map<String, MessageResponseConsumer<?>> messageCommandResponseConsumers;
    private final Map<String, Map<String, String>> registrationConfigs;
    private final Map<String, CompletableFuture<Adaptable>> customAdaptableResponseFutures;

    private Consumer<CommandResponse<?>> commandResponseConsumer;
    private WebSocket webSocket;
    private boolean sendMeTwinEvents = false;
    private boolean sendMeLiveMessages = false;
    private boolean sendMeLiveCommands = false;
    private boolean sendMeLiveEvents = false;

    /**
     * Constructs a new {@code WsMessagingProvider}.
     *
     * @param messagingConfiguration the specific configuration to apply.
     */
    private WebSocketMessagingProvider(final MessagingConfiguration messagingConfiguration,
            final AuthenticationProvider<WebSocket> authenticationProvider,
            final ExecutorService callbackExecutor) {
        this.messagingConfiguration = messagingConfiguration;
        this.authenticationProvider = authenticationProvider;
        this.callbackExecutor = callbackExecutor;

        sessionId = authenticationProvider.getConfiguration().getSessionId();
        subscriptionsAcks = new ConcurrentHashMap<>();
        subscriptions = new ConcurrentHashMap<>();
        reconnectExecutor = messagingConfiguration.isReconnectEnabled() ? createScheduledThreadPoolExecutor() : null;

        // by using an empty HeaderTranslator, make sure that all incoming and outgoing headers are just passed through
        protocolAdapter = DittoProtocolAdapter.of(HeaderTranslator.empty());
        // limit the max. outstanding MessageResponseConsumers to not produce a memory leak if messages are never answered
        messageCommandResponseConsumers = new LimitedHashMap<>(MAX_OUTSTANDING_MESSAGE_RESPONSES);
        registrationConfigs = new HashMap<>();
        customAdaptableResponseFutures = new HashMap<>();
    }

    private static ScheduledThreadPoolExecutor createScheduledThreadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("ditto-client-reconnect"));
    }

    /**
     * Returns a new {@code WebSocketMessagingProvider}.
     *
     * @param messagingConfiguration configuration of messaging.
     * @param authenticationProvider provides authentication.
     * @param callbackExecutor the executor for messages.
     * @return the provider.
     */
    public static WebSocketMessagingProvider newInstance(final MessagingConfiguration messagingConfiguration,
            final AuthenticationProvider<WebSocket> authenticationProvider,
            final ExecutorService callbackExecutor) {
        checkNotNull(messagingConfiguration, "messagingConfiguration");
        checkNotNull(authenticationProvider, "authenticationProvider");
        checkNotNull(callbackExecutor, "callbackExecutor");

        return new WebSocketMessagingProvider(messagingConfiguration, authenticationProvider, callbackExecutor);
    }

    @Override
    public AuthenticationConfiguration getAuthenticationConfiguration() {
        return authenticationProvider.getConfiguration();
    }

    @Override
    public MessagingConfiguration getMessagingConfiguration() {
        return messagingConfiguration;
    }

    @Override
    public ExecutorService getExecutorService() {
        return callbackExecutor;
    }

    @Override
    public void initialize() {
        if (webSocket != null && webSocket.isOpen()) {
            // if wsClient was already initialized, skip another initialization
            return;
        }

        safeGet(initiateConnection(createWebsocket()));
    }

    private WebSocket createWebsocket() {
        final WebSocketFactory webSocketFactory = WebSocketFactoryFactory.newWebSocketFactory(messagingConfiguration);
        final WebSocket ws;
        try {
            ws = webSocketFactory.createSocket(messagingConfiguration.getEndpointUri());
        } catch (final IOException e) {
            throw MessagingException.connectFailed(sessionId, e);
        }
        ws.addHeader("User-Agent", DITTO_CLIENT_USER_AGENT);
        ws.setMaxPayloadSize(256 * 1024); // 256 KiB
        ws.setMissingCloseFrameAllowed(true);
        ws.setFrameQueueSize(1); // allow applied backpressure from backend to block emitting of new messages
        return ws;
    }


    /**
     * Wrapper that catches exceptions of {@code future.get()} and wraps them in a {@link AuthenticationException}.
     *
     * @param future the Future to be wrapped.
     * @param <T> the type of the computed result.
     * @return the computed result.
     */
    @SuppressWarnings("squid:S2142")
    private <T> T safeGet(final Future<T> future) {
        try {
            return future.get(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            return handleInterruptedException(e);
        } catch (final TimeoutException e) {
            return handleTimeoutException(e);
        } catch (final ExecutionException e) {
            return handleExecutionException(e);
        }
    }

    private <T> T handleInterruptedException(final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw MessagingException.connectInterrupted(sessionId, e);
    }

    private <T> T handleTimeoutException(final TimeoutException e) {
        throw MessagingException.connectTimeout(sessionId, e);
    }

    private <T> T handleExecutionException(final ExecutionException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof AuthenticationException) {
            // no unneccessary boxing of AuthenticationException
            throw ((AuthenticationException) cause);
        } else if (cause instanceof WebSocketException) {
            LOGGER.error("Got exception: {}", cause.getMessage());

            if (isAuthenticationException((WebSocketException) cause)) {
                throw AuthenticationException.unauthorized(sessionId, cause);
            } else if (isForbidden((WebSocketException) cause)) {
                throw AuthenticationException.forbidden(sessionId, cause);
            } else if (cause instanceof OpeningHandshakeException) {
                final StatusLine statusLine = ((OpeningHandshakeException) cause).getStatusLine();
                throw AuthenticationException.withStatus(sessionId, cause, statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
        }

        throw AuthenticationException.of(sessionId, e);
    }

    private static boolean isAuthenticationException(final WebSocketException exception) {
        if (exception instanceof OpeningHandshakeException) {
            final StatusLine statusLine = ((OpeningHandshakeException) exception).getStatusLine();
            return statusLine.getStatusCode() == 401 && statusLine.getReasonPhrase().contains("nauthorized");
        }
        return false;
    }

    private static boolean isForbidden(final WebSocketException exception) {
        if (exception instanceof OpeningHandshakeException) {
            final StatusLine statusLine = ((OpeningHandshakeException) exception).getStatusLine();
            return statusLine.getStatusCode() == 403 && statusLine.getReasonPhrase().contains("orbidden");
        }
        return false;
    }

    /**
     * Initiates the connection to the web socket by using the provided {@code ws} and applying the passed {@code
     * webSocketListener} for web socket handling and incoming messages.
     *
     * @param ws the WebSocket instance to use for connecting.
     * @return a promise for the web socket which should be available after successful establishment of a connection.
     * This promise may be completed exceptionally if the connection upgrade attempt to web socket failed.
     * @throws NullPointerException if any argument is {@code null}.
     */
    private CompletableFuture<WebSocket> initiateConnection(final WebSocket ws) {
        checkNotNull(ws, "ws");

        authenticationProvider.prepareAuthentication(ws);
        ws.addListener(this);

        LOGGER.info("Connecting WebSocket on endpoint <{}>", ws.getURI());
        final ExecutorService connectionExecutor = createConnectionExecutor();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return safeGet(ws.connect(connectionExecutor));
            } finally {
                // right after the connection was established, the connectionExecutor may release its threads:
                connectionExecutor.shutdown();
            }
        }, connectionExecutor);
    }

    private static ExecutorService createConnectionExecutor() {
        final ThreadPoolExecutor connectionExecutor = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new DefaultThreadFactory("ditto-client-connect"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        connectionExecutor.allowCoreThreadTimeOut(true);
        return connectionExecutor;
    }

    @Override
    public void send(final Message<?> message, final TopicPath.Channel channel) {
        final DittoHeadersBuilder headersBuilder = DittoHeaders.newBuilder();
        final Optional<String> optionalCorrelationId = message.getCorrelationId();
        optionalCorrelationId.ifPresent(headersBuilder::correlationId);
        final DittoHeaders dittoHeaders = headersBuilder.build();

        final ThingId thingId = message.getThingEntityId();
        final Optional<HttpStatusCode> statusCodeOptional = message.getStatusCode();
        final Optional<String> featureIdOptional = message.getFeatureId();
        final Adaptable adaptable;
        if (statusCodeOptional.isPresent()) {
            // this is treated as a response message
            final HttpStatusCode statusCode = statusCodeOptional.get();
            final MessageCommandResponse<?, ?> messageCommandResponse = featureIdOptional.isPresent()
                    ? SendFeatureMessageResponse.of(thingId, featureIdOptional.get(), message, statusCode, dittoHeaders)
                    : SendThingMessageResponse.of(thingId, message, statusCode, dittoHeaders);
            adaptable = tryToConvertToAdaptable(messageCommandResponse);
        } else {
            final MessageCommand<?, ?> messageCommand = featureIdOptional.isPresent()
                    ? SendFeatureMessage.of(thingId, featureIdOptional.get(), message, dittoHeaders)
                    : SendThingMessage.of(thingId, message, dittoHeaders);
            adaptable = tryToConvertToAdaptable(messageCommand);

            final Optional<MessageResponseConsumer<?>> optionalResponseConsumer = message.getResponseConsumer();
            if (optionalCorrelationId.isPresent() && optionalResponseConsumer.isPresent()) {
                messageCommandResponseConsumers.put(optionalCorrelationId.get(), optionalResponseConsumer.get());
            }
        }
        doSendAdaptable(adaptable);
    }

    @Nullable
    private Adaptable tryToConvertToAdaptable(final MessageCommandResponse<?, ?> messageCommandResponse) {
        try {
            return protocolAdapter.toAdaptable(messageCommandResponse);
        } catch (final UnknownCommandException e) {
            logUnknownType(messageCommandResponse, e);
            return null;
        }
    }

    private <T> void logUnknownType(final T type, final Throwable throwable) {
        final String typeName = type.getClass().getSimpleName();
        LOGGER.error("Client <{}>: Unknown {} type: <{}> - NOT sending via Ditto WebSocket!",
                sessionId,
                typeName, throwable.getMessage());
    }

    @Nullable
    private Adaptable tryToConvertToAdaptable(final MessageCommand<?, ?> messageCommand) {
        try {
            return protocolAdapter.toAdaptable(messageCommand);
        } catch (final UnknownCommandException e) {
            logUnknownType(messageCommand, e);
            return null;
        }
    }

    @Override
    public void sendCommand(final Command<?> command, final TopicPath.Channel channel) {
        doSendAdaptable(tryToConvertToAdaptable(command, channel));
    }

    @Nullable
    private Adaptable tryToConvertToAdaptable(final Command<?> command, final TopicPath.Channel channel) {

        try {
            final Command<?> adjustedCommand = command.setDittoHeaders(adjustHeadersForLive(command));
            return protocolAdapter.toAdaptable(adjustedCommand, channel);
        } catch (final UnknownCommandException e) {
            logUnknownType(command, e);
            return null;
        }
    }

    @Override
    public void sendCommandResponse(final CommandResponse<?> commandResponse, final TopicPath.Channel channel) {
        doSendAdaptable(tryToConvertToAdaptable(commandResponse, channel));
    }

    @Nullable
    private Adaptable tryToConvertToAdaptable(final CommandResponse<?> commandResponse,
            final TopicPath.Channel channel) {

        try {
            final CommandResponse<?> adjustedResponse = commandResponse.setDittoHeaders(
                    adjustHeadersForLive(commandResponse));
            return protocolAdapter.toAdaptable(adjustedResponse, channel);
        } catch (final UnknownCommandException e) {
            logUnknownType(commandResponse, e);
            return null;
        }
    }

    @Override
    public void emitEvent(final Event<?> event, final TopicPath.Channel channel) {
        doSendAdaptable(tryToConvertToAdaptable(event, channel));
    }

    @Nullable
    private Adaptable tryToConvertToAdaptable(final Event<?> event, final TopicPath.Channel channel) {

        try {
            final Event<?> adjustedEvent = event.setDittoHeaders(adjustHeadersForLive(event));
            return protocolAdapter.toAdaptable(adjustedEvent, channel);
        } catch (final UnknownCommandException e) {
            logUnknownType(event, e);
            return null;
        }
    }

    private DittoHeaders adjustHeadersForLive(final WithDittoHeaders<?> withDittoHeaders) {

        return withDittoHeaders.getDittoHeaders().toBuilder()
                .removeHeader(DittoHeaderDefinition.READ_SUBJECTS.getKey())
                .removeHeader(DittoHeaderDefinition.AUTHORIZATION_SUBJECTS.getKey())
                .removeHeader(DittoHeaderDefinition.RESPONSE_REQUIRED.getKey())
                .build();
    }

    @Override
    public CompletableFuture<Adaptable> sendAdaptable(final Adaptable adaptable) {

        DittoHeaders headers = adaptable.getHeaders().orElseGet(DittoHeaders::empty);
        Adaptable adaptableToSend = adaptable;
        if (!headers.getCorrelationId().isPresent()) {
            final String newCorrelationId = UUID.randomUUID().toString();
            headers = headers.toBuilder().correlationId(newCorrelationId)
                    .build();
            adaptableToSend = adaptable.setDittoHeaders(headers);
        }

        final String correlationId = getCorrelationIdOrThrow(headers).toString();
        doSendAdaptable(adaptableToSend);

        final CompletableFuture<Adaptable> responseFuture = new CompletableFuture<>();
        customAdaptableResponseFutures.put(correlationId, responseFuture);
        return responseFuture;
    }

    private void doSendAdaptable(@Nullable final Adaptable adaptable) {
        if (null == adaptable) {
            return;
        }
        if (webSocket != null && webSocket.isOpen()) {
            final String stringMessage = ProtocolFactory.wrapAsJsonifiableAdaptable(adaptable).toJsonString();
            LOGGER.debug("Client <{}>: Sending JSON: {}", sessionId,
                    stringMessage);
            webSocket.sendText(stringMessage);
        } else {
            LOGGER.error("Client <{}>: WebSocket is not connected - going to discard Adaptable '{}'",
                    sessionId, adaptable);
        }
    }

    @Override
    public void registerReplyHandler(final Consumer<CommandResponse<?>> commandResponseHandler) {
        commandResponseConsumer = commandResponseHandler;
    }

    @Override
    public boolean registerMessageHandler(final String name, final Map<String, String> registrationConfig,
            final Consumer<Message<?>> handler, final CompletableFuture<Void> receiptFuture) {
        if (subscriptions.containsKey(name)) {
            LOGGER.info("Client <{}>: Handler {} already registered for client",
                    sessionId, name);
            receiptFuture.complete(null);
            return false;
        }

        LOGGER.trace("Client <{}>: Registering incoming message handler'", sessionId);
        subscriptions.put(name, handler);
        registrationConfigs.put(name, registrationConfig);

        // connection already opened - finish future:
        if (webSocket != null) {
            if (TwinImpl.CONSUME_TWIN_EVENTS_HANDLER.equals(name)) {
                sendMeTwinEvents = true;
                askBackend(PROTOCOL_CMD_START_SEND_EVENTS, registrationConfig, receiptFuture);
            } else if (LiveImpl.CONSUME_LIVE_MESSAGES_HANDLER.equals(name)) {
                sendMeLiveMessages = true;
                askBackend(PROTOCOL_CMD_START_SEND_MESSAGES, registrationConfig, receiptFuture);
            } else if (LiveImpl.CONSUME_LIVE_COMMANDS_HANDLER.equals(name)) {
                sendMeLiveCommands = true;
                askBackend(PROTOCOL_CMD_START_SEND_LIVE_COMMANDS, registrationConfig, receiptFuture);
            } else if (LiveImpl.CONSUME_LIVE_EVENTS_HANDLER.equals(name)) {
                sendMeLiveEvents = true;
                askBackend(PROTOCOL_CMD_START_SEND_LIVE_EVENTS, registrationConfig, receiptFuture);
            }
        }

        return true;
    }

    @Override
    public synchronized void deregisterMessageHandler(final String name, final CompletableFuture<Void> future) {
        subscriptions.remove(name);

        if (TwinImpl.CONSUME_TWIN_EVENTS_HANDLER.equals(name)) {
            sendMeTwinEvents = false;
            askBackend(PROTOCOL_CMD_STOP_SEND_EVENTS, Collections.emptyMap(), future);
        } else if (LiveImpl.CONSUME_LIVE_MESSAGES_HANDLER.equals(name)) {
            sendMeLiveMessages = false;
            askBackend(PROTOCOL_CMD_STOP_SEND_MESSAGES, Collections.emptyMap(), future);
        } else if (LiveImpl.CONSUME_LIVE_COMMANDS_HANDLER.equals(name)) {
            sendMeLiveCommands = false;
            askBackend(PROTOCOL_CMD_STOP_SEND_LIVE_COMMANDS, Collections.emptyMap(), future);
        } else if (LiveImpl.CONSUME_LIVE_EVENTS_HANDLER.equals(name)) {
            sendMeLiveEvents = false;
            askBackend(PROTOCOL_CMD_STOP_SEND_LIVE_EVENTS, Collections.emptyMap(), future);
        }
    }

    @Override
    public void close() {
        try {
            LOGGER.debug("Client <{}>: Closing WebSocket client of endpoint <{}>.", sessionId,
                    messagingConfiguration.getEndpointUri());

            if (null != reconnectExecutor) {
                reconnectExecutor.shutdownNow();
            }

            authenticationProvider.destroy();
            webSocket.disconnect();
            LOGGER.debug("Client <{}>: WebSocket destroyed.", sessionId);
        } catch (final Exception e) {
            LOGGER.info("Client <{}>: Exception occurred while trying to shutdown http client.", sessionId, e);
        }
    }

    @Override
    public void onConnected(final WebSocket websocket, final Map<String, List<String>> headers) {
        this.webSocket = websocket;

        callbackExecutor.execute(() -> {
            LOGGER.info("Client <{}>: WebSocket connection is established", sessionId);

            if (initiallyConnected.get()) {
                // we were already connected - so this is a reconnect
                LOGGER.info("Client <{}>: Subscribing again for messages from backend after reconnection",
                        sessionId);

                // ensures that on re-connection the client subscribes again for the previously subscribed stuff:
                final CompletableFuture<Void> receiptFuture = new CompletableFuture<>();
                if (sendMeTwinEvents) {
                    askBackend(PROTOCOL_CMD_START_SEND_EVENTS,
                            registrationConfigs.get(TwinImpl.CONSUME_TWIN_EVENTS_HANDLER), receiptFuture);
                }
                if (sendMeLiveMessages) {
                    askBackend(PROTOCOL_CMD_START_SEND_MESSAGES,
                            registrationConfigs.get(LiveImpl.CONSUME_LIVE_MESSAGES_HANDLER), receiptFuture);
                }
                if (sendMeLiveCommands) {
                    askBackend(PROTOCOL_CMD_START_SEND_LIVE_COMMANDS,
                            registrationConfigs.get(LiveImpl.CONSUME_LIVE_COMMANDS_HANDLER), receiptFuture);
                }
                if (sendMeLiveEvents) {
                    askBackend(PROTOCOL_CMD_START_SEND_LIVE_EVENTS,
                            registrationConfigs.get(LiveImpl.CONSUME_LIVE_EVENTS_HANDLER), receiptFuture);
                }
            }
            initiallyConnected.set(true);
        });
    }


    private void askBackend(final String protocolCmd, final Map<String, String> registrationConfig,
            final CompletableFuture<Void> receiptFuture) {
        LOGGER.info("Client <{}>: Requesting at backend that this client wants to <{}> with params <{}>",
                sessionId, protocolCmd, registrationConfig);
        if (webSocket != null) {
            final String paramsString = registrationConfig.entrySet()
                    .stream()
                    .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                    .collect(Collectors.joining("&"));
            final String toSend;
            if (paramsString.isEmpty()) {
                toSend = protocolCmd;
            } else {
                toSend = protocolCmd + "?" + paramsString;
            }
            LOGGER.debug("Sending: {}", toSend);
            webSocket.sendText(toSend);
        }

        final CompletableFuture<Void> loggingFuture = new CompletableFuture<>();
        // thenAcceptAsync is very important here! Otherwise the main thread is blocked and no other messages are received:
        loggingFuture.thenAcceptAsync(aVoid -> {
            LOGGER.debug("Client <{}>: Backend now <{}>.", sessionId, protocolCmd);
            receiptFuture.complete(aVoid);
        }, callbackExecutor);
        subscriptionsAcks.put(protocolCmd, loggingFuture);
    }

    private static String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Missing standard charset UTF 8 for encoding.", e);
        }
    }

    @Override
    public void onDisconnected(final WebSocket websocket, final WebSocketFrame serverCloseFrame,
            final WebSocketFrame clientCloseFrame,
            final boolean closedByServer) {

        callbackExecutor.execute(() -> {
            if (closedByServer) {
                LOGGER.info(
                        "Client <{}>: WebSocket connection to endpoint <{}> was closed by Server with code <{}> and " +
                                "reason <{}>.", sessionId, messagingConfiguration.getEndpointUri(),
                        serverCloseFrame.getCloseCode(),
                        serverCloseFrame.getCloseReason());
                handleReconnectionIfEnabled();
            } else {
                LOGGER.info("Client <{}>: WebSocket connection to endpoint <{}> was closed by client",
                        sessionId, messagingConfiguration.getEndpointUri());
            }
        });
    }

    @Override
    public void onError(final WebSocket websocket, final WebSocketException cause) {

        callbackExecutor.execute(() -> {
            final String msgPattern = "Client <{}>: Error in WebSocket: {}";
            final String errorMsg; // avoids cluttering the log
            if (null != cause) {
                errorMsg = cause.getClass().getSimpleName() + ": " + cause.getMessage();
            } else {
                errorMsg = "-";
            }
            LOGGER.error(msgPattern, sessionId, errorMsg);
            handleReconnectionIfEnabled();
        });
    }

    private void handleReconnectionIfEnabled() {

        if (messagingConfiguration.isReconnectEnabled()) {
            // reconnect in a while if client was initially connected and we are not reconnecting already
            if (initiallyConnected.get() && reconnecting.compareAndSet(false, true) && null != reconnectExecutor) {
                LOGGER.info("Client <{}>: Reconnection is enabled. Reconnecting in <{}> seconds ...",
                        sessionId, RECONNECTION_TIMEOUT_SECONDS);
                reconnectExecutor.schedule(this::initWebSocketConnectionWithReconnect, RECONNECTION_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS);
            }
        } else {
            LOGGER.info("Client <{}>: Reconnection is NOT enabled. Closing client ...", sessionId);
            close();
        }
    }

    private void initWebSocketConnectionWithReconnect() {
        int count = 0;
        WebSocket newWebSocket = null;
        while (null == newWebSocket) {
            if (0 < count) {
                waitUntilNextReconnectionAttempt();
            }
            ++count;
            newWebSocket = tryToReconnect(count);
        }
        reconnecting.set(false);
    }

    private void waitUntilNextReconnectionAttempt() {
        try {
            LOGGER.info("Client <{}>: Retrying connection initiation again in <{}> seconds ...", sessionId,
                    RECONNECTION_TIMEOUT_SECONDS);
            TimeUnit.SECONDS.sleep(RECONNECTION_TIMEOUT_SECONDS);
        } catch (final InterruptedException ie) {
            LOGGER.error("Client <{}>: Interrupted while waiting for reconnection.", sessionId, ie);
            Thread.currentThread().interrupt();
        }
    }

    @Nullable
    private WebSocket tryToReconnect(final int count) {
        try {
            LOGGER.info("Recreating Websocket..");
            webSocket.clearHeaders();
            webSocket.clearListeners();
            return safeGet(initiateConnection(webSocket.recreate()));
        } catch (final AuthenticationException | IOException e) {
            // log error, but try again (don't end loop)
            final String msgFormat = "Client <{}>: Failed to establish connection ({}): {}";
            LOGGER.error(msgFormat, sessionId, count, e.getMessage());
            return null;
        }
    }

    @Override
    public void onBinaryMessage(final WebSocket websocket, final byte[] binary) {
        callbackExecutor.execute(() -> {
            final String stringMessage = new String(binary, StandardCharsets.UTF_8);
            LOGGER.debug(
                    "Client <{}>: Received WebSocket byte array message <{}>, as string <{}> - don't know what to do" +
                            " with it!.", sessionId, binary, stringMessage);
        });
    }

    @Override
    public void onTextMessage(final WebSocket websocket, final String text) {
        callbackExecutor.execute(() -> {
            LOGGER.trace("Client <{}>: Received WebSocket string message <{}>", sessionId, text);
            handleIncomingMessage(text);
        });
    }

    private void handleIncomingMessage(final String message) {
        switch (message) {
            case PROTOCOL_CMD_START_SEND_EVENTS + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_START_SEND_EVENTS);
                return;
            case PROTOCOL_CMD_STOP_SEND_EVENTS + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_STOP_SEND_EVENTS);
                return;
            case PROTOCOL_CMD_START_SEND_MESSAGES + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_START_SEND_MESSAGES);
                return;
            case PROTOCOL_CMD_STOP_SEND_MESSAGES + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_STOP_SEND_MESSAGES);
                return;
            case PROTOCOL_CMD_START_SEND_LIVE_COMMANDS + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_START_SEND_LIVE_COMMANDS);
                return;
            case PROTOCOL_CMD_STOP_SEND_LIVE_COMMANDS + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_STOP_SEND_LIVE_COMMANDS);
                return;
            case PROTOCOL_CMD_START_SEND_LIVE_EVENTS + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_START_SEND_LIVE_EVENTS);
                return;
            case PROTOCOL_CMD_STOP_SEND_LIVE_EVENTS + PROTOCOL_CMD_ACK_SUFFIX:
                ackSubscription(PROTOCOL_CMD_STOP_SEND_LIVE_EVENTS);
                return;
            case PROTOCOL_CMD_JWT_TOKEN + PROTOCOL_CMD_ACK_SUFFIX:
                LOGGER.trace("Ack for JWT received.");
                return;
            default:
                // no protocol message, treat as JSON below ..
        }

        final JsonObject messageJson = tryToGetMessageAsJsonObject(message);
        if (null == messageJson) {
            return;
        }
        final JsonifiableAdaptable jsonifiableAdaptable = tryToGetJsonifiableAdaptableFromMessageJson(messageJson);
        if (null == jsonifiableAdaptable) {
            return;
        }

        final TopicPath.Channel channel = getChannelOrNull(jsonifiableAdaptable);
        final DittoHeaders headers = jsonifiableAdaptable.getHeaders().orElseGet(DittoHeaders::empty);
        final String correlationId = getCorrelationIdOrThrow(headers).toString();
        if (customAdaptableResponseFutures.containsKey(correlationId)) {
            customAdaptableResponseFutures.remove(correlationId)
                    .complete(jsonifiableAdaptable);
        } else if (TopicPath.Channel.TWIN == channel) {
            handleTwinMessage(message, correlationId, jsonifiableAdaptable);
        } else if (TopicPath.Channel.LIVE == channel) {
            handleLiveMessage(message, correlationId, jsonifiableAdaptable);
        } else if (TopicPath.Channel.NONE == channel) {
            handleNoneChannelMessage(message, jsonifiableAdaptable);
        } else {
            final String msgPattern = "Client <{}>: Got Jsonifiable on unknown channel <{}>: <{}>";
            LOGGER.warn(msgPattern, sessionId, channel, jsonifiableAdaptable);
        }
    }

    private void ackSubscription(final String protocolCommand) {
        final CompletableFuture<Void> subscriptionAckPromise = subscriptionsAcks.remove(protocolCommand);
        if (null != subscriptionAckPromise) {
            LOGGER.trace("Acking pending <{}>.", protocolCommand);
            subscriptionAckPromise.complete(null);
        }
    }

    @Nullable
    private JsonObject tryToGetMessageAsJsonObject(final String message) {
        try {
            return getMessageAsJsonObject(message);
        } catch (final JsonParseException e) {
            // What the hell was the message?
            final String msgPattern = "Client <{}>: Got unknown non-JSON message on WebSocket: {}";
            LOGGER.warn(msgPattern, sessionId, message, e);
            return null; // renounce on Optional because of object creation impact on performance (probably irrelevant)
        }
    }

    private static JsonObject getMessageAsJsonObject(final String message) {
        final JsonValue jsonValue = JsonFactory.readFrom(message);
        if (!jsonValue.isObject()) {
            final String msgPattern = "The WebSocket message was not a JSON object as required:\n{0}";
            throw new JsonParseException(MessageFormat.format(msgPattern, message));
        }
        return jsonValue.asObject();
    }

    @Nullable
    private JsonifiableAdaptable tryToGetJsonifiableAdaptableFromMessageJson(final JsonObject messageJson) {
        try {
            return getJsonifiableAdaptableFromMessageJson(messageJson);
        } catch (final JsonRuntimeException e) {
            // That should not happen; the backend sent a wrong format!
            final String msgPattern = "Client <{}>: Incoming message could not be parsed to JSON due to: <{}>:\n  <{}>";
            LOGGER.warn(msgPattern, sessionId, e.getMessage(), messageJson);
            return null;
        }
    }

    private static JsonifiableAdaptable getJsonifiableAdaptableFromMessageJson(final JsonObject messageJson) {
        return ProtocolFactory.jsonifiableAdaptableFromJson(messageJson);
    }

    @Nullable
    private Signal<?> tryToAdaptToSignal(final Adaptable adaptable, final String message) {
        try {
            return adaptToSignal(adaptable);
        } catch (final DittoRuntimeException e) {
            // That should not happen; the backend sent a wrong format!
            LOGGER.warn("Client <{}>: Incoming message could not be parsed to Signal due to: <{}>:\n <{}>",
                    sessionId, e.getMessage(), message);
            return null;
        }
    }

    private Signal<?> adaptToSignal(final Adaptable adaptable) {
        return protocolAdapter.fromAdaptable(adaptable);
    }

    @Nullable
    private static TopicPath.Channel getChannelOrNull(final Adaptable jsonifiableAdaptable) {
        final TopicPath topicPath = jsonifiableAdaptable.getTopicPath();
        TopicPath.Channel result = topicPath.getChannel();
        if (null == result) {
            result = jsonifiableAdaptable.getHeaders()
                    .flatMap(DittoHeaders::getChannel)
                    .flatMap(TopicPath.Channel::forName)
                    .orElse(null);
        }

        return result;
    }

    private static CharSequence getCorrelationIdOrThrow(final DittoHeaders dittoHeaders) {
        return dittoHeaders.getCorrelationId().orElseThrow(() -> {
            final JsonPointer headersPointer = JsonifiableAdaptable.JsonFields.HEADERS.getPointer();
            final JsonPointer correlationIdPointer = JsonPointer.of(DittoHeaderDefinition.CORRELATION_ID.getKey());

            return new JsonMissingFieldException(headersPointer.append(correlationIdPointer));
        });
    }

    private void handleTwinMessage(final String message, final CharSequence correlationId,
            final JsonifiableAdaptable jsonifiableAdaptable) {

        final Signal<?> signal = tryToAdaptToSignal(jsonifiableAdaptable, message);
        if (null == signal) {
            return;
        }

        if (signal instanceof ThingCommandResponse) {
            LOGGER.debug("Client <{}>: Received TWIN Response JSON: {}", sessionId, message);
            if (signal instanceof ThingErrorResponse) {
                final DittoRuntimeException cre = ((ErrorResponse<?>) signal).getDittoRuntimeException();
                final String description = cre.getDescription().orElse("");
                LOGGER.debug("Client <{}>: Got TWIN ThingErrorResponse: <{}: {} - {}>", sessionId,
                        cre.getClass().getSimpleName(), cre.getMessage(), description);
            }
            commandResponseConsumer.accept((ThingCommandResponse<?>) signal);
        } else if (signal instanceof ThingEvent) {
            LOGGER.debug("Client <{}>: Received TWIN Event JSON: {}", sessionId, message);
            handleThingEvent(correlationId, (ThingEvent<?>) signal, TwinImpl.CONSUME_TWIN_EVENTS_HANDLER,
                    jsonifiableAdaptable);
        } else {
            // if we are at this point we must ask: what the hell is that?
            LOGGER.warn("Client <{}>: Got unknown message on WebSocket on TWIN channel: {}",
                    sessionId, message);
        }
    }

    private void handleLiveMessage(final String message, final CharSequence correlationId,
            final JsonifiableAdaptable jsonifiableAdaptable) {

        final Signal<?> signal = tryToAdaptToSignal(jsonifiableAdaptable, message);
        if (null == signal) {
            return;
        }

        if (signal instanceof MessageCommand) {
            LOGGER.debug("Client <{}>: Received LIVE MessageCommand JSON: {}", sessionId,
                    message);
            handleLiveMessage((MessageCommand<?, ?>) signal);
        } else if (signal instanceof MessageCommandResponse) {
            LOGGER.debug("Client <{}>: Received LIVE MessageCommandResponse JSON: {}", sessionId,
                    message);
            handleLiveMessageResponse((MessageCommandResponse<?, ?>) signal);
        } else if (signal instanceof ThingCommand) {
            LOGGER.debug("Client <{}>: Received LIVE ThingCommand JSON: {}", sessionId, message);
            handleLiveCommand(LiveCommandFactory.getInstance().getLiveCommand((ThingCommand<?>) signal), jsonifiableAdaptable);
        } else if (signal instanceof ThingErrorResponse) {
            final DittoRuntimeException cre = ((ThingErrorResponse) signal).getDittoRuntimeException();
            final String description = cre.getDescription().orElse("");
            LOGGER.warn("Client <{}>: Got LIVE ThingErrorResponse: <{}: {} - {}>", sessionId,
                    cre.getClass().getSimpleName(), cre.getMessage(), description);
            if (messageCommandResponseConsumers.containsKey(correlationId.toString())) {
                handleLiveMessageResponse((ThingErrorResponse) signal);
            } else {
                handleLiveCommandResponse((ThingErrorResponse) signal);
            }
        } else if (signal instanceof ThingCommandResponse) {
            LOGGER.debug("Client <{}>: Received LIVE ThingCommandResponse JSON: {}", sessionId,
                    message);
            handleLiveCommandResponse((ThingCommandResponse<?>) signal);
        } else if (signal instanceof ThingEvent) {
            LOGGER.debug("Client <{}>: Received LIVE ThingEvent JSON: {}", sessionId, message);
            handleThingEvent(correlationId, (ThingEvent<?>) signal, LiveImpl.CONSUME_LIVE_EVENTS_HANDLER, jsonifiableAdaptable);
        } else {
            // if we are at this point we must ask: what the hell is that?
            LOGGER.warn("Client <{}>: Got unknown message on WebSocket on LIVE channel: {}",
                    sessionId, message);
        }
    }


    private void handleNoneChannelMessage(final String message, final JsonifiableAdaptable jsonifiableAdaptable) {

        final Signal<?> signal = tryToAdaptToSignal(jsonifiableAdaptable, message);
        if (null == signal) {
            return;
        }

        if (signal instanceof PolicyCommandResponse) {
            LOGGER.debug("Client <{}>: Received PolicyCommandResponse JSON: {}", sessionId, message);
            if (signal instanceof PolicyErrorResponse) {
                final DittoRuntimeException cre = ((PolicyErrorResponse) signal).getDittoRuntimeException();
                final String description = cre.getDescription().orElse("");
                LOGGER.debug("Client <{}>: Got PolicyErrorResponse: <{}: {} - {}>", sessionId,
                        cre.getClass().getSimpleName(), cre.getMessage(), description);
            }
            commandResponseConsumer.accept((PolicyCommandResponse<?>) signal);
        } else {
            // if we are at this point we must ask: what the hell is that?
            LOGGER.warn("Client <{}>: Got unknown message on WebSocket on NONE channel: {}",
                    sessionId, message);
        }
    }

    private void handleThingEvent(final CharSequence correlationId, final ThingEvent<?> jsonifiable,
            final String subscriptionKey, final JsonifiableAdaptable jsonifiableAdaptable) {

        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, jsonifiable.getEntityId(), jsonifiable.getType())
                        .correlationId(correlationId)
                        .build();
        final Message<ThingEvent<?>> eventMessage = Message.<ThingEvent<?>>newBuilder(messageHeaders)
                .payload(jsonifiable)
                .extra(jsonifiableAdaptable.getPayload().getExtra().orElse(null))
                .build();
        final Consumer<Message<?>> eventConsumer = subscriptions.get(subscriptionKey);
        if (eventConsumer != null) {
            eventConsumer.accept(eventMessage);
        } else {
            LOGGER.debug("Client <{}>: Dropping incoming event as no subscription for consuming events was" +
                            " registered. Did you call 'client.twin().startConsumption()' or" +
                            " 'client.live().startConsumption()' ?",
                    sessionId);
        }
    }

    private void handleLiveMessage(final MessageCommand<?, ?> messageCommand) {

        final Message<?> message = messageCommand.getMessage();
        final Consumer<Message<?>> messageConsumer = subscriptions.get(LiveImpl.CONSUME_LIVE_MESSAGES_HANDLER);
        if (messageConsumer != null) {
            messageConsumer.accept(message);
        } else {
            LOGGER.warn("Client <{}>: Dropping incoming message as no subscription for consuming messages was" +
                    " registered. Did you call 'client.twin().startConsumption()' or" +
                    " 'client.live().startConsumption()'?", sessionId);
        }
    }

    private void handleLiveMessageResponse(final MessageCommandResponse<?, ?> messageCommandResponse) {

        final Message message = messageCommandResponse.getMessage();
        LOGGER.debug("Client <{}>: Received response message: {}", sessionId, message);
        final String correlationId = messageCommandResponse.getDittoHeaders().getCorrelationId().orElse("");
        Optional.ofNullable(messageCommandResponseConsumers.remove(correlationId))
                .ifPresent(consumer -> consumer.getResponseConsumer().accept(message, null));
    }

    private void handleLiveMessageResponse(final ErrorResponse<?> errorResponse) {

        final String correlationId = errorResponse.getDittoHeaders().getCorrelationId().orElse("");
        Optional.ofNullable(messageCommandResponseConsumers.remove(correlationId))
                .ifPresent(consumer ->
                        consumer.getResponseConsumer().accept(null, errorResponse.getDittoRuntimeException()));
    }

    private void handleLiveCommand(final LiveCommand<?, ?> liveCommand,
            final JsonifiableAdaptable jsonifiableAdaptable) {

        final DittoHeaders dittoHeaders = liveCommand.getDittoHeaders();
        final Optional<JsonSchemaVersion> commandSchemaVersion = dittoHeaders.getSchemaVersion();
        // only accept events with either with:
        // * missing schemaVersion, or
        // * with the same schemaVersion as the client uses
        if (!commandSchemaVersion.isPresent() ||
                commandSchemaVersion.get().equals(messagingConfiguration.getJsonSchemaVersion())) {
            final MessageHeaders messageHeaders =
                    MessageHeaders.newBuilder(MessageDirection.FROM, liveCommand.getThingEntityId(),
                            liveCommand.getType())
                            .correlationId(dittoHeaders.getCorrelationId().orElse(null))
                            .build();
            final Message<LiveCommand<?, ?>> liveCommandMessage = Message.<LiveCommand<?, ?>>newBuilder(messageHeaders)
                    .payload(liveCommand)
                    .extra(jsonifiableAdaptable.getPayload().getExtra().orElse(null))
                    .build();

            final Consumer<Message<?>> liveCommandConsumer = subscriptions.get(LiveImpl.CONSUME_LIVE_COMMANDS_HANDLER);
            if (liveCommandConsumer != null) {
                liveCommandConsumer.accept(liveCommandMessage);
            } else {
                LOGGER.warn(
                        "Client <{}>: Dropping incoming live command as no subscription for consuming live commands " +
                                "was registered. Did you call 'client.live().startConsumption()' ?",
                        sessionId);
            }
        } else {
            LOGGER.trace(
                    "Client <{}>: Received live command in other JsonSchemaVersion ({}) than the client uses ({}), not  " +
                            "delivering it: {}", sessionId,
                    commandSchemaVersion.get(),
                    messagingConfiguration.getEndpointUri(), liveCommand);
        }
    }

    private void handleLiveCommandResponse(final CommandResponse<?> commandResponse) {
        commandResponseConsumer.accept(commandResponse);
    }

    /**
     * A LinkedHashMap with a {@code maxSize}, removing the oldest entries as new ones get in.
     */
    private static class LimitedHashMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = -2771080576933386538L;

        private final int maxSize;

        private LimitedHashMap(final int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            final LimitedHashMap<?, ?> that = (LimitedHashMap<?, ?>) o;
            return maxSize == that.maxSize;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), maxSize);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [" +
                    super.toString() +
                    ", maxSize=" + maxSize +
                    "]";
        }

    }

}
