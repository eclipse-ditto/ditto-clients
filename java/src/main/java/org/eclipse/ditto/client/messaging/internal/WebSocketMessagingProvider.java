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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.DisconnectedContext;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.internal.DefaultThreadFactory;
import org.eclipse.ditto.client.internal.VersionReader;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.BusFactory;
import org.eclipse.ditto.client.management.ClientReconnectingException;
import org.eclipse.ditto.client.messaging.AuthenticationException;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.MessagingException;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.StatusLine;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketError;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

/**
 * Messaging Provider providing messaging access to Ditto WebSocket which is directly provided by Eclipse Ditto Gateway.
 *
 * @since 1.0.0
 */
public final class WebSocketMessagingProvider extends WebSocketAdapter implements MessagingProvider {

    // how long this object survives after the websocket connection is closed by server and reconnect is disabled
    private static final Duration ZOMBIE_LIFETIME = Duration.ofSeconds(3L);

    private static final String DITTO_CLIENT_USER_AGENT = "DittoClient/" + VersionReader.determineClientVersion();
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMessagingProvider.class);
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final int RECONNECTION_TIMEOUT_SECONDS = 5;
    private static final long INITIAL_CHECK_DELAY = 0;
    private static final long RETRY_CHECK_PERIOD = 20;
    private static final int MIN_RECONNECTING_CHECK_TRIES = 4;

    private final AdaptableBus adaptableBus;
    private final MessagingConfiguration messagingConfiguration;
    private final AuthenticationProvider<WebSocket> authenticationProvider;
    private final ExecutorService callbackExecutor;
    private final String sessionId;
    private final ScheduledExecutorService connectExecutor;
    private final Map<Object, String> subscriptionMessages;
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final AtomicBoolean initializing = new AtomicBoolean(false);
    private final AtomicBoolean explicitlyClosing = new AtomicBoolean(false);
    private final CompletableFuture<WebSocket> initializationFuture = new CompletableFuture<>();

    private final AtomicReference<WebSocket> webSocket;

    private final DisconnectedContext.DisconnectionHandler disconnectionHandler;

    private final AtomicBoolean manuallyPreventReconnect = new AtomicBoolean(false);
    private final AtomicBoolean manuallyPerformReconnect = new AtomicBoolean(false);

    private Runnable channelCloser;
    @Nullable
    private Throwable lastReceivedDittoProtocolError = null;
    private CountDownLatch lastReceivedDittoProtocolErrorLatch = new CountDownLatch(1);

    /**
     * Constructs a new {@code WsMessagingProvider}.
     *
     * @param adaptableBus           the bus to publish all messages to.
     * @param messagingConfiguration the specific configuration to apply.
     * @param authenticationProvider provider for the authentication method with which to open the websocket.
     * @param callbackExecutor       the executor service to run callbacks with.
     */
    private WebSocketMessagingProvider(final AdaptableBus adaptableBus,
                                       final MessagingConfiguration messagingConfiguration,
                                       final AuthenticationProvider<WebSocket> authenticationProvider,
                                       final ExecutorService callbackExecutor) {
        this.adaptableBus = adaptableBus;
        this.messagingConfiguration = messagingConfiguration;
        this.authenticationProvider = authenticationProvider;
        this.callbackExecutor = callbackExecutor;

        sessionId = authenticationProvider.getConfiguration().getSessionId();
        connectExecutor = createConnectExecutor(sessionId);
        subscriptionMessages = new ConcurrentHashMap<>();
        webSocket = new AtomicReference<>();

        channelCloser = () -> {
        };
        disconnectionHandler = new DisconnectedContext.DisconnectionHandler() {

            @Override
            public DisconnectedContext.DisconnectionHandler closeChannel() {
                channelCloser.run();
                return this;
            }

            @Override
            public DisconnectedContext.DisconnectionHandler preventConfiguredReconnect(final boolean preventReconnect) {
                manuallyPreventReconnect.set(preventReconnect);
                return this;
            }

            @Override
            public DisconnectedContext.DisconnectionHandler performReconnect() {
                manuallyPerformReconnect.set(true);
                doReconnect();
                return this;
            }
        };
    }

    private static ScheduledExecutorService createConnectExecutor(final String sessionId) {
        final int corePoolSize;
        if (VersionReader.determineJavaRuntimeVersion() <= 8) {
            // for Java <= 8, because of bug https://bugs.openjdk.java.net/browse/JDK-8129861, the corePoolSize must be at least 1:
            corePoolSize = 1;
        } else {
            // bug has been fixed since Java 9, so scale down to 0 threads if the scheduledThreadPool is not needed:
            corePoolSize = 0;
        }
        return Executors.newScheduledThreadPool(corePoolSize,
                new DefaultThreadFactory("ditto-client-reconnect-" + sessionId));
    }

    /**
     * Returns a new {@code WebSocketMessagingProvider}.
     *
     * @param messagingConfiguration configuration of messaging.
     * @param authenticationProvider provides authentication.
     * @param defaultExecutor        the executor for messages.
     * @param scheduledExecutor      the scheduled executor for scheduling tasks.
     * @return the provider.
     */
    public static WebSocketMessagingProvider newInstance(final MessagingConfiguration messagingConfiguration,
                                                         final AuthenticationProvider<WebSocket> authenticationProvider,
                                                         final ExecutorService defaultExecutor,
                                                         final ScheduledExecutorService scheduledExecutor) {
        checkNotNull(messagingConfiguration, "messagingConfiguration");
        checkNotNull(authenticationProvider, "authenticationProvider");
        checkNotNull(defaultExecutor, "defaultExecutor");
        checkNotNull(scheduledExecutor, "scheduledExecutor");

        final AdaptableBus adaptableBus = BusFactory.createAdaptableBus(defaultExecutor, scheduledExecutor);
        return new WebSocketMessagingProvider(adaptableBus, messagingConfiguration, authenticationProvider,
                defaultExecutor);
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

    /**
     * Return the executor for reconnection.
     *
     * @return the reconnect executor.
     */
    public ScheduledExecutorService getConnectExecutor() {
        return connectExecutor;
    }

    @Override
    public AdaptableBus getAdaptableBus() {
        return adaptableBus;
    }

    @Override
    public MessagingProvider registerSubscriptionMessage(final Object key, final String message) {
        subscriptionMessages.put(key, message);
        return this;
    }

    @Override
    public MessagingProvider unregisterSubscriptionMessage(final Object key) {
        subscriptionMessages.remove(key);
        return this;
    }

    @Override
    public CompletionStage<?> initializeAsync() {
        // this method may be called multiple times.
        if (!initializing.getAndSet(true) && webSocket.get() == null) {
            return connectWithPotentialRetries("initialize WebSocket",
                    this::createWebsocket,
                    initializationFuture,
                    messagingConfiguration.isInitialConnectRetryEnabled())
                    .thenApply(ws -> {
                        setWebSocket(ws);
                        return this;
                    });
        }
        // no need to set flags for subsequent calls of this method
        return initializationFuture.thenApply(ws -> this);
    }

    private WebSocket createWebsocket() {
        final WebSocketFactory webSocketFactory = WebSocketFactoryFactory.newWebSocketFactory(messagingConfiguration);
        final WebSocket ws;
        try {
            final String declaredAcksJsonArrayString = messagingConfiguration.getDeclaredAcknowledgements()
                    .stream()
                    .map(AcknowledgementLabel::toString)
                    .map(JsonValue::of)
                    .collect(JsonCollectors.valuesToArray())
                    .toString();
            ws = webSocketFactory.createSocket(messagingConfiguration.getEndpointUri())
                    .addHeader(DittoHeaderDefinition.DECLARED_ACKS.getKey(), declaredAcksJsonArrayString);
        } catch (final IOException e) {
            throw MessagingException.connectFailed(sessionId, e);
        }
        return ws;
    }

    /**
     * Initiates the connection to the web socket by using the provided {@code ws} and applying the passed
     * {@code webSocketListener} for web socket handling and incoming messages.
     *
     * @param ws the WebSocket instance to use for connecting.
     * @return The connected websocket.
     * be empty.
     * @throws NullPointerException if any argument is {@code null}.
     */
    private CompletionStage<WebSocket> initiateConnection(final WebSocket ws) {
        checkNotNull(ws, "ws");

        ws.addHeader("User-Agent", DITTO_CLIENT_USER_AGENT);
        ws.setMaxPayloadSize(256 * 1024); // 256 KiB
        ws.setMissingCloseFrameAllowed(true);
        ws.setFrameQueueSize(0);
        ws.setPingInterval(CONNECTION_TIMEOUT_MS);
        authenticationProvider.prepareAuthentication(ws);
        ws.addListener(this);

        LOGGER.info("Connecting WebSocket on endpoint <{}>.", ws.getURI());
        final Callable<WebSocket> connectCallable = ws.connectable();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return connectCallable.call();
            } catch (final Throwable e) {
                throw mapConnectError(e);
            }
        }, connectExecutor);
    }

    @Override
    public void emit(final String message) {
        if (reconnecting.get()) {
            throw ClientReconnectingException.newInstance();
        } else {
            sendToWebsocket(message);
        }
    }

    private void sendToWebsocket(final String stringMessage) {
        final WebSocket ws = webSocket.get();
        if (ws != null && ws.isOpen()) {
            LOGGER.debug("Client <{}>: Sending: {}", sessionId, stringMessage);
            ws.sendText(stringMessage);
        } else {
            LOGGER.error("Client <{}>: WebSocket is not connected - going to discard message '{}'",
                    sessionId, stringMessage);
        }
    }

    @Override
    public void close() {
        try {
            if (explicitlyClosing.getAndSet(true)) {
                LOGGER.debug("Client <{}>: WebSocket client is already closing", sessionId);
                return;
            }
            LOGGER.debug("Client <{}>: Closing WebSocket client of endpoint <{}>.", sessionId,
                    messagingConfiguration.getEndpointUri());

            // Scheduled tasks obtained from "shutdownNow" are useless because they overrides Runnable.run()
            // to NOT run when the parent executor was shut down.
            connectExecutor.shutdownNow();
            authenticationProvider.destroy();
            adaptableBus.shutdownExecutors();
            final WebSocket ws = webSocket.get();
            if (ws != null) {
                ws.disconnect();
            }

            LOGGER.info("Client <{}>: WebSocket destroyed.", sessionId);
            initializationFuture.completeExceptionally(MessagingException.connectFailed(sessionId,
                    new IllegalStateException("The client was destroyed.")));
        } catch (final Exception e) {
            LOGGER.info("Client <{}>: Exception occurred while trying to shutdown http client.", sessionId, e);
            initializationFuture.completeExceptionally(MessagingException.connectFailed(sessionId, e));
        }
    }

    @Override
    public void registerChannelCloser(final Runnable channelCloser) {
        this.channelCloser = channelCloser;
    }

    @Override
    public void onDittoProtocolError(final Throwable throwable) {
        this.lastReceivedDittoProtocolError = throwable;
        this.lastReceivedDittoProtocolErrorLatch.countDown();
    }

    @Override
    public void onConnected(final WebSocket websocket, final Map<String, List<String>> headers) {
        callbackExecutor.execute(() -> {
            LOGGER.info("Client <{}>: WebSocket connection is established", sessionId);

            if (!subscriptionMessages.isEmpty()) {
                LOGGER.info("Client <{}>: Subscribing again for messages from backend after reconnection",
                        sessionId);
                final CompletableFuture<Boolean> isReconnecting = new CompletableFuture<>();
                final Runnable checkTask = () -> {
                    if (!reconnecting.get()) {
                        isReconnecting.complete(true);  // Complete the future if flag is true
                    }
                };
                final ScheduledFuture<?> fixedRateChecker = connectExecutor.scheduleAtFixedRate(checkTask, INITIAL_CHECK_DELAY, RETRY_CHECK_PERIOD, TimeUnit.MILLISECONDS);
                try {
                    if (Boolean.TRUE.equals(isReconnecting.get(RETRY_CHECK_PERIOD * MIN_RECONNECTING_CHECK_TRIES, TimeUnit.MILLISECONDS))) { // Ensures 4 retries of the scheduleAtFixedRate method.
                        fixedRateChecker.cancel(true);
                        LOGGER.debug("Reconnecting is completed -> emitting subscriptionMessages: {}", subscriptionMessages);
                        subscriptionMessages.values().forEach(this::emit);
                    }
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    isReconnecting.complete(false);
                    fixedRateChecker.cancel(true);
                    LOGGER.error("Reconnecting failed: {}", e.getMessage());
                }
            }
        });
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
                awaitLastReceivedDittoProtocolError();
                handleReconnectionIfEnabled(DisconnectedContext.Source.SERVER, lastReceivedDittoProtocolError);
            } else if (!explicitlyClosing.get()) {
                // client closed connection because of a connection interruption or something similar
                LOGGER.info("Client <{}>: WebSocket connection to endpoint <{}> was unintentionally closed by client " +
                                "- client will try to reconnect if enabled!",
                        sessionId, messagingConfiguration.getEndpointUri());
                awaitLastReceivedDittoProtocolError();
                handleReconnectionIfEnabled(DisconnectedContext.Source.CLIENT, lastReceivedDittoProtocolError);
            } else {
                // only when close() was called we should end here
                LOGGER.info("Client <{}>: WebSocket connection to endpoint <{}> was closed by user",
                        sessionId, messagingConfiguration.getEndpointUri());
                handleReconnectionIfEnabled(DisconnectedContext.Source.USER_CODE, null);
            }
        });
    }

    private boolean awaitLastReceivedDittoProtocolError() {
        try {
            return lastReceivedDittoProtocolErrorLatch.await(500, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            lastReceivedDittoProtocolErrorLatch = new CountDownLatch(1);
        }
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
            handleReconnectionIfEnabled(DisconnectedContext.Source.CLIENT, cause);
        });
    }

    private CompletionStage<WebSocket> connectWithPotentialRetries(final String actionName,
                                                                   final Supplier<WebSocket> webSocket,
                                                                   final CompletableFuture<WebSocket> future,
                                                                   final boolean retry) {

        try {
            final Predicate<Throwable> isRecoverable =
                    retry ? WebSocketMessagingProvider::isRecoverable : exception -> false;
            return Retry.retryTo(actionName,
                            () -> initiateConnection(webSocket.get()))
                    .inClientSession(sessionId)
                    .withExecutors(connectExecutor, callbackExecutor)
                    .notifyOnError(messagingConfiguration.getConnectionErrorHandler().orElse(null))
                    .isRecoverable(isRecoverable)
                    .completeFutureEventually(future);
        } catch (final Exception exception) {
            future.completeExceptionally(exception);
            return future;
        }
    }

    private void handleReconnectionIfEnabled(final DisconnectedContext.Source disconnectionSource,
                                             @Nullable final Throwable throwableSupplier) {

        final Optional<Consumer<DisconnectedContext>> disconnectedListener =
                messagingConfiguration.getDisconnectedListener();
        if (disconnectedListener.isPresent()) {
            final Consumer<DisconnectedContext> disconnectedContextConsumer = disconnectedListener.get();
            disconnectedContextConsumer.accept(
                    new DefaultDisconnectedContext(disconnectionSource, throwableSupplier, disconnectionHandler));
        }

        if (messagingConfiguration.isReconnectEnabled()) {
            if (manuallyPreventReconnect.get()) {
                LOGGER.info("Client <{}>: User defined disconnectedListener explicitly prevented reconnect which " +
                        "would have happened now. Closing client ...", sessionId);
                // delay self destruction in order to handle any final error message
                adaptableBus.getScheduledExecutor()
                        .schedule(this::close, ZOMBIE_LIFETIME.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                // reconnect in a while if client was initially connected and we are not reconnecting already
                LOGGER.info("Client <{}>: Reconnection is enabled", sessionId);
                doReconnect();
            }
        } else {
            if (manuallyPerformReconnect.get()) {
                LOGGER.info("Client <{}>: User defined disconnectedListener explicitly performed reconnect. " +
                        "NOT closing client ...", sessionId);
            } else {
                LOGGER.info("Client <{}>: Reconnection is NOT enabled. Closing client ...", sessionId);
                // delay self destruction in order to handle any final error message
                adaptableBus.getScheduledExecutor()
                        .schedule(this::close, ZOMBIE_LIFETIME.toMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private void doReconnect() {
        if (reconnecting.compareAndSet(false, true)) {
            LOGGER.info("Client <{}>: Reconnecting in <{}> seconds ...", sessionId, RECONNECTION_TIMEOUT_SECONDS);
            connectExecutor.schedule(this::reconnectWithRetries, RECONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void reconnectWithRetries() {
        this.connectWithPotentialRetries("reconnect WebSocket", this::recreateWebSocket, new CompletableFuture<>(),
                        messagingConfiguration.isReconnectEnabled() || manuallyPerformReconnect.get())
                .thenAccept(reconnectedWebSocket -> {
                    setWebSocket(reconnectedWebSocket);
                    reconnecting.set(false);
                    manuallyPerformReconnect.set(false);
                });
    }

    private void setWebSocket(final WebSocket webSocket) {
        explicitlyClosing.set(false); // reset potential explicit close request by the user
        synchronized (this.webSocket) {
            final WebSocket oldWebSocket = this.webSocket.get();
            this.webSocket.set(webSocket);
            try {
                if (oldWebSocket != null && oldWebSocket != webSocket) {
                    oldWebSocket.disconnect();
                }
            } catch (final Exception exception) {
                LOGGER.error("Client <{}>: Error disconnecting a previous websocket", sessionId, exception);
            }
        }
    }

    private WebSocket recreateWebSocket() {
        LOGGER.info("Recreating Websocket..");
        final WebSocket ws = webSocket.get();
        if (ws == null) {
            LOGGER.error("Client <{}>: attempt to recreate a null websocket", sessionId);
            throw new IllegalStateException("Cannot recreate a null websocket. This method should not have been " +
                    "called without having created a WebSocket before.");
        }
        ws.clearHeaders();
        ws.clearListeners();

        try {
            final String declaredAcksJsonArrayString = messagingConfiguration.getDeclaredAcknowledgements()
                    .stream()
                    .map(AcknowledgementLabel::toString)
                    .map(JsonValue::of)
                    .collect(JsonCollectors.valuesToArray())
                    .toString();

            return ws.recreate(CONNECTION_TIMEOUT_MS)
                    .addHeader(DittoHeaderDefinition.DECLARED_ACKS.getKey(), declaredAcksJsonArrayString);
        } catch (IOException e) {
            throw MessagingException.recreateFailed(sessionId, e);
        }
    }

    @Override
    public void onBinaryMessage(final WebSocket websocket, final byte[] binary) {
        final String stringMessage = new String(binary, StandardCharsets.UTF_8);
        LOGGER.debug("Client <{}>: Received WebSocket byte array message <{}>, as string <{}> - don't know what to do" +
                " with it!.", sessionId, binary, stringMessage);
    }

    @Override
    public void onTextMessage(final WebSocket websocket, final String text) {
        LOGGER.debug("Client <{}>: Received WebSocket string message <{}>", sessionId, text);
        handleIncomingMessage(text);
    }

    private void handleIncomingMessage(final String message) {
        adaptableBus.publish(message);
    }

    private RuntimeException mapConnectError(final Throwable e) {
        final Throwable cause = getRootCause(e);
        final RuntimeException result;
        if (cause instanceof WebSocketException) {
            LOGGER.error("Got exception: {}", cause.getMessage());
            if (cause instanceof OpeningHandshakeException) {
                final StatusLine statusLine = ((OpeningHandshakeException) cause).getStatusLine();
                final HttpStatus httpStatus = HttpStatus.tryGetInstance(statusLine.getStatusCode())
                        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
                if (httpStatus.isClientError()) {
                    if (HttpStatus.UNAUTHORIZED.equals(httpStatus)) {
                        result = AuthenticationException.unauthorized(sessionId, cause);
                    } else if (HttpStatus.FORBIDDEN.equals(httpStatus)) {
                        result = AuthenticationException.forbidden(sessionId, cause);
                    } else {
                        result = AuthenticationException.withStatus(sessionId, cause, statusLine.getStatusCode(),
                                statusLine.getReasonPhrase() + ": " +
                                        new String(((OpeningHandshakeException) cause).getBody()));
                    }
                } else {
                    result = MessagingException.connectFailed(sessionId, cause);
                }
            } else if (((WebSocketException) cause).getError() == WebSocketError.SOCKET_CONNECT_ERROR &&
                    cause.getCause() instanceof UnknownHostException) {
                result = MessagingException.connectFailed(sessionId, cause.getCause());
            } else {
                result = MessagingException.connectFailed(sessionId, cause);
            }
        } else {
            result = MessagingException.connectFailed(sessionId, cause);
        }
        return result;
    }

    /**
     * Test whether an exception to be delivered to error consumer can be recovered from.
     * Reconnection will be attempted only if it is.
     *
     * @param error the error to deliver to any configured error consumer.
     * @return whether the error can be recovered from.
     */
    private static boolean isRecoverable(final Throwable error) {
        // every exception should be recoverable
        return true;
    }

    private static Throwable getRootCause(final Throwable e) {
        if (e.getCause() == null) {
            return e;
        }
        return e instanceof CompletionException || e instanceof ExecutionException ? getRootCause(e.getCause()) : e;
    }

}
