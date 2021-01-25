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

import org.eclipse.ditto.client.live.LiveCommandProcessor;
import org.eclipse.ditto.signals.commands.live.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.signals.commands.live.modify.CreateThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.MergeThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveThingLiveCommand;

/**
 * Provides the necessary functionality for registering {@link org.eclipse.ditto.signals.commands.live.base.LiveCommand
 * LiveCommand} functions to receive commands to manage and retrieve <em>a particular</em> {@link
 * org.eclipse.ditto.model.things.Thing Thing}.
 *
 * @since 1.0.0
 */
public interface ThingCommandHandling extends LiveCommandProcessor {

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
    default void handleCreateThingCommands(
            final Function<CreateThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(CreateThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.CreateThing
     * CreateThing} commands.
     */
    default void stopHandlingCreateThingCommands() {
        unregister(CreateThingLiveCommand.class);
    }

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
    default void handleModifyThingCommands(
            final Function<ModifyThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(ModifyThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyThing
     * ModifyThing} commands.
     */
    default void stopHandlingModifyThingCommands() {
        unregister(ModifyThingLiveCommand.class);
    }

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing MergeThing}
     * commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingMergeThingCommands()
     */
    default void handleMergeThingCommands(
            final Function<MergeThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(MergeThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing
     * MergeThing} commands.
     */
    default void stopHandlingMergeThingCommands() {
        unregister(MergeThingLiveCommand.class);
    }

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
    default void handleDeleteThingCommands(
            final Function<DeleteThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(DeleteThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteThing
     * DeleteThing} commands.
     */
    default void stopHandlingDeleteThingCommands() {
        unregister(DeleteThingLiveCommand.class);
    }

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
    default void handleRetrieveThingCommandsFunction(
            final Function<RetrieveThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(RetrieveThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveThing
     * RetrieveThing} commands.
     */
    default void stopHandlingRetrieveThingCommands() {
        unregister(RetrieveThingLiveCommand.class);
    }

}
