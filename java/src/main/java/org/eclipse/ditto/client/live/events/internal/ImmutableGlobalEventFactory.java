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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.time.Instant;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.events.GlobalEventFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
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
        return ThingCreated.of(thing, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public ThingDeleted thingDeleted(final ThingId thingId) {
        return ThingDeleted.of(thingId, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public ThingModified thingModified(final Thing thing) {
        return ThingModified.of(thing, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public AttributeCreated attributeCreated(final ThingId thingId, final JsonPointer attributePointer,
            final JsonValue attributeValue) {
        return AttributeCreated.of(thingId, attributePointer, attributeValue, -1, Instant.now(),
                getDefaultDittoHeaders());
    }

    @Override
    public AttributeDeleted attributeDeleted(final ThingId thingId, final JsonPointer attributePointer) {
        return AttributeDeleted.of(thingId, attributePointer, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public AttributeModified attributeModified(final ThingId thingId, final JsonPointer attributePointer,
            final JsonValue attributeValue) {
        return AttributeModified.of(thingId, attributePointer, attributeValue, -1, Instant.now(),
                getDefaultDittoHeaders());
    }

    @Override
    public AttributesCreated attributesCreated(final ThingId thingId, final Attributes attributes) {
        return AttributesCreated.of(thingId, attributes, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public AttributesDeleted attributesDeleted(final ThingId thingId) {
        return AttributesDeleted.of(thingId, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public AttributesModified attributesModified(final ThingId thingId, final Attributes attributes) {
        return AttributesModified.of(thingId, attributes, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeatureCreated featureCreated(final ThingId thingId, final Feature feature) {
        return FeatureCreated.of(thingId, feature, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeatureDeleted featureDeleted(final ThingId thingId, final String featureId) {
        return FeatureDeleted.of(thingId, featureId, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeatureModified featureModified(final ThingId thingId, final Feature feature) {
        return FeatureModified.of(thingId, feature, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeaturesCreated featuresCreated(final ThingId thingId, final Features features) {
        return FeaturesCreated.of(thingId, features, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeaturesDeleted featuresDeleted(final ThingId thingId) {
        return FeaturesDeleted.of(thingId, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeaturesModified featuresModified(final ThingId thingId, final Features features) {
        return FeaturesModified.of(thingId, features, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeaturePropertiesCreated featurePropertiesCreated(final ThingId thingId, final String featureId,
            final FeatureProperties properties) {
        return FeaturePropertiesCreated.of(thingId, featureId, properties, -1, Instant.now(),
                getDefaultDittoHeaders());
    }

    @Override
    public FeaturePropertiesDeleted featurePropertiesDeleted(final ThingId thingId, final String featureId) {
        return FeaturePropertiesDeleted.of(thingId, featureId, -1, Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeaturePropertiesModified featurePropertiesModified(final ThingId thingId, final String featureId,
            final FeatureProperties properties) {
        return FeaturePropertiesModified.of(thingId, featureId, properties, -1, Instant.now(),
                getDefaultDittoHeaders());
    }

    @Override
    public FeaturePropertyCreated featurePropertyCreated(final ThingId thingId, final String featureId,
            final JsonPointer propertyJsonPointer, final JsonValue propertyJsonValue) {
        return FeaturePropertyCreated.of(thingId, featureId, propertyJsonPointer, propertyJsonValue, -1,
                Instant.now(), getDefaultDittoHeaders());
    }

    @Override
    public FeaturePropertyDeleted featurePropertyDeleted(final ThingId thingId, final String featureId,
            final JsonPointer propertyJsonPointer) {
        return FeaturePropertyDeleted.of(thingId, featureId, propertyJsonPointer, -1, Instant.now(),
                getDefaultDittoHeaders());
    }

    @Override
    public FeaturePropertyModified featurePropertyModified(final ThingId thingId, final String featureId,
            final JsonPointer propertyJsonPointer, final JsonValue propertyJsonValue) {
        return FeaturePropertyModified.of(thingId, featureId, propertyJsonPointer, propertyJsonValue, -1,
                Instant.now(), getDefaultDittoHeaders());
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
