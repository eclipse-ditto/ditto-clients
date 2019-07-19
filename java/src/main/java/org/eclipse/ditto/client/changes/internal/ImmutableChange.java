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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;

/**
 * An immutable holder for a Thing ID and a {@link ChangeAction}. Objects of this class are meant to be used in a
 * composition scenario.
 *
 * @since 1.0.0
 */
@Immutable
public final class ImmutableChange implements Change {

    private final String thingId;
    private final ChangeAction action;
    private final JsonPointer path;
    @Nullable private final JsonValue value;
    private final long revision;
    @Nullable private final Instant timestamp;

    /**
     * Constructs a new {@code ImmutableThingChange} object.
     *
     * @param thingId ID of the changed Thing.
     * @param action the operation which caused the change.
     * @param path the JsonPointer of the changed json field.
     * @param value the value of the changed json field.
     * @param revision the revision (change counter) of the change.
     * @param timestamp the timestamp of the change.
     */
    public ImmutableChange(final String thingId,
            final ChangeAction action,
            final JsonPointer path,
            @Nullable final JsonValue value,
            final long revision,
            @Nullable final Instant timestamp) {

        this.thingId = argumentNotNull(thingId, "Thing ID");
        this.action = argumentNotNull(action, "change action");
        this.path = argumentNotNull(path, "path");
        this.value = value;
        this.revision = revision;
        this.timestamp = timestamp;
    }

    @Override
    public String getThingId() {
        return thingId;
    }

    @Override
    public ChangeAction getAction() {
        return action;
    }

    @Override
    public JsonPointer getPath() {
        return path;
    }

    @Override
    public Optional<JsonValue> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public long getRevision() {
        return revision;
    }

    @Override
    public Optional<Instant> getTimestamp() {
        return Optional.ofNullable(timestamp);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableChange that = (ImmutableChange) o;
        return Objects.equals(thingId, that.thingId) &&
                action == that.action &&
                Objects.equals(path, that.path) &&
                Objects.equals(value, that.value) &&
                revision == that.revision &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thingId, action, path, value, revision, timestamp);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "thingId=" + thingId +
                ", action=" + action +
                ", path=" + path +
                ", value=" + value +
                ", revision=" + revision +
                ", timestamp=" + timestamp +
                "]";
    }

}
