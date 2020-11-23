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
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.model.base.acks.AcknowledgementRequest.parseAcknowledgementRequest;

import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.InvalidRqlExpressionException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.junit.Test;

/**
 * Test twin channel interactions not covered by tests conforming to the live-channel interaction patterns
 * namely:
 * - subscribe for event, send acknowledgement
 */
public final class DittoClientTwinTest extends AbstractConsumptionDittoClientTest {

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
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.OK);
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.FORBIDDEN);
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(
                HttpStatusCode.INTERNAL_SERVER_ERROR);
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
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.CONFLICT);
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.CREATED);
        assertThat(expectMsgClass(Acknowledgement.class).getStatusCode()).isEqualTo(HttpStatusCode.FORBIDDEN);
    }

    @Override
    protected CompletableFuture<Void> startConsumptionRequest() {
        return client.twin().startConsumption();
    }

    @Override
    protected void replyToConsumptionRequest() {
        expectProtocolMsgWithCorrelationId("START-SEND-EVENTS");
        reply("START-SEND-EVENTS:ACK");
    }

    @Override
    protected void startConsumptionAndExpectError() {
        final CompletableFuture<Void> future = client.twin().startConsumption();
        final String protocolMessage = messaging.expectEmitted();
        final String correlationId = determineCorrelationId(protocolMessage);
        final DittoHeaders dittoHeaders = DittoHeaders.newBuilder().correlationId(correlationId).build();
        final InvalidRqlExpressionException invalidRqlExpressionException = InvalidRqlExpressionException.newBuilder()
                .message("Invalid filter.")
                .dittoHeaders(dittoHeaders)
                .build();
        reply(ThingErrorResponse.of(invalidRqlExpressionException));
        assertFailedCompletion(future, InvalidRqlExpressionException.class);
    }

    @Override
    protected void startConsumptionSucceeds() {
        final CompletableFuture<Void> future = client.twin().startConsumption();
        reply(Classification.StreamingType.TWIN_EVENT.startAck());
        future.join();
    }

}
