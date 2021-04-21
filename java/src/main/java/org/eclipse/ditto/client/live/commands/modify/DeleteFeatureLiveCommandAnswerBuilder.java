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
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureResponse;
import org.eclipse.ditto.things.model.signals.events.FeatureDeleted;

/**
 * LiveCommandAnswer builder for producing {@code CommandResponse}s and {@code Event}s for {@link DeleteFeature}
 * commands.
 *
 * @since 2.0.0
 */
public interface DeleteFeatureLiveCommandAnswerBuilder extends LiveCommandAnswerBuilder.ModifyCommandResponseStep<
        DeleteFeatureLiveCommandAnswerBuilder.ResponseFactory, DeleteFeatureLiveCommandAnswerBuilder.EventFactory> {

    /**
     * Factory for {@code CommandResponse}s to {@link DeleteFeature} command.
     */
    interface ResponseFactory extends LiveCommandResponseFactory {

        /**
         * Builds a {@link DeleteFeatureResponse} using the values of the {@code Command}.
         *
         * @return the response.
         */
        @Nonnull
        DeleteFeatureResponse deleted();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the feature was not accessible.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureNotAccessibleException
         * FeatureNotAccessibleException
         */
        @Nonnull
        ThingErrorResponse featureNotAccessibleError();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the feature was not modifiable.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureNotModifiableException
         * FeatureNotModifiableException
         */
        @Nonnull
        ThingErrorResponse featureNotModifiableError();
    }

    /**
     * Factory for events triggered by {@link DeleteFeature} command.
     */
    @SuppressWarnings("squid:S1609")
    interface EventFactory extends LiveEventFactory {

        /**
         * Creates a {@link FeatureDeleted} event using the values of the {@code Command}.
         *
         * @return the event.
         */
        @Nonnull
        FeatureDeleted deleted();
    }

}
