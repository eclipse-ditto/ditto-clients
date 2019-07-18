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
package org.eclipse.ditto.client.live.commands;

import java.util.function.Function;

import org.eclipse.ditto.signals.commands.live.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.signals.commands.live.query.RetrieveThingsLiveCommand;

/**
 * Provides the necessary functionality for registering {@link org.eclipse.ditto.signals.commands.live.base.LiveCommand
 * LiveCommand} functions to receive commands to <em>generally</em> manage and retrieve {@link
 * org.eclipse.ditto.model.things.Thing Thing}s.
 *
 * @since 1.0.0
 */
public interface ThingsCommandHandling {

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveThings
     * RetrieveThings} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveThingsCommands()
     */
    void handleRetrieveThingsCommands(Function<RetrieveThingsLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveThings
     * RetrieveThings} commands.
     */
    void stopHandlingRetrieveThingsCommands();

}
