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
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.MergeThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeaturePropertyLiveCommand;

/**
 * Provides the necessary functionality for registering {@link org.eclipse.ditto.signals.commands.live.base.LiveCommand
 * LiveCommand} functions to receive commands to manage and retrieve {@link org.eclipse.ditto.model.things.FeatureProperties
 * FeatureProperties}.
 *
 * @since 1.0.0
 */
public interface FeaturePropertiesCommandHandling extends LiveCommandProcessor {

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties
     * ModifyFeatureProperties} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyFeaturePropertiesCommands()
     */
    default void handleModifyFeaturePropertiesCommands(
            final Function<ModifyFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(ModifyFeaturePropertiesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties
     * ModifyFeatureProperties} commands.
     */
    default void stopHandlingModifyFeaturePropertiesCommands() {
        unregister(ModifyFeaturePropertiesLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties
     * DeleteFeatureProperties} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingDeleteFeaturePropertiesCommands()
     */
    default void handleDeleteFeaturePropertiesCommands(
            final Function<DeleteFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(DeleteFeaturePropertiesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties
     * DeleteFeatureProperties} commands.
     */
    default void stopHandlingDeleteFeaturePropertiesCommands() {
        unregister(DeleteFeaturePropertiesLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty
     * ModifyFeatureProperty} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyFeaturePropertyCommands()
     */
    default void handleModifyFeaturePropertyCommands(
            final Function<ModifyFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(ModifyFeaturePropertyLiveCommand.class, handler));
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing
     * MergeThing} commands for changes on feature properties level.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyFeaturePropertyCommands()
     */
    default void handleMergeFeaturePropertyCommands(
            final Function<MergeThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(MergeThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing
     * MergeThing} commands.
     */
    default void stopHandlingModifyFeaturePropertyCommands() {
        unregister(ModifyFeaturePropertyLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty
     * DeleteFeatureProperty} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingDeleteFeaturePropertyCommands()
     */
    default void handleDeleteFeaturePropertyCommands(
            final Function<DeleteFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(DeleteFeaturePropertyLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty
     * DeleteFeatureProperty} commands.
     */
    default void stopHandlingDeleteFeaturePropertyCommands() {
        unregister(DeleteFeaturePropertyLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperty
     * RetrieveFeatureProperty} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveFeaturePropertyCommands()
     */
    default void handleRetrieveFeaturePropertyCommands(
            final Function<RetrieveFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(RetrieveFeaturePropertyLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperty
     * RetrieveFeatureProperty} commands.
     */
    default void stopHandlingRetrieveFeaturePropertyCommands() {
        unregister(RetrieveFeaturePropertyLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperties
     * RetrieveFeatureProperties} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveFeaturePropertiesCommands()
     */
    default void handleRetrieveFeaturePropertiesCommands(
            Function<RetrieveFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(RetrieveFeaturePropertiesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperties
     * RetrieveFeatureProperties} commands.
     */
    default void stopHandlingRetrieveFeaturePropertiesCommands() {
        unregister(RetrieveFeaturePropertiesLiveCommand.class);
    }

}
