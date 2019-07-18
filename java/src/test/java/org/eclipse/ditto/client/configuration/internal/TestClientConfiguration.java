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

import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.configuration.CommonConfiguration;
import org.eclipse.ditto.client.configuration.CredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
import org.eclipse.ditto.client.internal.bus.BusFactory;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.mock.MockMessagingProvider;

/**
 *
 */
public class TestClientConfiguration {

    public static final String CLIENT_ID = "theClient";

    public static CommonConfiguration getDittoClientConfiguration(final MockMessagingProvider provider,
            final CredentialsAuthenticationConfiguration authenticationConfiguration) {
        return CommonConfiguration.newBuilder()
                .providerConfiguration(new MockMessagingProvider.Configuration(provider, authenticationConfiguration,
                        new AuthenticationProvider<Object>() {
                            @Override
                            public String getClientSessionId() {
                                return authenticationConfiguration.getClientSessionId();
                            }

                            @Override
                            public Object prepareAuthentication(final Object channel,
                                    final Map<String, String> additionalAuthenticationHeaders,
                                    @Nullable final ProxyConfiguration proxyConfiguration) {
                                return channel;
                            }
                        }))
                .build();
    }

    public static CredentialsAuthenticationConfiguration getAuthenticationConfiguration() {
        return CredentialsAuthenticationConfiguration.newBuilder()
                .username("hans")
                .password("dampf")
                .build();
    }

    public static InternalConfiguration buildInternalConfiguration(final CommonConfiguration configuration) {
        final PointerBus twinBus = BusFactory.createPointerBus("test");

        return new InternalConfiguration(configuration, null, twinBus, null,
                configuration.getProviderConfiguration(), null);
    }

    public static InternalConfiguration buildInternalConfiguration(final CommonConfiguration configuration,
            final PointerBus twinBus) {

        return new InternalConfiguration(configuration, null, twinBus, null,
                configuration.getProviderConfiguration(),
                null);
    }
}
