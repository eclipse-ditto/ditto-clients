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
package org.eclipse.ditto.client.live.internal;

import java.nio.ByteBuffer;

import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.MessageSerializers;
import org.eclipse.ditto.client.live.messages.internal.DefaultMessageSerializerRegistry;
import org.eclipse.ditto.json.JsonValue;

/**
 * Contains a {@link MessageSerializerRegistry} which handles custom Serializers and De-Serializers for {@code Message}
 * payloads.
 *
 * @since 1.0.0
 */
public final class MessageSerializerFactory {

    private final MessageSerializerRegistry messageSerializerRegistry;

    private MessageSerializerFactory() {
        messageSerializerRegistry = initializeDefaultSerializerRegistry();
    }

    /**
     * Constructs a new SerializerConfiguration instance pre-configured with the default Serializers defined in {@link
     * #initializeDefaultSerializerRegistry()}.
     *
     * @return a new SerializerConfiguration instance pre-configured with the default Serializers.
     */
    public static MessageSerializerFactory newInstance() {
        return new MessageSerializerFactory();
    }

    /**
     * Returns a MessageSerializerRegistry with the default MessageSerializers initialized:
     * <ul>
     * <li>Content-Type "application/json" -&gt; Java-Type {@link JsonValue}</li>
     * <li>Content-Type "text/plain" -&gt; Java-Type {@link String}</li>
     * <li>Content-Type "application/octet-stream" -&gt; Java-Type {@link ByteBuffer}</li>
     * </ul>
     *
     * @return a MessageSerializerRegistry with the default Serializers initialized.
     */
    public static MessageSerializerRegistry initializeDefaultSerializerRegistry() {
        final DefaultMessageSerializerRegistry serializerRegistry = new DefaultMessageSerializerRegistry();
        serializerRegistry.registerMessageSerializer(MessageSerializers.textPlainAsString());
        serializerRegistry.registerMessageSerializer(MessageSerializers.applicationJsonAsJsonValue());
        serializerRegistry.registerMessageSerializer(MessageSerializers.applicationOctetStreamAsByteBuffer());
        return serializerRegistry;
    }

    /**
     * Returns the configured SerializerRegistry.
     *
     * @return the configured SerializerRegistry.
     */
    public MessageSerializerRegistry getMessageSerializerRegistry() {
        return messageSerializerRegistry;
    }
}
