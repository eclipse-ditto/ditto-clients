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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.ack.AcknowledgementRequestHandle;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.id.EntityIdWithType;
import org.eclipse.ditto.model.base.entity.type.EntityType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;

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
     * @param extra the extra data to be included in the change.
     * @param dittoHeaders the DittoHeaders of the event which lead to the change.
     * @param acknowledgementPublisher the consumer for publishing built acknowledgements to the Ditto backend.
     * @throws NullPointerException if any required argument is {@code null}.
     */
    public ImmutableFeaturesChange(final EntityIdWithType entityId,
            final ChangeAction changeAction,
            @Nullable final Features features,
            final JsonPointer path,
            final long revision,
            @Nullable final Instant timestamp,
            @Nullable final JsonObject extra,
            final DittoHeaders dittoHeaders,
            final Consumer<Acknowledgement> acknowledgementPublisher) {

        this(new ImmutableChange(entityId, changeAction, path, getJsonValueForFeatures(features), revision, timestamp,
                extra, dittoHeaders, acknowledgementPublisher), features);
    }

    /**
     * Constructs a new {@code ImmutableFeaturesChange} object.
     *
     * @param change the change to use for delegation.
     * @param features the additional {@code Features} this FeaturesChange is aware of.
     * @throws NullPointerException if {@code change} is {@code null}.
     * @since 1.1.0
     */
    public ImmutableFeaturesChange(final Change change, @Nullable final Features features) {
        this.change = checkNotNull(change, "change");
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
    public Optional<JsonObject> getExtra() {
        return change.getExtra();
    }

    @Override
    public Change withExtra(@Nullable final JsonObject extra) {
        return new ImmutableFeaturesChange(change.withExtra(extra), features);
    }

    @Override
    public DittoHeaders getDittoHeaders() {
        return change.getDittoHeaders();
    }

    @Override
    public Change setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new ImmutableFeaturesChange(change.setDittoHeaders(dittoHeaders), features);
    }

    @Override
    public Change withPathAndValue(final JsonPointer path, @Nullable final JsonValue value) {
        return new ImmutableFeaturesChange(change.withPathAndValue(path, value), features);
    }

    @Override
    public void handleAcknowledgementRequests(final Consumer<Collection<AcknowledgementRequestHandle>> acknowledgementHandles) {
        change.handleAcknowledgementRequests(acknowledgementHandles);
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
