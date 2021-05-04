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
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeaturePropertiesResponse;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesModified;

/**
 * LiveCommandAnswer builder for producing {@code CommandResponse}s and {@code Event}s for
 * {@link ModifyFeatureProperties} commands.
 *
 * @since 2.0.0
 */
public interface ModifyFeaturePropertiesLiveCommandAnswerBuilder
        extends LiveCommandAnswerBuilder.ModifyCommandResponseStep<
        ModifyFeaturePropertiesLiveCommandAnswerBuilder.ResponseFactory,
        ModifyFeaturePropertiesLiveCommandAnswerBuilder.EventFactory> {

    /**
     * Factory for {@code CommandResponse}s to {@link ModifyFeatureProperties} command.
     */
    interface ResponseFactory extends LiveCommandResponseFactory {

        /**
         * Builds a "created"  {@link ModifyFeaturePropertiesResponse} using the values of the {@code Command}.
         *
         * @return the response.
         */
        @Nonnull
        ModifyFeaturePropertiesResponse created();

        /**
         * Builds a "modified"  {@link ModifyFeaturePropertiesResponse} using the values of the {@code Command}.
         *
         * @return the response.
         */
        @Nonnull
        public ModifyFeaturePropertiesResponse modified();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the feature properties were not accessible.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeaturePropertiesNotAccessibleException
         * FeaturePropertiesNotAccessibleException
         */
        @Nonnull
        ThingErrorResponse featurePropertiesNotAccessibleError();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the feature properties were not modifiable.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeaturePropertiesNotModifiableException
         * FeaturePropertiesNotModifiableException
         */
        @Nonnull
        ThingErrorResponse featurePropertiesNotModifiableError();
    }

    /**
     * Factory for events triggered by {@link ModifyFeatureProperties} command.
     */
    interface EventFactory extends LiveEventFactory {

        /**
         * Creates a {@link FeaturePropertiesCreated} event using the values of the {@code Command}.
         *
         * @return the event.
         */
        @Nonnull
        FeaturePropertiesCreated created();

        /**
         * Creates a {@link FeaturePropertiesModified} event using the values of the {@code Command}.
         *
         * @return the event.
         */
        @Nonnull
        FeaturePropertiesModified modified();
    }

}
