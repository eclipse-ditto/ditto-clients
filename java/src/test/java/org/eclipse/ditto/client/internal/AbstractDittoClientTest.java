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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.client.internal.ActiveThreadsUtil.assertNoMoreActiveThreads;
import static org.eclipse.ditto.client.internal.ActiveThreadsUtil.getActiveThreads;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.awaitility.Awaitility;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.messaging.internal.MockMessagingProvider;
import org.eclipse.ditto.client.rule.FailOnExceptionRule;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageBuilder;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.protocol.adapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocol.adapter.ProtocolAdapter;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.signals.base.Signal;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prepares a Ditto Client instance for testing with uncaught error and dispatch handlers.
 */
public abstract class AbstractDittoClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDittoClientTest.class);

    protected static final int TIMEOUT = 1000;
    protected static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    protected static final ProtocolAdapter PROTOCOL_ADAPTER = DittoProtocolAdapter.newInstance();
    private static final Duration ACTIVE_THREADS_WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration ACTIVE_THREADS_POLL_INTERVAL = Duration.ofSeconds(1);

    private final Queue<Throwable> uncaught = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<CompletableFuture<?>> assertionFutures = new ArrayList<>();

    public DittoClient client;
    public MockMessagingProvider messaging;
    private List<String> startingThreadNames;

    protected static ThingId newThingId(final String thingId) {
        return ThingId.of("org.eclipse.ditto.test:" + thingId);
    }

    public static String extractUtf8StringFromBody(final Optional<ByteBuffer> body) {
        return Objects.requireNonNull(body
                .map(StandardCharsets.UTF_8::decode)
                .map(CharBuffer::toString)
                .orElse(null));
    }

    @Rule
    public TestRule loggingWatcher = new TestedMethodLoggingWatcher(LOGGER);

    @Rule
    public TestRule rule = new FailOnExceptionRule(uncaught);

    @Before
    public void before() {
        startingThreadNames = getActiveThreads(Collections.emptySet());
        LOGGER.debug("active threads before test: {}", startingThreadNames);
        messaging = new MockMessagingProvider();
        messaging.onSend(m -> LOGGER.info("Send message: " + m));
        client = DittoClients.newInstance(messaging)
                .connect()
                .toCompletableFuture()
                .join();
    }

    @After
    public void after() throws InterruptedException {
        assertAll();
        executorService.shutdown();
        assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        client.destroy();
        LOGGER.debug("Client destroyed...");

        Awaitility.await("active threads")
                .pollDelay(Duration.ZERO)
                .pollInterval(ACTIVE_THREADS_POLL_INTERVAL)
                .timeout(ACTIVE_THREADS_WAIT_TIMEOUT)
                .untilAsserted(() -> {
                    final List<String> activeThreads =
                            getActiveThreads(startingThreadNames, "awaitility[active threads]");
                    LOGGER.debug("active threads after test: {}", activeThreads);
                    assertNoMoreActiveThreads(activeThreads);
                });
    }

    protected void assertAll() {
        LOGGER.debug("Waiting for {} futures to complete.", assertionFutures.size());
        CompletableFuture.allOf(assertionFutures.toArray(new CompletableFuture[0])).join();
    }

    protected void assertEventualCompletion(final CompletionStage<?> future) {
        assertionFutures.add(CompletableFuture.runAsync(() -> assertCompletion(future), executorService));
    }

    protected String expectProtocolMsgWithCorrelationId(final String msg) {
        final String emitted = messaging.expectEmitted();
        assertThat(emitted)
                .startsWith(msg)
                .contains(DittoHeaderDefinition.CORRELATION_ID.getKey());
        return determineCorrelationId(emitted);
    }

    protected static String determineCorrelationId(final String protocolMessage) {
        final String parametersString = protocolMessage.split(Pattern.quote("?"), 2)[1];
        return Arrays.stream(parametersString.split("&"))
                .map(paramWithValue -> paramWithValue.split("=", 2))
                .filter(pv -> DittoHeaderDefinition.CORRELATION_ID.getKey().equals(pv[0]))
                .map(pv -> pv[1])
                .findFirst().orElseThrow(() -> new IllegalArgumentException(""));
    }

    protected <T> T expectMsgClass(final Class<T> clazz) {
        final String nextMessage = messaging.expectEmitted();
        if (clazz.isAssignableFrom(String.class)) {
            return clazz.cast(nextMessage);
        } else {
            final Signal<?> signal = PROTOCOL_ADAPTER.fromAdaptable(
                    ProtocolFactory.jsonifiableAdaptableFromJson(JsonObject.of(nextMessage)));
            if (clazz.isInstance(signal)) {
                return clazz.cast(signal);
            } else {
                throw new AssertionError("Expect " + clazz + ", got " + signal);
            }
        }
    }

    protected void reply(final Signal<?> signal) {
        reply(toAdaptableJsonString(signal));
    }

    protected void reply(final String ack) {
        messaging.getAdaptableBus().publish(ack);
    }

    protected static String toAdaptableJsonString(final Signal<?> signal) {
        return ProtocolFactory.wrapAsJsonifiableAdaptable(PROTOCOL_ADAPTER.toAdaptable(signal)).toJsonString();
    }

    protected static void assertCompletion(final CompletionStage<?> future) {
        try {
            future.toCompletableFuture().get(10L, TimeUnit.SECONDS);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    protected static void assertOnlyIfNoneMatchHeader(final Signal<?> signal) {
        assertThat(signal.getDittoHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_MATCH.getKey());
        assertThat(signal.getDittoHeaders())
                .containsEntry(DittoHeaderDefinition.IF_NONE_MATCH.getKey(), "*");
    }

    protected static void assertOnlyIfMatchHeader(final Signal<?> signal) {
        assertThat(signal.getDittoHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_NONE_MATCH.getKey());
        assertThat(signal.getDittoHeaders()).containsEntry(DittoHeaderDefinition.IF_MATCH.getKey(), "*");
    }

    protected static <T> MessageBuilder<T> newMessageBuilder(final String subject) {
        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, org.eclipse.ditto.client.TestConstants.Thing.THING_ID,
                        subject).correlationId(
                        null).build();
        return Message.newBuilder(messageHeaders);
    }


    protected static <T> MessageBuilder<T> newFeatureMessageBuilder(final String featureId) {
        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, org.eclipse.ditto.client.TestConstants.Thing.THING_ID,
                        "request")
                        .featureId(featureId)
                        .correlationId(null)
                        .build();
        return Message.newBuilder(messageHeaders);
    }

    private static final class TestedMethodLoggingWatcher extends TestWatcher {

        private final Logger logger;

        public TestedMethodLoggingWatcher(final Logger logger) {
            this.logger = logger;
        }

        @Override
        protected void starting(final org.junit.runner.Description description) {
            logger.info("Testing: {}#{}()", description.getTestClass().getSimpleName(), description.getMethodName());
        }

        @Override
        protected void finished(final Description description) {
            logger.info("Finished: {}#{}()", description.getTestClass().getSimpleName(), description.getMethodName());
        }
    }
}
