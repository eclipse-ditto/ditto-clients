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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;

/**
 * A {@link org.eclipse.ditto.client.configuration.AuthenticationConfiguration} for OAuth 2 client credentials
 * authentication.
 *
 * @since 1.0.0
 */
@Immutable
public final class ClientCredentialsAuthenticationConfiguration extends AbstractAuthenticationConfiguration {

    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final List<String> scopes;

    private ClientCredentialsAuthenticationConfiguration(final String tokenEndpoint, final String clientId,
            final String clientSecret, final Collection<String> scopes, final Map<String, String> additionalHeaders,
            @Nullable final ProxyConfiguration proxyConfiguration) {
        super(clientId, additionalHeaders, proxyConfiguration);
        this.tokenEndpoint = checkNotNull(tokenEndpoint, "tokenEndpoint");
        this.clientId = checkNotNull(clientId, "clientId");
        this.clientSecret = checkNotNull(clientSecret, "clientSecret");
        this.scopes = Collections.unmodifiableList(new ArrayList<>(scopes));
    }

    /**
     * @return a new builder to build {@code ClientCredentialsAuthenticationConfiguration}.
     */
    public static ClientCredentialsAuthenticationConfigurationBuilder newBuilder() {
        return new ClientCredentialsAuthenticationConfigurationBuilder();
    }

    /**
     * Returns the token endpoint.
     *
     * @return the endpoint.
     */
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    /**
     * Returns the client id.
     *
     * @return the client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns the client secret.
     *
     * @return the client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Returns the scopes.
     *
     * @return the scopes.
     */
    public Collection<String> getScopes() {
        return scopes;
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
        final ClientCredentialsAuthenticationConfiguration that = (ClientCredentialsAuthenticationConfiguration) o;
        return Objects.equals(tokenEndpoint, that.tokenEndpoint) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientSecret, that.clientSecret) &&
                Objects.equals(scopes, that.scopes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tokenEndpoint, clientId, clientSecret, scopes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", tokenEndpoint=" + tokenEndpoint +
                ", clientId=" + clientId +
                ", clientSecret=" + clientSecret +
                ", scopes=" + scopes +
                "]";
    }

    @NotThreadSafe
    public static class ClientCredentialsAuthenticationConfigurationBuilder
            implements AuthenticationConfiguration.Builder {

        private String tokenEndpoint;
        private String clientId;
        private String clientSecret;
        private Collection<String> scopes;
        private final Map<String, String> additionalHeaders = new HashMap<>();
        private ProxyConfiguration proxyConfiguration;

        /**
         * Sets the endpoint to retrieve tokens.
         *
         * @param tokenEndpoint the endpoint.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder tokenEndpoint(final String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        /**
         * Sets the client id to authenticate.
         *
         * @param clientId the client id.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder clientId(final String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Sets the client secret to authenticate.
         *
         * @param clientSecret the client secret.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder clientSecret(final String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * Sets the scopes to authenticate.
         *
         * @param scopes the scopes.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder scopes(final Collection<String> scopes) {
            this.scopes = scopes;
            return this;
        }

        @Override
        public ClientCredentialsAuthenticationConfigurationBuilder withAdditionalHeader(final String key,
                final String value) {
            additionalHeaders.put(checkNotNull(key, "key"), value);
            return this;
        }

        @Override
        public ClientCredentialsAuthenticationConfigurationBuilder proxyConfiguration(
                final ProxyConfiguration proxyConfiguration) {
            this.proxyConfiguration = checkNotNull(proxyConfiguration, "proxyConfiguration");
            return this;
        }

        @Override
        public ClientCredentialsAuthenticationConfiguration build() {
            return new ClientCredentialsAuthenticationConfiguration(tokenEndpoint, clientId, clientSecret, scopes,
                    additionalHeaders, proxyConfiguration);
        }

    }

}
