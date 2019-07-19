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

import java.util.Objects;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

/**
 * Common configuration properties and their Builders. Provides a convenient way to set all mandatory and optional
 * configurations for the common of the client.
 *
 * @since 1.0.0
 */
public final class CommonConfiguration {

    // required configuration
    private ProviderConfiguration<?, ?> providerConfiguration;

    // optional configuration
    private JsonSchemaVersion schemaVersion = JsonSchemaVersion.V_2; // default is currently v2 (policy based API)
    private ProxyConfiguration proxyConfiguration;
    private TrustStoreConfiguration trustStoreConfiguration;
    private BusConfiguration busConfiguration = BusConfiguration.newBuilder().build();
    private MessageSerializerConfiguration messageSerializerConfiguration =
            MessageSerializerConfiguration.newInstance();

    protected CommonConfiguration() {
        // noop
    }

    /**
     * Returns a builder for creating an instance of {@code CommonConfiguration}.
     *
     * @return a builder for creating a configuration object.
     */
    public static CommonConfigurationBuilder newBuilder() {
        return new CommonConfiguration.CommonConfigurationBuilder();
    }

    /**
     * Returns a builder for creating an instance of {@code CommonConfiguration}.
     */
    public interface ConfigurationBuilder extends ProviderConfigurationStep {
        // noop
    }

    /**
     * Returns the {@link JsonSchemaVersion} this client works with.
     *
     * @return the {@link JsonSchemaVersion} this client works with.
     */
    public JsonSchemaVersion getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Returns the providerConfiguration.
     *
     * @return the value of the providerConfiguration.
     */
    public ProviderConfiguration<?, ?> getProviderConfiguration() {
        return providerConfiguration;
    }

    /**
     * Returns the proxy configuration for this client.
     *
     * @return the proxy configuration.
     */
    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }

    /**
     * Returns the proxy configuration for this client.
     *
     * @return the proxy configuration.
     */
    public TrustStoreConfiguration getTrustStoreConfiguration() {
        return trustStoreConfiguration;
    }

    /**
     * Returns the Bus configuration of this client.
     *
     * @return the bus configuration.
     * @since 1.0.0
     */
    public BusConfiguration getBusConfiguration() { return busConfiguration; }

    /**
     * Returns the MessageSerializer configuration for this client.
     *
     * @return the MessageSerializer configuration.
     */
    public MessageSerializerConfiguration getMessageSerializerConfiguration() {
        return messageSerializerConfiguration;
    }

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommonConfiguration)) {
            return false;
        }
        final CommonConfiguration that = (CommonConfiguration) o;
        return that.canEqual(this) &&
                Objects.equals(providerConfiguration, that.providerConfiguration) &&
                schemaVersion == that.schemaVersion &&
                Objects.equals(proxyConfiguration, that.proxyConfiguration) &&
                Objects.equals(trustStoreConfiguration, that.trustStoreConfiguration) &&
                Objects.equals(busConfiguration, that.busConfiguration) &&
                Objects.equals(messageSerializerConfiguration, that.messageSerializerConfiguration) &&
                super.equals(that);
    }

    private boolean canEqual(@Nullable final Object other) {
        return other instanceof CommonConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerConfiguration, schemaVersion, proxyConfiguration,
                trustStoreConfiguration, busConfiguration, messageSerializerConfiguration);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "providerConfiguration=" + providerConfiguration +
                ", schemaVersion=" + schemaVersion +
                ", proxyConfiguration=" + proxyConfiguration +
                ", trustStoreConfiguration=" + trustStoreConfiguration +
                ", busConfiguration=" + busConfiguration +
                ", messageSerializerConfiguration=" + messageSerializerConfiguration +
                "]";
    }

    /**
     * Defines a method for setting the MessagingProvider value.
     */
    public interface ProviderConfigurationStep {

        /**
         * Sets the ProviderConfiguration for the {@link MessagingProvider MessagingProvider} to use. Can be obtained
         * via Factory {@link MessagingProviders MessagingProviders}.
         *
         * @param providerConfig the ProviderConfiguration.
         * @return an object handle for building a {@link CommonConfiguration} object based on the arguments provided to
         * this builder.
         */
        OptionalConfigurationStep providerConfiguration(ProviderConfiguration<?, ?> providerConfig);
    }

    /**
     * Defines a method for setting optional configuration values.
     */
    public interface OptionalConfigurationStep extends ProviderConfigurationStep, BuildStep {

        /**
         * Sets the {@link JsonSchemaVersion} this client works with.
         *
         * @param schemaVersion the {@link JsonSchemaVersion} this client works with.
         * @return an object handle for building a {@link CommonConfiguration} object based on the arguments provided to
         * this builder.
         */
        OptionalConfigurationStep schemaVersion(JsonSchemaVersion schemaVersion);

        /**
         * Sets the proxy configuration.
         *
         * @param proxyConfiguration the proxy configuration.
         * @return an object handle for building a {@link CommonConfiguration} object based on the arguments provided to
         * this builder.
         */
        OptionalConfigurationStep proxyConfiguration(ProxyConfiguration proxyConfiguration);

        /**
         * Sets the trust store configuration. A trust store contains certificates the client accepts.
         *
         * @param trustStoreConfiguration the trust store configuration.
         * @return an object handle for building a {@link CommonConfiguration} object based on the arguments provided to
         * this builder.
         */
        OptionalConfigurationStep trustStoreConfiguration(TrustStoreConfiguration trustStoreConfiguration);

        /**
         * Sets the Bus configuration. Used for registering custom Executors (e.g. ThreadPool) which with to invoke
         * callback methods of the client (e.g. change notifications).
         *
         * @param busConfiguration the Bus configuration.
         * @return an object handle for building a {@link CommonConfiguration} object based on the arguments provided to
         * this builder.
         */
        OptionalConfigurationStep busConfiguration(BusConfiguration busConfiguration);

        /**
         * Sets the Serializer configuration. Used for registering custom Message payload Serializers / De-Serializers.
         *
         * @param messageSerializerConfiguration the Serializer Configuration configuration.
         * @return an object handle for building a {@link CommonConfiguration} object based on the arguments provided to
         * this builder.
         */
        OptionalConfigurationStep serializerConfiguration(
                MessageSerializerConfiguration messageSerializerConfiguration);
    }

    /**
     * Defines a method for building a {@link CommonConfiguration} object based on the arguments provided to this
     * builder.
     */
    public interface BuildStep {

        /**
         * Creates a new instance of the {@link CommonConfiguration}.
         *
         * @return a new configuration object based on the arguments provided to this builder.
         */
        CommonConfiguration build();
    }

    /**
     * Abstract base class for configuration builders containing functionality to build relevant fields of {@link
     * CommonConfiguration} type.
     */
    public static class CommonConfigurationBuilder implements ConfigurationBuilder, ProviderConfigurationStep,
            OptionalConfigurationStep, BuildStep {

        private final CommonConfiguration commonConfiguration = new CommonConfiguration();

        @Override
        public OptionalConfigurationStep providerConfiguration(
                final ProviderConfiguration<?, ?> providerConfiguration) {
            commonConfiguration.providerConfiguration = providerConfiguration;
            return this;
        }

        @Override
        public OptionalConfigurationStep schemaVersion(final JsonSchemaVersion schemaVersion) {
            commonConfiguration.schemaVersion = schemaVersion;
            return this;
        }

        @Override
        public OptionalConfigurationStep proxyConfiguration(final ProxyConfiguration proxyConfiguration) {
            commonConfiguration.proxyConfiguration = proxyConfiguration;
            return this;
        }

        @Override
        public OptionalConfigurationStep trustStoreConfiguration(
                final TrustStoreConfiguration trustStoreConfiguration) {

            commonConfiguration.trustStoreConfiguration = trustStoreConfiguration;
            return this;
        }

        @Override
        public OptionalConfigurationStep busConfiguration(final BusConfiguration busConfiguration) {

            commonConfiguration.busConfiguration = busConfiguration;
            return this;
        }

        @Override
        public OptionalConfigurationStep serializerConfiguration(
                final MessageSerializerConfiguration messageSerializerConfiguration) {

            commonConfiguration.messageSerializerConfiguration = messageSerializerConfiguration;
            return this;
        }

        @Override
        public CommonConfiguration build() {
            final CommonConfiguration result = instantiateForBuild();
            result.providerConfiguration = commonConfiguration.providerConfiguration;
            result.schemaVersion = commonConfiguration.schemaVersion;
            result.proxyConfiguration = commonConfiguration.proxyConfiguration;
            result.trustStoreConfiguration = commonConfiguration.trustStoreConfiguration;
            result.busConfiguration = commonConfiguration.busConfiguration;
            return result;
        }

        /**
         * Instantiates a concrete instance of {@code <C>} which is used for building the configuration.
         *
         * @return the pre-instantiated instance of {@code <C>}
         */
        CommonConfiguration instantiateForBuild() {
            final CommonConfiguration result = new CommonConfiguration();
            result.messageSerializerConfiguration = commonConfiguration.messageSerializerConfiguration;
            return result;
        }

    }

}
