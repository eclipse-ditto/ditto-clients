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
package org.eclipse.ditto.client.management;

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.base.model.signals.acks.Acknowledgements;

/**
 * This exception is thrown in the Ditto client if the requested acknowledgements failed.
 *
 * @since 1.1.0
 */
@Immutable
public class AcknowledgementsFailedException extends RuntimeException {

    private static final long serialVersionUID = -4578923424099138760L;

    private static final String MESSAGE_TEMPLATE = "Requested acknowledgements failed with status code %d.";

    private final transient Acknowledgements acknowledgements;

    private AcknowledgementsFailedException(final Acknowledgements acknowledgements) {
        super(String.format(MESSAGE_TEMPLATE, checkNotNull(acknowledgements, "acknowledgements").getHttpStatus().getCode()));
        this.acknowledgements = acknowledgements;
    }

    /**
     * Create an {@code AcknowledgementsFailedException}.
     *
     * @param acknowledgements The failed acknowledgements.
     * @return The exception.
     */
    public static AcknowledgementsFailedException of(final Acknowledgements acknowledgements) {
        return new AcknowledgementsFailedException(acknowledgements);
    }

    /**
     * Get the failed acknowledgements that caused this exception.
     *
     * @return the failed acknowledgements.
     */
    public Acknowledgements getAcknowledgements() {
        return acknowledgements;
    }

}
