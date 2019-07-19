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
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Thing;

/**
 * Immutable implementation for {@link ThingChange}.
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
     * @param thingId the ID of the Thing to which this event belongs.
     * @param changeAction the operation which caused this change.
     * @param thing the Thing to which this event belongs. May be {@code null}, e.g. in case the thing has been
     * deleted.
     * @param path the JsonPointer of the changed json field.
     * @param revision the revision (change counter) of the change.
     * @param timestamp the timestamp of the change.
     * @throws NullPointerException if any required argument is {@code null}.
     */
    public ImmutableThingChange(final String thingId,
            final ChangeAction changeAction,
            @Nullable final Thing thing,
            final JsonPointer path,
            final long revision,
            @Nullable final Instant timestamp) {

        checkNotNull(thingId, "Thing ID");
        checkNotNull(changeAction, "change action");
        this.thing = thing;
        change = new ImmutableChange(thingId, changeAction, path, getJsonValueForThing(thing), revision, timestamp);
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
     * @throws NullPointerException if any required argument is {@code null}.
     */
    public ImmutableThingChange(final String thingId, final ChangeAction changeAction, @Nullable final Thing thing,
            final long revision, @Nullable final Instant timestamp) {
        this(thingId, changeAction, thing, JsonFactory.emptyPointer(), revision, timestamp);
    }

    @Nullable
    private static JsonValue getJsonValueForThing(@Nullable final Thing thing) {
        return null != thing ? thing.toJson(thing.getImplementedSchemaVersion()) : null;
    }

    @Override
    public String getThingId() {
        return change.getThingId();
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
    public int hashCode() {
        return Objects.hash(change, thing);
    }

    @Override
    public boolean equals(final Object obj) {
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
