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
package org.eclipse.ditto.client.live;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.ditto.client.live.commands.LiveCommandHandler;
import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswer;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.base.model.signals.Signal;
import org.slf4j.Logger;

/**
 * Internal interface for implementations capable of processing {@link LiveCommand}s.
 *
 * @since 1.0.0
 */
public interface LiveCommandProcessor {

    /**
     * Get a concurrent map of live command handlers.
     *
     * @return the live command handler.
     */
    Map<Class<? extends LiveCommand<?, ?>>, LiveCommandHandler<?, ?>> getLiveCommandHandlers();

    /**
     * Publish a signal.
     *
     * @param signal the signal to publish.
     * @throws org.eclipse.ditto.client.management.ClientReconnectingException if the client is in a reconnecting state.
     */
    void publishLiveSignal(Signal<?> signal);

    /**
     * Retrieve the logger.
     *
     * @return the logger.
     */
    Logger getLogger();

    /**
     * Register a live command handler.
     *
     * @param liveCommandHandler the live command handler.
     * @throws java.lang.IllegalStateException if the live command handled by the live command handler is already
     * registered.
     */
    default void register(final LiveCommandHandler<?, ?> liveCommandHandler) {
        final Class<? extends LiveCommand<?, ?>> liveCommandClass = liveCommandHandler.getType();
        getLiveCommandHandlers().compute(liveCommandClass, (clazz, handler) -> {
            if (handler != null) {
                throw new IllegalStateException(
                        "A Function for '" + liveCommandClass.getSimpleName() + "' is already " +
                                "defined. Stop the registered handler before registering a new handler.");
            } else {
                return liveCommandHandler;
            }
        });
    }

    /**
     * Remove the registration for a live command.
     *
     * @param liveCommandClass the class of the live command whose handler should be removed.
     */
    default void unregister(final Class<? extends LiveCommand<?, ?>> liveCommandClass) {
        getLiveCommandHandlers().remove(liveCommandClass);
    }

    /**
     * Processes the passed {@link LiveCommand} and reports the successful processing via return value.
     *
     * @param liveCommand the live command to process
     * @return {@code true} when the passed {@code liveCommand} was successfully processed, {@code false} if either the
     * implementation did not have a function to handle the type or a RuntimeException occurred during invocation.
     */
    default boolean processLiveCommand(final LiveCommand<?, ?> liveCommand) {
        return Arrays.stream(liveCommand.getClass().getInterfaces())
                .flatMap(clazz -> {
                    final LiveCommandHandler<?, ?> handler = getLiveCommandHandlers().get(clazz);
                    return handler == null ? Stream.empty() : Stream.of(handler);
                })
                .map(handler -> {
                    try {
                        final LiveCommandAnswerBuilder.BuildStep builder =
                                handler.castAndApply(liveCommand, this::publishLiveSignal);
                        final LiveCommandAnswer liveCommandAnswer = builder.build();
                        liveCommandAnswer.getResponse().ifPresent(this::publishLiveSignal);
                        liveCommandAnswer.getEvent().ifPresent(this::publishLiveSignal);
                        return true;
                    } catch (final RuntimeException e) {
                        getLogger().error(
                                "User defined function which processed LiveCommand '{}' threw RuntimeException: {}",
                                liveCommand.getType(), e.getMessage(), e);
                        return false;
                    }
                })
                .findAny()
                .orElse(false);
    }

}
