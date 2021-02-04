/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.DummyAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
import org.eclipse.ditto.client.configuration.TrustStoreConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neovisionaries.ws.client.WebSocket;

/**
 * TODO TJ add javadoc
 */
public class SearchPersistedTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(DittoClientUsageExamples.class);

    //        private static final String PROPERTIES_FILE = "ditto-client-starter-local.properties"; // for local development
    private static final String PROPERTIES_FILE = "ditto-client-starter-aws-dev.properties";
    private static final String PROXY_HOST;
    private static final String PROXY_PORT;
    private static final String DITTO_ENDPOINT_URL;

    private static final URL DITTO_TRUSTSTORE_LOCATION;
    private static final String DITTO_TRUSTSTORE_PASSWORD;
    private static final String DITTO_DUMMY_AUTH_USER;
    private static final String DITTO_USERNAME;
    private static final String DITTO_PASSWORD;
    private static final String DITTO_OAUTH_CLIENT_ID;
    private static final String DITTO_OAUTH_CLIENT_SECRET;
    private static final Collection<String> DITTO_OAUTH_SCOPES;
    private static final String DITTO_OAUTH_TOKEN_ENDPOINT;
    private static final String NAMESPACE;
    private static final Properties CONFIG;

    public static void main(final String... args) throws ExecutionException, InterruptedException, TimeoutException {
        final DittoClient client = DittoClients.newInstance(createMessagingProvider());

        final int counter = 100;
        for (int i=0; i<counter; i++) {
            final int count = i;
            final Thing thing = Thing.newBuilder()
                    .setGeneratedId()
                    .setAttribute(JsonPointer.of("foo"), JsonValue.of(count))
                    .build();
            final long startTs = System.nanoTime();
            System.out.println(count + ": Creating thing, waiting for 'search-persisted' ...");
            client.twin().create(thing, Options.headers(DittoHeaders.newBuilder()
                    .acknowledgementRequest(
                            AcknowledgementRequest.of(DittoAcknowledgementLabel.TWIN_PERSISTED),
                            AcknowledgementRequest.of(DittoAcknowledgementLabel.SEARCH_PERSISTED)
                    ).build())
            ).thenAccept(createdThing -> {

                System.out.println(count + ": Created thing after " + (System.nanoTime() - startTs) / 1000000.0 + "ms: "
                        + createdThing);
                final Thing searchedThing = client.twin().search().stream(qb -> qb.filter("eq(thingId,'" +
                        createdThing.getEntityId().get().toString() + "')"))
                        .findFirst()
                        .get();
                System.out.println(count + ": Found   thing after " + (System.nanoTime() - startTs) / 1000000.0 + "ms: "
                        + searchedThing);
            }).join();
        }
    }


    private static void promptEnterKey() throws InterruptedException {
        if (promptToContinue()) {
            Thread.sleep(500);
            System.out.println("Press \"ENTER\" to continue...");
            final Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
        }
    }

    /**
     * Create a messaging provider according to the configuration.
     *
     * @return the messaging provider.
     */
    public static MessagingProvider createMessagingProvider() {
        final MessagingConfiguration.Builder builder = WebSocketMessagingConfiguration.newBuilder()
                .endpoint(DITTO_ENDPOINT_URL)
                .jsonSchemaVersion(JsonSchemaVersion.V_2)
                .reconnectEnabled(false);

        final ProxyConfiguration proxyConfiguration;
        if (PROXY_HOST != null && !PROXY_HOST.isEmpty()) {
            proxyConfiguration = ProxyConfiguration.newBuilder()
                    .proxyHost(PROXY_HOST)
                    .proxyPort(Integer.parseInt(PROXY_PORT))
                    .build();
            builder.proxyConfiguration(proxyConfiguration);
        } else {
            proxyConfiguration = null;
        }

        if (DITTO_TRUSTSTORE_LOCATION != null) {
            builder.trustStoreConfiguration(TrustStoreConfiguration.newBuilder()
                    .location(DITTO_TRUSTSTORE_LOCATION)
                    .password(DITTO_TRUSTSTORE_PASSWORD)
                    .build());
        }

        final AuthenticationProvider<WebSocket> authenticationProvider;
        if (DITTO_DUMMY_AUTH_USER != null) {
            authenticationProvider =
                    AuthenticationProviders.dummy(DummyAuthenticationConfiguration.newBuilder()
                            .dummyUsername(DITTO_DUMMY_AUTH_USER)
                            .build());
        } else if (DITTO_OAUTH_CLIENT_ID != null && !DITTO_OAUTH_CLIENT_ID.isEmpty()) {
            final ClientCredentialsAuthenticationConfiguration.ClientCredentialsAuthenticationConfigurationBuilder
                    authenticationConfigurationBuilder =
                    ClientCredentialsAuthenticationConfiguration.newBuilder()
                            .clientId(DITTO_OAUTH_CLIENT_ID)
                            .clientSecret(DITTO_OAUTH_CLIENT_SECRET)
                            .scopes(DITTO_OAUTH_SCOPES)
                            .tokenEndpoint(DITTO_OAUTH_TOKEN_ENDPOINT);
            if (proxyConfiguration != null) {
                authenticationConfigurationBuilder.proxyConfiguration(proxyConfiguration);
            }
            authenticationProvider =
                    AuthenticationProviders.clientCredentials(authenticationConfigurationBuilder.build());
        } else {
            authenticationProvider =
                    AuthenticationProviders.basic(BasicAuthenticationConfiguration.newBuilder()
                            .username(DITTO_USERNAME)
                            .password(DITTO_PASSWORD)
                            .build());
        }

        return MessagingProviders.webSocket(builder.build(), authenticationProvider);
    }

    private static boolean shouldNotSkip(final String propertyName) {
        return !Boolean.parseBoolean(CONFIG.getProperty("skip." + propertyName, "false"));
    }

    private static boolean promptToContinue() {
        return Boolean.parseBoolean(CONFIG.getProperty("prompt.to.continue", "true"));
    }

    static {
        try {
            final Properties config = new Properties();
            if (new File(PROPERTIES_FILE).exists()) {
                config.load(new FileReader(PROPERTIES_FILE));
            } else {
                final InputStream i =
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);
                config.load(i);
                i.close();
            }

            PROXY_HOST = config.getProperty("proxy.host");
            PROXY_PORT = config.getProperty("proxy.port");

            DITTO_ENDPOINT_URL = config.getProperty("ditto.endpoint");

            if (!config.getProperty("ditto.truststore.location").isEmpty()) {
                DITTO_TRUSTSTORE_LOCATION =
                        DittoClientUsageExamples.class.getResource(config.getProperty("ditto.truststore.location"));
            } else {
                DITTO_TRUSTSTORE_LOCATION = null;
            }
            DITTO_TRUSTSTORE_PASSWORD = config.getProperty("ditto.truststore.password");
            DITTO_DUMMY_AUTH_USER = config.getProperty("ditto.dummy-auth-user");
            DITTO_USERNAME = config.getProperty("ditto.username");
            DITTO_PASSWORD = config.getProperty("ditto.password");
            DITTO_OAUTH_CLIENT_ID = config.getProperty("ditto.oauth.client-id");
            DITTO_OAUTH_CLIENT_SECRET = config.getProperty("ditto.oauth.client-secret");
            DITTO_OAUTH_SCOPES =
                    Arrays.stream(config.getProperty("ditto.oauth.scope").split(" ")).collect(Collectors.toSet());
            DITTO_OAUTH_TOKEN_ENDPOINT = config.getProperty("ditto.oauth.token-endpoint");
            NAMESPACE = config.getProperty("ditto.namespace");
            CONFIG = config;
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
