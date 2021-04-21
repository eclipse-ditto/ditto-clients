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
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureDefinitionNotAccessibleException;
import org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureDefinitionNotModifiableException;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinitionResponse;
import org.eclipse.ditto.signals.events.base.Event;
import org.eclipse.ditto.things.model.signals.events.FeatureDefinitionCreated;
import org.eclipse.ditto.things.model.signals.events.FeatureDefinitionModified;

/**
 * A mutable builder with a fluent API for creating a {@link LiveCommandAnswer} for a
 * {@link ModifyFeatureDefinitionLiveCommand}.
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@NotThreadSafe
final class ModifyFeatureDefinitionLiveCommandAnswerBuilderImpl
        extends AbstractLiveCommandAnswerBuilder<ModifyFeatureDefinitionLiveCommand,
        ModifyFeatureDefinitionLiveCommandAnswerBuilder.ResponseFactory,
        ModifyFeatureDefinitionLiveCommandAnswerBuilder.EventFactory>
        implements ModifyFeatureDefinitionLiveCommandAnswerBuilder {

    private ModifyFeatureDefinitionLiveCommandAnswerBuilderImpl(final ModifyFeatureDefinitionLiveCommand command) {
        super(command);
    }

    /**
     * Returns a new instance of {@code ModifyFeatureDefinitionLiveCommandAnswerBuilderImpl}.
     *
     * @param command the command to build an answer for.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    public static ModifyFeatureDefinitionLiveCommandAnswerBuilderImpl newInstance(
            final ModifyFeatureDefinitionLiveCommand command) {
        return new ModifyFeatureDefinitionLiveCommandAnswerBuilderImpl(command);
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
        public ModifyFeatureDefinitionResponse created() {
            return ModifyFeatureDefinitionResponse.created(command.getEntityId(), command.getFeatureId(),
                    command.getDefinition(),
                    command.getDittoHeaders());
        }

        @Nonnull
        @Override
        public ModifyFeatureDefinitionResponse modified() {
            return ModifyFeatureDefinitionResponse.modified(command.getEntityId(), command.getFeatureId(),
                    command.getDittoHeaders());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featureDefinitionNotAccessibleError() {
            return errorResponse(command.getEntityId(),
                    FeatureDefinitionNotAccessibleException.newBuilder(command.getEntityId(),
                            command.getFeatureId())
                            .dittoHeaders(command.getDittoHeaders())
                            .build());
        }

        @Nonnull
        @Override
        public ThingErrorResponse featureDefinitionNotModifiableError() {
            return errorResponse(command.getEntityId(),
                    FeatureDefinitionNotModifiableException.newBuilder(command.getEntityId(),
                            command.getFeatureId())
                            .dittoHeaders(command.getDittoHeaders())
                            .build());
        }

    }

    @Immutable
    private final class EventFactoryImpl implements EventFactory {

        @Nonnull
        @Override
        public FeatureDefinitionCreated created() {
            return FeatureDefinitionCreated.of(command.getEntityId(), command.getFeatureId(),
                    command.getDefinition(), -1, Instant.now(), command.getDittoHeaders(), null);
        }

        @Nonnull
        @Override
        public FeatureDefinitionModified modified() {
            return FeatureDefinitionModified.of(command.getEntityId(), command.getFeatureId(),
                    command.getDefinition(), -1, Instant.now(), command.getDittoHeaders(), null);
        }

    }

}
