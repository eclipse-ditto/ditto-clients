/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.UnknownPathException;
import org.eclipse.ditto.signals.commands.things.ThingResourceVisitor;

/**
 * Implements {@link org.eclipse.ditto.signals.commands.things.ThingResourceVisitor} to map a
 * {@link org.eclipse.ditto.json.JsonPointer} to a bus address factory that is used in the client for event
 * notification.
 */
final class PathToBusAddressVisitor implements ThingResourceVisitor<ThingId, String> {

    private static final PathToBusAddressVisitor INSTANCE = new PathToBusAddressVisitor();

    private PathToBusAddressVisitor() {
        // prevent instantiation
    }

    /**
     * @return the {@link org.eclipse.ditto.client.internal.PathToBusAddressVisitor} singleton instance
     */
    static PathToBusAddressVisitor getInstance() {
        return INSTANCE;
    }

    @Override
    public String visitThing(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forThing(checkNotNull(thingId, "thingId"));
    }

    @Override
    public String visitThingDefinition(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forThingDefinition(checkNotNull(thingId));
    }

    @Override
    public String visitPolicyId(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forPolicyId(checkNotNull(thingId));
    }

    @Override
    public String visitAcl(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forAcl(checkNotNull(thingId));
    }

    @Override
    public String visitAclEntry(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forAclEntry(checkNotNull(thingId), extractSubjectId(path));
    }

    @Override
    public String visitAttributes(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forAttributes(checkNotNull(thingId));
    }

    @Override
    public String visitAttribute(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forAttribute(checkNotNull(thingId), extractAttributePath(path));
    }

    @Override
    public String visitFeature(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forFeature(checkNotNull(thingId), extractFeatureId(path));
    }

    @Override
    public String visitFeatures(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forFeatures(checkNotNull(thingId));
    }

    @Override
    public String visitFeatureDefinition(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forFeatureDefinition(checkNotNull(thingId), extractFeatureId(path));
    }

    @Override
    public String visitFeatureProperties(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forFeatureProperties(checkNotNull(thingId), extractFeatureId(path));
    }

    @Override
    public String visitFeatureProperty(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forFeatureProperty(checkNotNull(thingId), extractFeatureId(path),
                extractFeaturePropertyPath(path));
    }

    @Override
    public String visitFeatureDesiredProperties(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forFeatureDesiredProperties(checkNotNull(thingId), extractFeatureId(path));
    }

    @Override
    public String visitFeatureDesiredProperty(final JsonPointer path, @Nullable final ThingId thingId) {
        return BusAddressFactory.forFeatureDesiredProperty(checkNotNull(thingId), extractFeatureId(path),
                extractFeaturePropertyPath(path));
    }

    @Override
    public DittoRuntimeException getUnknownPathException(final JsonPointer path) {
        return UnknownPathException.newBuilder(path).build();
    }
}
