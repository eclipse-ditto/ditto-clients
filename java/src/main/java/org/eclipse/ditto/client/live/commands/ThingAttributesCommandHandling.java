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
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.client.live.commands.modify.DeleteAttributeLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteAttributesLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.MergeThingLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyAttributeLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyAttributesLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveAttributeLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveAttributesLiveCommand;

/**
 * Provides the necessary functionality for registering {@link org.eclipse.ditto.client.live.commands.base.LiveCommand
 * LiveCommand} function to receive commands to manage and retrieve {@link java.util.jar.Attributes Attributes}.
 *
 * @since 1.0.0
 */
public interface ThingAttributesCommandHandling extends LiveCommandProcessor {

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes
     * ModifyAttributes} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyAttributesCommands()
     */
    default void handleModifyAttributesCommands(
            final Function<ModifyAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(ModifyAttributesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes
     * ModifyAttributes} commands.
     */
    default void stopHandlingModifyAttributesCommands() {
        unregister(ModifyAttributesLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes
     * DeleteAttributes} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingDeleteAttributesCommands()
     */
    default void handleDeleteAttributesCommands(
            final Function<DeleteAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(DeleteAttributesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes
     * DeleteAttributes} commands.
     */
    default void stopHandlingDeleteAttributesCommands() {
        unregister(DeleteAttributesLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute
     * ModifyAttribute} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyAttributeCommands()
     */
    default void handleModifyAttributeCommands(
            final Function<ModifyAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(ModifyAttributeLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute
     * ModifyAttribute} commands.
     */
    default void stopHandlingModifyAttributeCommands() {
        unregister(ModifyAttributeLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing
     * MergeThing} commands for changes on attributes level.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingMergeAttributeCommands()
     */
    default void handleMergeAttributeCommands(
            final Function<MergeThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(MergeThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing
     * MergeThing} commands.
     */
    default void stopHandlingMergeAttributeCommands() {
        unregister(MergeThingLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute
     * DeleteAttribute} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingDeleteAttributeCommands()
     */
    default void handleDeleteAttributeCommands(
            final Function<DeleteAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(DeleteAttributeLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute
     * DeleteAttribute} commands.
     */
    default void stopHandlingDeleteAttributeCommands() {
        unregister(DeleteAttributeLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveAttributes
     * RetrieveAttributes} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveAttributesCommands()
     */
    default void handleRetrieveAttributesCommands(
            final Function<RetrieveAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(RetrieveAttributesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveAttributes
     * RetrieveAttributes} commands.
     */
    default void stopHandlingRetrieveAttributesCommands() {
        unregister(RetrieveAttributesLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveAttribute
     * RetrieveAttribute}
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveAttributeCommand()
     */
    default void handleRetrieveAttributeCommand(
            final Function<RetrieveAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(RetrieveAttributeLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveAttribute
     * RetrieveAttribute} commands.
     */
    default void stopHandlingRetrieveAttributeCommand() {
        unregister(RetrieveAttributeLiveCommand.class);
    }

}
