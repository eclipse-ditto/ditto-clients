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

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;

/**
 * An implementation for {@link org.eclipse.ditto.client.configuration.AuthenticationConfiguration} based on "Ditto Dummy authentication" used for local
 * development and testing.
 *
 * @since 1.0.0
 */
@Immutable
public final class DummyAuthenticationConfiguration extends AbstractAuthenticationConfiguration {

    private final String dummyUsername;

    private DummyAuthenticationConfiguration(final String dummyUsername,
            final Map<String, String> additionalHeaders) {
        super(dummyUsername, additionalHeaders, null);
        this.dummyUsername = dummyUsername;
    }

    /**
     * @return a new builder to build {@code DummyAuthenticationConfiguration}
     */
    public static DummyAuthenticationConfigurationBuilder newBuilder() {
        return new DummyAuthenticationConfigurationBuilder();
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
        final DummyAuthenticationConfiguration that = (DummyAuthenticationConfiguration) o;
        return Objects.equals(dummyUsername, that.dummyUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dummyUsername);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", dummyUsername=" + dummyUsername +
                "]";
    }

    @NotThreadSafe
    public static class DummyAuthenticationConfigurationBuilder implements AuthenticationConfiguration.Builder {

        private String dummyUsername;
        private final Map<String, String> additionalHeaders = new HashMap<>();

        /**
         * Sets the dummyUsername to authenticate.
         *
         * @param dummyUsername the dummyUsername to authenticate
         * @return the builder object that allows setting the password
         */
        public DummyAuthenticationConfigurationBuilder dummyUsername(final String dummyUsername) {
            this.dummyUsername = requireNonNull(dummyUsername);
            return this;
        }

        @Override
        public DummyAuthenticationConfigurationBuilder withAdditionalHeader(final String key, final String value) {
            additionalHeaders.put(key, value);
            return this;
        }

        /**
         * Dummy authentication doesn't support proxy configuration. The provided configuration will therefore be
         * ignored.
         *
         * @return this builder.
         */
        @Override
        public DummyAuthenticationConfigurationBuilder proxyConfiguration(final ProxyConfiguration proxyConfiguration) {
            return this;
        }

        @Override
        public DummyAuthenticationConfiguration build() {
            return new DummyAuthenticationConfiguration(dummyUsername, additionalHeaders);
        }

    }
}
