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
package org.eclipse.ditto.client.messaging.internal;

import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.configuration.DisconnectedContext;

/**
 * Default implementation of {@link DisconnectedContext}.
 */
final class DefaultDisconnectedContext implements DisconnectedContext {

    private final Source source;
    @Nullable private final Throwable throwable;
    private final DisconnectionHandler disconnectionHandler;

    DefaultDisconnectedContext(final Source source,
            @Nullable final Throwable throwable,
            final DisconnectionHandler disconnectionHandler) {
        this.source = source;
        this.throwable = throwable;
        this.disconnectionHandler = disconnectionHandler;
    }

    @Override
    public Source getSource() {
        return source;
    }

    @Override
    public Optional<Throwable> getCause() {
        return Optional.ofNullable(throwable);
    }

    @Override
    public DisconnectionHandler handleDisconnect() {
        return disconnectionHandler;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "source=" + source +
                ", throwable=" + throwable +
                "]";
    }

}
