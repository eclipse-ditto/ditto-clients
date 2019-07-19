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
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertyLiveCommand;
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
public interface FeaturesCommandHandling {

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
    void handleModifyFeaturesCommands(
            Function<ModifyFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeature
     * ModifyFeature} commands.
     */
    void stopHandlingModifyFeaturesCommands();

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
    void handleDeleteFeaturesCommands(
            Function<DeleteFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures
     * DeleteFeatures} commands.
     */
    void stopHandlingDeleteFeaturesCommands();

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
    void handleModifyFeatureCommands(
            Function<ModifyFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeature
     * ModifyFeature} commands.
     */
    void stopHandlingModifyFeatureCommands();

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
    void handleDeleteFeatureCommands(
            Function<DeleteFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeature
     * DeleteFeature} commands.
     */
    void stopHandlingDeleteFeatureCommands();

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
    void handleModifyFeaturePropertiesCommands(
            Function<ModifyFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties
     * ModifyFeatureProperties} commands.
     */
    void stopHandlingModifyFeaturePropertiesCommands();

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
    void handleDeleteFeaturePropertiesCommands(
            Function<DeleteFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties
     * DeleteFeatureProperties} commands.
     */
    void stopHandlingDeleteFeaturePropertiesCommands();

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
    void handleModifyFeaturePropertyCommands(
            Function<ModifyFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty
     * ModifyFeatureProperty} commands.
     */
    void stopHandlingModifyFeaturePropertyCommands();

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
    void handleDeleteFeaturePropertyCommands(
            Function<DeleteFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty
     * DeleteFeatureProperty} commands.
     */
    void stopHandlingDeleteFeaturePropertyCommands();

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
    void handleRetrieveFeaturesCommands(
            Function<RetrieveFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatures
     * RetrieveFeatures} commands.
     */
    void stopHandlingRetrieveFeaturesCommands();

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
    void handleRetrieveFeatureCommands(
            Function<RetrieveFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeature
     * RetrieveFeature} commands.
     */
    void stopHandlingRetrieveFeatureCommands();

}
