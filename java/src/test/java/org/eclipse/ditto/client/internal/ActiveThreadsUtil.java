/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;

final class ActiveThreadsUtil {

    private static final List<String> ALLOWED_THREADS = asList("main", "Monitor Ctrl-Break", "BundleWatcher: 1",
            "surefire-forkedjvm-command-thread", "surefire-forkedjvm-ping-30s", "ping-30s", "Attach API wait loop",
            "FelixResolver-");

    static List<String> getActiveThreads(final Collection<String> startingThreadNames,
            final String... additionalAllowedThreads) {
        final Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);

        // filter out main thread and monitor thread
        return Stream.of(threads)
                .filter(Objects::nonNull)
                .map(Thread::getName)
                .filter(Objects::nonNull)
                .filter(name -> !ALLOWED_THREADS.contains(name) && !startingThreadNames.contains(name) &&
                        Arrays.stream(additionalAllowedThreads).noneMatch(name::equals))
                .collect(Collectors.toList());
    }

    static void assertNoMoreActiveThreads(final List<String> activeThreads) {
        Assertions.assertThat(activeThreads)
                .withFailMessage("There are %d threads active: %s", activeThreads.size(), activeThreads)
                .isEmpty();
    }

}
