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
package org.eclipse.ditto.client.live.events.internal;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.client.TestConstants.Feature.FEATURES;
import static org.eclipse.ditto.client.TestConstants.Feature.FLUX_CAPACITOR;
import static org.eclipse.ditto.client.TestConstants.Feature.FLUX_CAPACITOR_ID;
import static org.eclipse.ditto.client.TestConstants.Feature.FLUX_CAPACITOR_PROPERTIES;
import static org.eclipse.ditto.client.TestConstants.Thing.ATTRIBUTES;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_V1;
import static org.eclipse.ditto.signals.events.things.assertions.ThingEventAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.text.MessageFormat;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributeDeleted;
import org.eclipse.ditto.signals.events.things.AttributeModified;
import org.eclipse.ditto.signals.events.things.AttributesCreated;
import org.eclipse.ditto.signals.events.things.AttributesDeleted;
import org.eclipse.ditto.signals.events.things.AttributesModified;
import org.eclipse.ditto.signals.events.things.FeatureCreated;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.things.FeatureModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertyCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.FeaturesCreated;
import org.eclipse.ditto.signals.events.things.FeaturesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturesModified;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingModified;
import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableGlobalEventFactory}.
 */
public final class ImmutableGlobalEventFactoryTest {

    private static final String SOURCE = "mySource";
    private static final JsonSchemaVersion SCHEMA_VERSION = JsonSchemaVersion.V_1;
    private static final JsonPointer ATTRIBUTES_POINTER = JsonFactory.newPointer("/attributes");
    private static final JsonPointer ATTRIBUTE_JSON_POINTER = JsonFactory.newPointer("manufacturer/name");
    private static final JsonPointer ATTRIBUTE_RESOURCE_PATH = ATTRIBUTES_POINTER.append(ATTRIBUTE_JSON_POINTER);
    private static final JsonValue ATTRIBUTE_VALUE = JsonFactory.newValue("Ditto");
    private static final JsonPointer FEATURES_POINTER = JsonFactory.newPointer("/features");
    private static final JsonPointer PROPERTY_JSON_POINTER = JsonFactory.newPointer("target_year_1");
    private static final JsonValue PROPERTY_VALUE = JsonFactory.newValue("1955");

    private ImmutableGlobalEventFactory underTest = null;

    /**
     *
     */
    @Before
    public void setUp() {
        underTest = ImmutableGlobalEventFactory.getInstance(SOURCE, SCHEMA_VERSION);
    }

    /**
     *
     */
    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableGlobalEventFactory.class, areImmutable(),
                provided(JsonSchemaVersion.class, ThingId.class).areAlsoImmutable());
    }

    /**
     *
     */
    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableGlobalEventFactory.class).usingGetClass().verify();
    }

    /**
     *
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetInstanceWithNullSource() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> ImmutableGlobalEventFactory.getInstance(null, SCHEMA_VERSION))
                .withMessage(MessageFormat.format("The {0} must not be null!", "source"))
                .withNoCause();
    }

    /**
     *
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetInstanceWithNullJsonSchemaVersion() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> ImmutableGlobalEventFactory.getInstance(SOURCE, null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "schema version"))
                .withNoCause();
    }

    /**
     *
     */
    @Test
    public void getSourceReturnsExpected() {
        final ImmutableGlobalEventFactory underTest = ImmutableGlobalEventFactory.getInstance(SOURCE, SCHEMA_VERSION);

        Assertions.assertThat(underTest.getSource()).isEqualTo(SOURCE);
    }

    /**
     *
     */
    @Test
    public void getSchemaVersionReturnsExpected() {
        final ImmutableGlobalEventFactory underTest = ImmutableGlobalEventFactory.getInstance(SOURCE, SCHEMA_VERSION);

        assertThat((Object) underTest.getSchemaVersion()).isEqualTo(SCHEMA_VERSION);
    }

    /**
     *
     */
    @Test
    public void thingCreatedReturnsExpected() {
        final ThingCreated thingCreated = underTest.thingCreated(THING_V1);

        assertThat(thingCreated)
                .hasType(ThingCreated.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(THING_V1.toJson())
                .hasRevision(-1);
        assertThat(thingCreated.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void thingDeletedReturnsExpected() {
        final ThingDeleted thingDeleted = underTest.thingDeleted(THING_ID);

        assertThat(thingDeleted)
                .hasType(ThingDeleted.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasNoEntity()
                .hasRevision(-1);
        assertThat(thingDeleted.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void thingModifiedReturnsExpected() {
        final ThingModified thingModified = underTest.thingModified(THING_V1);

        assertThat(thingModified)
                .hasType(ThingModified.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(THING_V1.toJson())
                .hasRevision(-1);
        assertThat(thingModified.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void attributeCreatedReturnsExpected() {
        final AttributeCreated attributeCreated =
                underTest.attributeCreated(THING_ID, ATTRIBUTE_JSON_POINTER, ATTRIBUTE_VALUE);

        assertThat(attributeCreated)
                .hasType(AttributeCreated.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(ATTRIBUTE_VALUE)
                .hasResourcePath(JsonFactory.newPointer("/attributes" + ATTRIBUTE_JSON_POINTER))
                .hasRevision(-1);
        assertThat(attributeCreated.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void attributeDeletedReturnsExpected() {
        final AttributeDeleted attributeDeleted = underTest.attributeDeleted(THING_ID, ATTRIBUTE_JSON_POINTER);

        assertThat(attributeDeleted)
                .hasType(AttributeDeleted.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasNoEntity()
                .hasResourcePath(ATTRIBUTE_RESOURCE_PATH)
                .hasRevision(-1);
        assertThat(attributeDeleted.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void attributeModifiedReturnsExpected() {
        final AttributeModified attributeModified = underTest.attributeModified(THING_ID, ATTRIBUTE_JSON_POINTER,
                ATTRIBUTE_VALUE);

        assertThat(attributeModified)
                .hasType(AttributeModified.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(ATTRIBUTE_VALUE)
                .hasResourcePath(ATTRIBUTE_RESOURCE_PATH)
                .hasRevision(-1);
        assertThat(attributeModified.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void attributesCreatedReturnsExpected() {
        final AttributesCreated attributesCreated = underTest.attributesCreated(THING_ID, ATTRIBUTES);

        assertThat(attributesCreated)
                .hasType(AttributesCreated.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(ATTRIBUTES.toJson(SCHEMA_VERSION, FieldType.regularOrSpecial()))
                .hasResourcePath(ATTRIBUTES_POINTER)
                .hasRevision(-1);
        assertThat(attributesCreated.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void attributesDeletedReturnsExpected() {
        final AttributesDeleted attributesDeleted = underTest.attributesDeleted(THING_ID);

        assertThat(attributesDeleted)
                .hasType(AttributesDeleted.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasNoEntity()
                .hasResourcePath(ATTRIBUTES_POINTER)
                .hasRevision(-1);
        assertThat(attributesDeleted.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void attributesModifiedReturnsExpected() {
        final AttributesModified attributesModified = underTest.attributesModified(THING_ID, ATTRIBUTES);

        assertThat(attributesModified)
                .hasType(AttributesModified.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(ATTRIBUTES.toJson(SCHEMA_VERSION, FieldType.regularOrSpecial()))
                .hasResourcePath(ATTRIBUTES_POINTER)
                .hasRevision(-1);
        assertThat(attributesModified.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featureCreatedReturnsExpected() {
        final FeatureCreated featureCreated = underTest.featureCreated(THING_ID, FLUX_CAPACITOR);

        assertThat(featureCreated)
                .hasType(FeatureCreated.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(FLUX_CAPACITOR.toJson(SCHEMA_VERSION, FieldType.notHidden()))
                .hasResourcePath(FEATURES_POINTER.append(JsonPointer.of(FLUX_CAPACITOR_ID)))
                .hasRevision(-1);
        assertThat(featureCreated.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featureDeletedReturnsExpected() {
        final FeatureDeleted featureDeleted = underTest.featureDeleted(THING_ID, FLUX_CAPACITOR_ID);

        assertThat(featureDeleted)
                .hasType(FeatureDeleted.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasNoEntity()
                .hasResourcePath(FEATURES_POINTER.append(JsonPointer.of(FLUX_CAPACITOR_ID)))
                .hasRevision(-1);
        assertThat(featureDeleted.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featureModifiedReturnsExpected() {
        final FeatureModified featureModified = underTest.featureModified(THING_ID, FLUX_CAPACITOR);

        assertThat(featureModified)
                .hasType(FeatureModified.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(FLUX_CAPACITOR.toJson(SCHEMA_VERSION, FieldType.notHidden()))
                .hasResourcePath(FEATURES_POINTER.append(JsonPointer.of(FLUX_CAPACITOR_ID)))
                .hasRevision(-1);
        assertThat(featureModified.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featuresCreatedReturnsExpected() {
        final FeaturesCreated featuresCreated = underTest.featuresCreated(THING_ID, FEATURES);

        assertThat(featuresCreated)
                .hasType(FeaturesCreated.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(FEATURES.toJson(SCHEMA_VERSION, FieldType.notHidden()))
                .hasResourcePath(FEATURES_POINTER)
                .hasRevision(-1);
        assertThat(featuresCreated.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featuresDeletedReturnsExpected() {
        final FeaturesDeleted featuresDeleted = underTest.featuresDeleted(THING_ID);

        assertThat(featuresDeleted)
                .hasType(FeaturesDeleted.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasNoEntity()
                .hasResourcePath(FEATURES_POINTER)
                .hasRevision(-1);
        assertThat(featuresDeleted.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featuresModifiedReturnsExpected() {
        final FeaturesModified featuresModified = underTest.featuresModified(THING_ID, FEATURES);

        assertThat(featuresModified)
                .hasType(FeaturesModified.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(FEATURES.toJson(SCHEMA_VERSION, FieldType.notHidden()))
                .hasResourcePath(FEATURES_POINTER)
                .hasRevision(-1);
        assertThat(featuresModified.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featurePropertiesCreatedReturnsExpected() {
        final FeaturePropertiesCreated featurePropertiesCreated =
                underTest.featurePropertiesCreated(THING_ID, FLUX_CAPACITOR_ID, FLUX_CAPACITOR_PROPERTIES);

        assertThat(featurePropertiesCreated)
                .hasType(FeaturePropertiesCreated.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(FLUX_CAPACITOR_PROPERTIES.toJson(SCHEMA_VERSION, FieldType.regularOrSpecial()))
                .hasResourcePath(JsonFactory.newPointer(FEATURES_POINTER + "/" + FLUX_CAPACITOR_ID + "/properties"))
                .hasRevision(-1);
        assertThat(featurePropertiesCreated.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featurePropertiesDeletedReturnsExpected() {
        final FeaturePropertiesDeleted featurePropertiesDeleted =
                underTest.featurePropertiesDeleted(THING_ID, FLUX_CAPACITOR_ID);

        assertThat(featurePropertiesDeleted)
                .hasType(FeaturePropertiesDeleted.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasNoEntity()
                .hasResourcePath(JsonFactory.newPointer(FEATURES_POINTER + "/" + FLUX_CAPACITOR_ID + "/properties"))
                .hasRevision(-1);
        assertThat(featurePropertiesDeleted.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featurePropertiesModifiedReturnsExpected() {
        final FeaturePropertiesModified featurePropertiesModified =
                underTest.featurePropertiesModified(THING_ID, FLUX_CAPACITOR_ID, FLUX_CAPACITOR_PROPERTIES);

        assertThat(featurePropertiesModified)
                .hasType(FeaturePropertiesModified.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(FLUX_CAPACITOR_PROPERTIES.toJson(SCHEMA_VERSION, FieldType.regularOrSpecial()))
                .hasResourcePath(JsonFactory.newPointer(FEATURES_POINTER + "/" + FLUX_CAPACITOR_ID + "/properties"))
                .hasRevision(-1);
        assertThat(featurePropertiesModified.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featurePropertyCreatedReturnsExpected() {
        final FeaturePropertyCreated featurePropertyCreated =
                underTest.featurePropertyCreated(THING_ID, FLUX_CAPACITOR_ID, PROPERTY_JSON_POINTER, PROPERTY_VALUE);

        assertThat(featurePropertyCreated)
                .hasType(FeaturePropertyCreated.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(PROPERTY_VALUE)
                .hasResourcePath(JsonFactory.newPointer(FEATURES_POINTER + "/" + FLUX_CAPACITOR_ID + "/properties"
                        + PROPERTY_JSON_POINTER))
                .hasRevision(-1);
        assertThat(featurePropertyCreated.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featurePropertyDeletedReturnsExpected() {
        final FeaturePropertyDeleted featurePropertyDeleted =
                underTest.featurePropertyDeleted(THING_ID, FLUX_CAPACITOR_ID, PROPERTY_JSON_POINTER);

        assertThat(featurePropertyDeleted)
                .hasType(FeaturePropertyDeleted.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasNoEntity()
                .hasResourcePath(JsonFactory.newPointer(FEATURES_POINTER + "/" + FLUX_CAPACITOR_ID + "/properties"
                        + PROPERTY_JSON_POINTER))
                .hasRevision(-1);
        assertThat(featurePropertyDeleted.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void featurePropertyModifiedReturnsExpected() {
        final FeaturePropertyModified featurePropertyModified =
                underTest.featurePropertyModified(THING_ID, FLUX_CAPACITOR_ID, PROPERTY_JSON_POINTER, PROPERTY_VALUE);

        assertThat(featurePropertyModified)
                .hasType(FeaturePropertyModified.TYPE)
                .hasId(THING_ID)
                .hasThingId(THING_ID)
                .hasEntity(PROPERTY_VALUE)
                .hasResourcePath(JsonFactory.newPointer(FEATURES_POINTER + "/" + FLUX_CAPACITOR_ID + "/properties"
                        + PROPERTY_JSON_POINTER))
                .hasRevision(-1);
        assertThat(featurePropertyModified.getDittoHeaders())
                .hasCorrelationId()
                .hasSchemaVersion(underTest.getSchemaVersion())
                .hasSource(underTest.getSource());
    }

    /**
     *
     */
    @Test
    public void toStringReturnsExpected() {
        Assertions.assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains("source=", SOURCE)
                .contains("schemaVersion=", SCHEMA_VERSION.toString());
    }

}
