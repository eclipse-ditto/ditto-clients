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

import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;
import static org.eclipse.ditto.model.base.acks.AcknowledgementRequest.parseAcknowledgementRequest;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.eclipse.ditto.client.internal.AbstractDittoClientThingsTest;
import org.eclipse.ditto.client.management.AcknowledgementsFailedException;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;
import org.eclipse.ditto.signals.acks.base.Acknowledgements;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeature;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturePropertiesResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturePropertyResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturesResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeature;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturePropertiesResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturePropertyResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturesResponse;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeature;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureResponse;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.junit.Test;

/**
 * Test feature-related operations of the {@link DittoClient}.
 */
public final class DittoClientFeaturesTest extends AbstractDittoClientThingsTest {

    private static final String FEATURE_ID = "someFeature";
    private static final Feature FEATURE = ThingsModelFactory.newFeatureBuilder()
            .properties(ThingsModelFactory.newFeaturePropertiesBuilder()
                    .set("propertyPointer", "propertyValue")
                    .build())
            .withId(FEATURE_ID)
            .build();

    @Test
    public void testSetFeatureWithExistsOptionFalse() {
        assertEventualCompletion(
                getManagement().forId(THING_ID).putFeature(FEATURE, Options.Modify.exists(false))
        );
        final ModifyFeature command = expectMsgClass(ModifyFeature.class);
        reply(ModifyFeatureResponse.created(THING_ID, FEATURE, command.getDittoHeaders()));
        assertThat(command.getFeatureId()).isEqualTo(FEATURE_ID);
        assertOnlyIfNoneMatchHeader(command);
    }

    @Test
    public void testSetFeatureWithExistsOptionTrue() {
        assertEventualCompletion(
                getManagement().forId(THING_ID).putFeature(FEATURE, Options.Modify.exists(true))
        );
        final ModifyFeature command = expectMsgClass(ModifyFeature.class);
        reply(ModifyFeatureResponse.modified(THING_ID, FEATURE_ID, command.getDittoHeaders()));
        assertThat(command.getFeatureId()).isEqualTo(FEATURE_ID);
        assertOnlyIfMatchHeader(command);
    }

    @Test
    public void testDeleteFeature() {
        assertEventualCompletion(getManagement().forId(THING_ID).deleteFeature(FEATURE_ID));
        reply(DeleteFeatureResponse.of(THING_ID, FEATURE_ID, expectMsgClass(DeleteFeature.class).getDittoHeaders()));
    }

    @Test
    public void testDeleteFeatures() {
        assertEventualCompletion(getManagement().forId(THING_ID).deleteFeatures());
        final Signal<?> command = expectMsgClass(DeleteFeatures.class);
        reply(DeleteFeaturesResponse.of(THING_ID, command.getDittoHeaders()));
    }

    @Test
    public void testRetrieveFeature() throws Exception {
        final CompletableFuture<Feature> featureFuture =
                getManagement().forId(THING_ID).forFeature(FEATURE_ID).retrieve();
        reply(RetrieveFeatureResponse.of(THING_ID, FEATURE, expectMsgClass(RetrieveFeature.class).getDittoHeaders()));
        final Feature retrievedFeature = featureFuture.get(1L, TimeUnit.SECONDS);
        assertThat(retrievedFeature).isEqualTo(FEATURE);
    }

    @Test
    public void testSetFeatures() {
        assertEventualCompletion(getManagement().forId(THING_ID).setFeatures(ThingsModelFactory.newFeatures(FEATURE)));
        reply(ModifyFeaturesResponse.modified(THING_ID, expectMsgClass(ModifyFeatures.class).getDittoHeaders()));
    }

    @Test
    public void setFeatureDefinition() {
        final FeatureDefinition definition = FeatureDefinition.fromIdentifier("org.eclipse.ditto:test:0.1.0");
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .forFeature(FEATURE_ID)
                        .setDefinition(definition)
        );
        final ModifyFeatureDefinition command = expectMsgClass(ModifyFeatureDefinition.class);
        reply(ModifyFeatureDefinitionResponse.modified(THING_ID, FEATURE_ID, command.getDittoHeaders()));
        Assertions.assertThat(command.getDefinition()).isEqualTo(definition);
    }

    @Test
    public void deleteFeatureDefinition() {
        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .forFeature(FEATURE_ID)
                        .deleteDefinition()
        );
        reply(DeleteFeatureDefinitionResponse.of(THING_ID, FEATURE_ID,
                expectMsgClass(DeleteFeatureDefinition.class).getDittoHeaders()));
    }

    @Test
    public void testSetFeatureProperty() {
        final JsonPointer path = JsonFactory.newPointer("density");
        final int value = 42;
        assertEventualCompletion(
                getManagement().forId(THING_ID).forFeature(FEATURE_ID).putProperty(path, value)
        );
        reply(ModifyFeaturePropertyResponse.modified(THING_ID, FEATURE_ID, path,
                expectMsgClass(ModifyFeatureProperty.class).getDittoHeaders()));
    }

    @Test
    public void testDeleteFeatureProperty() {
        final JsonPointer path = JsonFactory.newPointer("density");
        assertEventualCompletion(
                getManagement().forId(THING_ID).forFeature(FEATURE_ID).deleteProperty(path)
        );
        reply(DeleteFeaturePropertyResponse.of(THING_ID, FEATURE_ID, path,
                expectMsgClass(DeleteFeatureProperty.class).getDittoHeaders()));
    }

    @Test
    public void testSetFeatureProperties() {
        final JsonObject properties = JsonFactory.newObjectBuilder().set("density", 42).build();
        assertEventualCompletion(getManagement().forId(THING_ID).forFeature(FEATURE_ID).setProperties(properties));
        reply(ModifyFeaturePropertiesResponse.modified(THING_ID, FEATURE_ID,
                expectMsgClass(ModifyFeatureProperties.class).getDittoHeaders()));
    }

    @Test
    public void testDeleteFeatureProperties() {
        assertEventualCompletion(getManagement().forId(THING_ID).forFeature(FEATURE_ID).deleteProperties());
        reply(DeleteFeaturePropertiesResponse.of(THING_ID, FEATURE_ID,
                expectMsgClass(DeleteFeatureProperties.class).getDittoHeaders()));
    }

    @Test
    public void testDeleteFeatureWith2Acknowledgements() {
        // skip this test for LIVE - 'twin-persisted' is obligatory
        Assumptions.assumeThat(channel).isEqualTo(TopicPath.Channel.TWIN);

        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        final AcknowledgementLabel label2 = AcknowledgementLabel.of("twin-persisted");
        assertEventualCompletion(getManagement().forId(THING_ID).forFeature(FEATURE_ID)
                .delete(Options.headers(DittoHeaders.newBuilder()
                        .acknowledgementRequest(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2))
                        .build())
                ));

        final DittoHeaders sentDittoHeaders = expectMsgClass(DeleteFeature.class).getDittoHeaders();
        reply(Acknowledgements.of(
                Arrays.asList(
                        Acknowledgement.of(label1, THING_ID, HttpStatusCode.OK, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatusCode.ACCEPTED, DittoHeaders.empty())
                ),
                sentDittoHeaders
        ));

        assertThat(sentDittoHeaders.getAcknowledgementRequests())
                .containsExactly(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2));
    }

    @Test
    public void testDeleteFeaturePropertiesWithFailedAcknowledgements() {
        // skip this test for LIVE - 'twin-persisted' is obligatory
        Assumptions.assumeThat(channel).isEqualTo(TopicPath.Channel.TWIN);

        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        final AcknowledgementLabel label2 = AcknowledgementLabel.of("twin-persisted");
        final Acknowledgements expectedAcknowledgements = Acknowledgements.of(
                Arrays.asList(
                        Acknowledgement.of(label1, THING_ID, HttpStatusCode.FORBIDDEN, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatusCode.ACCEPTED, DittoHeaders.empty())
                ),
                DittoHeaders.empty()
        );
        assertEventualCompletion(getManagement().forFeature(THING_ID, FEATURE_ID)
                .deleteProperties(Options.headers(DittoHeaders.newBuilder()
                        .acknowledgementRequest(
                                AcknowledgementRequest.of(label1),
                                AcknowledgementRequest.of(label2))
                        .build()))
                .exceptionally(error -> {
                    assertThat(error).isInstanceOf(CompletionException.class)
                            .hasCauseInstanceOf(AcknowledgementsFailedException.class);
                    final AcknowledgementsFailedException cause = (AcknowledgementsFailedException) error.getCause();
                    Assertions.assertThat(cause.getAcknowledgements().setDittoHeaders(DittoHeaders.empty()))
                            .isEqualTo(expectedAcknowledgements);
                    return null;
                })
        );

        final DittoHeaders sentDittoHeaders = expectMsgClass(DeleteFeatureProperties.class).getDittoHeaders();
        reply(expectedAcknowledgements.setDittoHeaders(sentDittoHeaders));

        assertThat(sentDittoHeaders.getAcknowledgementRequests())
                .containsExactly(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2));
    }

    @Test
    public void testEventAcknowledgement() {
        // Acknowledgements are not implemented for live signals yet
        Assumptions.assumeThat(channel).isEqualTo(TopicPath.Channel.TWIN);

        getManagement().startConsumption();
        getManagement().registerForFeatureChanges("Features", change ->
                change.handleAcknowledgementRequests(handles ->
                        handles.forEach(handle -> handle.acknowledge(
                                HttpStatusCode.forInt(Integer.parseInt(handle.getAcknowledgementLabel().toString()))
                                        .orElse(HttpStatusCode.EXPECTATION_FAILED)
                        ))
                )
        );
        // expect subscription messages
        assertThat(expectMsgClass(String.class)).startsWith("START-SEND-");

        reply(FeaturePropertyModified.of(THING_ID, FEATURE_ID, JsonPointer.of("hello"), JsonValue.of("World"), 5L,
                DittoHeaders.newBuilder()
                        .channel(channel.name())
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
