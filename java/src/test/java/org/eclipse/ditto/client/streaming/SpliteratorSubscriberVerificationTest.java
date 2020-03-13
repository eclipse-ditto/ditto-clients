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
package org.eclipse.ditto.client.streaming;

import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberWhiteboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verify reactive-streams compatibility of {@link SpliteratorSubscriber}.
 */
public final class SpliteratorSubscriberVerificationTest extends SubscriberWhiteboxVerification<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpliteratorSubscriberVerificationTest.class);

    public SpliteratorSubscriberVerificationTest() {
        super(new TestEnvironment(true));
    }

    @Override
    public Integer createElement(final int i) {
        return i;
    }

    @Override
    public Subscriber<Integer> createSubscriber(final WhiteboxSubscriberProbe<Integer> whiteboxSubscriberProbe) {
        final SpliteratorSubscriber<Integer> underTest = SpliteratorSubscriber.of();
        CompletableFuture.runAsync(() -> underTest.forEachRemaining(this::log));
        return WhiteboxSubscriber.wrap(underTest, whiteboxSubscriberProbe);
    }

    private void log(final Object element) {
        LOGGER.info("ELEMENT: <{}>", element);
    }
}
