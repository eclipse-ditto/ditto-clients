/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.ack.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.ack.AcknowledgementRequestHandle;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.entity.id.EntityIdWithType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersBuilder;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;

/**
 * An immutable implementation of {@link AcknowledgementRequestHandle}.
 *
 * @since 1.1.0
 */
@Immutable
public final class ImmutableAcknowledgementRequestHandle implements AcknowledgementRequestHandle {

    private final AcknowledgementLabel acknowledgementLabel;
    private final EntityIdWithType entityId;
    private final DittoHeaders dittoHeaders;
    private final Consumer<Acknowledgement> acknowledgementPublisher;

    /**
     * Creates a new ImmutableAcknowledgementRequestHandle instance.
     *
     * @param acknowledgementLabel the acknowledgement label to handle.
     * @param entityId the entity id to handle.
     * @param dittoHeaders the ditto headers which were contained in the acknowledgement request to handle.
     * @param acknowledgementPublisher the consumer for publishing built acknowledgements to the Ditto backend.
     */
    public ImmutableAcknowledgementRequestHandle(final AcknowledgementLabel acknowledgementLabel,
            final EntityIdWithType entityId,
            final DittoHeaders dittoHeaders,
            final Consumer<Acknowledgement> acknowledgementPublisher) {

        this.acknowledgementLabel = checkNotNull(acknowledgementLabel, "acknowledgementLabel");
        this.entityId = checkNotNull(entityId, "entityId");
        this.dittoHeaders = checkNotNull(dittoHeaders, "dittoHeaders");
        this.acknowledgementPublisher = checkNotNull(acknowledgementPublisher, "acknowledgementPublisher");
    }

    @Override
    public AcknowledgementLabel getAcknowledgementLabel() {
        return acknowledgementLabel;
    }

    @Override
    public EntityIdWithType getEntityId() {
        return entityId;
    }

    @Override
    public DittoHeaders getDittoHeaders() {
        return dittoHeaders;
    }

    @Override
    public AcknowledgementRequestHandle setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new ImmutableAcknowledgementRequestHandle(acknowledgementLabel, entityId, dittoHeaders,
                acknowledgementPublisher);
    }

    @Override
    public void acknowledge(final HttpStatusCode statusCode) {
        acknowledge(statusCode, null);
    }

    @Override
    public void acknowledge(final HttpStatusCode statusCode, @Nullable final JsonValue payload) {
        // only retain the bare minimum of received DittoHeaders by default:
        final DittoHeadersBuilder<?, ?> dittoHeadersBuilder = DittoHeaders.newBuilder();
        dittoHeaders.getCorrelationId().ifPresent(dittoHeadersBuilder::correlationId);
        final DittoHeaders minimizedDittoHeaders = dittoHeadersBuilder.build();
        acknowledge(Acknowledgement.of(acknowledgementLabel, entityId, statusCode, minimizedDittoHeaders, payload));
    }

    @Override
    public void acknowledge(final Acknowledgement acknowledgement) {
        acknowledgementPublisher.accept(acknowledgement);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableAcknowledgementRequestHandle that = (ImmutableAcknowledgementRequestHandle) o;
        return Objects.equals(acknowledgementLabel, that.acknowledgementLabel) &&
                Objects.equals(entityId, that.entityId) &&
                Objects.equals(dittoHeaders, that.dittoHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acknowledgementLabel, entityId, dittoHeaders);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "acknowledgementLabel=" + acknowledgementLabel +
                ", entityId=" + entityId +
                ", dittoHeaders=" + dittoHeaders +
                "]";
    }
}
