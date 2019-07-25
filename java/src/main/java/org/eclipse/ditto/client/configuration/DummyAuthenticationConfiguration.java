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
package org.eclipse.ditto.client.configuration;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

/**
 * An implementation for {@link AuthenticationConfiguration} based on "Ditto Dummy authentication" used for local
 * development and testing.
 *
 * @since 1.0.0
 */
public final class DummyAuthenticationConfiguration implements AuthenticationConfiguration {

    private final String sessionId;
    private final String dummyUsername;
    private final Map<String, String> additionalHeaders;

    private DummyAuthenticationConfiguration(final String dummyUsername,
            final Map<String, String> additionalHeaders) {
        sessionId = UUID.randomUUID().toString();
        this.dummyUsername = dummyUsername;
        this.additionalHeaders = Collections.unmodifiableMap(new HashMap<>(additionalHeaders));
    }

    /**
     * @return a new builder ready to build a {@code AuthenticationConfiguration}
     */
    public static DummyAuthenticationConfigurationBuilder newBuilder() {
        return new Builder();
    }

    @Override
    public String getClientSessionId() {
        return dummyUsername + ":" + sessionId;
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    /**
     * Returns the dummy username.
     *
     * @return the dummy username.
     */
    public String getDummyUsername() {
        return dummyUsername;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DummyAuthenticationConfiguration that = (DummyAuthenticationConfiguration) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(dummyUsername, that.dummyUsername) &&
                Objects.equals(additionalHeaders, that.additionalHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, dummyUsername, additionalHeaders);
    }

    @SuppressWarnings("squid:S2068")
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "sessionId=" + sessionId +
                ", dummyUsername=" + dummyUsername +
                ", additionalHeaders=" + additionalHeaders +
                "]";
    }

    /**
     * Builder for dummy based {@link AuthenticationConfiguration}.
     */
    public interface DummyAuthenticationConfigurationBuilder extends DummyUsernameSettable {
        // no-op
    }

    /**
     * Allows setting the dummy username.
     */
    public interface DummyUsernameSettable {

        /**
         * Sets the dummyUsername to authenticate.
         *
         * @param dummyUsername the dummyUsername to authenticate
         * @return the builder object that allows setting the password
         */
        Buildable dummyUsername(String dummyUsername);
    }

    /**
     * Allows building the {@link DummyAuthenticationConfiguration} instance as a final step.
     */
    public interface Buildable {

        /**
         * Adds an additional header to be used during dummy authentication.
         *
         * @param key the key of the additional header.
         * @param value the header's value.
         * @return the buildable.
         */
        Buildable withAdditionalHeader(String key, String value);

        /**
         * Build the dummy authentication configuration.
         *
         * @return the created DummyAuthenticationConfiguration
         */
        DummyAuthenticationConfiguration build();
    }

    @Immutable
    private static class Builder implements DummyAuthenticationConfigurationBuilder, DummyUsernameSettable, Buildable {

        private String dummyUsername;
        private final Map<String, String> additionalHeaders = new HashMap<>();

        @Override
        public Buildable dummyUsername(final String dummyUsername) {
            this.dummyUsername = requireNonNull(dummyUsername);
            return this;
        }

        @Override
        public Buildable withAdditionalHeader(final String key, final String value) {
            additionalHeaders.put(key, value);
            return this;
        }

        @Override
        public DummyAuthenticationConfiguration build() {
            return new DummyAuthenticationConfiguration(dummyUsername, additionalHeaders);
        }

    }
}
