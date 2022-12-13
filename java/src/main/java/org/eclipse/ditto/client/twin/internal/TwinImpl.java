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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.ParametersAreNonnullByDefault;

import org.eclipse.ditto.client.internal.CommonManagementImpl;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.management.ClientReconnectingException;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.client.twin.TwinSearchHandle;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.protocol.TopicPath;

/**
 * Default implementation for {@link Twin}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
public final class TwinImpl extends CommonManagementImpl<TwinThingHandle, TwinFeatureHandle> implements Twin {

    private final AtomicReference<AdaptableBus.SubscriptionId> twinEventSubscription = new AtomicReference<>();
    private final TwinSearchHandle search;

    private TwinImpl(final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        super(TopicPath.Channel.TWIN,
                messagingProvider,
                outgoingMessageFactory,
                new HandlerRegistry<>(bus),
                bus);
        search = new TwinSearchHandleImpl(messagingProvider);
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
    protected CompletionStage<Void> doStartConsumption(final Map<String, String> consumptionConfig) {
        try {
            final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
            final Classification.StreamingType streamingType = Classification.StreamingType.TWIN_EVENT;
            final String subscriptionMessage = buildProtocolCommand(streamingType.start(), consumptionConfig);
            messagingProvider.registerSubscriptionMessage(streamingType, subscriptionMessage);
            synchronized (twinEventSubscription) {
                final AdaptableBus.SubscriptionId previousSubscriptionId = twinEventSubscription.get();
                twinEventSubscription.set(subscribe(
                        previousSubscriptionId,
                        streamingType,
                        subscriptionMessage,
                        streamingType.startAck(),
                        ackFuture
                ));
            }
            return ackFuture;
        } catch (final ClientReconnectingException cre) {
            return CompletableFuture.supplyAsync(() -> {
                throw cre;
            });
        }
    }

    @Override
    public CompletionStage<Void> suspendConsumption() {
        try {
            final Classification.StreamingType streamingType = Classification.StreamingType.TWIN_EVENT;
            messagingProvider.unregisterSubscriptionMessage(streamingType);
            final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
            synchronized (twinEventSubscription) {
                unsubscribe(twinEventSubscription.get(), streamingType.stop(), streamingType.stopAck(), ackFuture);
                twinEventSubscription.set(null);
            }
            return ackFuture;
        } catch (final ClientReconnectingException cre) {
            return CompletableFuture.supplyAsync(() -> {
                throw cre;
            });
        }
    }

    @Override
    public TwinSearchHandle search() {
        return search;
    }

    @Override
    protected AcknowledgementLabel getThingResponseAcknowledgementLabel() {
        return DittoAcknowledgementLabel.TWIN_PERSISTED;
    }
}
