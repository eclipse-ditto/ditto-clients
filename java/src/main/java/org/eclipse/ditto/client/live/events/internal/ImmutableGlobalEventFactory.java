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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.time.Instant;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.events.GlobalEventFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.things.model.Attributes;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.FeatureProperties;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.events.AttributeCreated;
import org.eclipse.ditto.things.model.signals.events.AttributeDeleted;
import org.eclipse.ditto.things.model.signals.events.AttributeModified;
import org.eclipse.ditto.things.model.signals.events.AttributesCreated;
import org.eclipse.ditto.things.model.signals.events.AttributesDeleted;
import org.eclipse.ditto.things.model.signals.events.AttributesModified;
import org.eclipse.ditto.things.model.signals.events.FeatureCreated;
import org.eclipse.ditto.things.model.signals.events.FeatureDeleted;
import org.eclipse.ditto.things.model.signals.events.FeatureModified;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesModified;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyModified;
import org.eclipse.ditto.things.model.signals.events.FeaturesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturesDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturesModified;
import org.eclipse.ditto.things.model.signals.events.ThingCreated;
import org.eclipse.ditto.things.model.signals.events.ThingDeleted;
import org.eclipse.ditto.things.model.signals.events.ThingModified;

/**
 * An immutable implementation of {@link GlobalEventFactory}.
 *
 * @since 1.0.0
 */
@Immutable
@SuppressWarnings({"squid:S1610", "OverlyCoupledClass"})
public final class ImmutableGlobalEventFactory implements GlobalEventFactory {

    private final JsonSchemaVersion schemaVersion;

    private ImmutableGlobalEventFactory(final JsonSchemaVersion theSchemaVersion) {
        schemaVersion = theSchemaVersion;
    }


    /**
     * Returns an instance of {@code ImmutableGlobalEventFactory}.
     *
     * @param schemaVersion the JSON schema version this client was configured to handle.
     * @return the instance.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code source} is empty.
     */
    public static ImmutableGlobalEventFactory getInstance(final JsonSchemaVersion schemaVersion) {
        checkNotNull(schemaVersion, "schema version");
        return new ImmutableGlobalEventFactory(schemaVersion);
    }

    @Override
    public ThingCreated thingCreated(final Thing thing) {
        return ThingCreated.of(thing, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public ThingDeleted thingDeleted(final ThingId thingId) {
        return ThingDeleted.of(thingId, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public ThingModified thingModified(final Thing thing) {
        return ThingModified.of(thing, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public AttributeCreated attributeCreated(final ThingId thingId, final JsonPointer attributePointer,
            final JsonValue attributeValue) {
        return AttributeCreated.of(thingId, attributePointer, attributeValue, -1, Instant.now(),
                getDefaultDittoHeaders(), null);
    }

    @Override
    public AttributeDeleted attributeDeleted(final ThingId thingId, final JsonPointer attributePointer) {
        return AttributeDeleted.of(thingId, attributePointer, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public AttributeModified attributeModified(final ThingId thingId, final JsonPointer attributePointer,
            final JsonValue attributeValue) {
        return AttributeModified.of(thingId, attributePointer, attributeValue, -1, Instant.now(),
                getDefaultDittoHeaders(), null);
    }

    @Override
    public AttributesCreated attributesCreated(final ThingId thingId, final Attributes attributes) {
        return AttributesCreated.of(thingId, attributes, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public AttributesDeleted attributesDeleted(final ThingId thingId) {
        return AttributesDeleted.of(thingId, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public AttributesModified attributesModified(final ThingId thingId, final Attributes attributes) {
        return AttributesModified.of(thingId, attributes, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeatureCreated featureCreated(final ThingId thingId, final Feature feature) {
        return FeatureCreated.of(thingId, feature, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeatureDeleted featureDeleted(final ThingId thingId, final String featureId) {
        return FeatureDeleted.of(thingId, featureId, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeatureModified featureModified(final ThingId thingId, final Feature feature) {
        return FeatureModified.of(thingId, feature, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturesCreated featuresCreated(final ThingId thingId, final Features features) {
        return FeaturesCreated.of(thingId, features, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturesDeleted featuresDeleted(final ThingId thingId) {
        return FeaturesDeleted.of(thingId, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturesModified featuresModified(final ThingId thingId, final Features features) {
        return FeaturesModified.of(thingId, features, -1, Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturePropertiesCreated featurePropertiesCreated(final ThingId thingId, final String featureId,
            final FeatureProperties properties) {
        return FeaturePropertiesCreated.of(thingId, featureId, properties, -1, Instant.now(),
                getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturePropertiesDeleted featurePropertiesDeleted(final ThingId thingId, final String featureId) {
        return FeaturePropertiesDeleted.of(thingId, featureId, -1, Instant.now(), getDefaultDittoHeaders(),
                null);
    }

    @Override
    public FeaturePropertiesModified featurePropertiesModified(final ThingId thingId, final String featureId,
            final FeatureProperties properties) {
        return FeaturePropertiesModified.of(thingId, featureId, properties, -1, Instant.now(),
                getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturePropertyCreated featurePropertyCreated(final ThingId thingId, final String featureId,
            final JsonPointer propertyJsonPointer, final JsonValue propertyJsonValue) {
        return FeaturePropertyCreated.of(thingId, featureId, propertyJsonPointer, propertyJsonValue, -1,
                Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturePropertyDeleted featurePropertyDeleted(final ThingId thingId, final String featureId,
            final JsonPointer propertyJsonPointer) {
        return FeaturePropertyDeleted.of(thingId, featureId, propertyJsonPointer, -1, Instant.now(),
                getDefaultDittoHeaders(), null);
    }

    @Override
    public FeaturePropertyModified featurePropertyModified(final ThingId thingId, final String featureId,
            final JsonPointer propertyJsonPointer, final JsonValue propertyJsonValue) {
        return FeaturePropertyModified.of(thingId, featureId, propertyJsonPointer, propertyJsonValue, -1,
                Instant.now(), getDefaultDittoHeaders(), null);
    }

    @Override
    public JsonSchemaVersion getSchemaVersion() {
        return schemaVersion;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableGlobalEventFactory that = (ImmutableGlobalEventFactory) o;
        return schemaVersion == that.schemaVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), schemaVersion);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + ", schemaVersion=" + schemaVersion + "]";
    }

}
