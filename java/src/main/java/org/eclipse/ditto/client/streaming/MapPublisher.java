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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A simple map stage for a publisher.
 *
 * @param <T> the type of elements.
 * @since 1.1.0
 */
public final class MapPublisher<S, T> implements Publisher<T> {

    private final Publisher<S> publisher;
    private final Function<S, T> function;

    private MapPublisher(final Publisher<S> publisher, final Function<S, T> function) {
        this.publisher = publisher;
        this.function = function;
    }

    public static <S, T> MapPublisher<S, T> of(final Publisher<S> publisher, final Function<S, T> function) {
        return new MapPublisher<>(publisher, function);
    }

    @Override
    public void subscribe(final Subscriber<? super T> s) {
        publisher.subscribe(new MapSubscriber<>(s, function));
    }

    private static final class MapSubscriber<S, T> implements Subscriber<S> {

        private final Subscriber<? super T> subscriber;
        private final Function<S, T> function;
        private final AtomicReference<Subscription> subscription;

        private MapSubscriber(final Subscriber<? super T> subscriber, final Function<S, T> function) {
            this.subscriber = subscriber;
            this.function = function;
            subscription = new AtomicReference<>();
        }

        @Override
        public void onSubscribe(final Subscription s) {
            subscription.set(s);
            subscriber.onSubscribe(s); // will cancel subscription on duplicate
        }

        @Override
        public void onNext(final S s) {
            try {
                subscriber.onNext(function.apply(s));
            } catch (final Exception error) {
                subscriber.onError(error);
                final Subscription upstreamSubscription = subscription.get();
                if (upstreamSubscription != null) {
                    upstreamSubscription.cancel();
                }
            }
        }

        @Override
        public void onError(final Throwable t) {
            subscriber.onError(t);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }
    }
}
