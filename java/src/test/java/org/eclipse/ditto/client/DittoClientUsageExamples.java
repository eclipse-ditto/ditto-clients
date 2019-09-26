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
package org.eclipse.ditto.client;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.authentication.AuthenticationProvider;
import org.eclipse.ditto.client.authentication.AuthenticationProviders;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
import org.eclipse.ditto.client.configuration.TrustStoreConfiguration;
import org.eclipse.ditto.client.configuration.internal.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.DummyAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.internal.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.KnownMessageSubjects;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingBuilder;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.JsonifiableAdaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.signals.commands.live.modify.CreateThingLiveCommandAnswerBuilder;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertyLiveCommandAnswerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neovisionaries.ws.client.WebSocket;


/**
 * Get the Ditto-Client up and running against different environments with typical use cases + load tests.
 */
public final class DittoClientUsageExamples {

    private static final Logger LOGGER = LoggerFactory.getLogger(DittoClientUsageExamples.class);

    private static final String PROPERTIES_FILE = "ditto-client-starter-local.properties"; // for local development
    //    private static final String PROPERTIES_FILE = "ditto-client-starter-sandbox.properties";
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

    public static void main(final String... args) throws ExecutionException, InterruptedException {
        final DittoClient client = DittoClients.newInstance(createMessagingProvider());
        final DittoClient client2 = DittoClients.newInstance(createMessagingProvider());

        final JsonifiableAdaptable jsonifiableAdaptable = ProtocolFactory.jsonifiableAdaptableFromJson(
                JsonFactory.readFrom("{\n" +
                        "  \"topic\": \"com.acme/xdk_53/things/twin/commands/modify\",\n" +
                        "  \"headers\": {},\n" +
                        "  \"path\": \"/\",\n" +
                        "  \"value\": {\n" +
                        "    \"thingId\": \"com.acme:xdk_53\",\n" +
                        "    \"attributes\": {\n" +
                        "      \"location\": {\n" +
                        "        \"latitude\": 44.673856,\n" +
                        "        \"longitude\": 8.261719\n" +
                        "      }\n" +
                        "    },\n" +
                        "    \"features\": {\n" +
                        "      \"accelerometer\": {\n" +
                        "        \"properties\": {\n" +
                        "          \"x\": 3.141,\n" +
                        "          \"y\": 2.718,\n" +
                        "          \"z\": 1,\n" +
                        "          \"unit\": \"g\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}").asObject());
        client.sendDittoProtocol(jsonifiableAdaptable).whenComplete((a, t) -> {
            if (a != null) {
                LOGGER.info("sendDittoProtocol: Received adaptable as response: {}", a);
            }
            if (t != null) {
                LOGGER.warn("sendDittoProtocol: Received throwable as response", t);
            }
        });

        client.twin().startConsumption().get();
        client2.twin().startConsumption().get();
        LOGGER.info("Subscribed for Twin events");

        client.live().startConsumption().get();
        client2.live().startConsumption().get();
        LOGGER.info("Subscribed for Live events/commands/messages");

        System.out.println("\n\nContinuing with TWIN commands/events demo:");
        useTwinCommandsAndEvents(client, client2);
        System.out.println("\n\nFinished with TWIN commands/events demo");

        System.out.println("\n\nAbout to continue with LIVE commands/events demo:");
        promptEnterKey();

        useLiveCommands(client, client2);
        System.out.println("\n\nFinished with LIVE commands/events demo");

        System.out.println("\n\nAbout to continue with LIVE messages demo:");
        promptEnterKey();

        useLiveMessages(client, client2);
        System.out.println("\n\nFinished with LIVE messages demo");
        Thread.sleep(500);

        System.out.println("\n\nAbout to continue with small load test:");
        promptEnterKey();

        final int loadTestThings = 100;
        final int loadTestCount = 10;
        subscribeForLoadTestUpdateChanges(client2, loadTestCount * loadTestThings, false);
        performLoadTestUpdate(client, loadTestCount, loadTestThings, false);
        performLoadTestRead(client, loadTestCount, false);
    }

    private static void useTwinCommandsAndEvents(final DittoClient client, final DittoClient client2)
            throws InterruptedException, ExecutionException {
        final ThingId thingId = ThingId.of(NAMESPACE + ":dummy-" + UUID.randomUUID());

        client2.twin().registerForThingChanges("globalThingChangeHandler", change ->
                LOGGER.info("Received Change on Client 2: {}", change.toString()));

        client.twin().registerForThingChanges("globalThingHandler", change -> {
            if (change.isFull()) {
                LOGGER.warn("Received full Thing change: {}", change);
            } else {
                LOGGER.info("Received Thing change: {}", change);
            }
        });
        client.twin().registerForFeaturesChanges("globalFeaturesHandler", change -> {
            if (change.isFull()) {
                LOGGER.warn("Received full Features change: {}", change);
            } else {
                LOGGER.info("Received Features change: {}", change);
            }
        });
        client.twin().registerForFeatureChanges("globalFeatureHandler", change -> {
            if (change.isFull()) {
                LOGGER.warn("Received full Feature change: {}", change);
            } else {
                LOGGER.info("Received Feature change: {}", change);
            }
        });
        client.twin().registerForFeaturePropertyChanges("globalFeaturePropertyHandler", "foo", change -> {
            if (change.isFull()) {
                LOGGER.warn("Received full Feature property change on feature 'foo': {}", change);
            } else {
                LOGGER.info("Received Feature property change on feature 'foo': {}", change);
            }
        });
        client.twin()
                .registerForFeaturePropertyChanges("globalFeaturePropertyHandlerZzz", "foo", "zzz", change -> {
                    if (change.isFull()) {
                        LOGGER.warn("Received full Feature property change on feature 'foo' for property 'zzz': {}",
                                change);
                    } else {
                        LOGGER.info("Received Feature property change on feature 'foo' for for property 'zzz': {}",
                                change);
                    }
                });
        client.twin().registerForAttributesChanges("globalAttributeHandler", change -> {
            if (change.isFull()) {
                LOGGER.warn("Received full Attribute change: {}", change);
            } else {
                LOGGER.info("Received Attribute change: {}", change);
            }
        });
        client.twin().registerForAttributeChanges("globalAttributeHandlerFoo", "foo", change -> {
            if (change.isFull()) {
                LOGGER.warn("Received full Attribute change for 'foo': {}", change);
            } else {
                LOGGER.info("Received Attribute change for 'foo': {}", change);
            }
        });

        client.twin().create(thingId).handle((createdThing, throwable) -> {
            if (createdThing != null) {
                LOGGER.info("Created new thing: {}", createdThing);
            } else {
                LOGGER.error("Thing could not be created due to: {}", throwable.getMessage());
            }
            return client.twin().forId(thingId).putAttribute("new", OffsetDateTime.now().toString());
        }).get();

        final ThingId newId = ThingId.of(NAMESPACE + ":dummy-" + UUID.randomUUID());
        final ThingBuilder.FromScratch newThingBuilder = Thing.newBuilder().setId(newId);
        final Thing newThing = newThingBuilder.build();
        client.twin().create(newThing).get();

        client.twin().retrieve(JsonFieldSelector.newInstance("thingId"), thingId, newId).thenAccept(things ->
                LOGGER.info("Retrieved Things: {}", things));

        client.twin().delete(thingId).get();

        client.twin().forId(thingId).retrieve().whenComplete((thingAsPersisted, ex) -> {
            LOGGER.info("Thing that should be deleted: {}", thingAsPersisted);
            LOGGER.info("Expected Exception: {}", ex.getMessage());
        });

        final ThingId evenNewerId = ThingId.of(NAMESPACE + ":dummy-" + UUID.randomUUID());
        final Thing thing = ThingsModelFactory.newThingBuilder().setId(evenNewerId).build();

        try {
            client.twin().create(thing).handle((createdThing, throwable) -> {
                if (createdThing != null) {
                    LOGGER.info("Created new thing: {}", createdThing);
                } else {
                    LOGGER.error("Thing could not be created due to: {}", throwable.getMessage());
                }
                return client.twin().forId(evenNewerId).putAttribute("new", OffsetDateTime.now().toString());
            }).get(10, SECONDS);
        } catch (final TimeoutException e) {
            e.printStackTrace();
        }

        try {
            final ThingBuilder.FromScratch dummyThingBuilder =
                    Thing.newBuilder().setId(ThingId.of(NAMESPACE + ":dummy-" + UUID.randomUUID()));
            final Thing dummyThing = dummyThingBuilder.build();
            client.twin().create(dummyThing).get(10, SECONDS);
        } catch (final TimeoutException e) {
            e.printStackTrace();
        }

        // cleanup registrations:
        client2.twin().deregister("globalThingChangeHandler");
        client.twin().deregister("globalThingHandler");
        client.twin().deregister("globalFeaturesHandler");
        client.twin().deregister("globalFeatureHandler");
        client.twin().deregister("globalFeaturePropertyHandler");
        client.twin().deregister("globalFeaturePropertyHandlerZzz");
        client.twin().deregister("globalAttributeHandler");
        client.twin().deregister("globalAttributeHandlerFoo");
    }

    private static void useLiveCommands(final DittoClient backendClient, final DittoClient clientAtDevice)
            throws ExecutionException, InterruptedException {

        final ThingId thingId = ThingId.of(NAMESPACE + ":live-" + UUID.randomUUID().toString());

        backendClient.twin().create(thingId).get();

        // ###
        // ###
        // register for live commands at device and emit live commands in backend

        LOGGER.info("[AT DEVICE] register handler for 'CreateThing' LIVE commands..");
        promptEnterKey();
        clientAtDevice.live()
                .handleCreateThingCommands(command -> {
                    LOGGER.info("[AT DEVICE] Received live command: {}", command.getType());
                    LOGGER.info("[AT DEVICE] Thing to create: {}", command.getThing());
                    LOGGER.info("[AT DEVICE] Answering ...");
                    return command.answer()
                            .withResponse(CreateThingLiveCommandAnswerBuilder.ResponseFactory::created)
                            .withEvent(CreateThingLiveCommandAnswerBuilder.EventFactory::created);
                });

        LOGGER.info("[AT DEVICE] register handler for 'ModifyFeatureProperty' LIVE commands..");
        clientAtDevice.live()
                .forId(thingId)
                .forFeature("temp-sensor")
                .handleModifyFeaturePropertyCommands(command -> {
                    LOGGER.info("[AT DEVICE] Received live command: {}", command.getType());
                    LOGGER.info("[AT DEVICE] Property to modify: '{}' to value: '{}'", command.getPropertyPointer(),
                            command.getPropertyValue());
                    LOGGER.info("[AT DEVICE] Answering ...");
                    return command.answer()
                            .withResponse(ModifyFeaturePropertyLiveCommandAnswerBuilder.ResponseFactory::modified)
                            .withEvent(ModifyFeaturePropertyLiveCommandAnswerBuilder.EventFactory::modified);
                });

        LOGGER.info("[AT BACKEND] create a new LIVE Thing..");
        promptEnterKey();
        backendClient.live()
                .create(thingId)
                .whenComplete((thing, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("[AT BACKEND] Received error when creating the thing.", throwable);
                    } else if (thing.getEntityId().filter(thingId::equals).isPresent()) {
                        LOGGER.info("[AT BACKEND] Successfully created live Thing and got response: {}", thing);
                    } else {
                        LOGGER.warn("[AT BACKEND] Received unexpected thing {}.", thing);
                    }
                });

        LOGGER.info("[AT BACKEND] put 'temperature' property of 'temp-sensor' LIVE Feature..");
        promptEnterKey();
        backendClient.live()
                .forFeature(thingId, "temp-sensor")
                .putProperty("temperature", 23.21)
                .whenComplete((_void, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("[AT BACKEND] Received error when putting the property: {}",
                                throwable.getMessage(), throwable);
                    } else {
                        LOGGER.info("[AT BACKEND] Putting the property succeeded");
                    }
                });

        // ##
        // ##
        // register for live events in backend and emit live events at device

        LOGGER.info("[AT BACKEND] register for LIVE attribute changes of attribute 'location'..");
        promptEnterKey();
        backendClient.live()
                .registerForAttributeChanges("locationHandler", "location", change -> {
                    change.getEntityId();
                    LOGGER.info("[AT BACKEND] Received change of attribute 'location': {}",
                            change.getValue().orElse(null));
                });

        LOGGER.info("[AT BACKEND] register for LIVE feature property changes of feature 'lamp'..");
        backendClient.live()
                .forFeature(thingId, "lamp")
                .registerForPropertyChanges("lampPropertiesHandler",
                        change -> LOGGER.info("[AT BACKEND] Received change of Feature 'lamp' property '{}': {}",
                                change.getPath(),
                                change.getValue().orElse(null)));

        LOGGER.info("[AT DEVICE] Emitting LIVE event AttributeModified for attribute 'location'..");
        promptEnterKey();
        clientAtDevice.live()
                .forId(thingId)
                .emitEvent(thingEventFactory ->
                        thingEventFactory.attributeModified("location",
                                JsonObject.newBuilder()
                                        .set("longitude", 42.123)
                                        .set("latitude", 8.123)
                                        .build()
                        )
                );

        LOGGER.info("[AT DEVICE] Emitting LIVE event 'FeaturePropertyModified' for feature 'lamp', property 'on'..");
        promptEnterKey();
        clientAtDevice.live()
                .forId(thingId)
                .forFeature("lamp")
                .emitEvent(featureEventFactory ->
                        featureEventFactory.featurePropertyModified("on",
                                JsonValue.of(true)
                        )
                );
    }

    private static void useLiveMessages(final DittoClient backendClient, final DittoClient clientAtDevice)
            throws InterruptedException, ExecutionException {
        final ThingId thingId = ThingId.of(NAMESPACE + ":messages-" + UUID.randomUUID());

        // first create Thing:
        backendClient.twin().create(thingId).get();

        LOGGER.info("[AT DEVICE] Registering for messages..");
        promptEnterKey();
        clientAtDevice.live().registerForMessage("globalMessageHandler", "hello.world", message -> {
            LOGGER.warn("[AT DEVICE] Received Message with subject '{}' on Client 2: {}", message.getSubject(),
                    message.toString());
            message.reply().statusCode(HttpStatusCode.IM_A_TEAPOT).payload("Hello Teapot!").send();
        });
        clientAtDevice.live().registerForClaimMessage("globalClaimMessageHandler", String.class, claimMessage -> {
            LOGGER.info("[AT DEVICE] Received Claim Message on Client 2: '{}'", claimMessage.getPayload().orElse(null));
            claimMessage.reply().statusCode(HttpStatusCode.OK).payload("claim-acked").send();
        });

        LOGGER.info("[AT BACKEND] sending '{}' message..", KnownMessageSubjects.CLAIM_SUBJECT);
        promptEnterKey();
        backendClient.live().message().to(thingId).subject(KnownMessageSubjects.CLAIM_SUBJECT).payload("please-claim")
                .send(String.class, (response, throwable) -> {
                    if (response != null) {
                        LOGGER.info("[AT BACKEND] Received message response for {} message: '{}' - with headers '{}'",
                                KnownMessageSubjects.CLAIM_SUBJECT,
                                response.getPayload().orElse(null), response.getHeaders());
                    } else {
                        LOGGER.error("[AT BACKEND] Got error when expecting a claim response: {}",
                                throwable.getMessage(),
                                throwable);
                    }
                });

        LOGGER.info("[AT BACKEND] sending message with subject 'hello.world' ..");
        promptEnterKey();
        backendClient.live().forId(thingId).message().from().subject("hello.world").payload("I am a Teapot")
                .send(String.class, (response, throwable) ->
                        LOGGER.info("[AT BACKEND] Got response to subject '{}': '{}'", response.getSubject(),
                                response.getPayload().orElse(null)));
    }

    private static void performLoadTestUpdate(final DittoClient client, final int updateCount,
            final int thingCount, final boolean log) {

        final JsonObject attributesExample = JsonFactory.newObjectBuilder()
                .set("maker", "ACME Inc.")
                .set("VIN", UUID.randomUUID().toString())
                .set("rev", Math.random() * 1000.0)
                .build();

        final FeatureProperties featurePropertiesExample = FeatureProperties.newBuilder()
                .set("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 32489324)
                .set("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", false)
                .set("ccccccccccccccccccccccccccccccccc",
                        UUID.randomUUID().toString() + ":" + UUID.randomUUID().toString())
                .build();

        final ExecutorService executorService = Executors.newFixedThreadPool(16);
        for (int k = 1; k <= thingCount; k++) {
            final int thingIdx = k;
            executorService.execute(() ->
            {
                final ThingId thingId = ThingId.of(NAMESPACE + ":load-" + thingIdx + "-" + UUID.randomUUID());
                final Thing thing = Thing.newBuilder().setId(thingId)
                        .setAttributes(attributesExample)
                        .setFeature("the-feature", featurePropertiesExample)
                        .build();
                try {
                    client.twin().create(thing).get(10, TimeUnit.SECONDS);
                } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                    throw new IllegalStateException(e);
                }
                if (log) {
                    LOGGER.info("performLoadTestUpdate: Created new thing: {}", thing);
                }

                final long startTs = System.nanoTime();
                final AtomicInteger integer = new AtomicInteger(updateCount);
                for (int i = updateCount; i >= 0; i--) {
                    final int counter = i;
                    final long startTs2 = System.nanoTime();
                    client.twin().forId(thingId).putAttribute("counter", counter,
                            Options.Modify.responseRequired(false)).whenComplete((_void, throwable) ->
                    {
                        if (throwable != null) {
                            LOGGER.warn("performLoadTestUpdate: Updating attribute failed: {}", throwable.getMessage());
                        } else {
                            final double duration = getDuration(startTs2);
                            if (log) {
                                LOGGER.info("performLoadTestUpdate: Single update request ({}) latency: {}ms",
                                        counter, duration);
                            }
                            integer.decrementAndGet();
                        }
                    });
                }

                while (integer.get() > 0) {
                    try {
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }
                final double duration = getDuration(startTs);
                LOGGER.info("performLoadTestUpdate: Finished updating '{}' attributes after {}ms - " +
                        "that are ~{}req/s", updateCount, duration, (int) (updateCount / duration * 1000));
            });
        }
    }

    private static void subscribeForLoadTestUpdateChanges(final DittoClient client, final int count,
            final boolean log) {

        final long startTs = System.nanoTime();
        final AtomicInteger integer = new AtomicInteger(count);
        client.twin().registerForThingChanges("loadTestChanges", change -> {
            if (change.getAction() == ChangeAction.UPDATED) {
                if (log) {
                    LOGGER.info("subscribeForLoadTestUpdateChanges: Single change ({}) latency: {}ms",
                            integer.get(), getDuration(startTs));
                }

                if (integer.getAndDecrement() == 1) {
                    final double duration2 = getDuration(startTs);
                    LOGGER.info("subscribeForLoadTestUpdateChanges: Finished receiving '{}' changes after {}ms -" +
                            " that are ~{}changes/s", count, duration2, (int) (count / duration2 * 1000));
                }
            }
        });
    }

    private static double getDuration(final long startTimeStamp) {
        final int nanosecondsToMillisecondsFactor = 1_000_000;
        return (double) (System.nanoTime() - startTimeStamp) / nanosecondsToMillisecondsFactor;
    }

    private static void performLoadTestRead(final DittoClient client, final int count, final boolean log)
            throws InterruptedException, ExecutionException {

        final ThingId thingId = ThingId.of(NAMESPACE + ":load-read-" + UUID.randomUUID());
        client.twin().create(thingId).get();
        if (log) {
            LOGGER.info("performLoadTestRead: Created new thing: {}", thingId);
        }

        final long startTs = System.nanoTime();
        final AtomicInteger integer = new AtomicInteger(count);
        for (int i = count; i >= 0; i--) {
            final int counter = i;
            final long startTs2 = System.nanoTime();
            client.twin()
                    .forId(thingId)
                    .retrieve(JsonFieldSelector.newInstance("thingId"))
                    .thenAccept(thing -> {
                        if (log) {
                            LOGGER.info("performLoadTestRead: Single read request ({}) latency: {}ms", counter,
                                    getDuration(startTs2));
                        }
                        integer.decrementAndGet();
                    });
        }
        client.twin().forId(thingId).putAttribute("finished", true);

        while (integer.get() > 0) {
            Thread.sleep(10);
        }
        final double duration = getDuration(startTs);
        LOGGER.info("performLoadTestRead: Finished retrieving '{}' thingIds after {}ms - that are ~{}req/s", count,
                duration, (int) (count / duration * 1000));
    }

    private static void promptEnterKey() throws InterruptedException {
        Thread.sleep(500);
        System.out.println("Press \"ENTER\" to continue...");
        final Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private static MessagingProvider createMessagingProvider() {
        final MessagingConfiguration.Builder builder = WebSocketMessagingConfiguration.newBuilder()
                .endpoint(DITTO_ENDPOINT_URL)
                .jsonSchemaVersion(JsonSchemaVersion.V_1);

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
        } else if (DITTO_OAUTH_CLIENT_ID != null) {
            final ClientCredentialsAuthenticationConfiguration.Builder authenticationConfigurationBuilder =
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
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
