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

import static org.eclipse.ditto.base.model.common.ConditionChecker.argumentNotNull;

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
import org.eclipse.ditto.client.live.events.ThingEventFactory;
import org.eclipse.ditto.client.live.events.internal.ImmutableThingEventFactory;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.PendingMessageWithThingId;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.client.management.internal.ThingHandleImpl;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.base.model.acks.AcknowledgementLabel;
import org.eclipse.ditto.base.model.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.messages.model.KnownMessageSubjects;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.base.model.signals.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link LiveThingHandle}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
public final class LiveThingHandleImpl extends ThingHandleImpl<LiveThingHandle, LiveFeatureHandle>
        implements LiveThingHandle, LiveCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveThingHandleImpl.class);

    private final MessageSerializerRegistry messageSerializerRegistry;
    private final JsonSchemaVersion schemaVersion;
    private final Map<Class<? extends LiveCommand<?, ?>>, LiveCommandHandler<?, ?>> liveCommandHandlers;

    LiveThingHandleImpl(final ThingId thingId,
            final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<LiveThingHandle, LiveFeatureHandle> handlerRegistry,
            final MessageSerializerRegistry messageSerializerRegistry) {
        super(TopicPath.Channel.LIVE,
                thingId,
                messagingProvider,
                outgoingMessageFactory,
                handlerRegistry);

        this.messageSerializerRegistry = messageSerializerRegistry;
        this.schemaVersion = messagingProvider.getMessagingConfiguration().getJsonSchemaVersion();

        liveCommandHandlers = new ConcurrentHashMap<>();
    }

    @Override
    protected LiveFeatureHandleImpl createFeatureHandle(final ThingId thingId, final String featureId) {
        return new LiveFeatureHandleImpl(thingId, featureId, getMessagingProvider(),
                getOutgoingMessageFactory(), getHandlerRegistry(), messageSerializerRegistry);
    }

    /*
     * ###### Section
     * ###### "live" Messages
     */

    @Override
    public <T> PendingMessageWithThingId<T> message() {
        return PendingMessageImpl.<T>of(LOGGER, outgoingMessageFactory, messageSerializerRegistry, PROTOCOL_ADAPTER,
                messagingProvider).withThingId(getEntityId());
    }

    @Override
    public <T, U> void registerForMessage(final String registrationId,
            final String subject,
            final Class<T> type,
            final Consumer<RepliableMessage<T, U>> handler) {

        argumentNotNull(registrationId, "registrationId");
        argumentNotNull(subject, "subject");
        argumentNotNull(type, "type");
        argumentNotNull(handler, "handler");

        LiveMessagesUtil.checkSerializerExistForMessageType(messageSerializerRegistry, type, subject);

        // selector for thing messages:
        final JsonPointerSelector thingSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/'{subject}'",
                getEntityId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/{1}", getEntityId(),
                        subject);
        // selector for feature messages:
        final JsonPointerSelector featureSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER,
                "/things/{0}/features/'{featureId}'/'{direction}'/messages/'{subject}'", getEntityId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/'{featureId}'/'{direction}'/messages/{0}",
                        getEntityId(), subject);

        getHandlerRegistry().register(registrationId, SelectorUtil.or(thingSelector, featureSelector),
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry,
                        type, handler));
    }

    @Override
    public <U> void registerForMessage(final String registrationId, final String subject,
            final Consumer<RepliableMessage<?, U>> handler) {
        argumentNotNull(registrationId, "registrationId");
        argumentNotNull(subject, "subject");
        argumentNotNull(handler, "handler");

        // selector for thing messages:
        final JsonPointerSelector thingSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/'{subject}'",
                getEntityId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/{1}", getEntityId(),
                        subject);
        // selector for feature messages:
        final JsonPointerSelector featureSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER,
                "/things/{0}/features/'{featureId}'/'{direction}'/messages/'{subject}'",
                getEntityId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/'{featureId}'/'{direction}'/messages/{1}",
                        getEntityId(), subject);

        getHandlerRegistry().register(registrationId, SelectorUtil.or(thingSelector, featureSelector),
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry,
                        handler));
    }

    @Override
    public <T, U> void registerForClaimMessage(final String registrationId, final Class<T> type,
            final Consumer<RepliableMessage<T, U>> handler) {

        argumentNotNull(registrationId, "registrationId");
        argumentNotNull(type, "type");
        argumentNotNull(handler, "handler");

        LiveMessagesUtil.checkSerializerExistForMessageType(messageSerializerRegistry, type);

        final JsonPointerSelector selector =
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/{1}", getEntityId(),
                        KnownMessageSubjects.CLAIM_SUBJECT);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry,
                        type, handler));
    }

    @Override
    public <U> void registerForClaimMessage(final String registrationId,
            final Consumer<RepliableMessage<?, U>> handler) {
        argumentNotNull(registrationId, "registrationId");
        argumentNotNull(handler, "handler");

        final JsonPointerSelector selector =
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/inbox/messages/{1}", getEntityId(),
                        KnownMessageSubjects.CLAIM_SUBJECT);

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
    public void emitEvent(final Function<ThingEventFactory, Event<?>> eventFunction) {
        argumentNotNull(eventFunction);
        final ThingEventFactory thingEventFactory =
                ImmutableThingEventFactory.getInstance(schemaVersion, getEntityId());
        final Event<?> eventToEmit = eventFunction.apply(thingEventFactory);
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
