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

/**
 * Package-private implementation of {@code LiveCommandHandler}.
 *
 * @param <L> the type of live commands. MUST be an interface satisfying the recursive type bound.
 * @param <B> the type of live command answer builder.
 * @since 1.2.0
 */
final class LiveCommandHandlerImpl<L extends LiveCommand<L, B>, B extends LiveCommandAnswerBuilder>
        implements LiveCommandHandler<L, B> {

    private final Class<L> type;
    private final Function<LiveCommandAcknowledgeable<L, B>, LiveCommandAnswerBuilder.BuildStep> commandHandler;

    LiveCommandHandlerImpl(final Class<L> type,
            final Function<LiveCommandAcknowledgeable<L, B>, LiveCommandAnswerBuilder.BuildStep> commandHandler
    ) {
        this.type = type;
        this.commandHandler = commandHandler;
    }

    LiveCommandHandlerImpl(final Class<L> type,
            final Function<L, LiveCommandAnswerBuilder.BuildStep> commandHandler,
            final Consumer<LiveCommandAcknowledgeable<L, B>> acknowledgeableConsumer) {
        this.type = type;
        this.commandHandler = combineCommandHandlers(commandHandler, acknowledgeableConsumer);
    }

    @Override
    public Class<L> getType() {
        return type;
    }

    @Override
    public Function<LiveCommandAcknowledgeable<L, B>, LiveCommandAnswerBuilder.BuildStep> getCommandHandler() {
        return commandHandler;
    }

    private static <L extends LiveCommand<L, B>, B extends LiveCommandAnswerBuilder>
    Function<LiveCommandAcknowledgeable<L, B>, LiveCommandAnswerBuilder.BuildStep> combineCommandHandlers(
            final Function<L, LiveCommandAnswerBuilder.BuildStep> commandHandler,
            final Consumer<LiveCommandAcknowledgeable<L, B>> acknowledgeableConsumer) {
        return acknowledgeable -> {
            acknowledgeableConsumer.accept(acknowledgeable);
            return commandHandler.apply(acknowledgeable.getLiveCommand());
        };
    }
}
