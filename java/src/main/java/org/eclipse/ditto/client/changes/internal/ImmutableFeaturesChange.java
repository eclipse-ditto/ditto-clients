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
package org.eclipse.ditto.client.changes.internal;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.things.Features;

/**
 * An immutable implementation of {@link org.eclipse.ditto.client.changes.FeaturesChange}.
 *
 * @since 1.0.0
 */
@Immutable
public final class ImmutableFeaturesChange implements FeaturesChange {

    private final Change change;
    private final Features features;

    /**
     * Constructs a new {@code ImmutableFeaturesChange} object.
     *
     * @param entityId the identifier of the changed Thing.
     * @param changeAction the operation which cause the change.
     * @param features the Features which were object to the change.
     * @param path the JsonPointer of the changed json field.
     * @param revision the revision (change counter) of the change.
     * @param timestamp the timestamp of the change.
     * @throws IllegalArgumentException if any argument is {@code null}.
     */
    public ImmutableFeaturesChange(final EntityId entityId,
            final ChangeAction changeAction,
            @Nullable final Features features,
            final JsonPointer path,
            final long revision,
            @Nullable final Instant timestamp) {

        change = new ImmutableChange(entityId, changeAction, path, getJsonValueForFeatures(features), revision,
                timestamp);
        this.features = features;
    }

    @Nullable
    private static JsonValue getJsonValueForFeatures(@Nullable final Features features) {
        return null != features ? features.toJson(features.getImplementedSchemaVersion()) : null;
    }

    @Override
    public EntityId getEntityId() {
        return change.getEntityId();
    }

    @Override
    public ChangeAction getAction() {
        return change.getAction();
    }

    @Override
    public JsonPointer getPath() {
        return change.getPath();
    }

    @Override
    public Optional<JsonValue> getValue() {
        return change.getValue();
    }

    @Override
    public Features getFeatures() {
        return features;
    }

    @Override
    public long getRevision() {
        return change.getRevision();
    }

    @Override
    public Optional<Instant> getTimestamp() {
        return change.getTimestamp();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableFeaturesChange that = (ImmutableFeaturesChange) o;
        return Objects.equals(change, that.change) && Objects.equals(features, that.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(change, features);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "change=" + change + ", features=" + features + "]";
    }

}
