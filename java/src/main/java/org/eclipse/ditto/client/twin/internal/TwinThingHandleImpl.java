/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.twin.internal;

import javax.annotation.ParametersAreNonnullByDefault;

import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.management.internal.ThingHandleImpl;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.protocol.TopicPath;

/**
 * Default implementation for {@link TwinThingHandle}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
public final class TwinThingHandleImpl extends ThingHandleImpl<TwinThingHandle, TwinFeatureHandle> implements
        TwinThingHandle {

    /**
     * Creates a new {@link TwinThingHandleImpl} instance.
     *
     * @param thingId thing id
     * @param twinMessagingProvider twin messaging provider
     * @param outgoingMessageFactory outgoing message factory
     * @param handlerRegistry handler registry
     */
    TwinThingHandleImpl(final ThingId thingId,
            final MessagingProvider twinMessagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<TwinThingHandle, TwinFeatureHandle> handlerRegistry) {
        super(TopicPath.Channel.TWIN, thingId, twinMessagingProvider, outgoingMessageFactory,
                handlerRegistry);
    }

    @Override
    public TwinFeatureHandle createFeatureHandle(final ThingId thingId, final String featureId) {

        return new TwinFeatureHandleImpl(
                thingId,
                featureId,
                getMessagingProvider(),
                getOutgoingMessageFactory(),
                getHandlerRegistry());
    }

    @Override
    protected AcknowledgementLabel getThingResponseAcknowledgementLabel() {
        return DittoAcknowledgementLabel.TWIN_PERSISTED;
    }
}
