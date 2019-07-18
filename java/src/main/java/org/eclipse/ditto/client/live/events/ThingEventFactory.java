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
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Features;
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
 * Creates {@link org.eclipse.ditto.signals.events.base.Event Event}s on "Thing" scope where the {@code thingId} is
 * known. Therefore the {@code thingId} must not be explicitly passed to the factory methods.
 *
 * @since 1.0.0
 */
public interface ThingEventFactory extends EventFactory {

    /**
     * Creates a new {@link ThingDeleted} event.
     *
     * @return the new ThingDeleted.
     */
    ThingDeleted thingDeleted();

    /**
     * Creates a new {@link AttributeCreated} event with the required passed arguments.
     *
     * @param attributePointer the JsonPointer of the attribute which was created.
     * @param attributeValue the JsonValue of the attribute which was created.
     * @return the new AttributeCreated.
     */
    default AttributeCreated attributeCreated(final CharSequence attributePointer, final JsonValue attributeValue) {
        return attributeCreated(JsonPointer.of(attributePointer), attributeValue);
    }

    /**
     * Creates a new {@link AttributeCreated} event with the required passed arguments.
     *
     * @param attributePointer the JsonPointer of the attribute which was created.
     * @param attributeValue the JsonValue of the attribute which was created.
     * @return the new AttributeCreated.
     */
    AttributeCreated attributeCreated(JsonPointer attributePointer, JsonValue attributeValue);

    /**
     * Creates a new {@link AttributeDeleted} event with the required passed arguments.
     *
     * @param attributePointer the JsonPointer of the attribute which was deleted.
     * @return the new AttributeDeleted.
     */
    default AttributeDeleted attributeDeleted(final CharSequence attributePointer) {
        return attributeDeleted(JsonPointer.of(attributePointer));
    }

    /**
     * Creates a new {@link AttributeDeleted} event with the required passed arguments.
     *
     * @param attributePointer the JsonPointer of the attribute which was deleted.
     * @return the new AttributeDeleted.
     */
    AttributeDeleted attributeDeleted(JsonPointer attributePointer);

    /**
     * Creates a new {@link AttributeModified} event with the required passed arguments.
     *
     * @param attributePointer the JsonPointer of the attribute which was modified.
     * @param attributeValue the JsonValue of the attribute which was modified.
     * @return the new AttributeModified.
     */
    default AttributeModified attributeModified(final CharSequence attributePointer, final JsonValue attributeValue) {
        return attributeModified(JsonPointer.of(attributePointer), attributeValue);
    }

    /**
     * Creates a new {@link AttributeModified} event with the required passed arguments.
     *
     * @param attributePointer the JsonPointer of the attribute which was modified.
     * @param attributeValue the JsonValue of the attribute which was modified.
     * @return the new AttributeModified.
     */
    AttributeModified attributeModified(JsonPointer attributePointer, JsonValue attributeValue);

    /**
     * Creates a new {@link AttributesCreated} event with the required passed arguments.
     *
     * @param attributes the Attributes which were created.
     * @return the new AttributesCreated.
     */
    AttributesCreated attributesCreated(Attributes attributes);

    /**
     * Creates a new {@link AttributesDeleted} event.
     *
     * @return the new AttributesDeleted.
     */
    AttributesDeleted attributesDeleted();

    /**
     * Creates a new {@link AttributesModified} event with the required passed arguments.
     *
     * @param attributes the Attributes which were modified.
     * @return the new AttributesModified.
     */
    AttributesModified attributesModified(Attributes attributes);

    /**
     * Creates a new {@link FeatureCreated} event with the required passed arguments.
     *
     * @param feature the Feature which was created.
     * @return the new FeatureCreated.
     */
    FeatureCreated featureCreated(Feature feature);

    /**
     * Creates a new {@link FeatureDeleted} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature which was deleted.
     * @return the new FeatureDeleted..
     */
    FeatureDeleted featureDeleted(String featureId);

    /**
     * Creates a new {@link FeatureModified} event with the required passed arguments.
     *
     * @param feature the Feature which was modified.
     * @return the new FeatureModified.
     */
    FeatureModified featureModified(Feature feature);

    /**
     * Creates a new {@link FeaturesCreated} event with the required passed arguments.
     *
     * @param features the Features which were created.
     * @return the new FeaturesCreated.
     */
    FeaturesCreated featuresCreated(Features features);

    /**
     * Creates a new {@link FeaturesDeleted} event.
     *
     * @return the new FeaturesDeleted.
     */
    FeaturesDeleted featuresDeleted();

    /**
     * Creates a new {@link FeaturesModified} event with the required passed arguments.
     *
     * @param features the Features which were modified.
     * @return the new FeaturesModified.
     * @throws NullPointerException if {@code features} is {@code null}.
     */
    FeaturesModified featuresModified(Features features);

    /**
     * Creates a new {@link FeaturePropertiesCreated} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperties were created.
     * @param properties the FeatureProperties which were created.
     * @return the new FeaturePropertiesCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertiesCreated featurePropertiesCreated(String featureId, FeatureProperties properties);

    /**
     * Creates a new {@link FeaturePropertiesDeleted} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperties were deleted.
     * @return the new FeaturePropertiesDeleted.
     * @throws NullPointerException if {@code featureId} is {@code null}.
     */
    FeaturePropertiesDeleted featurePropertiesDeleted(String featureId);

    /**
     * Creates a new {@link FeaturePropertiesModified} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperties were modified.
     * @param properties the FeatureProperties which were modified.
     * @return the new FeaturePropertiesModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertiesModified featurePropertiesModified(String featureId, FeatureProperties properties);

    /**
     * Creates a new {@link FeaturePropertyCreated} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperty was created.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was created.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was created.
     * @return the new FeaturePropertyCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyCreated featurePropertyCreated(final String featureId,
            final CharSequence propertyJsonPointer, final JsonValue propertyJsonValue) {
        return featurePropertyCreated(featureId, JsonPointer.of(propertyJsonPointer), propertyJsonValue);
    }

    /**
     * Creates a new {@link FeaturePropertyCreated} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperty was created.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was created.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was created.
     * @return the new FeaturePropertyCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyCreated featurePropertyCreated(String featureId, JsonPointer propertyJsonPointer,
            JsonValue propertyJsonValue);

    /**
     * Creates a new {@link FeaturePropertyDeleted} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperty was deleted.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was deleted.
     * @return the new FeaturePropertyDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyDeleted featurePropertyDeleted(final String featureId,
            final CharSequence propertyJsonPointer) {
        return featurePropertyDeleted(featureId, JsonPointer.of(propertyJsonPointer));
    }

    /**
     * Creates a new {@link FeaturePropertyDeleted} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperty was deleted.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was deleted.
     * @return the new FeaturePropertyDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyDeleted featurePropertyDeleted(String featureId, JsonPointer propertyJsonPointer);

    /**
     * Creates a new {@link FeaturePropertyModified} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperty was modified.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was modified.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was modified.
     * @return the new FeaturePropertyModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyModified featurePropertyModified(final String featureId,
            final CharSequence propertyJsonPointer, final JsonValue propertyJsonValue) {
        return featurePropertyModified(featureId, JsonPointer.of(propertyJsonPointer), propertyJsonValue);
    }

    /**
     * Creates a new {@link FeaturePropertyModified} event with the required passed arguments.
     *
     * @param featureId the ID of the Feature in which the FeatureProperty was modified.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was modified.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was modified.
     * @return the new FeaturePropertyModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyModified featurePropertyModified(String featureId, JsonPointer propertyJsonPointer,
            JsonValue propertyJsonValue);

}
