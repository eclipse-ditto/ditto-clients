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

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Contains information about the proxy that is used by the client when connecting to the back end service.
 *
 * @since 1.0.0
 */
public final class ProxyConfiguration {

    private final String host;
    private final int port;
    @Nullable private final String username;
    @Nullable private final String password;

    private ProxyConfiguration(final String host, final int port, @Nullable final String username,
            @Nullable final String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * @return a new builder used to create a ProxyConfiguration object
     */
    public static ProxyConfigurationBuilder newBuilder() {
        return new Builder();
    }

    /**
     * @return host address of the proxy
     */
    public String getHost() {
        return host;
    }

    /**
     * @return port of the proxy
     */
    public int getPort() {
        return port;
    }

    /**
     * @return username of the proxy.
     */
    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    /**
     * @return password of the proxy
     */
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    /**
     * Entry point for building a ProxyConfiguration object.
     */
    public interface ProxyConfigurationBuilder extends ProxyHostSettable {
    }

    /**
     * Allows setting the proxy host.
     */
    public interface ProxyHostSettable {

        /**
         * @param host the proxy host.
         * @return a builder object to set the proxy port.
         */
        ProxyPortSettable proxyHost(String host);
    }

    /**
     * Allows setting the proxy port.
     */
    public interface ProxyPortSettable {

        /**
         * @param port port of the proxy.
         * @return a builder object for optional proxy settings.
         */
        ProxyOptionalSettable proxyPort(int port);
    }

    /**
     * Allows setting the proxy username.
     */
    public interface ProxyOptionalSettable extends ProxyConfigurationBuildable {

        /**
         * @param username the username for proxy authentication.
         * @return a builder object to set the password.
         */
        ProxyPasswordSettable proxyUsername(String username);

    }

    /**
     * Allows setting the proxy password.
     */
    public interface ProxyPasswordSettable extends ProxyConfigurationBuildable {

        /**
         * @param password the password for proxy authentication.
         * @return a builder object for optional proxy settings.
         */
        ProxyOptionalSettable proxyPassword(String password);
    }

    public interface ProxyConfigurationBuildable {

        /**
         * @return new ProxyConfiguration instance
         */
        ProxyConfiguration build();
    }

    private static final class Builder implements ProxyConfigurationBuilder, ProxyHostSettable,
            ProxyPortSettable, ProxyOptionalSettable, ProxyPasswordSettable, ProxyConfigurationBuildable {

        private String host;
        private int port;
        private String username;
        private String password;

        private Builder() {
        }

        @Override
        public ProxyConfiguration build() {
            return new ProxyConfiguration(host, port, username, password);
        }

        @Override
        public ProxyPortSettable proxyHost(final String host) {
            this.host = requireNonNull(host, "Proxy host must not be null.");
            return this;
        }

        @Override
        public ProxyOptionalSettable proxyPort(final int port) {
            if (port <= 0) {
                throw new IllegalArgumentException("Proxy port must not be negative or zero.");
            }
            this.port = port;
            return this;
        }

        @Override
        public ProxyPasswordSettable proxyUsername(final String username) {
            this.username = requireNonNull(username, "Proxy username must not be null.");
            return this;
        }

        @Override
        public ProxyOptionalSettable proxyPassword(final String password) {
            this.password = requireNonNull(password, "Proxy password must not be null.");
            return this;
        }
    }

}
