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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.time.Duration;
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
    private final Duration expiryGracePeriod;

    public ClientCredentialsAuthenticationConfiguration(
            final ClientCredentialsAuthenticationConfigurationBuilder builder) {

        super(builder.clientId, builder.additionalHeaders, builder.proxyConfiguration);
        tokenEndpoint = checkNotNull(builder.tokenEndpoint, "tokenEndpoint");
        clientId = checkNotNull(builder.clientId, "clientId");
        clientSecret = checkNotNull(builder.clientSecret, "clientSecret");
        scopes = Collections.unmodifiableList(new ArrayList<>(builder.scopes));
        expiryGracePeriod = checkNotNull(builder.expiryGracePeriod, "expiryGracePeriod");
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

    /**
     * Returns the expiry grace period.
     *
     * @return the period.
     */
    public Duration getExpiryGracePeriod() {
        return expiryGracePeriod;
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
                Objects.equals(scopes, that.scopes) &&
                Objects.equals(expiryGracePeriod, that.expiryGracePeriod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tokenEndpoint, clientId, clientSecret, scopes, expiryGracePeriod);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", tokenEndpoint=" + tokenEndpoint +
                ", clientId=" + clientId +
                ", clientSecret=" + clientSecret +
                ", scopes=" + scopes +
                ", expiryGracePeriod=" + expiryGracePeriod +
                "]";
    }

    @NotThreadSafe
    public static final class ClientCredentialsAuthenticationConfigurationBuilder
            implements AuthenticationConfiguration.Builder {

        private static final Duration DEFAULT_EXPIRY_GRACE_PERIOD = Duration.ofSeconds(5);

        private String tokenEndpoint;
        private String clientId;
        private String clientSecret;
        private Collection<String> scopes;
        private Duration expiryGracePeriod;
        @Nullable private ProxyConfiguration proxyConfiguration;
        private final Map<String, String> additionalHeaders;

        private ClientCredentialsAuthenticationConfigurationBuilder() {
            scopes = Collections.emptyList();
            expiryGracePeriod = DEFAULT_EXPIRY_GRACE_PERIOD;
            proxyConfiguration = null;
            additionalHeaders = new HashMap<>();
        }

        /**
         * Sets the endpoint to retrieve tokens.
         *
         * @param tokenEndpoint the endpoint.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder tokenEndpoint(final String tokenEndpoint) {
            this.tokenEndpoint = checkNotNull(tokenEndpoint, "tokenEndpoint");
            return this;
        }

        /**
         * Sets the client ID to authenticate.
         *
         * @param clientId the client ID.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder clientId(final String clientId) {
            this.clientId = checkNotNull(clientId, "clientId");
            return this;
        }

        /**
         * Sets the client secret to authenticate.
         * The secret will <strong>never</strong> be sent to a Ditto backend but directly to the configured endpoint.
         *
         * @param clientSecret the client secret.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder clientSecret(final String clientSecret) {
            this.clientSecret = checkNotNull(clientSecret, "clientSecret");
            return this;
        }

        /**
         * Sets the scopes to authenticate.
         *
         * @param scopes the scopes.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder scopes(final Collection<String> scopes) {
            this.scopes = new ArrayList<>(checkNotNull(scopes, "scopes"));
            return this;
        }

        /**
         * Sets the expiry grace period.
         *
         * @param expiryGracePeriod the period.
         * @return this builder.
         */
        public ClientCredentialsAuthenticationConfigurationBuilder expiryGracePeriod(final Duration expiryGracePeriod) {
            this.expiryGracePeriod = checkNotNull(expiryGracePeriod, "expiryGracePeriod");
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
                @Nullable final ProxyConfiguration proxyConfiguration) {

            this.proxyConfiguration = proxyConfiguration;
            return this;
        }

        @Override
        public ClientCredentialsAuthenticationConfiguration build() {
            return new ClientCredentialsAuthenticationConfiguration(this);
        }

    }

}
