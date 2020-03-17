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
package org.eclipse.ditto.client.live.messages.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.ditto.client.live.messages.MessageSerializationException;
import org.eclipse.ditto.client.live.messages.MessageSerializer;
import org.eclipse.ditto.client.live.messages.MessageSerializerKey;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@code MessageSerializerRegistry} which manages and finds {@link MessageSerializer}s.
 *
 * @since 1.0.0
 */
public final class DefaultMessageSerializerRegistry implements MessageSerializerRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageSerializerRegistry.class);

    private final Map<MessageSerializerKey<?>, MessageSerializer<?>> serializers;

    /**
     * Constructs a new {@code SerializerRegistryImpl}.
     */
    public DefaultMessageSerializerRegistry() {
        serializers = new ConcurrentHashMap<>();
    }

    @Override
    public <T> void registerMessageSerializer(final MessageSerializer<T> messageSerializer) {
        final MessageSerializerKey<T> key = messageSerializer.getKey();
        if (serializers.containsKey(key)) {
            throw new MessageSerializationException("Serializer for combination '" + key + "' already registered. " +
                    "Unregister first if you intend to overwrite the existing one.");
        } else {
            serializers.put(key, messageSerializer);
        }
    }

    @Override
    public <T> void unregisterMessageSerializer(final MessageSerializer<T> messageSerializer) {
        serializers.remove(messageSerializer.getKey());
    }

    @Override
    public <T> boolean containsMessageSerializerFor(final MessageSerializerKey<T> key) {
        return serializers.containsKey(key);
    }

    @Override
    public <T> boolean containsMessageSerializerFor(final String contentType, final Class<T> javaType,
            final String subject) {
        return serializers.containsKey(ImmutableMessageSerializerKey.of(contentType, javaType, subject));
    }

    @Override
    public <T> boolean containsMessageSerializerFor(final Class<T> javaType, final String subject) {
        return getSerializerKeysForJavaTypeAndSubject(javaType, subject).anyMatch(key -> true);
    }

    @Override
    public <T> boolean containsMessageSerializerFor(final Class<T> javaType) {
        return getSerializerKeysForJavaTypeAndSubject(javaType, MessageSerializerKey.SUBJECT_WILDCARD).anyMatch(
                key -> true);
    }

    @Override
    public <T> Optional<MessageSerializer<T>> findSerializerFor(final MessageSerializerKey<T> key) {
        LOGGER.trace("Finding MessageSerializer for key '{}' ...", key);
        final MessageSerializer<?> foundSerializer = serializers.get(key);
        if (foundSerializer != null) {
            return Optional.of((MessageSerializer<T>) foundSerializer);
        } else {
            final Class<T> javaType = key.getJavaType();
            final Optional<MessageSerializer<T>> fallbackWithSubject = findSerializerFor(javaType, key.getSubject());
            if (fallbackWithSubject.isPresent()) {
                return fallbackWithSubject;
            } else {
                return findSerializerFor(javaType);
            }
        }
    }

    @Override
    public <T> Optional<MessageSerializer<T>> findSerializerFor(final String contentType, final Class<T> javaType,
            final String subject) {
        return findSerializerFor(ImmutableMessageSerializerKey.of(contentType, javaType, subject));
    }

    @Override
    public <T> Optional<MessageSerializer<T>> findSerializerFor(final String contentType, final Class<T> javaType) {
        return findSerializerFor(ImmutableMessageSerializerKey.of(contentType, javaType));
    }

    @Override
    public <T> Optional<MessageSerializer<T>> findSerializerFor(final Class<T> javaType, final String subject) {
        LOGGER.trace("Finding MessageSerializer for type '{}' and subject '{}' ...", javaType, subject);
        return findKeyFor(javaType, subject).map(key -> (MessageSerializer<T>) serializers.get(key));
    }

    @Override
    public <T> Optional<MessageSerializer<T>> findSerializerFor(final Class<T> javaType) {
        return findSerializerFor(javaType, MessageSerializerKey.SUBJECT_WILDCARD);
    }

    @Override
    public <T> Optional<MessageSerializerKey<T>> findKeyFor(final Class<T> javaType, final String subject) {
        LOGGER.trace("Finding MessageSerializerKey for type '{}' and subject '{}' ...", javaType, subject);
        final List<MessageSerializerKey<?>> foundCandidates = getSerializerKeysForJavaTypeAndSubject(javaType, subject)
                .collect(Collectors.toList());
        if (foundCandidates.size() == 1) {
            return Optional.of((MessageSerializerKey<T>) foundCandidates.get(0));
        } else if (foundCandidates.size() == 0) {
            if (!subject.equals(MessageSerializerKey.SUBJECT_WILDCARD)) {
                final Optional<MessageSerializerKey<?>> wildcardKey = getSerializerKeysForJavaTypeAndSubject(javaType,
                        MessageSerializerKey.SUBJECT_WILDCARD).findFirst();
                if (wildcardKey.isPresent()) {
                    return Optional.of((MessageSerializerKey<T>) wildcardKey.get());
                } else {
                    LOGGER.warn("Found no MessageSerializerKey for type '{}' and subject '{}'", javaType,
                            MessageSerializerKey.SUBJECT_WILDCARD);
                    return Optional.empty();
                }
            }
            LOGGER.warn("Found no MessageSerializerKey for type '{}' and subject '{}'", javaType, subject);
            return Optional.empty();
        } else {
            LOGGER.warn(
                    "Found multiple candidates as MessageSerializerKey for type '{}' and subject '{}' - thus returning "
                            + "none: {}", javaType, subject, foundCandidates);
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<MessageSerializerKey<T>> findKeyFor(final Class<T> javaType) {
        return findKeyFor(javaType, MessageSerializerKey.SUBJECT_WILDCARD);
    }

    private <T> Stream<MessageSerializerKey<?>> getSerializerKeysForJavaTypeAndSubject(final Class<T> javaType,
            final String subject) {
        return serializers.keySet()
                .stream()
                .filter(key -> key.getJavaType().isAssignableFrom(javaType))
                .filter(key -> key.getSubject().equals(subject));
    }

}
