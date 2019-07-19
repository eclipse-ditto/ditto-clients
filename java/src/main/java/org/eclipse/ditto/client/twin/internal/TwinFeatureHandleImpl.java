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
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.management.internal.FeatureHandleImpl;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.protocoladapter.TopicPath;

/**
 * Default implementation for {@link TwinFeatureHandle}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
final class TwinFeatureHandleImpl extends FeatureHandleImpl<TwinThingHandle, TwinFeatureHandle>
        implements TwinFeatureHandle {

    /**
     * Creates a new {@link TwinFeatureHandleImpl} instance.
     *
     * @param thingId thing id
     * @param featureId feature id
     * @param twinMessagingProvider twin messaging provider
     * @param responseForwarder response forwarder
     * @param outgoingMessageFactory outgoing message factory
     * @param handlerRegistry handler registry
     */
    TwinFeatureHandleImpl(
            final String thingId,
            final String featureId,
            final MessagingProvider twinMessagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<TwinThingHandle, TwinFeatureHandle> handlerRegistry) {
        super(TopicPath.Channel.TWIN,
                thingId,
                featureId,
                twinMessagingProvider,
                responseForwarder,
                outgoingMessageFactory,
                handlerRegistry);
    }
}
