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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientThingsTest;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.ThingsModelFactory;
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

}
