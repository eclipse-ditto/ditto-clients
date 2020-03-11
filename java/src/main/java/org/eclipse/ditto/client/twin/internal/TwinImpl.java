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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.ParametersAreNonnullByDefault;

import org.eclipse.ditto.client.internal.CommonManagementImpl;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.ResponseForwarder;
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

    /**
     * Handler name for consuming twin events.
     * TODO: delete if unused
     */
    public static final String CONSUME_TWIN_EVENTS_HANDLER = "consume-twin-events";

    // TODO: configure timeout.
    private static final Duration TIMEOUT = Duration.ofSeconds(60L);

    private final AtomicReference<AdaptableBus.SubscriptionId> twinEventSubscription = new AtomicReference<>();

    private TwinImpl(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        super(TopicPath.Channel.TWIN,
                messagingProvider,
                responseForwarder,
                outgoingMessageFactory,
                new HandlerRegistry<>(bus),
                bus);
        addTwinClassifiers(messagingProvider.getAdaptableBus());
    }

    /**
     * Creates a new {@code TwinImpl} instance.
     *
     * @param messagingProvider implementation of underlying messaging provider.
     * @param responseForwarder fast cache of response addresses.
     * @param outgoingMessageFactory a factory for messages.
     * @param bus the bus for message routing.
     * @return the new {@code TwinImpl} instance.
     */
    public static TwinImpl newInstance(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus) {
        return new TwinImpl(messagingProvider, responseForwarder, outgoingMessageFactory, bus);
    }

    @Override
    protected TwinThingHandle createThingHandle(final ThingId thingId) {

        return new TwinThingHandleImpl(
                thingId,
                getMessagingProvider(),
                getResponseForwarder(),
                getOutgoingMessageFactory(),
                getHandlerRegistry());
    }

    @Override
    public TwinFeatureHandle createFeatureHandle(final ThingId thingId, final String featureId) {

        return new TwinFeatureHandleImpl(
                thingId,
                featureId,
                getMessagingProvider(),
                getResponseForwarder(),
                getOutgoingMessageFactory(),
                getHandlerRegistry());
    }

    @Override
    protected CompletableFuture<Void> doStartConsumption(final Map<String, String> consumptionConfig) {
        // TODO: log START-SEND-EVENTS
        // TODO: test broken pipe
        final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
        twinEventSubscription.getAndUpdate(previousSubscriptionId -> subscribe(
                previousSubscriptionId,
                Classifiers.StreamingType.TWIN_EVENT,
                buildProtocolCommand(Classifiers.StreamingType.TWIN_EVENT.start(), consumptionConfig),
                Classifiers.StreamingType.TWIN_EVENT.startAck(),
                ackFuture,
                CommonManagementImpl::asThingMessage
        ));
        return ackFuture;
    }

    @Override
    public CompletableFuture<Void> suspendConsumption() {
        // TODO: log STOP-SEND-EVENTS
        final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
        twinEventSubscription.getAndUpdate(subscriptionId -> {
            unsubscribe(subscriptionId, Classifiers.StreamingType.TWIN_EVENT.stop(),
                    Classifiers.StreamingType.TWIN_EVENT.stopAck(), ackFuture);
            return null;
        });
        return ackFuture;
    }

    private static void addTwinClassifiers(final AdaptableBus adaptableBus) {
        // TODO: ensure classifiers are not duplicate
        // TODO: move streaming type classifier to BusFactory.
        adaptableBus.addAdaptableClassifier(Classifiers.correlationId())
                .addAdaptableClassifier(Classifiers.streamingType());
    }

}
