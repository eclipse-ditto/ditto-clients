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
 * An implementation for {@link AuthenticationConfiguration} based on {@code Basic Authentication} username/password
 * authentication.
 *
 * @since 1.0.0
 */
public final class CredentialsAuthenticationConfiguration implements AuthenticationConfiguration {

    private final String sessionId;
    private final String username;
    private final String password;
    private final Map<String, String> additionalHeaders;

    private CredentialsAuthenticationConfiguration(final String username, final String password,
            final Map<String, String> additionalHeaders) {
        sessionId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.additionalHeaders = Collections.unmodifiableMap(new HashMap<>(additionalHeaders));
    }

    /**
     * @return a new builder ready to build a {@code AuthenticationConfiguration}
     */
    public static CredentialsAuthenticationConfigurationBuilder newBuilder() {
        return new Builder();
    }

    @Override
    public String getClientSessionId() {
        return username + ":" + sessionId;
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    /**
     * Returns the username.
     *
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CredentialsAuthenticationConfiguration that = (CredentialsAuthenticationConfiguration) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(additionalHeaders, that.additionalHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, username, password, additionalHeaders);
    }

    @SuppressWarnings("squid:S2068")
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "sessionId=" + sessionId +
                ", username=" + username +
                ", password=***" +
                ", additionalHeaders=" + additionalHeaders +
                "]";
    }

    /**
     * Builder for credentials based {@link AuthenticationConfiguration}.
     */
    public interface CredentialsAuthenticationConfigurationBuilder extends UsernameSettable {
        // no-op
    }

    /**
     * Allows setting the username.
     */
    public interface UsernameSettable {

        /**
         * Sets the username to authenticate.
         *
         * @param username the username to authenticate
         * @return the builder object that allows setting the password
         */
        PasswordSettable username(String username);
    }

    /**
     * Allows setting the password.
     */
    public interface PasswordSettable {

        /**
         * Sets the password to authenticate with.
         *
         * @param password the password to authenticate with
         * @return the buildable.
         */
        Buildable password(String password);
    }

    /**
     * Allows building the {@link CredentialsAuthenticationConfiguration} instance as a final step.
     */
    public interface Buildable {

        /**
         * Adds an additional header to be used during authentication.
         *
         * @param key the key of the additional header.
         * @param value the header's value.
         * @return the buildable.
         */
        Buildable withAdditionalHeader(String key, String value);

        /**
         * Build the credentials authentication configuration.
         *
         * @return the created CredentialsAuthenticationConfiguration
         */
        CredentialsAuthenticationConfiguration build();
    }

    @Immutable
    private static class Builder implements CredentialsAuthenticationConfigurationBuilder, UsernameSettable,
            PasswordSettable, Buildable {

        private String username;
        private String password;
        private final Map<String, String> additionalHeaders = new HashMap<>();

        @Override
        public PasswordSettable username(final String username) {
            this.username = requireNonNull(username);
            return this;
        }

        @Override
        public Buildable password(final String password) {
            this.password = requireNonNull(password);
            return this;
        }

        @Override
        public Buildable withAdditionalHeader(final String key, final String value) {
            additionalHeaders.put(key, value);
            return this;
        }

        @Override
        public CredentialsAuthenticationConfiguration build() {
            return new CredentialsAuthenticationConfiguration(username, password, additionalHeaders);
        }

    }
}
