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
import org.eclipse.ditto.base.model.signals.commands.CommandResponse;
import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeaturesNotAccessibleException;
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeaturesNotModifiableException;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeaturesResponse;
import org.eclipse.ditto.base.model.signals.events.Event;
import org.eclipse.ditto.things.model.signals.events.FeaturesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturesModified;

/**
 * A mutable builder with a fluent API for creating a {@link LiveCommandAnswer} for a {@link
 * ModifyFeaturesLiveCommand}.
 */
@ParametersAreNonnullByDefault
@NotThreadSafe
final class ModifyFeaturesLiveCommandAnswerBuilderImpl
        extends AbstractLiveCommandAnswerBuilder<ModifyFeaturesLiveCommand, ModifyFeaturesLiveCommandAnswerBuilder.ResponseFactory, ModifyFeaturesLiveCommandAnswerBuilder.EventFactory>
        implements ModifyFeaturesLiveCommandAnswerBuilder {

    private ModifyFeaturesLiveCommandAnswerBuilderImpl(final ModifyFeaturesLiveCommand command) {
        super(command);
    }

    /**
     * Returns a new instance of {@code ModifyFeaturesLiveCommandAnswerBuilderImpl}.
     *
     * @param command the command to build an answer for.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    public static ModifyFeaturesLiveCommandAnswerBuilderImpl newInstance(final ModifyFeaturesLiveCommand command) {
        return new ModifyFeaturesLiveCommandAnswerBuilderImpl(command);
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
        public ModifyFeaturesResponse created() {
            return ModifyFeaturesResponse.created(command.getEntityId(), command.getFeatures(),
                    command.getDittoHeaders());
        }

        @Nonnull
        @Override
        public ModifyFeaturesResponse modified() {
            return ModifyFeaturesResponse.modified(command.getEntityId(), command.getDittoHeaders());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featuresNotAccessibleError() {
            return errorResponse(command.getEntityId(),
                    FeaturesNotAccessibleException.newBuilder(command.getEntityId())
                    .dittoHeaders(command.getDittoHeaders())
                    .build());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featuresNotModifiableError() {
            return errorResponse(command.getEntityId(),
                    FeaturesNotModifiableException.newBuilder(command.getEntityId())
                    .dittoHeaders(command.getDittoHeaders())
                    .build());
        }
    }

    @Immutable
    private final class EventFactoryImpl implements EventFactory {

        @Nonnull
        @Override
        public FeaturesCreated created() {
            return FeaturesCreated.of(command.getEntityId(), command.getFeatures(), -1, Instant.now(),
                    command.getDittoHeaders(), null);
        }

        @Nonnull
        @Override
        public FeaturesModified modified() {
            return FeaturesModified.of(command.getEntityId(), command.getFeatures(), -1, Instant.now(),
                    command.getDittoHeaders(), null);
        }
    }

}
