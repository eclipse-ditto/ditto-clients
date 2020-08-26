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

import java.util.Set;

import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;

/**
 * Validate an acknowledgement request from the client.
 */
public final class AcknowledgementRequestsValidator {

    private AcknowledgementRequestsValidator() {}

    /**
     * Validate acknowledgement requests from the client.
     *
     * @param acknowledgementRequests the acknowledgement requests of a signal sent by the client.
     * @param mandatoryLabel the mandatory acknowledgement label for the channel.
     */
    public static void validate(final Set<AcknowledgementRequest> acknowledgementRequests,
            final AcknowledgementLabel mandatoryLabel) {

        if (!acknowledgementRequests.isEmpty() &&
                !acknowledgementRequests.contains(AcknowledgementRequest.of(mandatoryLabel))) {
            throw new IllegalArgumentException("Expected acknowledgement request for label '" +
                    mandatoryLabel +
                    "' not found. Please make sure to always request the '" +
                    mandatoryLabel +
                    "' Acknowledgement if you need to process the response in the client.");
        }
    }

    /**
     * Create an {@code IllegalStateException} when receiving acknowledgements from the back-end not containing
     * an expected acknowledgement label.
     *
     * @param mandatoryLabel the mandatory acknowledgement label.
     * @return the {@code IllegalStateException}.
     */
    public static IllegalStateException didNotReceiveAcknowledgement(final AcknowledgementLabel mandatoryLabel) {
        return new IllegalStateException("Didn't receive an Acknowledgement for label '" +
                mandatoryLabel + "'. Please make sure to always request the '" +
                mandatoryLabel + "' Acknowledgement if you need to process the " +
                "response in the client.");
    }
}
