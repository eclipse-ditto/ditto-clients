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
package org.eclipse.ditto.client.live.messages;

import java.util.Optional;

/**
 * The MessageSerializerRegistry is responsible for registering and looking up {@link MessageSerializer} for {@code
 * Message} payloads sent via {@link MessageSender} TO/FROM {@code Thing}s or {@code Feature}s.
 *
 * @since 1.0.0
 */
public interface MessageSerializerRegistry {

    /**
     * Registers a MessageSerializer capable of serializing and de-serializing {@code ByteBuffer}s from/to instances of
     * type {@code <T>}.
     *
     * @param messageSerializer the MessageSerializer containing the {@code contentType}, the {@code javaType}, the
     * {@code subject} and the functions to serialize and de-serialize.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @throws MessageSerializationException if there already is a MessageSerializer registered for that specific {@code
     * contentType}, {@code javaType} and {@code subject} combination.
     */
    <T> void registerMessageSerializer(MessageSerializer<T> messageSerializer);

    /**
     * Un-Registers the passed in MessageSerializer.
     *
     * @param messageSerializer the MessageSerializer to un-register.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     */
    <T> void unregisterMessageSerializer(MessageSerializer<T> messageSerializer);

    /**
     * Determines whether for the {@code contentType}, {@code javaType} and {@code subject} combination of the given
     * {@code key} a {@code MessageSerializer} is registered.
     *
     * @param key the MessageSerializerKey containing the {@code contentType}, the {@code javaType} and {@code
     * subject}.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return whether the given {@code messageSerializer} is already registered.
     */
    <T> boolean containsMessageSerializerFor(MessageSerializerKey<T> key);

    /**
     * Determines whether for the given {@code contentType}, {@code javaType} and {@code subject} combination a {@code
     * MessageSerializer} is registered.
     *
     * @param contentType the content-type to look for.
     * @param javaType the java-type to look for.
     * @param subject the subject to look for.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return whether a {@code messageSerializer} is already registered for the given combination.
     */
    <T> boolean containsMessageSerializerFor(String contentType, Class<T> javaType, String subject);

    /**
     * Determines whether for the given {@code javaType} and {@code subject} combination a {@code MessageSerializer} is
     * registered.
     *
     * @param javaType the java-type to look for.
     * @param subject the subject to look for.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return whether a {@code messageSerializer} is already registered for the given combination.
     */
    <T> boolean containsMessageSerializerFor(Class<T> javaType, String subject);

    /**
     * Determines whether for the given {@code javaType} a {@code MessageSerializer} is registered.
     *
     * @param javaType the java-type to look for.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return whether a {@code messageSerializer} is already registered for the given combination.
     */
    <T> boolean containsMessageSerializerFor(Class<T> javaType);

    /**
     * Finds the MessageSerializer which should be used for serializing payloads of the given {@code
     * MessageSerializerKey}.
     * <p>
     * If no MessageSerializer is registered with the specified subject, a fallback lookup is made for a
     * MessageSerializer with the same {@code contentType} and {@code javaType} but with wildcard "{@code *}" {@code
     * subject} instead.
     * </p>
     *
     * @param key the MessageSerializerKey containing the {@code contentType}, the {@code javaType} and {@code
     * subject}.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return an Optional of the MessageSerializer if it could be found.
     */
    <T> Optional<MessageSerializer<T>> findSerializerFor(MessageSerializerKey<T> key);

    /**
     * Finds the MessageSerializer which should be used for serializing payloads of the given {@code contentType},
     * {@code javaType} and {@code subject} combination.
     * <p>
     * If no MessageSerializer is registered with the specified subject, a fallback lookup is made for a
     * MessageSerializer with the same {@code contentType} and {@code javaType} but with wildcard "{@code *}" {@code
     * subject} instead.
     * </p>
     *
     * @param contentType the Content-Type to find the MessageSerializer for.
     * @param javaType the Java-Type the MessageSerializer handles.
     * @param subject the subject for which a specialized MessageSerializer was registered.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return an Optional of the MessageSerializer if it could be found.
     */
    <T> Optional<MessageSerializer<T>> findSerializerFor(String contentType, Class<T> javaType, String subject);

    /**
     * Finds the MessageSerializer which should be used for serializing payloads of the given {@code contentType},
     * {@code javaType} combination.
     *
     * @param contentType the Content-Type to find the MessageSerializer for.
     * @param javaType the Java-Type the MessageSerializer handles.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return an Optional of the MessageSerializer if it could be found.
     */
    <T> Optional<MessageSerializer<T>> findSerializerFor(String contentType, Class<T> javaType);

    /**
     * Finds the MessageSerializer which should be used for serializing payloads of the given {@code javaType}, {@code
     * subject} combination.
     *
     * @param javaType the Java-Type the MessageSerializer handles.
     * @param subject the subject for which a specialized MessageSerializer was registered.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return an Optional of the MessageSerializer if it could be found.
     */
    <T> Optional<MessageSerializer<T>> findSerializerFor(Class<T> javaType, String subject);

    /**
     * Finds the MessageSerializer which should be used for serializing payloads of the given {@code javaType}
     *
     * @param javaType the Java-Type the MessageSerializer handles.
     * @param <T> the type of the payload of the messages the MessageSerializer handles.
     * @return an Optional of the MessageSerializer if it could be found.
     */
    <T> Optional<MessageSerializer<T>> findSerializerFor(Class<T> javaType);

    /**
     * Finds the MessageSerializerKey for the given {@code javaType}, {@code subject} combination if a MessageSerializer
     * for those is registered.
     *
     * @param javaType the Java-Type of the MessageSerializerKey.
     * @param subject the subject of the MessageSerializerKey.
     * @param <T> the type of the payload the MessageSerializer registered with the Key to find handles.
     * @return an Optional of the MessageSerializerKey if it could be found.
     */
    <T> Optional<MessageSerializerKey<T>> findKeyFor(Class<T> javaType, String subject);

    /**
     * Finds the MessageSerializerKey for the given {@code javaType} if a MessageSerializer for this type is
     * registered.
     *
     * @param javaType the Java-Type of the MessageSerializerKey.
     * @param <T> the type of the payload the MessageSerializer registered with the Key to find handles.
     * @return an Optional of the MessageSerializerKey if it could be found.
     */
    <T> Optional<MessageSerializerKey<T>> findKeyFor(Class<T> javaType);
}
