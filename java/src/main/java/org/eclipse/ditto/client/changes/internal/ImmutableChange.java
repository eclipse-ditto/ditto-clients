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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.ack.internal.ImmutableAcknowledgementRequestHandle;
import org.eclipse.ditto.client.ack.AcknowledgementRequestHandle;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.id.EntityIdWithType;
import org.eclipse.ditto.model.base.entity.type.EntityType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;

/**
 * An immutable holder for an {@link org.eclipse.ditto.model.base.entity.id.EntityId} and a {@link ChangeAction}.
 * Objects of this class are meant to be used in a composition scenario.
 *
 * @since 1.0.0
 */
@Immutable
public final class ImmutableChange implements Change {

    private final EntityIdWithType entityId;
    private final ChangeAction action;
    private final JsonPointer path;
    @Nullable private final JsonValue value;
    private final long revision;
    @Nullable private final Instant timestamp;
    @Nullable private final JsonObject extra;
    private final DittoHeaders dittoHeaders;
    private final Consumer<Acknowledgement> acknowledgementPublisher;

    /**
     * Constructs a new {@code ImmutableChange} object.
     *
     * @param entityId ID (with EntityType) of the changed entity.
     * @param changeAction the operation which caused the change.
     * @param path the JsonPointer of the changed json field.
     * @param value the value of the changed json field.
     * @param revision the revision (change counter) of the change.
     * @param timestamp the timestamp of the change.
     * @param extra the extra data to be included in the change.
     * @param dittoHeaders the DittoHeaders of the event which lead to the change.
     * @param acknowledgementPublisher the consumer for publishing built acknowledgements to the Ditto backend.
     * @throws NullPointerException if any required argument is {@code null}.
     */
    public ImmutableChange(final EntityIdWithType entityId,
            final ChangeAction changeAction,
            final JsonPointer path,
            @Nullable final JsonValue value,
            final long revision,
            @Nullable final Instant timestamp,
            @Nullable final JsonObject extra,
            final DittoHeaders dittoHeaders,
            final Consumer<Acknowledgement> acknowledgementPublisher) {

        this.entityId = checkNotNull(entityId, "entityId");
        this.action = checkNotNull(changeAction, "changeAction");
        this.path = checkNotNull(path, "path");
        this.value = value;
        this.revision = revision;
        this.timestamp = timestamp;
        this.extra = extra;
        this.dittoHeaders = checkNotNull(dittoHeaders, "dittoHeaders");
        this.acknowledgementPublisher = checkNotNull(acknowledgementPublisher, "acknowledgementPublisher");
    }

    @Override
    public EntityId getEntityId() {
        return entityId;
    }

    @Override
    public EntityType getEntityType() {
        return entityId.getEntityType();
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
    public Optional<JsonObject> getExtra() {
        return Optional.ofNullable(extra);
    }

    @Override
    public Change withExtra(@Nullable final JsonObject extra) {
        return new ImmutableChange(entityId, action, path, value, revision, timestamp, extra, dittoHeaders,
                acknowledgementPublisher);
    }

    @Override
    public DittoHeaders getDittoHeaders() {
        return dittoHeaders;
    }

    @Override
    public Change setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new ImmutableChange(entityId, action, path, value, revision, timestamp, extra, dittoHeaders,
                acknowledgementPublisher);
    }

    @Override
    public void handleAcknowledgementRequests(final Consumer<Collection<AcknowledgementRequestHandle>> acknowledgementHandles) {

        checkNotNull(acknowledgementHandles, "handleConsumers");
        acknowledgementHandles.accept(
                getDittoHeaders().getAcknowledgementRequests().stream()
                        .map(request -> new ImmutableAcknowledgementRequestHandle(request.getLabel(), entityId, dittoHeaders,
                                acknowledgementPublisher))
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

    @Override
    public Change withPathAndValue(final JsonPointer path, @Nullable final JsonValue value) {
        return new ImmutableChange(entityId, action, path, value, revision, timestamp, extra, dittoHeaders,
                acknowledgementPublisher);
    }

    @Override
    public void handleAcknowledgementRequest(final AcknowledgementLabel acknowledgementLabel,
            final Consumer<AcknowledgementRequestHandle> acknowledgementHandle) {

        checkNotNull(acknowledgementLabel, "acknowledgementLabel");
        checkNotNull(acknowledgementHandle, "handleConsumer");
        getDittoHeaders().getAcknowledgementRequests().stream()
                .filter(req -> req.getLabel().equals(acknowledgementLabel))
                .map(request -> new ImmutableAcknowledgementRequestHandle(request.getLabel(), entityId, dittoHeaders,
                        acknowledgementPublisher))
                .forEach(acknowledgementHandle);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableChange that = (ImmutableChange) o;
        return Objects.equals(entityId, that.entityId) &&
                action == that.action &&
                Objects.equals(path, that.path) &&
                Objects.equals(value, that.value) &&
                revision == that.revision &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(extra, that.extra) &&
                Objects.equals(dittoHeaders, that.dittoHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, action, path, value, revision, timestamp, extra, dittoHeaders);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "entityId=" + entityId +
                ", action=" + action +
                ", path=" + path +
                ", value=" + value +
                ", revision=" + revision +
                ", timestamp=" + timestamp +
                ", extra=" + extra +
                ", dittoHeaders=" + dittoHeaders +
                "]";
    }

}
