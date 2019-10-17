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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.BiFunction;

import org.eclipse.ditto.client.exceptions.MessageSerializationException;
import org.eclipse.ditto.client.live.messages.MessageSerializer;
import org.eclipse.ditto.client.live.messages.MessageSerializerKey;
import org.eclipse.ditto.client.live.messages.MessageSerializers;
import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DefaultMessageSerializerRegistry}
 */
@SuppressWarnings("squid:S3655")
public final class DefaultMessageSerializerRegistryTest {

    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";

    private DefaultMessageSerializerRegistry sut;

    @Before
    public void setupBefore() {
        sut = new DefaultMessageSerializerRegistry();
    }

    /**
     *
     */
    @Test(expected = MessageSerializationException.class)
    public void registerMessageSerializerTwiceResultsInException() {
        final MessageSerializerKey<String> key = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class);
        // registering once is ok
        final MessageSerializer<String> messageSerializer =
                DefaultMessageSerializer.of(key, (string, charset) -> ByteBuffer.wrap(string.getBytes(charset)),
                        (byteBuffer, charset) -> new String(byteBuffer.array(), charset));
        sut.registerMessageSerializer(messageSerializer);

        // registering the second time not
        sut.registerMessageSerializer(messageSerializer);
        Assert.fail("Registering a messageSerializer for the twice should have failed!");
    }

    /**
     *
     */
    @Test
    public void registerMessageSerializerAndUnregisterToNewRegister() {
        final MessageSerializerKey<String> key = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class);
        final MessageSerializer<String> messageSerializer =
                DefaultMessageSerializer.of(key, (string, charset) -> ByteBuffer.wrap(string.getBytes(charset)),
                        (byteBuffer, charset) -> new String(byteBuffer.array(), charset));
        sut.registerMessageSerializer(messageSerializer);

        // unregistering should work
        sut.unregisterMessageSerializer(messageSerializer);

        // now registering the second time is also ok
        sut.registerMessageSerializer(messageSerializer);
    }

    /**
     *
     */
    @Test
    public void registerMessageSerializerAndCheckForExistance() {
        final MessageSerializerKey<String> key = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class);
        Assert.assertFalse("Registry should't contain serializer for Key '" + key + "' but did",
                sut.containsMessageSerializerFor(key));

        final MessageSerializer<String> messageSerializer =
                DefaultMessageSerializer.of(key, (string, charset) -> ByteBuffer.wrap(string.getBytes(charset)),
                        (byteBuffer, charset) -> new String(byteBuffer.array(), charset));
        sut.registerMessageSerializer(messageSerializer);
        Assert.assertTrue("Registry should contain serializer for Key '" + key + "' but didn't",
                sut.containsMessageSerializerFor(key));

        Assert.assertTrue("Registry should contain deserializer for Key '" + key + "' but didn't",
                sut.containsMessageSerializerFor(key.getContentType(), key.getJavaType(), key.getSubject()));

        Assert.assertTrue("Registry should contain deserializer for Key '" + key + "' but didn't",
                sut.containsMessageSerializerFor(key.getJavaType(), key.getSubject()));
    }

    /**
     *
     */
    @Test
    public void findMessageSerializerWithDifferentSubjects() {
        final MessageSerializerKey<String>
                key1 = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class, "my.subject.1");
        final BiFunction<String, Charset, ByteBuffer> s1 =
                (string, charset) -> ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
        final BiFunction<ByteBuffer, Charset, String> d1 =
                (byteBuffer, charset) -> new String(byteBuffer.array(), StandardCharsets.UTF_8);
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key1, s1, d1));

        final MessageSerializerKey<String>
                key2 = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class, "my.subject.2");
        final BiFunction<String, Charset, ByteBuffer> s2 =
                (string, charset) -> ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
        final BiFunction<ByteBuffer, Charset, String> d2 =
                (byteBuffer, charset) -> new String(byteBuffer.array(), StandardCharsets.UTF_8);
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key2, s2, d2));


        Assert.assertEquals("Serializer function was not the expected one", s2,
                sut.findSerializerFor(key2).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", d2,
                sut.findSerializerFor(key2).get().getDeserializer());
    }

    /**
     *
     */
    @Test
    public void findMessageSerializerWithDifferentSubjectsAndFallback() {
        final MessageSerializerKey<String> key0 = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class);
        final BiFunction<String, Charset, ByteBuffer> s0 =
                (string, charset) -> ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
        final BiFunction<ByteBuffer, Charset, String> d0 =
                (byteBuffer, charset) -> new String(byteBuffer.array(), StandardCharsets.UTF_8);
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key0, s0, d0));

        final MessageSerializerKey<String>
                key1 = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class, "my.subject.1");
        final BiFunction<String, Charset, ByteBuffer> s1 =
                (string, charset) -> ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
        final BiFunction<ByteBuffer, Charset, String> d1 =
                (byteBuffer, charset) -> new String(byteBuffer.array(), StandardCharsets.UTF_8);
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key1, s1, d1));

        final MessageSerializerKey<String>
                key2 = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class, "my.subject.2");
        final BiFunction<String, Charset, ByteBuffer> s2 =
                (string, charset) -> ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
        final BiFunction<ByteBuffer, Charset, String> d2 =
                (byteBuffer, charset) -> new String(byteBuffer.array(), StandardCharsets.UTF_8);
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key2, s2, d2));

        final MessageSerializerKey<String> unknownSubjectKey =
                ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class,
                        "my.subject.unknown");

        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(unknownSubjectKey).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", d0,
                sut.findSerializerFor(unknownSubjectKey).get().getDeserializer());
        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(String.class).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s2,
                sut.findSerializerFor(String.class, "my.subject.2").get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(TEXT_PLAIN, String.class).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s1,
                sut.findSerializerFor(TEXT_PLAIN, String.class, "my.subject.1").get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor("foo/bar", String.class).get().getSerializer());
    }

    /**
     *
     */
    @Test
    public void findMessageSerializerWithTypeInheritance() {
        final MessageSerializerKey<JsonValue> key0 =
                ImmutableMessageSerializerKey.of(APPLICATION_JSON, JsonValue.class);
        final BiFunction<JsonValue, Charset, ByteBuffer> s0 = (jsonValue, charset) -> {
            final String jsonString = jsonValue.toString();
            return ByteBuffer.wrap(jsonString.getBytes(charset));
        };
        final BiFunction<ByteBuffer, Charset, JsonValue> d0 = (byteBuffer, charset) -> {
            final String jsonString = charset.decode(byteBuffer).toString();
            return JsonFactory.readFrom(jsonString);
        };
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key0, s0, d0));

        final MessageSerializerKey<JsonValue> unknownSubjectKey =
                ImmutableMessageSerializerKey.of(APPLICATION_JSON, JsonValue.class,
                        "my.subject.unknown");

        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(unknownSubjectKey).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(JsonValue.class).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(JsonObject.class).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(JsonArray.class).get().getSerializer());
    }

    /**
     *
     */
    @Test
    public void findMessageSerializerKey() {
        sut.registerMessageSerializer(MessageSerializers.textPlainAsString());
        sut.registerMessageSerializer(MessageSerializers.applicationJsonAsJsonValue());
        sut.registerMessageSerializer(MessageSerializers.applicationOctetStreamAsByteBuffer());

        Assert.assertEquals("Determined content-type was not the expected one",
                MessageSerializers.CONTENT_TYPE_APPLICATION_JSON,
                sut.findKeyFor(JsonValue.class).get().getContentType());
        Assert.assertEquals("Determined content-type was not the expected one",
                MessageSerializers.CONTENT_TYPE_APPLICATION_JSON,
                sut.findKeyFor(JsonArray.class, "my.subject").get().getContentType());
        Assert.assertEquals("Determined content-type was not the expected one",
                MessageSerializers.CONTENT_TYPE_TEXT_PLAIN,
                sut.findKeyFor(String.class).get().getContentType());
        Assert.assertEquals("Determined content-type was not the expected one",
                MessageSerializers.CONTENT_TYPE_APPLICATION_OCTET_STREAM,
                sut.findKeyFor(ByteBuffer.class).get().getContentType());
    }

    /**
     *
     */
    @Test
    public void findUnkownMessageSerializerKey() {
        sut.registerMessageSerializer(MessageSerializers.textPlainAsString());

        Assert.assertFalse("Key for unknown java-type could be determined but shouldn't",
                sut.findKeyFor(Date.class).isPresent());
        Assert.assertFalse("Key for unknown java-type could be determined but shouldn't",
                sut.findKeyFor(JsonValue.class).isPresent());
    }

    /**
     *
     */
    @Test
    public void registerMultipleSerialzersForSameContentType() {
        final MessageSerializerKey<JsonValue> key0 =
                ImmutableMessageSerializerKey.of(APPLICATION_JSON, JsonValue.class);
        final BiFunction<JsonValue, Charset, ByteBuffer> s0 = (jsonValue, charset) -> {
            final String jsonString = jsonValue.toString();
            return ByteBuffer.wrap(jsonString.getBytes(charset));
        };
        final BiFunction<ByteBuffer, Charset, JsonValue> d0 = (byteBuffer, charset) -> {
            final String jsonString = charset.decode(byteBuffer).toString();
            return JsonFactory.readFrom(jsonString);
        };
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key0, s0, d0));

        final MessageSerializerKey<String> key1 = ImmutableMessageSerializerKey.of(APPLICATION_JSON, String.class);
        final BiFunction<String, Charset, ByteBuffer> s1 = (jsonValueString, charset) ->
                ByteBuffer.wrap(jsonValueString.getBytes(charset));
        final BiFunction<ByteBuffer, Charset, String> d1 = (byteBuffer, charset) -> {
            final String jsonString = charset.decode(byteBuffer).toString();
            return JsonFactory.readFrom(jsonString).toString();
        };
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key1, s1, d1));


        Assert.assertEquals("Serializer function was not the expected one", s0,
                sut.findSerializerFor(APPLICATION_JSON, JsonValue.class).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s1,
                sut.findSerializerFor(APPLICATION_JSON, String.class).get().getSerializer());
        Assert.assertEquals("Determined content-type was not the expected one", APPLICATION_JSON,
                sut.findKeyFor(JsonValue.class).get().getContentType());
        Assert.assertEquals("Determined content-type was not the expected one", APPLICATION_JSON,
                sut.findKeyFor(String.class).get().getContentType());


        final MessageSerializerKey<String> key2 =
                ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class, "plain.subject");
        final BiFunction<String, Charset, ByteBuffer> s2 =
                (string, charset) -> ByteBuffer.wrap(string.getBytes(charset));
        final BiFunction<ByteBuffer, Charset, String> d2 =
                (byteBuffer, charset) -> charset.decode(byteBuffer).toString();
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key2, s2, d2));

        Assert.assertEquals("Serializer function was not the expected one", s1,
                sut.findSerializerFor(APPLICATION_JSON, String.class).get().getSerializer());
        Assert.assertEquals("Serializer function was not the expected one", s2,
                sut.findSerializerFor(APPLICATION_JSON, String.class, "plain.subject").get().getSerializer());

        final MessageSerializerKey<String> key3 = ImmutableMessageSerializerKey.of(TEXT_PLAIN, String.class);
        final BiFunction<String, Charset, ByteBuffer> s3 =
                (string, charset) -> ByteBuffer.wrap(string.getBytes(charset));
        final BiFunction<ByteBuffer, Charset, String> d3 =
                (byteBuffer, charset) -> charset.decode(byteBuffer).toString();
        sut.registerMessageSerializer(DefaultMessageSerializer.of(key3, s3, d3));

        Assert.assertFalse("Key for java-type 'String' could be determined but shouldn't as multiple Serializers are "
                + "registered for it.", sut.findKeyFor(String.class).isPresent());
    }
}
