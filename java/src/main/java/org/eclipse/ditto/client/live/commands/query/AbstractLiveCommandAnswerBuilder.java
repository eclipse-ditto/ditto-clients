/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.live.commands.query;

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswer;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerFactory;
import org.eclipse.ditto.client.live.commands.base.LiveCommandResponseFactory;
import org.eclipse.ditto.base.model.signals.commands.CommandResponse;

/**
 * Abstract base implementation for all {@link LiveCommandAnswerBuilder}s for query commands.
 *
 * @param <C> the type of the LiveCommand
 * @param <R> the type of the LiveCommandResponseFactory to be used as function parameter for the
 * {@link #createResponseFunction}
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@NotThreadSafe
abstract class AbstractLiveCommandAnswerBuilder<C extends LiveCommand, R extends LiveCommandResponseFactory>
        implements LiveCommandAnswerBuilder.QueryCommandResponseStep<R>, LiveCommandAnswerBuilder.BuildStep {

    // This variable being protected is no problem as it is a) immutable and b) not visible beyond "modify" package.
    protected final C command;
    private Function<R, CommandResponse<?>> createResponseFunction;

    /**
     * Constructs a new {@code AbstractLiveCommandAnswerBuilder} object.
     *
     * @param command the command to build an answer for.
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    protected AbstractLiveCommandAnswerBuilder(final C command) {
        this.command = checkNotNull(command, "command");
        createResponseFunction = r -> null;
    }

    @Override
    public BuildStep withResponse(final Function<R, CommandResponse<?>> createResponseFunction) {
        this.createResponseFunction =
                checkNotNull(createResponseFunction, "function for creating a command response");
        return this;
    }

    @Override
    public BuildStep withoutResponse() {
        return this;
    }

    @Override
    public LiveCommandAnswer build() {
        final CommandResponse<?> commandResponse = doCreateResponse(createResponseFunction);

        return LiveCommandAnswerFactory.newLiveCommandAnswer(commandResponse);
    }

    /**
     * Creates a CommandResponse using the given Function.
     *
     * @param createResponseFunction the function for creating a CommandResponse with the help of the implied
     * LiveCommandResponseFactory.
     * @return the CommandResponse.
     */
    protected abstract CommandResponse doCreateResponse(Function<R, CommandResponse<?>> createResponseFunction);

}
