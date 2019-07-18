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
import org.eclipse.ditto.signals.commands.live.modify.DeleteAttributeLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteAttributesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyAttributeLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyAttributesLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveAttributeLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveAttributesLiveCommand;

/**
 * Provides the necessary functionality for registering {@link org.eclipse.ditto.signals.commands.live.base.LiveCommand
 * LiveCommand} function to receive commands to manage and retrieve {@link java.util.jar.Attributes Attributes}.
 *
 * @since 1.0.0
 */
public interface ThingAttributesCommandHandling {

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
    void handleModifyAttributesCommands(
            Function<ModifyAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes
     * ModifyAttributes} commands.
     */
    void stopHandlingModifyAttributesCommands();

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
    void handleDeleteAttributesCommands(
            Function<DeleteAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes
     * DeleteAttributes} commands.
     */
    void stopHandlingDeleteAttributesCommands();

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
    void handleModifyAttributeCommands(
            Function<ModifyAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute
     * ModifyAttribute} commands.
     */
    void stopHandlingModifyAttributeCommands();

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
    void handleDeleteAttributeCommands(
            Function<DeleteAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute
     * DeleteAttribute} commands.
     */
    void stopHandlingDeleteAttributeCommands();

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
    void handleRetrieveAttributesCommands(
            Function<RetrieveAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveAttributes
     * RetrieveAttributes} commands.
     */
    void stopHandlingRetrieveAttributesCommands();

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
    void handleRetrieveAttributeCommand(
            Function<RetrieveAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler);

    /**
     * De-registers the handler to receive {@link org.eclipse.ditto.signals.commands.things.query.RetrieveAttribute
     * RetrieveAttribute} commands.
     */
    void stopHandlingRetrieveAttributeCommand();

}
