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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.messages.MessageSerializerKey;

/**
 * Immutable implementation of {@code MessageSerializerKey}.
 *
 * @param <T> the type of the payload a MessageSerializer registered with this Key handles.
 * @since 1.0.0
 */
@Immutable
public final class ImmutableMessageSerializerKey<T> implements MessageSerializerKey<T> {

    private final String contentType;
    private final Class<T> javaType;
    private final String subject;

    private ImmutableMessageSerializerKey(final String contentType, final Class<T> javaType, final String subject) {
        this.contentType = argumentNotNull(contentType, "contentType");
        this.javaType = argumentNotNull(javaType, "javaType");
        this.subject = argumentNotNull(subject, "subject");
    }

    private ImmutableMessageSerializerKey(final Class<T> javaType, final String subject) {
        this("", javaType, subject);
    }

    /**
     * Constructs a new {@code MessageSerializerKey} for the given {@code contentType}, {@code javaType} and {@code
     * subject}.
     * <p>
     * Use this constructor if the MessageSerializer registered with the returned MessageSerializerKey should only
     * handle payloads of one specific {@code Message} {@code subject} in combination with the given {@code contentType}
     * and {@code javaType}.
     * </p>
     *
     * @param contentType the content-type a MessageSerializer registered for the MessageSerializerKey handles.
     * @param javaType the Java type a MessageSerializer registered for the MessageSerializerKey handles.
     * @param subject the subject a MessageSerializer registered for the MessageSerializerKey handles.
     * @param <T> the type of the payload the MessageSerializer registered for the MessageSerializerKey handles.
     * @return the new MessageSerializerKey instance.
     */
    public static <T> MessageSerializerKey<T> of(final String contentType, final Class<T> javaType,
            final String subject) {
        return new ImmutableMessageSerializerKey<>(contentType, javaType, subject);
    }

    /**
     * Constructs a new {@code MessageSerializerKey} for the given {@code contentType} and {@code javaType}.
     * <p>
     * Use this constructor if the MessageSerializer registered with the returned MessageSerializerKey should handle all
     * {@code Message} payloads {@code contentType}, {@code javaType} combination, not regarding the {@code subject}.
     * </p>
     *
     * @param contentType the content-type a MessageSerializer registered for the MessageSerializerKey handles.
     * @param javaType the Java type a MessageSerializer registered for the MessageSerializerKey handles.
     * @param <T> the type of the payload the MessageSerializer registered for the MessageSerializerKey handles.
     * @return the new MessageSerializerKey instance.
     */
    public static <T> MessageSerializerKey<T> of(final String contentType, final Class<T> javaType) {
        return new ImmutableMessageSerializerKey<>(contentType, javaType, SUBJECT_WILDCARD);
    }

    /**
     * Constructs a new {@code MessageSerializerKey} for the given {@code javaType} and {@code subject}.
     * <p>
     * Use this constructor if the MessageSerializer registered with the returned MessageSerializerKey should handle all
     * {@code Message} payloads of one specific {@code Message} {@code subject} in combination with the given {@code
     * javaType}.
     * </p>
     *
     * @param javaType the Java type a MessageSerializer registered for the MessageSerializerKey handles.
     * @param subject the subject a MessageSerializer registered for the MessageSerializerKey handles.
     * @param <T> the type of the payload the MessageSerializer registered for the MessageSerializerKey handles.
     * @return the new MessageSerializerKey instance.
     */
    public static <T> MessageSerializerKey<T> of(final Class<T> javaType, final String subject) {
        return new ImmutableMessageSerializerKey<>(javaType, subject);
    }

    /**
     * Constructs a new {@code MessageSerializerKey} for the given {@code javaType}.
     * <p>
     * Use this constructor if the MessageSerializer registered with the returned MessageSerializerKey should handle all
     * {@code Message} payloads of one {@code javaType}.
     * </p>
     *
     * @param javaType the Java type a MessageSerializer registered for the MessageSerializerKey handles.
     * @param <T> the type of the payload the MessageSerializer registered for the MessageSerializerKey handles.
     * @return the new MessageSerializerKey instance.
     */
    public static <T> MessageSerializerKey<T> of(final Class<T> javaType) {
        return new ImmutableMessageSerializerKey<>(javaType, SUBJECT_WILDCARD);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableMessageSerializerKey<?> that = (ImmutableMessageSerializerKey<?>) o;
        return Objects.equals(contentType, that.contentType) &&
                Objects.equals(javaType, that.javaType) &&
                Objects.equals(subject, that.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, javaType, subject);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "contentType=" + contentType +
                ", javaType=" + javaType +
                ", subject=" + subject +
                "]";
    }
}
