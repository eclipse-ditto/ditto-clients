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

import org.eclipse.ditto.client.live.events.FeatureEventFactory;
import org.eclipse.ditto.client.live.events.ThingEventFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.things.model.FeatureProperties;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.events.FeatureDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesModified;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyModified;

/**
 * An immutable implementation of {@link FeatureEventFactory}.
 *
 * @since 1.0.0
 */
@Immutable
public final class ImmutableFeatureEventFactory implements FeatureEventFactory {

    private final ThingEventFactory thingEventFactory;
    private final String featureId;

    private ImmutableFeatureEventFactory(final ThingEventFactory theThingEventFactory, final String theFeatureId) {
        featureId = theFeatureId;
        thingEventFactory = theThingEventFactory;
    }

    /**
     * Returns an instance of {@code ImmutableFeatureEventFactory}.
     *
     * @param schemaVersion the JSON schema version this client was configured to handle.
     * @param thingId the ID of the Thing to create events for.
     * @param featureId the ID of the Feature to create events for.
     * @return the instance.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code source}, {@code thingId} or {@code featureId} is empty.
     */
    public static ImmutableFeatureEventFactory getInstance(final JsonSchemaVersion schemaVersion,
            final ThingId thingId, final String featureId) {
        return new ImmutableFeatureEventFactory(ImmutableThingEventFactory.getInstance(schemaVersion, thingId),
                argumentNotEmpty(featureId, "Feature ID"));
    }

    @Override
    public FeatureDeleted featureDeleted() {
        return thingEventFactory.featureDeleted(featureId);
    }

    @Override
    public FeaturePropertiesCreated featurePropertiesCreated(final FeatureProperties properties) {
        return thingEventFactory.featurePropertiesCreated(featureId, properties);
    }

    @Override
    public FeaturePropertiesDeleted featurePropertiesDeleted() {
        return thingEventFactory.featurePropertiesDeleted(featureId);
    }

    @Override
    public FeaturePropertiesModified featurePropertiesModified(final FeatureProperties properties) {
        return thingEventFactory.featurePropertiesModified(featureId, properties);
    }

    @Override
    public FeaturePropertyCreated featurePropertyCreated(final JsonPointer propertyJsonPointer,
            final JsonValue propertyJsonValue) {
        return thingEventFactory.featurePropertyCreated(featureId, propertyJsonPointer, propertyJsonValue);
    }

    @Override
    public FeaturePropertyDeleted featurePropertyDeleted(final JsonPointer propertyJsonPointer) {
        return thingEventFactory.featurePropertyDeleted(featureId, propertyJsonPointer);
    }

    @Override
    public FeaturePropertyModified featurePropertyModified(final JsonPointer propertyJsonPointer,
            final JsonValue propertyJsonValue) {
        return thingEventFactory.featurePropertyModified(featureId, propertyJsonPointer, propertyJsonValue);
    }

    @Override
    public JsonSchemaVersion getSchemaVersion() {
        return thingEventFactory.getSchemaVersion();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableFeatureEventFactory that = (ImmutableFeatureEventFactory) o;
        return Objects.equals(thingEventFactory, that.thingEventFactory) && Objects.equals(featureId, that.featureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thingEventFactory, featureId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "thingEventFactory=" + thingEventFactory + ", featureId="
                + featureId + "]";
    }

}
