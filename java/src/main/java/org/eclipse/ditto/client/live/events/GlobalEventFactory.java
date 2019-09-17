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
 * Creates {@link org.eclipse.ditto.signals.events.base.Event Event}s on "global" scope on which neither {@code thingId}
 * nor {@code featureId} are known. Therefore those must always be passed to the factory methods.
 *
 * @since 1.0.0
 */
@SuppressWarnings({"squid:S1610", "OverlyCoupledClass"})
public interface GlobalEventFactory extends EventFactory {

    /**
     * Creates a new {@link ThingCreated} event with the required passed arguments.
     *
     * @param thing the Thing which was created.
     * @return the new ThingCreated.
     * @throws NullPointerException if {@code thing} is {@code null}.
     */
    ThingCreated thingCreated(Thing thing);

    /**
     * Creates a new {@link ThingDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing which was deleted.
     * @return the new ThingDeleted.
     * @throws NullPointerException if {@code thingId} is {@code null}.
     */
    ThingDeleted thingDeleted(ThingId thingId);

    /**
     * Creates a new {@link ThingModified} event with the required passed arguments.
     *
     * @param thing the Thing which was modified.
     * @return the new ThingModified.
     * @throws NullPointerException if {@code thing} is {@code null}.
     */
    ThingModified thingModified(Thing thing);

    /**
     * Creates a new {@link AttributeCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attribute was created.
     * @param attributePointer the JsonPointer of the attribute which was created.
     * @param attributeValue the JsonValue of the attribute which was created.
     * @return the new AttributeCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default AttributeCreated attributeCreated(final ThingId thingId, final CharSequence attributePointer,
            final JsonValue attributeValue) {
        return attributeCreated(thingId, JsonPointer.of(attributePointer), attributeValue);
    }

    /**
     * Creates a new {@link AttributeCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attribute was created.
     * @param attributePointer the JsonPointer of the attribute which was created.
     * @param attributeValue the JsonValue of the attribute which was created.
     * @return the new AttributeCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    AttributeCreated attributeCreated(ThingId thingId, JsonPointer attributePointer, JsonValue attributeValue);

    /**
     * Creates a new {@link AttributeDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attribute was deleted.
     * @param attributePointer the JsonPointer of the attribute which was deleted.
     * @return the new AttributeDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default AttributeDeleted attributeDeleted(final ThingId thingId, final CharSequence attributePointer) {
        return attributeDeleted(thingId, JsonPointer.of(attributePointer));
    }

    /**
     * Creates a new {@link AttributeDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attribute was deleted.
     * @param attributePointer the JsonPointer of the attribute which was deleted.
     * @return the new AttributeDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    AttributeDeleted attributeDeleted(ThingId thingId, JsonPointer attributePointer);

    /**
     * Creates a new {@link AttributeModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attribute was modified.
     * @param attributePointer the JsonPointer of the attribute which was modified.
     * @param attributeValue the JsonValue of the attribute which was modified.
     * @return the new AttributeModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default AttributeModified attributeModified(final ThingId thingId, final CharSequence attributePointer,
            final JsonValue attributeValue) {
        return attributeModified(thingId, JsonPointer.of(attributePointer), attributeValue);
    }

    /**
     * Creates a new {@link AttributeModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attribute was modified.
     * @param attributePointer the JsonPointer of the attribute which was modified.
     * @param attributeValue the JsonValue of the attribute which was modified.
     * @return the new AttributeModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    AttributeModified attributeModified(ThingId thingId, JsonPointer attributePointer, JsonValue attributeValue);

    /**
     * Creates a new {@link AttributesCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attributes were created.
     * @param attributes the Attributes which were created.
     * @return the new AttributesCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    AttributesCreated attributesCreated(ThingId thingId, Attributes attributes);

    /**
     * Creates a new {@link AttributesDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attributes were deleted.
     * @return the new AttributesDeleted.
     * @throws NullPointerException if {@code thingId} is {@code null}.
     */
    AttributesDeleted attributesDeleted(ThingId thingId);

    /**
     * Creates a new {@link AttributesModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the attributes were modified.
     * @param attributes the Attributes which were modified.
     * @return the new AttributesModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    AttributesModified attributesModified(ThingId thingId, Attributes attributes);

    /**
     * Creates a new {@link FeatureCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the Feature was created.
     * @param feature the Feature which was created.
     * @return the new FeatureCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeatureCreated featureCreated(ThingId thingId, Feature feature);

    /**
     * Creates a new {@link FeatureDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the Feature was deleted.
     * @param featureId the ID of the Feature which was deleted.
     * @return the new FeatureDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeatureDeleted featureDeleted(ThingId thingId, String featureId);

    /**
     * Creates a new {@link FeatureModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the Feature was modified.
     * @param feature the Feature which was modified.
     * @return the new FeatureModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeatureModified featureModified(ThingId thingId, Feature feature);

    /**
     * Creates a new {@link FeaturesCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the Features were created.
     * @param features the Features which were created.
     * @return the new FeaturesCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturesCreated featuresCreated(ThingId thingId, Features features);

    /**
     * Creates a new {@link FeaturesDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the Features were deleted.
     * @return the new FeaturesDeleted.
     * @throws NullPointerException if {@code thingId} is {@code null}.
     */
    FeaturesDeleted featuresDeleted(ThingId thingId);

    /**
     * Creates a new {@link FeaturesModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the Features were modified.
     * @param features the Features which were modified.
     * @return the new FeaturesModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturesModified featuresModified(ThingId thingId, Features features);

    /**
     * Creates a new {@link FeaturePropertiesCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperties were created.
     * @param featureId the ID of the Feature in which the FeatureProperties were created.
     * @param properties the FeatureProperties which were created.
     * @return the new FeaturePropertiesCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertiesCreated featurePropertiesCreated(ThingId thingId, String featureId, FeatureProperties properties);

    /**
     * Creates a new {@link FeaturePropertiesDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperties were deleted.
     * @param featureId the ID of the Feature in which the FeatureProperties were deleted.
     * @return the new FeaturePropertiesDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertiesDeleted featurePropertiesDeleted(ThingId thingId, String featureId);

    /**
     * Creates a new {@link FeaturePropertiesModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperties were modified.
     * @param featureId the ID of the Feature in which the FeatureProperties were modified.
     * @param properties the FeatureProperties which were modified.
     * @return the new FeaturePropertiesModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertiesModified featurePropertiesModified(ThingId thingId, String featureId,
            FeatureProperties properties);

    /**
     * Creates a new {@link FeaturePropertyCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperty was created.
     * @param featureId the ID of the Feature in which the FeatureProperty was created.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was created.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was created.
     * @return the new FeaturePropertyCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyCreated featurePropertyCreated(final ThingId thingId, final String featureId,
            final CharSequence propertyJsonPointer, final JsonValue propertyJsonValue) {
        return featurePropertyCreated(thingId, featureId, JsonPointer.of(propertyJsonPointer),
                propertyJsonValue);
    }

    /**
     * Creates a new {@link FeaturePropertyCreated} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperty was created.
     * @param featureId the ID of the Feature in which the FeatureProperty was created.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was created.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was created.
     * @return the new FeaturePropertyCreated.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyCreated featurePropertyCreated(ThingId thingId, String featureId, JsonPointer propertyJsonPointer,
            JsonValue propertyJsonValue);

    /**
     * Creates a new {@link FeaturePropertyDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperty was deleted.
     * @param featureId the ID of the Feature in which the FeatureProperty was deleted.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was deleted.
     * @return the new FeaturePropertyDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyDeleted featurePropertyDeleted(final ThingId thingId, final String featureId,
            final CharSequence propertyJsonPointer) {
        return featurePropertyDeleted(thingId, featureId, JsonPointer.of(propertyJsonPointer));
    }

    /**
     * Creates a new {@link FeaturePropertyDeleted} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperty was deleted.
     * @param featureId the ID of the Feature in which the FeatureProperty was deleted.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was deleted.
     * @return the new FeaturePropertyDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyDeleted featurePropertyDeleted(ThingId thingId, String featureId, JsonPointer propertyJsonPointer);

    /**
     * Creates a new {@link FeaturePropertyModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperty was modified.
     * @param featureId the ID of the Feature in which the FeatureProperty was modified.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was modified.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was modified.
     * @return the new FeaturePropertyModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    default FeaturePropertyModified featurePropertyModified(final ThingId thingId, final String featureId,
            final CharSequence propertyJsonPointer, final JsonValue propertyJsonValue) {
        return featurePropertyModified(thingId, featureId, JsonPointer.of(propertyJsonPointer),
                propertyJsonValue);
    }

    /**
     * Creates a new {@link FeaturePropertyModified} event with the required passed arguments.
     *
     * @param thingId the ID of the Thing in which the FeatureProperty was modified.
     * @param featureId the ID of the Feature in which the FeatureProperty was modified.
     * @param propertyJsonPointer the JsonPointer of the FeatureProperty which was modified.
     * @param propertyJsonValue the JsonValue of the FeatureProperty which was modified.
     * @return the new FeaturePropertyModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    FeaturePropertyModified featurePropertyModified(ThingId thingId, String featureId, JsonPointer
            propertyJsonPointer, JsonValue propertyJsonValue);

}
