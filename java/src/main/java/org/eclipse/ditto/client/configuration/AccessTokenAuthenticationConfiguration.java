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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.client.messaging.JsonWebTokenSupplier;

/**
 * A {@link org.eclipse.ditto.client.configuration.AuthenticationConfiguration} for access token authentication.
 *
 * @since 1.0.0
 */
@Immutable
public final class AccessTokenAuthenticationConfiguration extends AbstractAuthenticationConfiguration
        implements TokenAuthenticationConfiguration {

    private final String identifier;
    private final JsonWebTokenSupplier jsonWebTokenSupplier;
    private final Duration expiryGracePeriod;

    private AccessTokenAuthenticationConfiguration(final AccessTokenAuthenticationConfigurationBuilder builder) {
        super(builder.identifier, builder.additionalHeaders, builder.proxyConfiguration);
        this.identifier = builder.identifier;
        this.jsonWebTokenSupplier = checkNotNull(builder.jsonWebTokenSupplier, "jsonWebTokenSupplier");
        this.expiryGracePeriod = checkNotNull(builder.expiryGracePeriod, "expiryGracePeriod");
    }

    /**
     * @return a new builder to build {@code AccessTokenAuthenticationConfiguration}.
     */
    public static AccessTokenAuthenticationConfigurationBuilder newBuilder() {
        return new AccessTokenAuthenticationConfigurationBuilder();
    }

    /**
     * Returns the identifier.
     *
     * @return the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the grace period which will be subtracted from token expiry to trigger the configured token supplier.
     *
     * @return the grace period.
     */
    public JsonWebTokenSupplier getJsonWebTokenSupplier() {
        return jsonWebTokenSupplier;
    }

    @Override
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
        final AccessTokenAuthenticationConfiguration that = (AccessTokenAuthenticationConfiguration) o;
        return Objects.equals(identifier, that.identifier) &&
                Objects.equals(jsonWebTokenSupplier, that.jsonWebTokenSupplier) &&
                Objects.equals(expiryGracePeriod, that.expiryGracePeriod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), identifier, jsonWebTokenSupplier, expiryGracePeriod);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", identifier=" + identifier +
                ", jsonWebTokenSupplier=" + jsonWebTokenSupplier +
                ", expiryGracePeriod=" + expiryGracePeriod +
                "]";
    }

    @NotThreadSafe
    public static class AccessTokenAuthenticationConfigurationBuilder
            implements AuthenticationConfiguration.Builder {

        private static final Duration DEFAULT_EXPIRY_GRACE_PERIOD = Duration.ofSeconds(5);

        private final Map<String, String> additionalHeaders = new HashMap<>();

        private String identifier;
        private JsonWebTokenSupplier jsonWebTokenSupplier;
        private Duration expiryGracePeriod = DEFAULT_EXPIRY_GRACE_PERIOD;
        @Nullable private ProxyConfiguration proxyConfiguration;

        /**
         * Sets the identifier to authenticate.
         *
         * @param identifier the identifier.
         * @return this builder.
         */
        public AccessTokenAuthenticationConfigurationBuilder identifier(final String identifier) {
            this.identifier = identifier;
            return this;
        }

        /**
         * Sets the access token supplier to authenticate.
         *
         * @param jsonWebTokenSupplier the supplier.
         * @return this builder.
         */
        public AccessTokenAuthenticationConfigurationBuilder accessTokenSupplier(
                final JsonWebTokenSupplier jsonWebTokenSupplier) {
            this.jsonWebTokenSupplier = jsonWebTokenSupplier;
            return this;
        }

        /**
         * Sets the expiry grace period.
         *
         * @param expiryGracePeriod the period.
         * @return this builder.
         */
        public AccessTokenAuthenticationConfigurationBuilder expiryGracePeriod(final Duration expiryGracePeriod) {
            this.expiryGracePeriod = expiryGracePeriod;
            return this;
        }

        @Override
        public AccessTokenAuthenticationConfigurationBuilder withAdditionalHeader(final String key,
                final String value) {
            additionalHeaders.put(checkNotNull(key, "key"), value);
            return this;
        }

        @Override
        public AccessTokenAuthenticationConfigurationBuilder proxyConfiguration(
                @Nullable final ProxyConfiguration proxyConfiguration) {
            this.proxyConfiguration = proxyConfiguration;
            return this;
        }

        @Override
        public AccessTokenAuthenticationConfiguration build() {
            return new AccessTokenAuthenticationConfiguration(this);
        }

    }

}
