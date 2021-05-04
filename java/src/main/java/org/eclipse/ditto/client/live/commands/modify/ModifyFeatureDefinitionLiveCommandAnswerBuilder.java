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

import javax.annotation.Nonnull;

import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.client.live.commands.base.LiveCommandResponseFactory;
import org.eclipse.ditto.client.live.commands.base.LiveEventFactory;
import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinitionResponse;
import org.eclipse.ditto.things.model.signals.events.FeatureDefinitionCreated;
import org.eclipse.ditto.things.model.signals.events.FeatureDefinitionModified;

/**
 * LiveCommandAnswer builder for producing {@code CommandResponse}s and {@code Event}s for
 * {@link ModifyFeatureDefinition} commands.
 *
 * @since 2.0.0
 */
public interface ModifyFeatureDefinitionLiveCommandAnswerBuilder extends
        LiveCommandAnswerBuilder.ModifyCommandResponseStep<ModifyFeatureDefinitionLiveCommandAnswerBuilder.ResponseFactory,
                ModifyFeatureDefinitionLiveCommandAnswerBuilder.EventFactory> {

    /**
     * Factory for {@code CommandResponse}s to {@link ModifyFeatureDefinition} command.
     */
    interface ResponseFactory extends LiveCommandResponseFactory {

        /**
         * Builds a "created"  {@link ModifyFeatureDefinitionResponse} using the values of the {@code Command}.
         *
         * @return the response.
         */
        @Nonnull
        ModifyFeatureDefinitionResponse created();

        /**
         * Builds a "modified"  {@link ModifyFeatureDefinitionResponse} using the values of the {@code Command}.
         *
         * @return the response.
         */
        @Nonnull
        public ModifyFeatureDefinitionResponse modified();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the Feature Definition was not accessible.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureDefinitionNotAccessibleException
         * FeatureDefinitionNotAccessibleException
         */
        @Nonnull
        ThingErrorResponse featureDefinitionNotAccessibleError();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the Feature Definition was not modifiable.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureDefinitionNotModifiableException
         * FeatureDefinitionNotModifiableException
         */
        @Nonnull
        ThingErrorResponse featureDefinitionNotModifiableError();

    }

    /**
     * Factory for events triggered by {@link ModifyFeatureDefinition} command.
     */
    interface EventFactory extends LiveEventFactory {

        /**
         * Creates a {@link FeatureDefinitionCreated} event using the values of the {@code Command}.
         *
         * @return the event.
         */
        @Nonnull
        FeatureDefinitionCreated created();

        /**
         * Creates a {@link FeatureDefinitionModified} event using the values of the {@code Command}.
         *
         * @return the event.
         */
        @Nonnull
        FeatureDefinitionModified modified();

    }

}
