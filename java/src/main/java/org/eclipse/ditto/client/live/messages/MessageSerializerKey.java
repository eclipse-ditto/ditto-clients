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

/**
 * Lookup type for registering custom {@code Message} payload Serializers and De-Serializers at the {@link
 * MessageSerializerRegistry}. Must always at least contain a {@code contentType} and a {@code javaType}, the {@code
 * subject} is Optional.
 *
 * @param <T> the type of the payload a MessageSerializer registered with this Key handles.
 * @since 1.0.0
 */
public interface MessageSerializerKey<T> {

    /**
     * The Wildcard for Subjects.
     */
    String SUBJECT_WILDCARD = "*";

    /**
     * Returns the content-type of this Key, e.g. "{@code application/json}" or "{@code text/plain}".
     *
     * @return the content-type of this Key.
     */
    String getContentType();

    /**
     * Returns the Java type a Serializer or De-Serializer registered with this Key handles, e.g. {@code
     * JsonValue.class} or {@code String.class}.
     *
     * @return the Java type a Serializer or De-Serializer registered with this Key handles.
     */
    Class<T> getJavaType();

    /**
     * Returns the subject for which a Serializer or De-Serializer is registered. If this is the {@link
     * #SUBJECT_WILDCARD} "{@value #SUBJECT_WILDCARD}", the Serializer or De-Serializer handles all subjects for the
     * given content-type and Java type combination.
     *
     * @return the optional subject for which a Serializer or De-Serializer is registered.
     */
    String getSubject();
}
