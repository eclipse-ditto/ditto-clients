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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.BiFunction;

/**
 * MessageSerializer holds information about which payloads identified by the contained {@link MessageSerializerKey}
 * should be {@link #getSerializer() serialized} and {@link #getDeserializer() deserialized} how.
 *
 * @param <T> the type of the payload the MessageSerializer handles.
 * @since 1.0.0
 */
public interface MessageSerializer<T> {

    /**
     * Returns the key of this MessageSerializer containing the {@code contentType}, {@code javaType} and {@code
     * subject}.
     *
     * @return the key of this MessageSerializer.
     */
    MessageSerializerKey<T> getKey();

    /**
     * Returns the BiFunction used to serialize payloads of the type {@code <T>} with a from the {@code contentType}
     * determined {@code Charset} into a raw {@code ByteBuffer}.
     *
     * @return the BiFunction used to serialize payloads.
     */
    BiFunction<T, Charset, ByteBuffer> getSerializer();

    /**
     * Returns the BiFunction used to de-serialize raw {@code ByteBuffer} data with a from the {@code contentType}
     * determined {@code Charset} into payload of the type {@code <T>} .
     *
     * @return the BiFunction used to de-serialize raw data.
     */
    BiFunction<ByteBuffer, Charset, T> getDeserializer();
}
