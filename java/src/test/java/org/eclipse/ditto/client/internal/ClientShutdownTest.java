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

import static java.util.Arrays.asList;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.DisconnectedDittoClient;
import org.eclipse.ditto.client.DittoClientUsageExamples;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.messaging.internal.WebSocketMessagingProvider;
import org.junit.Test;

/**
 * Test if all threads were shutdown after client is destroyed.
 */
public class ClientShutdownTest {

    private static final List<String> ALLOWED_THREADS = asList("main", "Monitor Ctrl-Break", "BundleWatcher: 1",
            "surefire-forkedjvm-command-thread", "surefire-forkedjvm-ping-30s", "ping-30s", "Attach API wait loop",
            "FelixResolver-");

    @Test
    public void noThreadLeakWithWebsocketMessagingProvider() throws Exception {
        final List<String> startingThreadNames = getActiveThreads(Collections.emptySet());

        final WebSocketMessagingProvider messaging =
                (WebSocketMessagingProvider) DittoClientUsageExamples.createMessagingProvider();

        // GIVEN: known executors for messaging and adaptable bus have tasks submitted
        messaging.getExecutorService().submit(() -> {});
        messaging.getConnectExecutor().submit(() -> {});
        final CompletionStage<String> stringFuture = messaging.getAdaptableBus()
                .subscribeOnceForString(Classification.forString("string-message"), Duration.ofDays(1L));
        messaging.getAdaptableBus().publish("string-message");
        stringFuture.toCompletableFuture().join();

        // WHEN: client is created
        final DisconnectedDittoClient client = DittoClients.newDisconnectedInstance(messaging);

        // THEN: threads are allocated
        Assertions.assertThat(getActiveThreads(startingThreadNames)).isNotEmpty();

        // WHEN: client is destroyed
        client.destroy();
        // wait for threads to stop
        TimeUnit.SECONDS.sleep(2L);

        // THEN: all allocated threads are destroyed
        final List<String> activeThreads = getActiveThreads(startingThreadNames);
        Assertions.assertThat(activeThreads)
                .withFailMessage("There are %d threads active: %s", activeThreads.size(), activeThreads)
                .isEmpty();
    }

    private static List<String> getActiveThreads(final Collection<String> startingThreadNames) {
        final Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);

        // filter out main thread and monitor thread
        return Stream.of(threads)
                .filter(Objects::nonNull)
                .map(Thread::getName)
                .filter(Objects::nonNull)
                .filter(name -> !ALLOWED_THREADS.contains(name) && !startingThreadNames.contains(name))
                .collect(Collectors.toList());
    }

}
