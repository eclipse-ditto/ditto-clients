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

import java.util.Collections;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Lightweight glue-code between {@code org.reactivestreams} and {@code java.util.stream} with buffering to minimize
 * probability of blocking.
 *
 * @param <T> the type of elements.
 */
public final class SpliteratorSubscriber<T> implements Subscriber<T>, Spliterator<T> {

    private final BlockingQueue<Optional<T>> buffer;
    private final long timeoutMillis;
    private final int capacity;
    private final int batchSize;
    private final AtomicReference<Subscription> subscription;
    private final AtomicInteger splits;

    private SpliteratorSubscriber(final long timeoutMillis, final int bufferSize, final int batchSize) {
        // reserve 2*bufferSize+1 space in buffer for <=bufferSize elements and bufferSize+1 EOS markers
        final int actualBufferSize = bufferSize * 2 + 1;
        this.buffer = new ArrayBlockingQueue<>(actualBufferSize);
        this.timeoutMillis = timeoutMillis;
        this.batchSize = Math.min(batchSize, bufferSize);
        capacity = bufferSize;
        subscription = new AtomicReference<>();
        splits = new AtomicInteger(1);
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
        checkNotNull(s);
        subscription.getAndUpdate(previousSubscription -> {
            if (previousSubscription != null) {
                s.cancel();
                return previousSubscription;
            } else {
                s.request(batchSize);
                return s;
            }
        });
    }

    @Override
    public void onNext(final T t) {
        buffer.add(Optional.of(checkNotNull(t)));
        request();
    }

    @Override
    public void onError(final Throwable t) {
        addEos();
    }

    @Override
    public void onComplete() {
        addEos();
    }

    private void addEos() {
        // overfill buffer with EOS to prevent concurrent pulling in order to support infinite splitting of this.
        buffer.addAll(Collections.nCopies(capacity + 1, Optional.empty()));
    }

    private void request() {
        final Subscription s = subscription.get();
        if (s != null && capacity - buffer.size() >= batchSize) {
            s.request(batchSize);
        }
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> consumer) {
        final Optional<T> next;
        try {
            next = buffer.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final Exception e) {
            throw new CompletionException(e);
        }
        if (next == null) {
            throw new IllegalStateException("timed out after " + timeoutMillis + " ms");
        } else if (next.isPresent()) {
            consumer.accept(next.get());
            request();
            return true;
        } else {
            // next == Optional.empty() signifying stream termination
            // add EOS back to the buffer in case others are polling
            buffer.add(next);
            return false;
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
}
