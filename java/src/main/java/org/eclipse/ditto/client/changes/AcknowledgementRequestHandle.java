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
package org.eclipse.ditto.client.changes;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.entity.id.EntityIdWithType;
import org.eclipse.ditto.model.base.headers.DittoHeadersSettable;
import org.eclipse.ditto.signals.acks.base.Acknowledgement;

/**
 * Abstraction for handling {@code AcknowledgementRequest}s issued by the Ditto backend together with e.g.
 * {@code Event}s. This handle provides means to {@code acknowledge} such acknowledgement requests with a custom
 * HttpStatus, an optional {@code payload} or - alternatively - with a self built {@code Acknowledgement}.
 *
 * @since 1.1.0
 */
@Immutable
public interface AcknowledgementRequestHandle extends DittoHeadersSettable<AcknowledgementRequestHandle> {

    /**
     * Returns the {@code AcknowledgementLabel} this handle was created for.
     *
     * @return the acknowledgement label to handle.
     */
    AcknowledgementLabel getAcknowledgementLabel();

    /**
     * Returns the entity ID containing the entity type this handle was created for.
     *
     * @return the entity id to handle.
     */
    EntityIdWithType getEntityId();

    /**
     * Builds and sends an {@code Acknowledgement} to the Ditto backend based on the information this handle instance
     * already has, combined with the passed {@code statusCode} and no {@code payload}.
     *
     * @param httpStatus the HTTP status to apply for the acknowledgement to send: use a range between 200 and 300
     * in order to declare a successful acknowledgement and a status code above 400 to declare a not successful one.
     * @since 2.0.0
     */
    void acknowledge(HttpStatus httpStatus);

    /**
     * Builds and sends an {@code Acknowledgement} to the Ditto backend based on the information this handle instance
     * already has, combined with the passed HTTP status and the passed {@code payload}.
     *
     * @param httpStatus the HTTP status to apply for the acknowledgement to send: use a range between 200 and 300
     * in order to declare a successful acknowledgement and a status code above 400 to declare a not successful one.
     * @param payload the payload as {@code JsonValue} to include in the sent acknowledgement.
     * @since 2.0.0
     */
    void acknowledge(HttpStatus httpStatus, @Nullable JsonValue payload);

    /**
     * Sends the passed {@code acknowledgement} to the Ditto backend.
     *
     * @param acknowledgement the already built {@code Acknowledgement} to send to the Ditto backend.
     */
    void acknowledge(Acknowledgement acknowledgement);

}
