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
package org.eclipse.ditto.client.internal.bus;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonParseException;
import org.eclipse.ditto.json.JsonRuntimeException;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DefaultAdaptableBus implements AdaptableBus {

    private static final String ACK_SUFFIX = ":ACK";
    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptableBus.class);

    private final ScheduledExecutorService scheduledExecutorService;
    private final Collection<Classifier<String>> stringClassifiers;
    private final Collection<Classifier<Adaptable>> adaptableClassifiers;

    private final Map<Object, Set<Entry<Consumer<String>>>> oneTimeStringConsumers;
    private final Map<Object, Set<Entry<Consumer<Adaptable>>>> oneTimeAdaptableConsumers;
    private final Map<Object, Set<Entry<Consumer<Adaptable>>>> persistentAdaptableConsumers;

    DefaultAdaptableBus(final ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
        stringClassifiers = new ConcurrentLinkedQueue<>();
        adaptableClassifiers = new ConcurrentLinkedQueue<>();
        oneTimeStringConsumers = new ConcurrentHashMap<>();
        oneTimeAdaptableConsumers = new ConcurrentHashMap<>();
        persistentAdaptableConsumers = new ConcurrentHashMap<>();
    }

    @Override
    public AdaptableBus addStringClassifier(final Classifier<String> classifier) {
        stringClassifiers.add(classifier);
        return this;
    }

    @Override
    public AdaptableBus addAdaptableClassifier(final Classifier<Adaptable> adaptableClassifier) {
        adaptableClassifiers.add(adaptableClassifier);
        return this;
    }

    @Override
    public CompletionStage<String> subscribeOnceForString(final Object tag, final Duration timeout) {
        return subscribeOnce(oneTimeStringConsumers, tag, timeout);
    }

    @Override
    public CompletionStage<Adaptable> subscribeOnceForAdaptable(final Object tag, final Duration timeout) {
        return subscribeOnce(oneTimeAdaptableConsumers, tag, timeout);
    }

    @Override
    public SubscriptionId subscribeForAdaptable(final Object tag, final Consumer<Adaptable> adaptableConsumer) {
        final Entry<Consumer<Adaptable>> entry = new Entry<>(tag, adaptableConsumer);
        addEntry(persistentAdaptableConsumers, entry);
        return entry;
    }

    @Override
    public SubscriptionId subscribeForAdaptableWithTimeout(final Object tag, final Duration timeout,
            final Consumer<Adaptable> adaptableConsumer, final Predicate<Adaptable> terminationPredicate) {
        final CompletableFuture<Adaptable> terminationFuture = new CompletableFuture<>();
        final AtomicReference<Instant> lastMessage = new AtomicReference<>(Instant.now());
        final Entry<Consumer<Adaptable>> entry = new Entry<>(
                tag,
                withTermination(adaptableConsumer, terminationPredicate, terminationFuture, lastMessage)
        );
        addEntry(persistentAdaptableConsumers, entry);
        removeAfterIdle(persistentAdaptableConsumers, entry, timeout, terminationFuture, lastMessage);
        terminationFuture.thenAccept(terminated ->
                // removed due to termination message
                removeEntry(persistentAdaptableConsumers, entry, () -> {}));
        return entry;
    }

    @Override
    public boolean unsubscribe(@Nullable final SubscriptionId subscriptionId) {
        if (subscriptionId != null) {
            final AtomicBoolean removed = new AtomicBoolean(false);
            if (subscriptionId instanceof Entry) {
                removeEntry(persistentAdaptableConsumers, (Entry<?>) subscriptionId, () -> removed.set(true));
            }
            return removed.get();
        } else {
            return false;
        }
    }

    @Override
    public void publish(final String message) {
        if (publishToOneTimeStringSubscribers(message)) {
            return;
        }
        if (message.endsWith(ACK_SUFFIX)) {
            LOGGER.trace("Client got acknowledgement for which there is no subscriber: {}", message);
        } else {
            final Optional<Adaptable> adaptableOptional = parseAsAdaptable(message);
            if (adaptableOptional.isPresent()) {
                final Adaptable adaptable = adaptableOptional.get();
                final List<Object> tags = getAllAdaptableTags(adaptable);
                if (publishToOneTimeAdaptableSubscribers(adaptable, tags) ||
                        publishToPersistentAdaptableSubscribers(adaptable, tags)) {
                    return;
                }
            }
            LOGGER.warn("Client got unhandled message: {}", message);
        }
    }

    private Consumer<Adaptable> withTermination(
            final Consumer<Adaptable> adaptableConsumer,
            final Predicate<Adaptable> terminationPredicate,
            final CompletableFuture<Adaptable> terminationFuture,
            final AtomicReference<Instant> lastMessage) {

        return message -> {
            if (terminationPredicate.test(message)) {
                // termination by message
                terminationFuture.complete(message);
            } else {
                // not terminated
                lastMessage.set(Instant.now());
                adaptableConsumer.accept(message);
            }
        };
    }

    private <T> CompletionStage<T> subscribeOnce(
            final Map<Object, Set<Entry<Consumer<T>>>> registry,
            final Object tag,
            final Duration timeout) {
        final CompletableFuture<T> resultFuture = new CompletableFuture<>();
        final Entry<Consumer<T>> subscriber = new Entry<>(tag, resultFuture::complete);
        addEntry(registry, subscriber);
        removeAfter(registry, subscriber, timeout, resultFuture);
        return resultFuture;
    }

    private boolean publishToOneTimeStringSubscribers(final String message) {
        for (final Classifier<String> stringClassifier : stringClassifiers) {
            final Optional<Object> tag = stringClassifier.classify(message);
            if (tag.isPresent()) {
                final Consumer<String> subscriber = removeOne(oneTimeStringConsumers, tag.get());
                if (subscriber != null) {
                    subscriber.accept(message);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean publishToOneTimeAdaptableSubscribers(final Adaptable adaptable, final List<Object> tags) {
        for (final Object tag : tags) {
            final Consumer<Adaptable> oneTimeSubscriber = removeOne(oneTimeAdaptableConsumers, tag);
            if (oneTimeSubscriber != null) {
                oneTimeSubscriber.accept(adaptable);
                return true;
            }
        }
        return false;
    }

    private boolean publishToPersistentAdaptableSubscribers(final Adaptable adaptable, final List<Object> tags) {
        boolean publishedToPersistentSubscribers = false;
        for (final Object tag : tags) {
            final Set<Entry<Consumer<Adaptable>>> persistentConsumers = persistentAdaptableConsumers.get(tag);
            if (persistentConsumers != null && !persistentConsumers.isEmpty()) {
                publishedToPersistentSubscribers = true;
                for (final Entry<Consumer<Adaptable>> entry : persistentConsumers) {
                    entry.value.accept(adaptable);
                }
            }
        }
        return publishedToPersistentSubscribers;
    }

    private List<Object> getAllAdaptableTags(final Adaptable adaptable) {
        return adaptableClassifiers.stream()
                .flatMap(classifier -> classifier.classify(adaptable).map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());
    }

    private <T> void removeAfter(final Map<Object, Set<Entry<T>>> registry,
            final Entry<T> entry,
            final Duration after,
            final CompletableFuture<?> futureToFail) {
        final Runnable cancellationRunnable = () ->
                removeEntry(registry, entry, () -> futureToFail.completeExceptionally(timeout(after)));
        scheduledExecutorService.schedule(cancellationRunnable, after.toMillis(), TimeUnit.MILLISECONDS);
    }

    private <T> void removeAfterIdle(
            final Map<Object, Set<Entry<T>>> registry,
            final Entry<T> entry,
            final Duration timeout,
            final CompletableFuture<Adaptable> terminationFuture,
            final AtomicReference<Instant> lastMessage) {

        final Runnable cancellationRunnable = () -> {
            if (timeout.minus(Duration.between(lastMessage.get(), Instant.now())).isNegative()) {
                // timeout reached; fail with idle timeout
                removeEntry(registry, entry, () -> terminationFuture.completeExceptionally(timeout(timeout)));
            } else {
                // timeout not reached; re-submit.
                removeAfterIdle(registry, entry, timeout, terminationFuture, lastMessage);
            }
        };
        scheduledExecutorService.schedule(cancellationRunnable, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static <T> void addEntry(final Map<Object, Set<Entry<T>>> registry, final Entry<T> entry) {
        registry.compute(entry.key, (key, previousSet) -> {
            final Set<Entry<T>> concurrentHashSet =
                    previousSet != null ? previousSet : ConcurrentHashMap.newKeySet();
            concurrentHashSet.add(entry);
            return concurrentHashSet;
        });
    }

    private Optional<Adaptable> parseAsAdaptable(final String message) {
        try {
            final JsonObject jsonObject = JsonObject.of(message);
            return Optional.of(ProtocolFactory.jsonifiableAdaptableFromJson(jsonObject));
        } catch (final JsonParseException e) {
            final String msgPattern = "Client got unknown non-JSON message: {}";
            LOGGER.warn(msgPattern, message, e);
        } catch (final JsonRuntimeException e) {
            final String msgPattern = "Client could not understand incoming JSON due to: <{}>:\n  <{}>";
            LOGGER.warn(msgPattern, e.getMessage(), message);
        }
        return Optional.empty();
    }

    private static <T> void removeEntry(final Map<Object, Set<Entry<T>>> registry, final Entry<?> entry,
            final Runnable onRemove) {
        registry.computeIfPresent(entry.key, (key, set) -> {
            if (set.remove(entry)) {
                onRemove.run();
            }
            return set.isEmpty() ? null : set;
        });
    }

    @Nullable
    private static <T> T removeOne(final Map<Object, Set<Entry<T>>> registry, final Object tag) {
        final AtomicReference<T> result = new AtomicReference<>(null);
        registry.computeIfPresent(tag, (k, set) -> set.stream()
                .findAny()
                .map(entry -> {
                    if (set.remove(entry)) {
                        result.set(entry.value);
                    }
                    return set.isEmpty() ? null : set;
                })
                .orElse(null));
        return result.get();
    }

    private static Throwable timeout(final Duration duration) {
        return new TimeoutException("Timed out after " + duration);
    }

    /**
     * Similar to Map.Entry but with object reference identity and fixed key type to act as identifier for
     * a subscription.
     */
    private static final class Entry<T> implements SubscriptionId {

        private final Object key;
        private final T value;

        private Entry(final Object key, final T value) {
            this.key = key;
            this.value = value;
        }
    }
}
