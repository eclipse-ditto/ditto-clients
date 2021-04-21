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
package org.eclipse.ditto.client.live.commands.modify;

import java.time.Instant;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswer;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureNotAccessibleException;
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureNotModifiableException;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureResponse;
import org.eclipse.ditto.signals.events.base.Event;
import org.eclipse.ditto.things.model.signals.events.FeatureDeleted;

/**
 * A mutable builder with a fluent API for creating a {@link LiveCommandAnswer} for a {@link DeleteFeatureLiveCommand}.
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@NotThreadSafe
final class DeleteFeatureLiveCommandAnswerBuilderImpl
        extends
        AbstractLiveCommandAnswerBuilder<DeleteFeatureLiveCommand, DeleteFeatureLiveCommandAnswerBuilder.ResponseFactory,
                DeleteFeatureLiveCommandAnswerBuilder.EventFactory>
        implements DeleteFeatureLiveCommandAnswerBuilder {

    private DeleteFeatureLiveCommandAnswerBuilderImpl(final DeleteFeatureLiveCommand command) {
        super(command);
    }

    /**
     * Returns a new instance of {@code DeleteFeatureLiveCommandAnswerBuilderImpl}.
     *
     * @param command the command to build an answer for.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    public static DeleteFeatureLiveCommandAnswerBuilderImpl newInstance(final DeleteFeatureLiveCommand command) {
        return new DeleteFeatureLiveCommandAnswerBuilderImpl(command);
    }

    @Override
    protected CommandResponse doCreateResponse(
            final Function<ResponseFactory, CommandResponse<?>> createResponseFunction) {
        return createResponseFunction.apply(new ResponseFactoryImpl());
    }

    @Override
    protected Event doCreateEvent(final Function<EventFactory, Event<?>> createEventFunction) {
        return createEventFunction.apply(new EventFactoryImpl());
    }

    @Immutable
    private final class ResponseFactoryImpl implements ResponseFactory {

        @Nonnull
        @Override
        public DeleteFeatureResponse deleted() {
            return DeleteFeatureResponse.of(command.getEntityId(), command.getFeatureId(),
                    command.getDittoHeaders());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featureNotAccessibleError() {
            return errorResponse(command.getEntityId(),
                    FeatureNotAccessibleException.newBuilder(command.getEntityId(), command.getFeatureId())
                            .dittoHeaders(command.getDittoHeaders())
                            .build());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featureNotModifiableError() {
            return errorResponse(command.getEntityId(),
                    FeatureNotModifiableException.newBuilder(command.getEntityId(), command.getFeatureId())
                            .dittoHeaders(command.getDittoHeaders())
                            .build());
        }
    }

    @Immutable
    private final class EventFactoryImpl implements EventFactory {

        @Nonnull
        @Override
        public FeatureDeleted deleted() {
            return FeatureDeleted.of(command.getEntityId(), command.getFeatureId(), -1, Instant.now(),
                    command.getDittoHeaders(), null);
        }
    }

}
