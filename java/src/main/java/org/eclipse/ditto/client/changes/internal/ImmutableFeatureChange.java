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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.changes.AcknowledgementRequestHandle;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.FeatureChange;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.entity.id.EntityId;
import org.eclipse.ditto.base.model.entity.type.EntityType;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgement;

/**
 * An immutable implementation of {@link org.eclipse.ditto.client.changes.FeatureChange}.
 *
 * @since 1.0.0
 */
@Immutable
public final class ImmutableFeatureChange implements FeatureChange {

    private final Change change;
    private final Feature feature;

    /**
     * Constructs a new {@code ImmutableFeatureChange} object.
     *
     * @param entityId the identifier of the changed Thing.
     * @param feature the Feature which was changed.
     * @param changeAction the operation which caused the change.
     * @param path the JsonPointer of the changed json field.
     * @param revision the revision (change counter) of the change.
     * @param timestamp the timestamp of the change.
     * @param extra the extra data to be included in the change.
     * @param dittoHeaders the DittoHeaders of the event which lead to the change.
     * @param acknowledgementPublisher the consumer for publishing built acknowledgements to the Ditto backend.
     * @throws NullPointerException if any required argument is {@code null}.
     */
    public ImmutableFeatureChange(final EntityId entityId,
            final ChangeAction changeAction,
            @Nullable final Feature feature,
            final JsonPointer path,
            final long revision,
            @Nullable final Instant timestamp,
            @Nullable final JsonObject extra,
            final DittoHeaders dittoHeaders,
            final Consumer<Acknowledgement> acknowledgementPublisher) {

        this(new ImmutableChange(entityId, changeAction, path, getJsonValueForFeature(feature), revision, timestamp,
                extra, dittoHeaders, acknowledgementPublisher), feature);
    }

    /**
     * Constructs a new {@code ImmutableFeatureChange} object.
     *
     * @param change the change to use for delegation.
     * @param feature the additional {@code Feature} this FeatureChange is aware of.
     * @throws NullPointerException if {@code change} is {@code null}.
     * @since 1.1.0
     */
    public ImmutableFeatureChange(final Change change, @Nullable final Feature feature) {
        this.change = checkNotNull(change, "change");
        this.feature = feature;
    }

    @Nullable
    private static JsonValue getJsonValueForFeature(@Nullable final Feature feature) {
        return null != feature ? feature.toJson(feature.getImplementedSchemaVersion()) : null;
    }

    @Override
    public EntityId getEntityId() {
        return change.getEntityId();
    }

    @Override
    public EntityType getEntityType() {
        return change.getEntityType();
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
    public Feature getFeature() {
        return feature;
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
    public Optional<JsonObject> getExtra() {
        return change.getExtra();
    }

    @Override
    public ImmutableFeatureChange withExtra(@Nullable final JsonObject extra) {
        return new ImmutableFeatureChange(change.withExtra(extra), feature);
    }

    @Override
    public DittoHeaders getDittoHeaders() {
        return change.getDittoHeaders();
    }

    @Override
    public Change setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new ImmutableFeatureChange(change.setDittoHeaders(dittoHeaders), feature);
    }

    @Override
    public void handleAcknowledgementRequests(final Consumer<Collection<AcknowledgementRequestHandle>> acknowledgementHandles) {
        change.handleAcknowledgementRequests(acknowledgementHandles);
    }

    @Override
    public Change withPathAndValue(final JsonPointer path, @Nullable final JsonValue value) {
        return new ImmutableFeatureChange(change.withPathAndValue(path, value), feature);
    }

    @Override
    public void handleAcknowledgementRequest(final AcknowledgementLabel acknowledgementLabel,
            final Consumer<AcknowledgementRequestHandle> acknowledgementHandle) {
        change.handleAcknowledgementRequest(acknowledgementLabel, acknowledgementHandle);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableFeatureChange that = (ImmutableFeatureChange) o;
        return Objects.equals(change, that.change) && Objects.equals(feature, that.feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(change, feature);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "change=" + change + ", feature=" + feature + "]";
    }

}
