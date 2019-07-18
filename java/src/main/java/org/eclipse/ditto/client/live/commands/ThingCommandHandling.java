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
import org.eclipse.ditto.signals.commands.live.modify.CreateThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveThingLiveCommand;

/**
 * Provides the necessary functionality for registering {@link org.eclipse.ditto.signals.commands.live.base.LiveCommand
 * LiveCommand} functions to receive commands to manage and retrieve <em>a particular</em> {@link
 * org.eclipse.ditto.model.things.Thing Thing}.
 *
 * @since 1.0.0
 */
public interface ThingCommandHandling {

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.CreateThing CreateThing}
     * commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingCreateThingCommands()
     */
    void handleCreateThingCommands(Function<CreateThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.CreateThing
     * CreateThing} commands.
     */
    void stopHandlingCreateThingCommands();

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyThing ModifyThing}
     * commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyThingCommands()
     */
    void handleModifyThingCommands(Function<ModifyThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyThing
     * ModifyThing} commands.
     */
    void stopHandlingModifyThingCommands();

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteThing DeleteThing}
     * commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingDeleteThingCommands()
     */
    void handleDeleteThingCommands(Function<DeleteThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteThing
     * DeleteThing} commands.
     */
    void stopHandlingDeleteThingCommands();

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveThing
     * RetrieveThing} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveThingCommands()
     */
    void handleRetrieveThingCommandsFunction(
            Function<RetrieveThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveThing
     * RetrieveThing} commands.
     */
    void stopHandlingRetrieveThingCommands();

}
