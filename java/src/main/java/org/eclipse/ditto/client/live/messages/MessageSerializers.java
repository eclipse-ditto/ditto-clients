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
import java.util.Optional;
import java.util.function.BiFunction;

import org.eclipse.ditto.client.live.messages.internal.ImmutableMessageSerializerKey;
import org.eclipse.ditto.client.live.messages.internal.MessageSerializerImpl;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonValue;

/**
 * This utility class contains the default Serializers used in CR Integration and helps creating new
 * MessageSerializers.
 *
 * @since 1.0.0
 */
public class MessageSerializers {

    /**
     * The Content-type for plain text. By default handled with serializer {@link #textPlainAsString()} and java type
     * {@link String}.
     */
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    /**
     * The Content-type for JSON. By default handled with serializer {@link #applicationJsonAsJsonValue()} and java type
     * {@link JsonValue}.
     */
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    /**
     * The Content-type for byte octet-streams. By default handled with serializer {@link
     * #applicationOctetStreamAsByteBuffer()} and java type {@link ByteBuffer}.
     */
    public static final String CONTENT_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

    private static final String SUBJECT_WILDCARD = MessageSerializerKey.SUBJECT_WILDCARD;

    private MessageSerializers() {
        // no instantiation!
    }

    /**
     * Constructs a new {@code MessageSerializer} for the given {@code contentType}, {@code javaType} and {@code
     * subject} with the given {@code BiFunction}s to serialize and de-serialize. <p> Use this constructor if the
     * registered MessageSerializer should only handle payloads of one specific {@code Message} {@code subject} in
     * combination with the given {@code contentType} and {@code javaType}. </p>
     *
     * @param contentType the content-type the registered MessageSerializer handles.
     * @param javaType the Java type the registered MessageSerializer handles.
     * @param subject the subject the registered MessageSerializer handles.
     * @param serializer the BiFunction used for serialization.
     * @param deserializer the BiFunction used for de-serialization.
     * @param <T> the type of the payload the MessageSerializer handles.
     * @return the new MessageSerializer instance.
     */
    public static <T> MessageSerializer<T> of(final String contentType, final Class<T> javaType, final String subject,
            final BiFunction<T, Charset, ByteBuffer> serializer,
            final BiFunction<ByteBuffer, Charset, T> deserializer) {
        return MessageSerializerImpl.of(ImmutableMessageSerializerKey.of(contentType, javaType, subject),
                serializer, deserializer);
    }

    /**
     * Default MessageSerializer for handling Content-Type "{@value #CONTENT_TYPE_TEXT_PLAIN}" with Java type {@link
     * String} for all subjects ("{@value #SUBJECT_WILDCARD}").
     *
     * @return MessageSerializer for handling Content-Type "{@value #CONTENT_TYPE_TEXT_PLAIN}".
     */
    public static MessageSerializer<String> textPlainAsString() {
        return MessageSerializers.of(CONTENT_TYPE_TEXT_PLAIN, String.class, SUBJECT_WILDCARD,
                (plainString, charset) -> ByteBuffer.wrap(plainString.getBytes(charset)),
                (byteBuffer, charset) -> charset.decode(byteBuffer).toString());
    }

    /**
     * Default MessageSerializer for handling Content-Type "{@value #CONTENT_TYPE_APPLICATION_JSON}" with Java type
     * {@link JsonValue} for all subjects ("{@value #SUBJECT_WILDCARD}").
     *
     * @return MessageSerializer for handling Content-Type "{@value #CONTENT_TYPE_APPLICATION_JSON}".
     */
    public static MessageSerializer<JsonValue> applicationJsonAsJsonValue() {
        return MessageSerializers.of(CONTENT_TYPE_APPLICATION_JSON, JsonValue.class, SUBJECT_WILDCARD,
                (jsonValue, charset) -> {
                    final String jsonString = jsonValue.toString();
                    return ByteBuffer.wrap(jsonString.getBytes(charset));
                },
                (byteBuffer, charset) -> {
                    final String jsonString = charset.decode(byteBuffer).toString();
                    return JsonFactory.readFrom(jsonString);
                });
    }

    /**
     * Default MessageSerializer for handling Content-Type "{@value #CONTENT_TYPE_APPLICATION_OCTET_STREAM}" with Java
     * type {@link ByteBuffer} for all subjects ("{@value #SUBJECT_WILDCARD}").
     *
     * @return MessageSerializer for handling Content-Type "{@value #CONTENT_TYPE_APPLICATION_OCTET_STREAM}".
     */
    public static MessageSerializer<ByteBuffer> applicationOctetStreamAsByteBuffer() {
        return MessageSerializers.of(CONTENT_TYPE_APPLICATION_OCTET_STREAM, ByteBuffer.class, SUBJECT_WILDCARD,
                (byteBuffer, charset) -> byteBuffer,
                (byteBuffer, charset) -> byteBuffer);
    }

    /**
     * Determines from the passed in Optional Content-Type string the "charset" which is defined as follows: "{@code
     * application/json; charset=utf-8}" and tries to instantiate a Java {@link Charset} from that.
     *
     * @param fullContentTypeString the Optional Content-Type string potentially containing a "charset" definition.
     * @return the Optional (if one could be determined) {@link Charset}.
     */
    public static Optional<Charset> determineCharsetFromContentType(final Optional<String> fullContentTypeString) {
        // determine charset, if one was set in the form of:
        // application/json; charset=utf-8
        return fullContentTypeString.filter(ct -> ct.contains(";"))
                .map(ct -> ct.split(";")[1])
                .filter(charsetStr -> charsetStr.contains("="))
                .map(charsetStr -> charsetStr.split("=")[1])
                .filter(Charset::isSupported)
                .map(Charset::forName);
    }
}
