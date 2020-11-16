/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.model.base.acks.AcknowledgementRequest.parseAcknowledgementRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.twin.internal.UncompletedTwinConsumptionRequestException;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.junit.Test;

/**
 * Test twin channel interactions not covered by tests conforming to the live-channel interaction patterns
 * namely:
 * - subscribe for event, send acknowledgement
 */
public final class DittoClientTwinTest extends AbstractDittoClientTest {

    @Test
    public void testThingEventAcknowledgement() {
        client.twin().startConsumption();
        client.twin().registerForThingChanges("Ackermann", change ->
                change.handleAcknowledgementRequests(handles ->
                        handles.forEach(handle -> handle.acknowledge(
                                HttpStatusCode.forInt(Integer.parseInt(handle.getAcknowledgementLabel().toString()))
                                        .orElse(HttpStatusCode.EXPECTATION_FAILED)
                        ))
                )
        );
        // expect subscription messages
        assertThat(expectMsgClass(String.class)).startsWith("START-SEND-");

        reply(ThingDeleted.of(THING_ID, 1L, DittoHeaders.newBuilder()
                .channel(TopicPath.Channel.TWIN.getName())
                .acknowledgementRequest(
                        parseAcknowledgementRequest("100"),
                        parseAcknowledgementRequest("301"),
                        parseAcknowledgementRequest("403")
                )
                .build()
        ));

        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.CONTINUE);
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.MOVED_PERMANENTLY);
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.FORBIDDEN);
    }

    @Test
    public void testAttributeEventAcknowledgement() {
        client.twin().startConsumption();
        client.twin().registerForAttributesChanges("Attributes", change ->
                change.handleAcknowledgementRequests(handles ->
                        handles.forEach(handle -> handle.acknowledge(
                                HttpStatusCode.forInt(Integer.parseInt(handle.getAcknowledgementLabel().toString()))
                                        .orElse(HttpStatusCode.EXPECTATION_FAILED)
                        ))
                )
        );
        // expect subscription messages
        assertThat(expectMsgClass(String.class)).startsWith("START-SEND-");

        reply(AttributeCreated.of(THING_ID, JsonPointer.of("hello"), JsonValue.of("World"), 5L,
                DittoHeaders.newBuilder()
                        .channel(TopicPath.Channel.TWIN.getName())
                        .acknowledgementRequest(
                                parseAcknowledgementRequest("200"),
                                parseAcknowledgementRequest("403"),
                                parseAcknowledgementRequest("500")
                        )
                        .build())
        );
        Assertions.assertThat(expectMsgClass(Acknowledgement.class).getStatusCode())
                .isEqualTo(HttpStatusCode.OK);
        Assertions.assertThat(expectMsgClass(Acknowledgement.class).getStatusCode())
                .isEqualTo(HttpStatusCode.FORBIDDEN);
        Assertions.assertThat(expectMsgClass(Acknowledgement.class).getStatusCode())
                .isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void startConsumptionParallelOnSameTwinChannelShouldThrowException()
            throws InterruptedException, ExecutionException, TimeoutException {

        final CompletableFuture<Void> voidCompletableFuture1 = client.twin().startConsumption();
        final CompletableFuture<Void> voidCompletableFuture2 = client.twin().startConsumption();

        messaging.receivePlainString("START-SEND-EVENTS:ACK");

        voidCompletableFuture1.get(10, TimeUnit.SECONDS);
        assertThatExceptionOfType(ExecutionException.class)
                .isThrownBy(() -> voidCompletableFuture2.get(10, TimeUnit.SECONDS))
                .withCauseInstanceOf(UncompletedTwinConsumptionRequestException.class);
    }

    @Test
    public void testFeatureEventAcknowledgement() {
        client.twin().startConsumption();
        client.twin().registerForFeatureChanges("Features", change ->
                change.handleAcknowledgementRequests(handles ->
                        handles.forEach(handle -> handle.acknowledge(
                                HttpStatusCode.forInt(Integer.parseInt(handle.getAcknowledgementLabel().toString()))
                                        .orElse(HttpStatusCode.EXPECTATION_FAILED)
                        ))
                )
        );
        // expect subscription messages
        assertThat(expectMsgClass(String.class)).startsWith("START-SEND-");

        reply(FeaturePropertyModified.of(THING_ID, "featureId", JsonPointer.of("hello"), JsonValue.of("World"), 5L,
                DittoHeaders.newBuilder()
                        .channel(TopicPath.Channel.TWIN.getName())
                        .acknowledgementRequest(
                                parseAcknowledgementRequest("409"),
                                parseAcknowledgementRequest("201"),
                                parseAcknowledgementRequest("403")
                        )
                        .build())
        );
        Assertions.assertThat(expectMsgClass(Acknowledgement.class).getStatusCode())
                .isEqualTo(HttpStatusCode.CONFLICT);
        Assertions.assertThat(expectMsgClass(Acknowledgement.class).getStatusCode())
                .isEqualTo(HttpStatusCode.CREATED);
        Assertions.assertThat(expectMsgClass(Acknowledgement.class).getStatusCode())
                .isEqualTo(HttpStatusCode.FORBIDDEN);
    }
}
