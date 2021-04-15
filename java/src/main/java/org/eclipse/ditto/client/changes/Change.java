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
package org.eclipse.ditto.client.changes;

import java.time.Instant;
import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.ack.Acknowledgeable;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.id.WithEntityId;
import org.eclipse.ditto.model.base.entity.type.WithEntityType;
import org.eclipse.ditto.model.base.headers.DittoHeadersSettable;

/**
 * Common interface for all Thing related changes.
 *
 * @since 1.0.0
 */
public interface Change extends WithEntityId, WithEntityType, DittoHeadersSettable<Change>, Acknowledgeable {

    /**
     * Returns the {@link ChangeAction} which caused this change.
     *
     * @return the applied action.
     */
    ChangeAction getAction();

    /**
     * Returns whether the Change was a partial change relative to the change registration entry point. E.g. when a
     * change registration was done for all attributes and only a single attribute was changed.
     *
     * @return whether the Change was a partial change.
     */
    default boolean isPartial() {
        return !isFull();
    }

    /**
     * Returns whether the Change was a full change relative to the change registration entry point. E.g. when a change
     * registration was done for all attributes and all attributes were changed at once or the complete Thing was
     * changed (and as a result also the full attributes).
     *
     * @return whether the Change was a full change.
     */
    default boolean isFull() {
        return getPath().isEmpty();
    }

    /**
     * Returns the relative path (relative to the change registration) of the changed JSON field.
     *
     * @return the relative path of the changed JSON field.
     */
    JsonPointer getPath();

    /**
     * Returns the optional value of the changed JSON field.
     *
     * @return the optional value of the changed JSON field.
     */
    Optional<JsonValue> getValue();

    /**
     * Returns the revision (change counter) of the change.
     *
     * @return the revision of the change.
     */
    long getRevision();

    /**
     * Returns the timestamp of the change.
     *
     * @return the timestamp of the change.
     */
    Optional<Instant> getTimestamp();

    /**
     * Returns the extra information which enriches the actual value of this change.
     *
     * @return the extra data or an empty Optional.
     * @since 1.1.0
     */
    Optional<JsonObject> getExtra();

    /**
     * Sets the given extra information which enriches the actual value of the change.
     * Previously set extra is replaced.
     *
     * @param extra the extra data information or {@code null}.
     * @return a new instance of this change with the added {@code extra} data.
     * @since 1.1.0
     */
    Change withExtra(@Nullable JsonObject extra);

    /**
     * Sets the given {@code path} and {@code value} into the change.
     *
     * @param path the relative path of the changed JSON field.
     * @param value the optional value of the changed JSON field.
     * @return a new instance of this change with the added data.
     * @since 1.1.0
     */
    Change withPathAndValue(JsonPointer path, @Nullable JsonValue value);

}
