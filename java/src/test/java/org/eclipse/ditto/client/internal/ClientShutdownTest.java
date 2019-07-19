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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.configuration.CommonConfiguration;
import org.eclipse.ditto.client.configuration.CredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.InternalConfiguration;
import org.eclipse.ditto.client.configuration.internal.TestClientConfiguration;
import org.eclipse.ditto.client.messaging.mock.MockMessagingProvider;
import org.junit.Test;

/**
 * Test if all threads were shutdown after client is destroyed.
 */
public class ClientShutdownTest {

    private static final List<String> ALLOWED_THREADS = asList("main", "Monitor Ctrl-Break", "BundleWatcher: 1",
            "surefire-forkedjvm-command-thread", "surefire-forkedjvm-ping-30s", "ping-30s", "Attach API wait loop");

    @Test
    public void testNoMoreActiveThreads() throws InterruptedException {
        final CredentialsAuthenticationConfiguration authenticationConfiguration =
                TestClientConfiguration.getAuthenticationConfiguration();

        final CommonConfiguration configuration =
                TestClientConfiguration.getDittoClientConfiguration(new MockMessagingProvider(),
                        authenticationConfiguration);

        final MockMessagingProvider messaging = new MockMessagingProvider();
        messaging.onSend(m -> {});

        // create client and destroy again
        final InternalConfiguration cfg = TestClientConfiguration.buildInternalConfiguration(configuration);
        new DittoClientImpl(cfg).destroy();

        // wait some time for executors/threads to shutdown
        Thread.sleep(2000);

        final Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);

        // filter out main thread and monitor thread
        final List<String> activeThreads = Stream.of(threads)
                .map(Thread::getName)
                .filter(name -> !ALLOWED_THREADS.contains(name))
                .collect(Collectors.toList());

        // expect no more active threads
        Assertions.assertThat(activeThreads)
                .withFailMessage("There are %d threads active: %s", threads.length,
                        Arrays.toString(threads))
                .isEmpty();
    }

}
