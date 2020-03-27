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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight glue-code between {@code org.reactivestreams} and {@code java.util.stream} with buffering to minimize
 * probability of blocking.
 *
 * @param <T> the type of elements.
 * @since 1.1.0
 */
public final class SpliteratorSubscriber<T> implements Subscriber<T>, Spliterator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpliteratorSubscriber.class);

    private final BlockingQueue<Element<T>> buffer;
    private final long timeoutMillis;
    private final int capacity;
    private final int batchSize;
    private final AtomicReference<Subscription> subscription;
    private final AtomicInteger splits;
    private final AtomicInteger quota;
    private final AtomicBoolean cancelled;

    private SpliteratorSubscriber(final long timeoutMillis, final int bufferSize, final int batchSize) {
        // reserve 2*bufferSize+1 space in buffer for <=bufferSize elements and bufferSize+1 EOS markers
        final int actualBufferSize = bufferSize * 2 + 1;
        this.buffer = new ArrayBlockingQueue<>(actualBufferSize);
        this.timeoutMillis = timeoutMillis;
        this.batchSize = Math.min(batchSize, bufferSize);
        capacity = bufferSize;
        subscription = new AtomicReference<>();
        splits = new AtomicInteger(1);
        quota = new AtomicInteger(0);
        cancelled = new AtomicBoolean(false);
    }

    /**
     * Create a spliterator-subscriber with default settings.
     *
     * @param <T> type of elements.
     * @return the spliterator-subscriber.
     */
    public static <T> SpliteratorSubscriber<T> of() {
        return new SpliteratorSubscriber<>(10000L, 2, 1);
    }

    public static <T> SpliteratorSubscriber<T> of(final Duration timeout, final int bufferSize, final int batchSize) {
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Expect positive timeout, got: " + timeout);
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Expect positive batchSize, got: " + batchSize);
        }
        if (batchSize > bufferSize) {
            throw new IllegalArgumentException("Expect bufferSize to be at least batchSize=" + batchSize +
                    ", got: " + bufferSize);
        }
        return new SpliteratorSubscriber<>(Math.max(1L, timeout.toMillis()), bufferSize, batchSize);
    }

    /**
     * Represent this spliterator as a stream.
     *
     * @return this spliterator as a stream.
     */
    public Stream<T> asStream() {
        return StreamSupport.stream(this, false);
    }

    @Override
    public void onSubscribe(final Subscription s) {
        LOGGER.trace("onSubscribe <{}>", s);
        checkNotNull(s);
        final Subscription previousSubscription;
        synchronized (subscription) {
            previousSubscription = subscription.get();
            if (previousSubscription == null) {
                subscription.set(s);
            }
        }
        if (previousSubscription == null) {
            LOGGER.trace("Initial request: <{}>", capacity);
            s.request(capacity);
        } else {
            LOGGER.warn("onSubscribe() called a second time; cancelling subscription <{}>.", s);
            s.cancel();
        }
    }

    @Override
    public void onNext(final T t) {
        LOGGER.trace("onNext <{}>", t);
        buffer.add(new HasElement<>(checkNotNull(t)));
    }

    @Override
    public void onError(final Throwable t) {
        LOGGER.trace("onError", t);
        cancelled.set(true);
        addErrors(t);
    }

    @Override
    public void onComplete() {
        LOGGER.trace("onComplete");
        cancelled.set(true);
        addEos();
    }

    // always cancel the stream on error thrown, because user code catching the error is outside
    // the element handling code and should consider this spliterator "used up."
    // as a precaution, the error is propagated to all threads reading from this spliterator.
    private void cancelOnError(final Consumer<? super T> consumer, final T element) {
        try {
            consumer.accept(element);
        } catch (final RuntimeException e) {
            cancelled.set(true);
            addErrors(e);
            final Subscription s = subscription.get();
            if (s != null) {
                s.cancel();
            }
            throw e;
        }
    }

    private void addEos() {
        // overfill buffer with EOS to prevent concurrent pulling in order to support infinite splitting of this.
        addNCopies(new Completed<>());
    }

    private void addErrors(final Throwable error) {
        addNCopies(new Failed<>(error));
    }

    private void addNCopies(final Element<T> element) {
        buffer.addAll(Collections.nCopies(capacity + 1, element));
    }

    private void request() {
        if (!cancelled.get()) {
            final int previousQuota = quota.getAndUpdate(i -> i >= batchSize ? i - batchSize : i);
            if (previousQuota >= batchSize) {
                LOGGER.trace("Request <{}>", batchSize);
                subscription.get().request(batchSize);
            } else {
                LOGGER.trace("Not requesting: not enough quota.");
            }
        }
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> consumer) {
        final Element<T> next;
        try {
            next = buffer.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final Exception e) {
            throw new CompletionException(e);
        }
        if (next == null) {
            throw new IllegalStateException("timed out after " + timeoutMillis + " ms");
        } else {
            quota.getAndUpdate(i -> Math.min(capacity, i + 1));
            return next.eval(
                    e -> {
                        cancelOnError(consumer, e);
                        request();
                        return true;
                    },
                    () -> {
                        buffer.add(next);
                        return false;
                    },
                    error -> {
                        buffer.add(next);
                        throw wrapAsRuntimeException(error);
                    }
            );
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        final int nextSplits = splits.updateAndGet(n -> Math.min(capacity, n) + 1);
        if (nextSplits <= capacity) {
            // 'this' is thread safe.
            // no need to check subscription != null because return type does not expose subscriber methods
            return this;
        } else {
            // cannot split any more: buffer size cannot support safety of tryAdvance() with this many splits
            return null;
        }
    }

    @Override
    public long estimateSize() {
        final int occupation = buffer.size();
        if (occupation > capacity) {
            /// terminated
            return 0L;
        } else if (0 < occupation && occupation < capacity) {
            // buffer not full and not empty
            return occupation;
        } else {
            // buffer full or empty; no idea how many elements remaining
            return Long.MAX_VALUE;
        }
    }

    /**
     * Return the characteristics of this spliterator: non-null, immutable, concurrent.
     * This spliterator is not 100% ordered because {@code this#trySplit()} distributes elements on
     * first-come-first-serve basis. If {@code this#trySplit()} is never called, then
     * {@code this#tryAdvance(java.util.function.Consumer)} and
     * {@code this#forEachRemaining(java.util.function.Consumer)}
     * have both a well-defined encounter order.
     *
     * @return characteristics of this spliterator.
     */
    @Override
    public int characteristics() {
        return NONNULL | IMMUTABLE | CONCURRENT;
    }

    private static RuntimeException wrapAsRuntimeException(final Throwable error) {
        if (error instanceof RuntimeException) {
            return (RuntimeException) error;
        } else {
            return new CompletionException("SpliteratorSubscriber encountered " + error.getClass() +
                    " while reading from its publisher", error);
        }
    }

    private interface Element<T> {

        <S> S eval(Function<T, S> onElement, Supplier<S> onComplete, Function<Throwable, S> onError);
    }

    private static final class HasElement<T> implements Element<T> {

        private final T element;

        private HasElement(final T element) {
            this.element = element;
        }

        @Override
        public <S> S eval(final Function<T, S> onElement, final Supplier<S> onComplete,
                final Function<Throwable, S> onError) {
            return onElement.apply(element);
        }
    }

    private static final class Completed<T> implements Element<T> {

        @Override
        public <S> S eval(final Function<T, S> onElement, final Supplier<S> onComplete,
                final Function<Throwable, S> onError) {
            return onComplete.get();
        }
    }

    private static final class Failed<T> implements Element<T> {

        private final Throwable error;

        private Failed(final Throwable error) {
            this.error = error;
        }

        @Override
        public <S> S eval(final Function<T, S> onElement, final Supplier<S> onComplete,
                final Function<Throwable, S> onError) {
            return onError.apply(error);
        }
    }
}
