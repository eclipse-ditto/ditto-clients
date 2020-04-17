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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
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
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocoladapter.ProtocolAdapter;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.signals.base.Signal;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prepares a Ditto Client instance for testing with uncaught error and dispatch handlers.
 */
public abstract class AbstractDittoClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDittoClientTest.class);

    protected static final int TIMEOUT = 100;
    protected static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    protected static final ProtocolAdapter PROTOCOL_ADAPTER = DittoProtocolAdapter.newInstance();

    private final Queue<Throwable> uncaught = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<CompletableFuture<?>> assertionFutures = new ArrayList<>();

    public DittoClient client;
    public MockMessagingProvider messaging;

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
    public TestRule rule = new FailOnExceptionRule(uncaught);

    @Before
    public void before() {
        messaging = new MockMessagingProvider();
        messaging.onSend(m -> LOGGER.info("Send message: " + m));
        client = DittoClients.newInstance(messaging);
    }

    @After
    public void after() {
        client.destroy();
        assertAll();
        executorService.shutdown();
    }

    protected void assertAll() {
        CompletableFuture.allOf(assertionFutures.toArray(new CompletableFuture[0])).join();
    }

    protected void assertEventualCompletion(final CompletableFuture<?> future) {
        assertionFutures.add(CompletableFuture.runAsync(() -> assertCompletion(future), executorService));
    }

    protected void expectMsg(final String msg) {
        Assertions.assertThat(messaging.expectEmitted()).isEqualTo(msg);
    }

    protected <T> T expectMsgClass(final Class<T> clazz) {
        final String nextMessage = messaging.expectEmitted();
        final Signal<?> signal = PROTOCOL_ADAPTER.fromAdaptable(
                ProtocolFactory.jsonifiableAdaptableFromJson(JsonObject.of(nextMessage)));
        if (clazz.isInstance(signal)) {
            return clazz.cast(signal);
        } else {
            throw new AssertionError("Expect " + clazz + ", got " + signal);
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

    protected static void assertCompletion(final CompletableFuture<?> future) {
        try {
            future.get(1L, TimeUnit.SECONDS);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    protected static void assertOnlyIfNoneMatchHeader(final Signal<?> signal) {
        Assertions.assertThat(signal.getDittoHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_MATCH.getKey());
        Assertions.assertThat(signal.getDittoHeaders())
                .containsEntry(DittoHeaderDefinition.IF_NONE_MATCH.getKey(), "*");
    }

    protected static void assertOnlyIfMatchHeader(final Signal<?> signal) {
        Assertions.assertThat(signal.getDittoHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_NONE_MATCH.getKey());
        Assertions.assertThat(signal.getDittoHeaders()).containsEntry(DittoHeaderDefinition.IF_MATCH.getKey(), "*");
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
}
