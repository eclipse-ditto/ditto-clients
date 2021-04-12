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
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.MergeThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeaturesLiveCommand;

/**
 * Provides the necessary functionality for registering {@link org.eclipse.ditto.signals.commands.live.base.LiveCommand
 * LiveCommand} functions to receive commands to manage and retrieve {@link org.eclipse.ditto.model.things.Feature
 * Feature}s.
 *
 * @since 1.0.0
 */
public interface FeaturesCommandHandling extends FeaturePropertiesCommandHandling {

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures
     * ModifyFeatures} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyFeatureCommands()
     */
    default void handleModifyFeaturesCommands(
            final Function<ModifyFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(ModifyFeaturesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeature
     * ModifyFeature} commands.
     */
    default void stopHandlingModifyFeaturesCommands() {
        unregister(ModifyFeaturesLiveCommand.class);
    }

    /**
     * Registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing
     * MergeThing} commands for changes on features level.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingMergeFeaturesCommands()
     */
    default void handleMergeFeaturesCommands(
            final Function<MergeThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(MergeThingLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing
     * MergeThing} commands.
     */
    default void stopHandlingMergeFeaturesCommands() {
        unregister(MergeThingLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures
     * DeleteFeatures} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingDeleteFeatureCommands()
     */
    default void handleDeleteFeaturesCommands(
            final Function<DeleteFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(DeleteFeaturesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures
     * DeleteFeatures} commands.
     */
    default void stopHandlingDeleteFeaturesCommands() {
        unregister(DeleteFeaturesLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeature
     * ModifyFeature} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingModifyFeatureCommands()
     */
    default void handleModifyFeatureCommands(
            final Function<ModifyFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(ModifyFeatureLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeature
     * ModifyFeature} commands.
     */
    default void stopHandlingModifyFeatureCommands() {
        unregister(ModifyFeatureLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeature
     * DeleteFeature} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingDeleteFeatureCommands()
     */
    default void handleDeleteFeatureCommands(
            final Function<DeleteFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(DeleteFeatureLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeature
     * DeleteFeature} commands.
     */
    default void stopHandlingDeleteFeatureCommands() {
        unregister(DeleteFeatureLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatures
     * RetrieveFeatures} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveFeaturesCommands()
     */
    default void handleRetrieveFeaturesCommands(
            final Function<RetrieveFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(RetrieveFeaturesLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatures
     * RetrieveFeatures} commands.
     */
    default void stopHandlingRetrieveFeaturesCommands() {
        unregister(RetrieveFeaturesLiveCommand.class);
    }

    /**
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeature
     * RetrieveFeature} commands.
     * <p>
     * If registered for a specific {@code Thing}, it will only receive commands for that Thing and its Features.
     * Otherwise, it will receive messages for <em>all</em> Thing's Features.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveFeatureCommands()
     */
    default void handleRetrieveFeatureCommands(
            final Function<RetrieveFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        register(LiveCommandHandler.of(RetrieveFeatureLiveCommand.class, handler));
    }

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeature
     * RetrieveFeature} commands.
     */
    default void stopHandlingRetrieveFeatureCommands() {
        unregister(RetrieveFeatureLiveCommand.class);
    }

}
