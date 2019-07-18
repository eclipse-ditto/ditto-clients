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
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertyLiveCommand;
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
public interface FeaturePropertiesCommandHandling {

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
     * Registers a handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperty
     * RetrieveFeatureProperty} commands.
     *
     * @param handler the handler to receive the commands
     * @throws NullPointerException if {@code handler} is {@code null}
     * @throws IllegalStateException if there is already a handler registered. Stop the registered handler before
     * calling this method
     * @see #stopHandlingRetrieveFeaturePropertyCommands()
     */
    void handleRetrieveFeaturePropertyCommands(
            Function<RetrieveFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperty
     * RetrieveFeatureProperty} commands.
     */
    void stopHandlingRetrieveFeaturePropertyCommands();

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
    void handleRetrieveFeaturePropertiesCommands(
            Function<RetrieveFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureProperties
     * RetrieveFeatureProperties} commands.
     */
    void stopHandlingRetrieveFeaturePropertiesCommands();

}
