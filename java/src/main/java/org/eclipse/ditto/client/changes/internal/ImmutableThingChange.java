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

import org.eclipse.ditto.client.changes.AcknowledgementRequestHandle;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.type.EntityType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;

/**
 * Immutable implementation for {@link org.eclipse.ditto.client.changes.ThingChange}.
 *
 * @since 1.0.0
 */
@Immutable
public final class ImmutableThingChange implements ThingChange {

    private final Change change;
    @Nullable private final Thing thing;

    /**
     * Constructs a new {@code ImmutableThingChange} object.
     *
     * @param entityId the ID of the Thing to which this event belongs.
     * @param changeAction the operation which caused this change.
     * @param thing the Thing to which this event belongs. May be {@code null}, e.g. in case the thing has been
     * deleted.
     * @param path the JsonPointer of the changed json field.
     * @param revision the revision (change counter) of the change.
     * @param timestamp the timestamp of the change.
     * @param extra the extra data to be included in the change.
     * @param dittoHeaders the DittoHeaders of the event which lead to the change.
     * @param acknowledgementPublisher the consumer for publishing built acknowledgements to the Ditto backend.
     * @throws NullPointerException if any required argument is {@code null}.
     */
    public ImmutableThingChange(final EntityId entityId,
            final ChangeAction changeAction,
            @Nullable final Thing thing,
            final JsonPointer path,
            final long revision,
            @Nullable final Instant timestamp,
            @Nullable final JsonObject extra,
            final DittoHeaders dittoHeaders,
            final Consumer<Acknowledgement> acknowledgementPublisher) {

        this(new ImmutableChange(checkNotNull(entityId, "Thing ID"), checkNotNull(changeAction, "change action"), path,
                        getJsonValueForThing(thing), revision, timestamp, extra, dittoHeaders, acknowledgementPublisher),
                thing);
    }

    @Nullable
    private static JsonValue getJsonValueForThing(@Nullable final Thing thing) {
        return null != thing ? thing.toJson(thing.getImplementedSchemaVersion()) : null;
    }

    /**
     * Constructs a new {@code ImmutableThingChange} object.
     *
     * @param thingId the ID of the Thing to which this event belongs.
     * @param changeAction the operation which caused this change.
     * @param thing the Thing to which this event belongs. May be {@code null}, e.g. in case the thing has been
     * deleted.
     * @param revision the revision (change counter) of the change.
     * @param timestamp the timestamp of the change.
     * @param dittoHeaders the DittoHeaders of the event which lead to the change.
     * @param acknowledgementPublisher the consumer for publishing built acknowledgements to the Ditto backend.
     * @param extra the extra data to be included in the change.
     * @throws NullPointerException if any required argument is {@code null}.
     */
    public ImmutableThingChange(final ThingId thingId,
            final ChangeAction changeAction,
            @Nullable final Thing thing,
            final long revision,
            @Nullable final Instant timestamp,
            @Nullable final JsonObject extra,
            final DittoHeaders dittoHeaders,
            final Consumer<Acknowledgement> acknowledgementPublisher) {

        this(thingId, changeAction, thing, JsonFactory.emptyPointer(), revision, timestamp, extra, dittoHeaders,
                acknowledgementPublisher);
    }

    /**
     * Constructs a new {@code ImmutableThingChange} object.
     *
     * @param change the change to use for delegation.
     * @param thing the additional {@code Thing} this ThingChange is aware of.
     * @throws NullPointerException if {@code change} is {@code null}.
     * @since 1.1.0
     */
    public ImmutableThingChange(final Change change, @Nullable final Thing thing) {
        this.change = checkNotNull(change, "change");
        this.thing = thing;
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
    public Optional<Thing> getThing() {
        return Optional.ofNullable(thing);
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
    public ImmutableThingChange withExtra(@Nullable final JsonObject extra) {
        return new ImmutableThingChange(change.withExtra(extra), thing);
    }

    @Override
    public DittoHeaders getDittoHeaders() {
        return change.getDittoHeaders();
    }

    @Override
    public Change setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new ImmutableThingChange(change.setDittoHeaders(dittoHeaders), thing);
    }

    @Override
    public Change withPathAndValue(final JsonPointer path, @Nullable final JsonValue value) {
        return new ImmutableThingChange(change.withPathAndValue(path, value), thing);
    }

    @Override
    public void handleAcknowledgementRequests(
            final Consumer<Collection<AcknowledgementRequestHandle>> acknowledgementHandles) {
        change.handleAcknowledgementRequests(acknowledgementHandles);
    }

    @Override
    public void handleAcknowledgementRequest(final AcknowledgementLabel acknowledgementLabel,
            final Consumer<AcknowledgementRequestHandle> acknowledgementHandle) {
        change.handleAcknowledgementRequest(acknowledgementLabel, acknowledgementHandle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(change, thing);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImmutableThingChange other = (ImmutableThingChange) obj;
        return Objects.equals(change, other.change) && Objects.equals(thing, other.thing);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "change=" + change + ", thing=" + thing + "]";
    }

}
