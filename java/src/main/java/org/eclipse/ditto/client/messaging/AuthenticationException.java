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
package org.eclipse.ditto.client.messaging;

import javax.annotation.concurrent.Immutable;

/**
 * This exception is thrown if an error occurred during authentication process of the Client.
 *
 * @since 1.0.0
 */
@Immutable
public class AuthenticationException extends RuntimeException {

    private static final String DEFAULT_MESSAGE_TEMPLATE = "Authentication of session <%s> failed.";

    private static final String UNAUTHORIZED_MESSAGE_TEMPLATE =
            "Authentication of session <%s> failed because of invalid credentials.";

    private static final String FORBIDDEN_MESSAGE_TEMPLATE =
            "Authentication of session <%s> failed because of insufficient permissions.";

    private static final String STATUS_MESSAGE_TEMPLATE =
            "Authentication of session <%s> failed with status <%d> and reason: %s";

    private static final long serialVersionUID = 4405868587578425044L;

    private AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static AuthenticationException of(final String sessionId, final Throwable cause) {
        return new AuthenticationException(String.format(DEFAULT_MESSAGE_TEMPLATE, sessionId), cause);
    }

    public static AuthenticationException unauthorized(final String sessionId, final Throwable cause) {
        return new AuthenticationException(String.format(UNAUTHORIZED_MESSAGE_TEMPLATE, sessionId), cause);
    }

    public static AuthenticationException forbidden(final String sessionId, final Throwable cause) {
        return new AuthenticationException(String.format(FORBIDDEN_MESSAGE_TEMPLATE, sessionId), cause);
    }

    /**
     * Creates an AutenticationException for the {@code status code its {@code reason}.
     * @param sessionId the sessionId that failed to authenticate.
     * @param cause the cause of the failure.
     * @param status the status code of the failure.
     * @param reason a reason message for the failure.
     * @return the AuthenticationException with the provided information.
     * @since 1.1.0
     */
    public static AuthenticationException withStatus(final String sessionId, final Throwable cause,
            final int status, final String reason) {
        return new AuthenticationException(
                String.format(STATUS_MESSAGE_TEMPLATE, sessionId, status, reason),
                cause);
    }

}
