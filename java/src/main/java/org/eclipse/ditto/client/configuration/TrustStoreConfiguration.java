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

import java.net.URL;

import javax.annotation.Nullable;

/**
 * Contains information about the truststore that is used by the client. A truststore contains certificates or
 * certificate chains that are trusted by this client. The referenced truststore must be in JKS format.
 *
 * @since 1.0.0
 */
public final class TrustStoreConfiguration {

    private final URL location;
    private final String password;

    private TrustStoreConfiguration(@Nullable final URL location, @Nullable final String password) {
        this.location = location;
        this.password = password;
    }

    /**
     * @return a new TrustStoreConfigurationBuilder instance
     */
    public static TrustStoreConfigurationBuilder newBuilder() {
        return new Builder();
    }

    /**
     * @return the URL where the truststore is located
     */
    @Nullable
    public URL getLocation() {
        return location;
    }

    /**
     * @return the password required to read the truststore
     */
    @Nullable
    public String getPassword() {
        return password;
    }

    /**
     * Entry point for building a new TrustStoreConfiguration object.
     */
    public interface TrustStoreConfigurationBuilder extends LocationSettable {
    }

    /**
     * Allows setting the truststore location.
     */
    public interface LocationSettable {

        /**
         * @param location an URL where the truststore file can be found
         * @return a build object that allows setting a truststore pasword
         */
        PasswordSettable location(URL location);
    }

    /**
     * Allows setting the truststore password.
     */
    public interface PasswordSettable extends TrustStoreConfigurationBuildable {

        /**
         * @param password the password required to open the truststore
         * @return a builder object to finish the creation of the TrustStoreConfiguration object
         */
        TrustStoreConfigurationBuildable password(String password);
    }

    /**
     * Final interface to finish building a new TrustStoreConfiguration object.
     */
    public interface TrustStoreConfigurationBuildable {

        /**
         * @return new TrustStoreConfiguration instance from the builder
         */
        TrustStoreConfiguration build();
    }

    private static final class Builder
            implements TrustStoreConfigurationBuilder, LocationSettable, PasswordSettable,
            TrustStoreConfigurationBuildable {

        private URL location;
        private String password;

        private Builder() {
        }

        @Override
        public TrustStoreConfiguration build() {
            return new TrustStoreConfiguration(location, password);
        }

        @Override
        public PasswordSettable location(URL location) {
            this.location = requireNonNull(location, "TrustStore location must not be null.");
            return this;
        }

        @Override
        public TrustStoreConfigurationBuildable password(String password) {
            this.password = requireNonNull(password, "TrustStore password must not be null.");
            return this;
        }
    }
}
