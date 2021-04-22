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

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientThingsTest;
import org.eclipse.ditto.client.management.AcknowledgementsFailedException;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.acks.AcknowledgementRequest;
import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.FeatureDefinition;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgement;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgements;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDefinitionResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeaturePropertiesResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeaturePropertyResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeaturesResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThingResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinitionResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeaturePropertiesResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeaturePropertyResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeaturesResponse;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeature;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureResponse;
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
    public void testMergeFeatureWithExistsOptionFalse() {
        final JsonPointer path = Thing.JsonFields.FEATURES.getPointer().append(JsonPointer.of(FEATURE_ID));

        assertEventualCompletion(
                getManagement().forId(THING_ID).mergeFeature(FEATURE, Options.Modify.exists(false))
        );
        final MergeThing command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID,
                path,
                command.getDittoHeaders()));
        assertThat(command.getResourcePath().toFieldSelector())
                .isEqualTo(path.toFieldSelector());
        assertOnlyIfNoneMatchHeader(command);
    }

    @Test
    public void testMergeFeatureWithExistsOptionTrue() {
        final JsonPointer path = Thing.JsonFields.FEATURES.getPointer().append(JsonPointer.of(FEATURE_ID));

        assertEventualCompletion(
                getManagement().forId(THING_ID).mergeFeature(FEATURE, Options.Modify.exists(true))
        );
        final MergeThing command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID,
                path,
                command.getDittoHeaders()));
        assertThat(command.getResourcePath().toFieldSelector())
                .isEqualTo(path.toFieldSelector());
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
        final CompletionStage<Feature> featureFuture =
                getManagement().forId(THING_ID).forFeature(FEATURE_ID).retrieve();
        reply(RetrieveFeatureResponse.of(THING_ID, FEATURE, expectMsgClass(RetrieveFeature.class).getDittoHeaders()));
        final Feature retrievedFeature = featureFuture.toCompletableFuture().get(1L, TimeUnit.SECONDS);
        assertThat(retrievedFeature).isEqualTo(FEATURE);
    }

    @Test
    public void testSetFeatures() {
        assertEventualCompletion(getManagement().forId(THING_ID).setFeatures(ThingsModelFactory.newFeatures(FEATURE)));
        reply(ModifyFeaturesResponse.modified(THING_ID, expectMsgClass(ModifyFeatures.class).getDittoHeaders()));
    }

    @Test
    public void testMergeFeatures() {
        final Features features = ThingsModelFactory.newFeatures(FEATURE);
        assertEventualCompletion(getManagement().forId(THING_ID).mergeFeatures(features));
        reply(MergeThingResponse.of(THING_ID,
                Thing.JsonFields.FEATURES.getPointer(),
                expectMsgClass(MergeThing.class).getDittoHeaders()));
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
    public void mergeFeatureDefinition() {
        final FeatureDefinition definition = FeatureDefinition.fromIdentifier("org.eclipse.ditto:test:0.1.0");
        final JsonPointer path = Thing.JsonFields.FEATURES.getPointer()
                .append(JsonPointer.of(FEATURE_ID))
                .append(Feature.JsonFields.DEFINITION.getPointer());

        assertEventualCompletion(
                getManagement()
                        .forId(THING_ID)
                        .forFeature(FEATURE_ID)
                        .mergeDefinition(definition)
        );
        final MergeThing command = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID,
                path,
                command.getDittoHeaders()));
        Assertions.assertThat(FeatureDefinition.fromJson(command.getValue().asArray())).isEqualTo(definition);
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
    public void testMergeFeatureProperty() {
        final JsonPointer path = JsonFactory.newPointer("density");
        final JsonPointer completePath = Thing.JsonFields.FEATURES.getPointer()
                .append(JsonPointer.of(FEATURE_ID))
                .append(Feature.JsonFields.PROPERTIES.getPointer())
                .append(ThingsModelFactory.validateFeaturePropertyPointer(path));

        final int value = 42;
        assertEventualCompletion(
                getManagement().forId(THING_ID).forFeature(FEATURE_ID).mergeProperty(path, value)
        );
        final MergeThing mergeThing = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID,
                completePath,
                mergeThing.getDittoHeaders()));
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
    public void testMergeFeatureProperties() {
        final JsonPointer path = Thing.JsonFields.FEATURES.getPointer()
                .append(JsonPointer.of(FEATURE_ID))
                .append(Feature.JsonFields.PROPERTIES.getPointer());

        final JsonObject properties = JsonFactory.newObjectBuilder().set("density", 42).build();
        assertEventualCompletion(getManagement().forId(THING_ID).forFeature(FEATURE_ID).mergeProperties(properties));

        final MergeThing mergeThing = expectMsgClass(MergeThing.class);
        reply(MergeThingResponse.of(THING_ID, path,
                mergeThing.getDittoHeaders()));
    }

    @Test
    public void testDeleteFeatureProperties() {
        assertEventualCompletion(getManagement().forId(THING_ID).forFeature(FEATURE_ID).deleteProperties());
        reply(DeleteFeaturePropertiesResponse.of(THING_ID, FEATURE_ID,
                expectMsgClass(DeleteFeatureProperties.class).getDittoHeaders()));
    }

    @Test
    public void testDeleteFeatureWith2Acknowledgements() {
        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        final AcknowledgementLabel label2 = getChannelAcknowledgementLabel();
        assertEventualCompletion(getManagement().forId(THING_ID).forFeature(FEATURE_ID)
                .delete(Options.headers(DittoHeaders.newBuilder()
                        .acknowledgementRequest(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2))
                        .build())
                ));

        final DittoHeaders sentDittoHeaders = expectMsgClass(DeleteFeature.class).getDittoHeaders();
        reply(Acknowledgements.of(
                Arrays.asList(
                        Acknowledgement.of(label1, THING_ID, HttpStatus.OK, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatus.ACCEPTED, DittoHeaders.empty())
                ),
                sentDittoHeaders
        ));

        assertThat(sentDittoHeaders.getAcknowledgementRequests())
                .containsExactly(AcknowledgementRequest.of(label1), AcknowledgementRequest.of(label2));
    }

    @Test
    public void testDeleteFeaturePropertiesWithFailedAcknowledgements() {
        final AcknowledgementLabel label1 = AcknowledgementLabel.of("custom-ack-1");
        final AcknowledgementLabel label2 = getChannelAcknowledgementLabel();
        final Acknowledgements expectedAcknowledgements = Acknowledgements.of(
                Arrays.asList(
                        Acknowledgement.of(label1, THING_ID, HttpStatus.FORBIDDEN, DittoHeaders.empty()),
                        Acknowledgement.of(label2, THING_ID, HttpStatus.ACCEPTED, DittoHeaders.empty())
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
}
