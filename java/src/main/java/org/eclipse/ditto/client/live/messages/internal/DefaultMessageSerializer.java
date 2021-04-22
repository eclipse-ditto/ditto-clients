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

import static org.eclipse.ditto.base.model.common.ConditionChecker.argumentNotNull;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.BiFunction;

import org.eclipse.ditto.client.live.messages.MessageSerializer;
import org.eclipse.ditto.client.live.messages.MessageSerializerKey;

/**
 * Implementation of {@code MessageSerializer}.
 *
 * @param <T> the type of the payload the MessageSerializer handles.
 * @since 1.0.0
 */
public final class DefaultMessageSerializer<T> implements MessageSerializer<T> {

    private final MessageSerializerKey<T> key;
    private final BiFunction<T, Charset, ByteBuffer> serializer;
    private final BiFunction<ByteBuffer, Charset, T> deserializer;

    private DefaultMessageSerializer(final MessageSerializerKey<T> key,
            final BiFunction<T, Charset, ByteBuffer> serializer,
            final BiFunction<ByteBuffer, Charset, T> deserializer) {
        this.key = argumentNotNull(key, "key");
        this.serializer = argumentNotNull(serializer, "serializer");
        this.deserializer = argumentNotNull(deserializer, "deserializer");
    }

    /**
     * Constructs a new {@code MessageSerializer} for the given {@code contentType}, {@code javaType} and {@code
     * subject} with the given {@code BiFunction}s to serialize and de-serialize. <p> Use this constructor if the
     * registered MessageSerializer should only handle payloads of one specific {@code Message} {@code subject} in
     * combination with the given {@code contentType} and {@code javaType}. </p>
     *
     * @param key the Key of the MessageSerializer containing {@code contentType} , {@code javaType} and {@code
     * subject}.
     * @param serializer the BiFunction used for serialization.
     * @param deserializer the BiFunction used for de-serialization.
     * @param <T> the type of the payload the MessageSerializer handles.
     * @return the new MessageSerializer instance.
     */
    public static <T> MessageSerializer<T> of(final MessageSerializerKey<T> key,
            final BiFunction<T, Charset, ByteBuffer> serializer,
            final BiFunction<ByteBuffer, Charset, T> deserializer) {
        return new DefaultMessageSerializer<>(key, serializer, deserializer);
    }

    @Override
    public MessageSerializerKey<T> getKey() {
        return key;
    }

    @Override
    public BiFunction<T, Charset, ByteBuffer> getSerializer() {
        return serializer;
    }

    @Override
    public BiFunction<ByteBuffer, Charset, T> getDeserializer() {
        return deserializer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultMessageSerializer<?> that = (DefaultMessageSerializer<?>) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(serializer, that.serializer) &&
                Objects.equals(deserializer, that.deserializer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, serializer, deserializer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "key=" + key +
                ", serializer=" + serializer +
                ", deserializer=" + deserializer +
                "]";
    }
}
