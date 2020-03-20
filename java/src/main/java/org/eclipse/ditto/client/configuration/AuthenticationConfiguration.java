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

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Interface for configuration provider specifying the necessary information for authenticating a client at Eclipse
 * Ditto.
 *
 * @since 1.0.0
 */
public interface AuthenticationConfiguration {

    /**
     * Returns the session identifier for this client - has to be unique for each newly instantiated client.
     *
     * @return the session identifier for this client
     */
    String getSessionId();

    /**
     * Returns additional header fields which should be used during authentication.
     *
     * @return additional header fields which should be used during authentication.
     */
    Map<String, String> getAdditionalHeaders();

    /**
     * Returns the proxy configuration.
     *
     * @return the configuration or an empty optional.
     */
    Optional<ProxyConfiguration> getProxyConfiguration();

    /**
     * Builder for {@link org.eclipse.ditto.client.configuration.AuthenticationConfiguration}.
     */
    interface Builder {

        /**
         * Adds an additional header to be used during authentication.
         *
         * @param key the key of the additional header.
         * @param value the header's value.
         * @return the buildable.
         */
        Builder withAdditionalHeader(String key, String value);

        /**
         * Sets the {@code proxyConfiguration}.
         *
         * @param proxyConfiguration the proxy configuration to set.
         * @return this builder.
         */
        Builder proxyConfiguration(@Nullable ProxyConfiguration proxyConfiguration);

        /**
         * Build the authentication configuration.
         *
         * @return the configuration.
         */
        AuthenticationConfiguration build();

    }

}
