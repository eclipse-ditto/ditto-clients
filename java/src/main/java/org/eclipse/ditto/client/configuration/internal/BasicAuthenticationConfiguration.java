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
package org.eclipse.ditto.client.configuration.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;

/**
 * A {@link org.eclipse.ditto.client.configuration.AuthenticationConfiguration} implementing
 * {@code Basic Authentication} with username and password.
 *
 * @since 1.0.0
 */
@Immutable
public final class BasicAuthenticationConfiguration extends AbstractAuthenticationConfiguration {

    private final String username;
    private final String password;

    private BasicAuthenticationConfiguration(final String username, final String password,
            final Map<String, String> additionalHeaders,
            @Nullable final ProxyConfiguration proxyConfiguration) {
        super(username, additionalHeaders, proxyConfiguration);
        this.username = username;
        this.password = password;
    }

    /**
     * @return a new builder to build {@code BasicAuthenticationConfiguration}.
     */
    public static BasicAuthenticationConfigurationBuilder newBuilder() {
        return new BasicAuthenticationConfigurationBuilder();
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
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final BasicAuthenticationConfiguration that = (BasicAuthenticationConfiguration) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, password);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", username=" + username +
                ", password=" + password +
                "]";
    }

    @NotThreadSafe
    public static class BasicAuthenticationConfigurationBuilder implements AuthenticationConfiguration.Builder {

        private String username;
        private String password;
        private final Map<String, String> additionalHeaders = new HashMap<>();
        private ProxyConfiguration proxyConfiguration;

        /**
         * Sets the username to authenticate.
         *
         * @param username the username to authenticate
         * @return the builder object that allows setting the password
         */
        public BasicAuthenticationConfigurationBuilder username(final String username) {
            this.username = checkNotNull(username, "username");
            return this;
        }

        /**
         * Sets the password to authenticate with.
         *
         * @param password the password to authenticate with
         * @return the buildable.
         */
        public BasicAuthenticationConfigurationBuilder password(final String password) {
            this.password = checkNotNull(password, "password");
            return this;
        }

        @Override
        public BasicAuthenticationConfigurationBuilder withAdditionalHeader(final String key, final String value) {
            additionalHeaders.put(checkNotNull(key, "key"), value);
            return this;
        }

        @Override
        public BasicAuthenticationConfigurationBuilder proxyConfiguration(final ProxyConfiguration proxyConfiguration) {
            this.proxyConfiguration = checkNotNull(proxyConfiguration, "proxyConfiguration");
            return this;
        }

        @Override
        public BasicAuthenticationConfiguration build() {
            return new BasicAuthenticationConfiguration(username, password, additionalHeaders, proxyConfiguration);
        }

    }

}
