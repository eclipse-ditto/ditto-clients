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
package org.eclipse.ditto.client.live.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.bus.JsonPointerSelector;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.live.LiveCommandProcessor;
import org.eclipse.ditto.client.live.LiveFeatureHandle;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.live.commands.LiveCommandHandler;
import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.events.FeatureEventFactory;
import org.eclipse.ditto.client.live.events.internal.ImmutableFeatureEventFactory;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.PendingMessageWithFeatureId;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.client.management.internal.FeatureHandleImpl;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.events.base.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link LiveFeatureHandle}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
final class LiveFeatureHandleImpl extends FeatureHandleImpl<LiveThingHandle, LiveFeatureHandle>
        implements LiveFeatureHandle, LiveCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveFeatureHandleImpl.class);

    private final MessageSerializerRegistry messageSerializerRegistry;
    private final JsonSchemaVersion schemaVersion;
    private final Map<Class<? extends LiveCommand<?, ?>>, LiveCommandHandler<?, ?>> liveCommandHandlers;

    LiveFeatureHandleImpl(final ThingId thingId, final String featureId,
            final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<LiveThingHandle, LiveFeatureHandle> handlerRegistry,
            final MessageSerializerRegistry messageSerializerRegistry) {
        super(TopicPath.Channel.LIVE, thingId, featureId,
                messagingProvider,
                outgoingMessageFactory,
                handlerRegistry);

        this.messageSerializerRegistry = messageSerializerRegistry;
        this.schemaVersion = messagingProvider.getMessagingConfiguration().getJsonSchemaVersion();

        liveCommandHandlers = new ConcurrentHashMap<>();
    }

    /*
     * ###### Section
     * ###### "live" Messages
     */

    @Override
    public <T> PendingMessageWithFeatureId<T> message() {
        return PendingMessageImpl.<T>of(LOGGER, outgoingMessageFactory, messageSerializerRegistry, PROTOCOL_ADAPTER,
                messagingProvider).withThingAndFeatureIds(getEntityId(), getFeatureId());
    }

    @Override
    public <T, U> void registerForMessage(final String registrationId,
            final String subject,
            final Class<T> type,
            final Consumer<RepliableMessage<T, U>> handler) {

        argumentNotNull(subject);
        argumentNotNull(type);
        argumentNotNull(handler);

        LiveMessagesUtil.checkSerializerExistForMessageType(messageSerializerRegistry, type, subject);

        final JsonPointerSelector selector = "*".equals(subject) ?
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/'{direction}'/messages/'{subject}'",
                        getEntityId(), getFeatureId())
                : SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/'{direction}'/messages/{2}",
                getEntityId(), getFeatureId(), subject);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry,
                        type, handler));
    }

    @Override
    public <U> void registerForMessage(final String registrationId, final String subject,
            final Consumer<RepliableMessage<?, U>> handler) {

        argumentNotNull(subject, "registrationId");
        argumentNotNull(subject, "subject");
        argumentNotNull(handler, "handler");

        final JsonPointerSelector selector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/'{direction}'/messages/'{subject}'",
                getEntityId(), getFeatureId())
                : SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/'{direction}'/messages/{2}",
                getEntityId(), getFeatureId(), subject);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry,
                        handler));
    }


    /*
     * ###### Section
     * ###### Emit "live" Events
     */

    @Override
    public void emitEvent(final Function<FeatureEventFactory, Event<?>> eventFunction) {
        argumentNotNull(eventFunction);
        final FeatureEventFactory featureEventFactory =
                ImmutableFeatureEventFactory.getInstance(schemaVersion, getEntityId(), getFeatureId());
        final Event<?> eventToEmit = eventFunction.apply(featureEventFactory);
        getMessagingProvider().emit(signalToJsonString(adjustHeadersForLiveSignal(eventToEmit)));
    }

    @Override
    protected AcknowledgementLabel getThingResponseAcknowledgementLabel() {
        return DittoAcknowledgementLabel.LIVE_RESPONSE;
    }

    @Override
    public Map<Class<? extends LiveCommand<?, ?>>, LiveCommandHandler<?, ?>> getLiveCommandHandlers() {
        return liveCommandHandlers;
    }

    @Override
    public void publishLiveSignal(final Signal<?> signal) {
        getMessagingProvider().emitAdaptable(adaptOutgoingLiveSignal(signal));
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
