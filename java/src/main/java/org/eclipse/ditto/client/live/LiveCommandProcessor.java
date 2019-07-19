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
package org.eclipse.ditto.client.live;

import org.eclipse.ditto.signals.commands.live.base.LiveCommand;

/**
 * Internal interface for implementations capable of processing {@link LiveCommand}s.
 *
 * @since 1.0.0
 */
public interface LiveCommandProcessor {

    /**
     * Processes the passed {@link LiveCommand} and reports the successful processing via return value.
     *
     * @param liveCommand the live command to process
     * @return {@code true} when the passed {@code liveCommand} was successfully processed, {@code false} if either the
     * implementation did not have a function to handle the type or a RuntimeException occurred during invocation.
     */
    boolean processLiveCommand(LiveCommand liveCommand);

}
