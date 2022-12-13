/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.base.model.signals.Signal;

/**
 * Generic interface for handling a live command.
 *
 * @param <L> the type of live commands. MUST be an interface satisfying the recursive type bound.
 * @param <B> the type of the live command answer builder.
 * @since 1.2.0
 */
public interface LiveCommandHandler<L extends LiveCommand<L, B>, B extends LiveCommandAnswerBuilder> {

    /**
     * Create a live command handler.
     *
     * @param type the type of live commands. MUST be an interface satisfying the recursive type bound.
     * @param commandHandler constructor of any response or event to publish.
     * @param <L> type of live commands.
     * @param <B> type of live command answers.
     * @return the live command handler.
     */
    static <L extends LiveCommand<L, B>, B extends LiveCommandAnswerBuilder> LiveCommandHandler<L, B> of(
            final Class<L> type,
            final Function<L, LiveCommandAnswerBuilder.BuildStep> commandHandler) {

        return new LiveCommandHandlerImpl<>(type, commandHandler, acknowledgeable -> {});
    }

    /**
     * Create a live command handler with acknowledgement handling.
     *
     * @param type the type of live commands. MUST be an interface satisfying the recursive type bound.
     * @param commandHandler constructor of any response or event to publish and sender of any acknowledgements.
     * @param <L> type of live commands.
     * @param <B> type of live command answers.
     * @return the live command handler.
     */
    static <L extends LiveCommand<L, B>, B extends LiveCommandAnswerBuilder> LiveCommandHandler<L, B> withAcks(
            final Class<L> type,
            final Function<LiveCommandAcknowledgeable<L, B>, LiveCommandAnswerBuilder.BuildStep> commandHandler) {

        return new LiveCommandHandlerImpl<>(type, commandHandler);
    }

    /**
     * The type of live commands to handle.
     *
     * @return The type of live commands to handle.
     */
    Class<L> getType();

    /**
     * Handle a live command.
     *
     * @return the function supplying an answer build-step containing any response or event to send as replies.
     */
    Function<LiveCommandAcknowledgeable<L, B>, LiveCommandAnswerBuilder.BuildStep> getCommandHandler();

    /**
     * Apply the command handler after casting a live command of unknown type into the type of this handler.
     * To be called after runtime type check of the live command.
     *
     * @param liveCommand the live command.
     * @param signalPublisher the signal publisher.
     * @return the result of calling the command handler on the command.
     */
    default LiveCommandAnswerBuilder.BuildStep castAndApply(final LiveCommand<?, ?> liveCommand,
            final Consumer<Signal<?>> signalPublisher) {
        return getCommandHandler().apply(LiveCommandAcknowledgeable.of(getType().cast(liveCommand), signalPublisher));
    }
}
