/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.live.messages.internal;

import java.net.URI;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeExceptionBuilder;
import org.eclipse.ditto.model.base.headers.DittoHeaders;

public final class MissingCorrelationIdException extends DittoRuntimeException {

    private static final String ERROR_CODE = "correlation.missing";
    private static final String MESSAGE = "Missing required correlation ID.";

    private MissingCorrelationIdException(
            final DittoHeaders dittoHeaders, @Nullable final String message,
            @Nullable final String description, @Nullable final Throwable cause,
            @Nullable final URI href) {
        super(ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR, dittoHeaders, message, description, cause, href);
    }

    public static Builder newBuilder() {
        return new Builder(MESSAGE);
    }


    @Override
    public DittoRuntimeException setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new Builder()
                .message(getMessage())
                .description(getDescription().orElse(null))
                .cause(getCause())
                .href(getHref().orElse(null))
                .dittoHeaders(dittoHeaders)
                .build();
    }

    @NotThreadSafe
    public static final class Builder extends DittoRuntimeExceptionBuilder<MissingCorrelationIdException> {

        private Builder() {}

        private Builder(final String message) {
            message(message);
        }

        @Override
        protected MissingCorrelationIdException doBuild(final DittoHeaders dittoHeaders,
                @Nullable final String message,
                @Nullable final String description,
                @Nullable final Throwable cause,
                @Nullable final URI href) {

            return new MissingCorrelationIdException(dittoHeaders, message, description, cause, href);
        }

    }
}
