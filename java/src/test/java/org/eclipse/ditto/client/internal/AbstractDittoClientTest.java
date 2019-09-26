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
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.messaging.mock.MockMessagingProvider;
import org.eclipse.ditto.client.rule.FailOnExceptionRule;
import org.eclipse.ditto.model.things.ThingId;
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

    private final Queue<Throwable> uncaught = new ConcurrentLinkedQueue<>();

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
    }

}
