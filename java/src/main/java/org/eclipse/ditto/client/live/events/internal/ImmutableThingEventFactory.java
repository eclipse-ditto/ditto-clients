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

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.events.GlobalEventFactory;
import org.eclipse.ditto.client.live.events.ThingEventFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Features;
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
import org.eclipse.ditto.signals.events.things.ThingDeleted;

/**
 * An immutable implementation of {@link ThingEventFactory}.
 *
 * @since 1.0.0
 */
@Immutable
public final class ImmutableThingEventFactory implements ThingEventFactory {

    private final ThingId thingId;
    private final GlobalEventFactory globalEventFactory;

    private ImmutableThingEventFactory(final GlobalEventFactory globalEventFactory, final ThingId thingId) {
        this.globalEventFactory = globalEventFactory;
        this.thingId = thingId;
    }

    /**
     * Returns an instance of {@code ImmutableThingEventFactory}.
     *
     * @param source the source this client instance represents.
     * @param schemaVersion the JSON schema version this client was configured to handle.
     * @param thingId the ID of the Thing to create events for.
     * @return the instance.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code source} or {@code thingId} is empty.
     */
    public static ImmutableThingEventFactory getInstance(final String source, final JsonSchemaVersion schemaVersion,
            final ThingId thingId) {
        argumentNotEmpty(thingId, "Thing ID");
        return new ImmutableThingEventFactory(ImmutableGlobalEventFactory.getInstance(source, schemaVersion), thingId);
    }

    @Override
    public ThingDeleted thingDeleted() {
        return globalEventFactory.thingDeleted(thingId);
    }

    @Override
    public AttributeCreated attributeCreated(final JsonPointer attributePointer, final JsonValue attributeValue) {
        return globalEventFactory.attributeCreated(thingId, attributePointer, attributeValue);
    }

    @Override
    public AttributeDeleted attributeDeleted(final JsonPointer attributePointer) {
        return globalEventFactory.attributeDeleted(thingId, attributePointer);
    }

    @Override
    public AttributeModified attributeModified(final JsonPointer attributePointer, final JsonValue attributeValue) {
        return globalEventFactory.attributeModified(thingId, attributePointer, attributeValue);
    }

    @Override
    public AttributesCreated attributesCreated(final Attributes attributes) {
        return globalEventFactory.attributesCreated(thingId, attributes);
    }

    @Override
    public AttributesDeleted attributesDeleted() {
        return globalEventFactory.attributesDeleted(thingId);
    }

    @Override
    public AttributesModified attributesModified(final Attributes attributes) {
        return globalEventFactory.attributesModified(thingId, attributes);
    }

    @Override
    public FeatureCreated featureCreated(final Feature feature) {
        return globalEventFactory.featureCreated(thingId, feature);
    }

    @Override
    public FeatureDeleted featureDeleted(final String featureId) {
        return globalEventFactory.featureDeleted(thingId, featureId);
    }

    @Override
    public FeatureModified featureModified(final Feature feature) {
        return globalEventFactory.featureModified(thingId, feature);
    }

    @Override
    public FeaturesCreated featuresCreated(final Features features) {
        return globalEventFactory.featuresCreated(thingId, features);
    }

    @Override
    public FeaturesDeleted featuresDeleted() {
        return globalEventFactory.featuresDeleted(thingId);
    }

    @Override
    public FeaturesModified featuresModified(final Features features) {
        return globalEventFactory.featuresModified(thingId, features);
    }

    @Override
    public FeaturePropertiesCreated featurePropertiesCreated(final String featureId,
            final FeatureProperties properties) {
        return globalEventFactory.featurePropertiesCreated(thingId, featureId, properties);
    }

    @Override
    public FeaturePropertiesDeleted featurePropertiesDeleted(final String featureId) {
        return globalEventFactory.featurePropertiesDeleted(thingId, featureId);
    }

    @Override
    public FeaturePropertiesModified featurePropertiesModified(final String featureId,
            final FeatureProperties properties) {
        return globalEventFactory.featurePropertiesModified(thingId, featureId, properties);
    }

    @Override
    public FeaturePropertyCreated featurePropertyCreated(final String featureId, final JsonPointer propertyJsonPointer,
            final JsonValue propertyJsonValue) {
        return globalEventFactory.featurePropertyCreated(thingId, featureId, propertyJsonPointer, propertyJsonValue);
    }

    @Override
    public FeaturePropertyDeleted featurePropertyDeleted(final String featureId,
            final JsonPointer propertyJsonPointer) {
        return globalEventFactory.featurePropertyDeleted(thingId, featureId, propertyJsonPointer);
    }

    @Override
    public FeaturePropertyModified featurePropertyModified(final String featureId,
            final JsonPointer propertyJsonPointer, final JsonValue propertyJsonValue) {
        return globalEventFactory.featurePropertyModified(thingId, featureId, propertyJsonPointer, propertyJsonValue);
    }

    @Override
    public String getSource() {
        return globalEventFactory.getSource();
    }

    @Override
    public JsonSchemaVersion getSchemaVersion() {
        return globalEventFactory.getSchemaVersion();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableThingEventFactory that = (ImmutableThingEventFactory) o;
        return Objects.equals(thingId, that.thingId) && Objects.equals(globalEventFactory, that.globalEventFactory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thingId, globalEventFactory);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "thingId=" + thingId + ", globalEventFactory=" + globalEventFactory
                + "]";
    }

}
