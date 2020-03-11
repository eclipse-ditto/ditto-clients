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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_ID;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.messages.SendClaimMessage;
import org.eclipse.ditto.signals.commands.messages.SendClaimMessageResponse;
import org.junit.Test;

/**
 * Test live channel interactions not covered by tests conforming to the twin-channel interaction patterns
 * (send-command-get-response, subscribe-for-events), namely:
 * - send message, receive response
 * - subscribe for command, send response
 * - subscribe for message, send response
 * - emit event
 * <p>
 * Each pattern is to be tested against a thing and against a feature.
 */
public final class DittoClientLiveTest extends AbstractDittoClientTest {

    private static final String FEATURE_ID = "someFeature";
    private static final JsonPointer ATTRIBUTE_KEY_NEW = JsonFactory.newPointer("new");
    private static final String ATTRIBUTE_VALUE = "value";
    private static final Feature FEATURE = ThingsModelFactory.newFeatureBuilder()
            .properties(ThingsModelFactory.newFeaturePropertiesBuilder()
                    .set("propertyPointer", "propertyValue")
                    .build())
            .withId(FEATURE_ID)
            .build();

    private static final Thing THING = ThingsModelFactory.newThingBuilder()
            .setId(THING_ID)
            .setPolicyId(POLICY_ID)
            .setAttribute(ATTRIBUTE_KEY_NEW, JsonFactory.newValue(ATTRIBUTE_VALUE))
            .setFeature(FEATURE)
            .build();

    @Test
    public void sendClaimMessageReceiveResponse() {
        final String subject = "claim";
        final String payload = "THOU BELONGEST TO ME!";
        final String responsePayload = "THOU WISHEST!";
        final CountDownLatch latch = new CountDownLatch(1);

        // TODO: decide if should demand startConsumption() before sending a message with response consumer
        assertEventualCompletion(startConsumption());

        client.live().message().to(THING_ID).subject(subject).payload(payload).send((response, error) -> {
            assertThat(response.getSubject()).isEqualTo(subject);
            assertThat(response.getStatusCode()).contains(HttpStatusCode.PAYMENT_REQUIRED);
            // TODO: fix type error: getPayload() returns Optional<String>
            assertThat(response.getPayload()).isEqualTo(Optional.of(responsePayload));
            latch.countDown();
        });
        final SendClaimMessage<?> sendClaimMessage = expectMsgClass(SendClaimMessage.class);
        final String correlationId = sendClaimMessage.getDittoHeaders().getCorrelationId().orElse(null);
        final SendClaimMessageResponse<?> response = SendClaimMessageResponse.of(sendClaimMessage.getEntityId(),
                newMessageBuilder(THING_ID, subject, correlationId)
                        .payload(responsePayload)
                        .build(),
                HttpStatusCode.PAYMENT_REQUIRED,
                sendClaimMessage.getDittoHeaders());
        reply(response);
        waitForCountDown(latch);
    }

    private void waitForCountDown(final CountDownLatch latch) {
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (final Throwable error) {
            throw new AssertionError(error);
        }
        assertThat(latch.getCount()).isEqualTo(0L);
    }

    private CompletableFuture<Void> startConsumption() {
        final CompletableFuture<Void> result = client.live().startConsumption();
        expectMsg("START-SEND-LIVE-EVENTS");
        expectMsg("START-SEND-MESSAGES");
        expectMsg("START-SEND-LIVE-COMMANDS");
        reply("START-SEND-LIVE-EVENTS:ACK");
        reply("START-SEND-MESSAGES:ACK");
        reply("START-SEND-LIVE-COMMANDS:ACK");
        return result;
    }
}
