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
package org.eclipse.ditto.client.live.commands.modify;

import javax.annotation.Nonnull;

import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.client.live.commands.base.LiveCommandResponseFactory;
import org.eclipse.ditto.client.live.commands.base.LiveEventFactory;
import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDesiredProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDesiredPropertyResponse;
import org.eclipse.ditto.things.model.signals.events.FeatureDesiredPropertyDeleted;

/**
 * LiveCommandAnswer builder for producing {@code CommandResponse}s and {@code Event}s for
 * {@link DeleteFeatureDesiredProperty} commands.
 *
 * @since 2.0.0
 */
public interface DeleteFeatureDesiredPropertyLiveCommandAnswerBuilder
        extends
        LiveCommandAnswerBuilder.ModifyCommandResponseStep<DeleteFeatureDesiredPropertyLiveCommandAnswerBuilder.ResponseFactory,
                DeleteFeatureDesiredPropertyLiveCommandAnswerBuilder.EventFactory> {

    /**
     * Factory for {@code CommandResponse}s to {@link DeleteFeatureDesiredProperty} command.
     */
    interface ResponseFactory extends LiveCommandResponseFactory {

        /**
         * Builds a {@link DeleteFeatureDesiredPropertyResponse} using the values of the {@code Command}.
         *
         * @return the response.
         */
        @Nonnull
        DeleteFeatureDesiredPropertyResponse deleted();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the feature's desired property was not accessible.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureDesiredPropertyNotAccessibleException
         * FeatureDesiredPropertyNotAccessibleException
         */
        @Nonnull
        ThingErrorResponse featureDesiredPropertyNotAccessibleError();

        /**
         * Builds a {@link ThingErrorResponse} indicating that the feature's desired property was not modifiable.
         *
         * @return the response.
         * @see org.eclipse.ditto.things.model.signals.commands.exceptions.FeatureDesiredPropertyNotModifiableException
         * FeatureDesiredPropertyNotModifiableException
         */
        @Nonnull
        ThingErrorResponse featureDesiredPropertyNotModifiableError();
    }

    /**
     * Factory for events triggered by {@link DeleteFeatureDesiredProperty} command.
     */
    @SuppressWarnings("squid:S1609")
    interface EventFactory extends LiveEventFactory {

        /**
         * Creates a {@link FeatureDesiredPropertyDeleted} event using the values of the {@code Command}.
         *
         * @return the event.
         */
        @Nonnull
        FeatureDesiredPropertyDeleted deleted();
    }

}
