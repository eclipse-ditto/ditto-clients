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
package org.eclipse.ditto.client.live.events;

import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.FeatureProperties;
import org.eclipse.ditto.things.model.signals.events.FeatureDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesModified;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyModified;

/**
 * Creates {@link org.eclipse.ditto.signals.events.base.Event Event}s on "Feature" scope the {@code thingId} and {@code
 * featureId} are known. Therefore those must not be explicitly passed to the factory methods.
 *
 * @since 1.0.0
 */
public interface FeatureEventFactory extends EventFactory {

    /**
     * Creates a new {@link FeatureDeleted} event.
     *
     * @return the new FeatureDeleted.
     */
    FeatureDeleted featureDeleted();

    /**
     * Creates a new {@link FeaturePropertiesCreated} event with the required passed arguments.
     *
     * @param properties the FeatureProperties which were created.
     * @return the new FeaturePropertiesCreated.
     * @throws NullPointerException if {@code properties} is {@code null}.
     */
    FeaturePropertiesCreated featurePropertiesCreated(FeatureProperties properties);

    /**
     * Creates a new {@link FeaturePropertiesDeleted} event.
     *
     * @return the new FeaturePropertiesDeleted.
     */
    FeaturePropertiesDeleted featurePropertiesDeleted();

    /**
     * Creates a new {@link FeaturePropertiesModified} event with the required passed arguments.
     *
     * @param properties the FeatureProperties which were modified.
     * @return the new FeaturePropertiesModified.
     * @throws NullPointerException if {@code properties} is {@code null}.
     */
    FeaturePropertiesModified featurePropertiesModified(FeatureProperties properties);

    /**
     * Creates a new {@link FeaturePropertyCreated} event with the required passed arguments.
     *
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was created.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was created.
     * @return the new FeaturePropertyCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyCreated featurePropertyCreated(final CharSequence propertyJsonPointer,
            final JsonValue propertyJsonValue) {
        return featurePropertyCreated(JsonPointer.of(propertyJsonPointer), propertyJsonValue);
    }

    /**
     * Creates a new {@link FeaturePropertyCreated} event with the required passed arguments.
     *
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was created.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was created.
     * @return the new FeaturePropertyCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyCreated featurePropertyCreated(JsonPointer propertyJsonPointer, JsonValue propertyJsonValue);

    /**
     * Creates a new {@link FeaturePropertyDeleted} event with the required passed arguments.
     *
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was deleted.
     * @return the new FeaturePropertyDeleted.
     * @throws NullPointerException if {@code propertyJsonPointer} is {@code null}.
     */
    default FeaturePropertyDeleted featurePropertyDeleted(final CharSequence propertyJsonPointer) {
        return featurePropertyDeleted(JsonPointer.of(propertyJsonPointer));
    }

    /**
     * Creates a new {@link FeaturePropertyDeleted} event with the required passed arguments.
     *
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was deleted.
     * @return the new FeaturePropertyDeleted.
     * @throws NullPointerException if {@code propertyJsonPointer} is {@code null}.
     */
    FeaturePropertyDeleted featurePropertyDeleted(JsonPointer propertyJsonPointer);

    /**
     * Creates a new {@link FeaturePropertyModified} event with the required passed arguments.
     *
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was modified.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was modified.
     * @return the new FeaturePropertyModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyModified featurePropertyModified(final CharSequence propertyJsonPointer,
            final JsonValue propertyJsonValue) {
        return featurePropertyModified(JsonPointer.of(propertyJsonPointer), propertyJsonValue);
    }

    /**
     * Creates a new {@link FeaturePropertyModified} event with the required passed arguments.
     *
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was modified.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was modified.
     * @return the new FeaturePropertyModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyModified featurePropertyModified(JsonPointer propertyJsonPointer, JsonValue propertyJsonValue);

}
