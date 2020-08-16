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
package org.eclipse.ditto.client.ack;

import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;

/**
 * A signal that can be acknowledged.
 *
 * @since 1.2.0
 */
public interface Acknowledgeable {

    /**
     * Handles {@code AcknowledgementRequest}s issued by the Ditto backend for a received signal
     * translated into this Acknowledgeable by invoking the passed {@code acknowledgementHandles} consumer with
     * client side {@code AcknowledgementHandle}s.
     *
     * @param acknowledgementHandles the consumer to invoke with a collection of {@code AcknowledgementHandle}s used to
     * send back {@code Acknowledgements}.
     * @since 1.2.0
     */
    void handleAcknowledgementRequests(Consumer<Collection<AcknowledgementRequestHandle>> acknowledgementHandles);

    /**
     * Handles an {@code AcknowledgementRequest} identified by the passed {@code acknowledgementLabel} issued by the
     * Ditto backend for a received signal translated into this Acknowledgeable by invoking the passed
     * {@code acknowledgementHandle} consumer with a client side {@code AcknowledgementHandle} - if the passed
     * acknowledgementLabel was present in the requested acknowledgements.
     *
     * @param acknowledgementLabel the {@code AcknowledgementLabel} which should be handled - if present - by the passed
     * {@code acknowledgementHandle}.
     * @param acknowledgementHandle the consumer to invoke with a {@code AcknowledgementHandle} used to
     * send back an {@code Acknowledgement}.
     * @since 1.2.0
     */
    void handleAcknowledgementRequest(AcknowledgementLabel acknowledgementLabel,
            Consumer<AcknowledgementRequestHandle> acknowledgementHandle);

}
