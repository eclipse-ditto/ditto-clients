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

import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeature;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeature;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures;
import org.junit.Test;

/**
 * Test feature-related operations of the {@link DittoClient}.
 */
public final class DittoClientFeaturesTest extends AbstractDittoClientTest {

    private static final String FEATURE_ID = "someFeature";
    private static final Feature FEATURE = ThingsModelFactory.newFeatureBuilder()
            .properties(ThingsModelFactory.newFeaturePropertiesBuilder()
                    .set("propertyPointer", "propertyValue")
                    .build())
            .withId(FEATURE_ID)
            .build();

    @Test
    public void testSetFeatureWithoutExistsOption() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(ModifyFeature.TYPE)
                    .hasNoConditionalHeaders();

            latch.countDown();
        });

        client.twin().forId(THING_ID).putFeature(FEATURE);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testSetFeatureWithExistsOptionFalse() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(ModifyFeature.TYPE)
                    .hasOnlyIfNoneMatchHeader();

            latch.countDown();
        });

        client.twin().forId(THING_ID).putFeature(FEATURE, Options.Modify.exists(false));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testSetFeatureWithExistsOptionTrue() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(ModifyFeature.TYPE)
                    .hasOnlyIfMatchHeader();

            latch.countDown();
        });

        client.twin().forId(THING_ID).putFeature(FEATURE, Options.Modify.exists(true));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testDeleteFeature() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(DeleteFeature.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).deleteFeature(FEATURE.getId());

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testSetEmptyFeatures() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyFeatures.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).setFeatures(ThingsModelFactory.emptyFeatures());

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testDeleteFeatures() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(DeleteFeatures.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).deleteFeatures();

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testSetFeatures() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasSubject(ModifyFeatures.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).setFeatures(ThingsModelFactory.newFeatures(FEATURE));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void setFeatureDefinition() throws InterruptedException {
        final FeatureDefinition definition = FeatureDefinition.fromIdentifier("org.eclipse.ditto:test:0.1.0");
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        messaging.onSend(message -> {
            assertThat(message)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(ModifyFeatureDefinition.TYPE);

            final Optional<?> payloadOptional = message.getPayload();

            Assertions.assertThat(payloadOptional).isPresent();

            final Object payload = payloadOptional.get();

            Assertions.assertThat(payload).isInstanceOf(ModifyFeatureDefinition.class);

            final ModifyFeatureDefinition modifyFeatureDefinition = (ModifyFeatureDefinition) payload;
            final FeatureDefinition actualDefinition = modifyFeatureDefinition.getDefinition();

            Assertions.assertThat(actualDefinition).isEqualTo(definition);

            countDownLatch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .forFeature(FEATURE_ID)
                .setDefinition(definition);

        Assertions.assertThat(countDownLatch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void deleteFeatureDefinition() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        messaging.onSend(message -> {
            assertThat(message)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(DeleteFeatureDefinition.TYPE);

            countDownLatch.countDown();
        });

        client.twin()
                .forId(THING_ID)
                .forFeature(FEATURE_ID)
                .deleteDefinition();

        Assertions.assertThat(countDownLatch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testSetFeatureProperty() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final String path = "density";
        final int value = 42;

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(ModifyFeatureProperty.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).forFeature(FEATURE_ID).putProperty(JsonFactory.newPointer(path), value);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testDeleteFeatureProperty() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final String path = "density";

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(DeleteFeatureProperty.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).forFeature(FEATURE_ID).deleteProperty(JsonFactory.newPointer(path));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testSetFeatureProperties() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final JsonObject properties = JsonFactory.newObjectBuilder().set("density", 42).build();

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(ModifyFeatureProperties.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).forFeature(FEATURE_ID).setProperties(properties);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testDeleteFeatureProperties() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onSend(m -> {
            assertThat(m)
                    .hasThingId(THING_ID)
                    .hasFeatureId(FEATURE_ID)
                    .hasSubject(DeleteFeatureProperties.TYPE);

            latch.countDown();
        });

        client.twin().forId(THING_ID).forFeature(FEATURE_ID).deleteProperties();

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

}
