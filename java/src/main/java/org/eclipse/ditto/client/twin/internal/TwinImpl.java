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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.ParametersAreNonnullByDefault;

import org.eclipse.ditto.client.internal.CommonManagementImpl;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classifiers;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.TopicPath;

/**
 * Default implementation for {@link Twin}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
public final class TwinImpl extends CommonManagementImpl<TwinThingHandle, TwinFeatureHandle> implements Twin {

    private final AtomicReference<AdaptableBus.SubscriptionId> twinEventSubscription = new AtomicReference<>();

    private TwinImpl(final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        super(TopicPath.Channel.TWIN,
                messagingProvider,
                outgoingMessageFactory,
                new HandlerRegistry<>(bus),
                bus);
    }

    /**
     * Creates a new {@code TwinImpl} instance.
     *
     * @param messagingProvider implementation of underlying messaging provider.
     * @param outgoingMessageFactory a factory for messages.
     * @param bus the bus for message routing.
     * @return the new {@code TwinImpl} instance.
     */
    public static TwinImpl newInstance(final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        return new TwinImpl(messagingProvider, outgoingMessageFactory, bus);
    }

    @Override
    protected TwinThingHandle createThingHandle(final ThingId thingId) {

        return new TwinThingHandleImpl(
                thingId,
                getMessagingProvider(),
                getOutgoingMessageFactory(),
                getHandlerRegistry());
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
    protected CompletableFuture<Void> doStartConsumption(final Map<String, String> consumptionConfig) {
        final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
        final Classifiers.StreamingType streamingType = Classifiers.StreamingType.TWIN_EVENT;
        final String subscriptionMessage = buildProtocolCommand(streamingType.start(), consumptionConfig);
        messagingProvider.registerSubscriptionMessage(streamingType, subscriptionMessage);
        twinEventSubscription.getAndUpdate(previousSubscriptionId -> subscribe(
                previousSubscriptionId,
                streamingType,
                subscriptionMessage,
                streamingType.startAck(),
                ackFuture,
                CommonManagementImpl::asThingMessage
        ));
        return ackFuture;
    }

    @Override
    public CompletableFuture<Void> suspendConsumption() {
        final Classifiers.StreamingType streamingType = Classifiers.StreamingType.TWIN_EVENT;
        messagingProvider.unregisterSubscriptionMessage(streamingType);
        final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
        twinEventSubscription.getAndUpdate(subscriptionId -> {
            unsubscribe(subscriptionId, streamingType.stop(), streamingType.stopAck(), ackFuture);
            return null;
        });
        return ackFuture;
    }
}
