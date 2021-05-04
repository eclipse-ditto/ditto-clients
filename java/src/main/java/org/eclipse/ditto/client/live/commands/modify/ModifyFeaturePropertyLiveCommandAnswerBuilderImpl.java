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
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeaturePropertyNotAccessibleException;
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeaturePropertyNotModifiableException;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeaturePropertyResponse;
import org.eclipse.ditto.base.model.signals.events.Event;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyModified;

/**
 * A mutable builder with a fluent API for creating a {@link LiveCommandAnswer} for a
 * {@link ModifyFeaturePropertyLiveCommand}.
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@NotThreadSafe
final class ModifyFeaturePropertyLiveCommandAnswerBuilderImpl
        extends AbstractLiveCommandAnswerBuilder<ModifyFeaturePropertyLiveCommand,
        ModifyFeaturePropertyLiveCommandAnswerBuilder.ResponseFactory,
        ModifyFeaturePropertyLiveCommandAnswerBuilder.EventFactory>
        implements ModifyFeaturePropertyLiveCommandAnswerBuilder {

    private ModifyFeaturePropertyLiveCommandAnswerBuilderImpl(final ModifyFeaturePropertyLiveCommand command) {
        super(command);
    }

    /**
     * Returns a new instance of {@code ModifyFeaturePropertyLiveCommandAnswerBuilderImpl}.
     *
     * @param command the command to build an answer for.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    public static ModifyFeaturePropertyLiveCommandAnswerBuilderImpl newInstance(
            final ModifyFeaturePropertyLiveCommand command) {
        return new ModifyFeaturePropertyLiveCommandAnswerBuilderImpl(command);
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
        public ModifyFeaturePropertyResponse created() {
            return ModifyFeaturePropertyResponse.created(command.getEntityId(), command.getFeatureId(),
                    command.getPropertyPointer(),
                    command.getPropertyValue(), command.getDittoHeaders());
        }

        @Nonnull
        @Override
        public ModifyFeaturePropertyResponse modified() {
            return ModifyFeaturePropertyResponse.modified(command.getEntityId(), command.getFeatureId(),
                    command.getPropertyPointer(),
                    command.getDittoHeaders());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featurePropertyNotAccessibleError() {
            return errorResponse(command.getEntityId(),
                    FeaturePropertyNotAccessibleException.newBuilder(command.getEntityId(),
                            command.getFeatureId(),
                            command.getPropertyPointer())
                            .dittoHeaders(command.getDittoHeaders())
                            .build());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featurePropertyNotModifiableError() {
            return errorResponse(command.getEntityId(),
                    FeaturePropertyNotModifiableException.newBuilder(command.getEntityId(),
                            command.getFeatureId(), command.getPropertyPointer())
                            .dittoHeaders(command.getDittoHeaders())
                            .build());
        }
    }

    @Immutable
    private final class EventFactoryImpl implements EventFactory {

        @Nonnull
        @Override
        public FeaturePropertyCreated created() {
            return FeaturePropertyCreated.of(command.getEntityId(), command.getFeatureId(),
                    command.getPropertyPointer(),
                    command.getPropertyValue(), -1, Instant.now(), command.getDittoHeaders(), null);
        }

        @Nonnull
        @Override
        public FeaturePropertyModified modified() {
            return FeaturePropertyModified.of(command.getEntityId(), command.getFeatureId(),
                    command.getPropertyPointer(), command.getPropertyValue(), -1, Instant.now(),
                    command.getDittoHeaders(), null);
        }
    }

}
