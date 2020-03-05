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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.reactivestreams.tck.SubscriberWhiteboxVerification;

/**
 * Wraps a subscriber for a test probe.
 */
final class WhiteboxSubscriber<T> implements Subscriber<T> {

    private final Subscriber<T> delegate;
    private final SubscriberWhiteboxVerification.WhiteboxSubscriberProbe<T> probe;

    private WhiteboxSubscriber(final Subscriber<T> delegate,
            final SubscriberWhiteboxVerification.WhiteboxSubscriberProbe<T> probe) {
        this.delegate = delegate;
        this.probe = probe;
    }

    static <T> Subscriber<T> wrap(final Subscriber<T> subscriber,
            final SubscriberWhiteboxVerification.WhiteboxSubscriberProbe<T> probe) {
        return new WhiteboxSubscriber<>(subscriber, probe);
    }

    @Override
    public void onSubscribe(final Subscription s) {
        delegate.onSubscribe(s);
        probe.registerOnSubscribe(new SubscriberWhiteboxVerification.SubscriberPuppet() {

            @Override
            public void triggerRequest(long elements) {
                s.request(elements);
            }

            @Override
            public void signalCancel() {
                s.cancel();
            }
        });
    }

    @Override
    public void onNext(final T element) {
        delegate.onNext(element);
        probe.registerOnNext(element);
    }

    @Override
    public void onError(final Throwable cause) {
        delegate.onError(cause);
        probe.registerOnError(cause);
    }

    @Override
    public void onComplete() {
        delegate.onComplete();
        probe.registerOnComplete();
    }
}