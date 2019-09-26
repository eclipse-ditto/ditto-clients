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
 * This exception is thrown if an error occurred within messaging.
 *
 * @since 1.0.0
 */
@Immutable
public class MessagingException extends RuntimeException {

    private static final String DEFAULT_MESSAGE_TEMPLATE = "Connect of session <%s> failed.";

    private static final String INTERRUPTED_MESSAGE_TEMPLATE =
            "Connect of session <%s> failed because it was interrupted.";

    private static final String TIMEOUT_MESSAGE_TEMPLATE =
            "Connect of session <%s> failed because it timed out.";

    private static final long serialVersionUID = 6930767503633213674L;

    private MessagingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static MessagingException connectFailed(final String sessionId, final Throwable cause) {
        return new MessagingException(String.format(DEFAULT_MESSAGE_TEMPLATE, sessionId), cause);
    }

    public static MessagingException connectInterrupted(final String sessionId, final Throwable cause) {
        return new MessagingException(String.format(INTERRUPTED_MESSAGE_TEMPLATE, sessionId), cause);
    }

    public static MessagingException connectTimeout(final String sessionId, final Throwable cause) {
        return new MessagingException(String.format(TIMEOUT_MESSAGE_TEMPLATE, sessionId), cause);
    }

}
