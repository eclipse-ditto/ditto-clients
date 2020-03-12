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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.eclipse.ditto.client.internal.CommonManagementImpl;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classifiers;
import org.eclipse.ditto.client.internal.bus.JsonPointerSelector;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.live.Live;
import org.eclipse.ditto.client.live.LiveCommandProcessor;
import org.eclipse.ditto.client.live.LiveFeatureHandle;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.live.events.GlobalEventFactory;
import org.eclipse.ditto.client.live.events.internal.ImmutableGlobalEventFactory;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.PendingMessage;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.KnownMessageSubjects;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.base.WithFeatureId;
import org.eclipse.ditto.signals.commands.live.LiveCommandFactory;
import org.eclipse.ditto.signals.commands.live.base.LiveCommand;
import org.eclipse.ditto.signals.commands.live.base.LiveCommandAnswer;
import org.eclipse.ditto.signals.commands.live.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.signals.commands.live.modify.CreateThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteAttributeLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteAttributesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyAttributeLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyAttributesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveAttributeLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveAttributesLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveThingLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveThingsLiveCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.events.base.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link Live}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
public final class LiveImpl extends CommonManagementImpl<LiveThingHandle, LiveFeatureHandle>
        implements Live, LiveCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveImpl.class);

    /**
     * Handler name for consuming live events.
     */
    public static final String CONSUME_LIVE_EVENTS_HANDLER = "consume-live-events";

    /**
     * Handler name for consuming live messages.
     */
    public static final String CONSUME_LIVE_MESSAGES_HANDLER = "consume-live-messages";

    /**
     * Handler name for consuming live commands.
     */
    public static final String CONSUME_LIVE_COMMANDS_HANDLER = "consume-live-commands";

    private final JsonSchemaVersion schemaVersion;
    private final MessageSerializerRegistry messageSerializerRegistry;
    private final Map<Class<? extends LiveCommand>, Function<? extends LiveCommand, LiveCommandAnswerBuilder.BuildStep>>
            liveCommandsFunctions;
    private final Map<Classifiers.StreamingType, AdaptableBus.SubscriptionId> subscriptionIds;

    private LiveImpl(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus,
            final JsonSchemaVersion schemaVersion,
            final MessageSerializerRegistry messageSerializerRegistry) {
        super(TopicPath.Channel.LIVE,
                messagingProvider,
                responseForwarder,
                outgoingMessageFactory,
                new HandlerRegistry<>(bus),
                bus);

        this.schemaVersion = schemaVersion;
        this.messageSerializerRegistry = messageSerializerRegistry;
        liveCommandsFunctions = new ConcurrentHashMap<>();
        subscriptionIds = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new {@code LiveImpl} instance.
     *
     * @param messagingProvider implementation of underlying messaging provider.
     * @param responseForwarder fast cache of response addresses.
     * @param outgoingMessageFactory a factory for messages.
     * @param bus the bus for message routing.
     * @param schemaVersion the json schema version of the messaging protocol.
     * @param messageSerializerRegistry the registry to serialize and de-serialize messages.
     * @return the new {@code LiveImpl} instance.
     */
    public static LiveImpl newInstance(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final PointerBus bus,
            final JsonSchemaVersion schemaVersion,
            final MessageSerializerRegistry messageSerializerRegistry) {
        return new LiveImpl(messagingProvider, responseForwarder, outgoingMessageFactory, bus, schemaVersion,
                messageSerializerRegistry);
    }

    @Override
    protected LiveThingHandleImpl createThingHandle(final ThingId thingId) {
        return new LiveThingHandleImpl(thingId, getMessagingProvider(), getResponseForwarder(),
                getOutgoingMessageFactory(), getHandlerRegistry(), messageSerializerRegistry);
    }

    @Override
    protected LiveFeatureHandleImpl createFeatureHandle(final ThingId thingId, final String featureId) {
        return new LiveFeatureHandleImpl(thingId, featureId, getMessagingProvider(), getResponseForwarder(),
                getOutgoingMessageFactory(), getHandlerRegistry(), messageSerializerRegistry);
    }

    @Override
    protected CompletableFuture<Void> doStartConsumption(final Map<String, String> consumptionConfig) {
        final CompletableFuture<Void> completableFutureEvents = new CompletableFuture<>();
        final CompletableFuture<Void> completableFutureMessages = new CompletableFuture<>();
        final CompletableFuture<Void> completableFutureLiveCommands = new CompletableFuture<>();
        final CompletableFuture<Void> completableFutureCombined =
                CompletableFuture.allOf(completableFutureEvents, completableFutureMessages,
                        completableFutureLiveCommands);

        // register message handler which handles live events:
        subscriptionIds.compute(Classifiers.StreamingType.LIVE_EVENT, (streamingType, previousSubscriptionId) ->
                subscribe(previousSubscriptionId,
                        streamingType,
                        buildProtocolCommand(Classifiers.StreamingType.LIVE_EVENT.start(), consumptionConfig),
                        Classifiers.StreamingType.LIVE_EVENT.startAck(),
                        completableFutureEvents,
                        CommonManagementImpl::asThingMessage)
        );

        // register message handler which handles incoming messages:
        subscriptionIds.compute(Classifiers.StreamingType.LIVE_MESSAGE, (streamingType, previousSubscriptionId) ->
                subscribeAndPublishMessage(previousSubscriptionId,
                        streamingType,
                        buildProtocolCommand(Classifiers.StreamingType.LIVE_MESSAGE.start(), consumptionConfig),
                        Classifiers.StreamingType.LIVE_MESSAGE.startAck(),
                        completableFutureMessages,
                        adaptable -> bus -> {
                            final Message<?> message = adaptableAsLiveMessage(adaptable);
                            // TODO: instead of calculateMessagePath, use the path in adaptable.
                            // ignore messages with null message path on purpose.
                            final CharSequence messagePath = calculateMessagePath(message);
                            if (messagePath != null) {
                                bus.notify(messagePath, message);
                            }
                        })
        );

        // register message handler which handles live commands:
        subscriptionIds.compute(Classifiers.StreamingType.LIVE_COMMAND, (streamingType, previousSubscriptionId) ->
                subscribeAndPublishMessage(previousSubscriptionId,
                        streamingType,
                        buildProtocolCommand(Classifiers.StreamingType.LIVE_COMMAND.start(), consumptionConfig),
                        Classifiers.StreamingType.LIVE_COMMAND.startAck(),
                        completableFutureLiveCommands,
                        adaptable -> bus -> bus.getExecutor().submit(() -> handleLiveCommandOrResponse(adaptable))
                )
        );
        return completableFutureCombined;
    }

    @Override
    public CompletableFuture<Void> suspendConsumption() {
        return CompletableFuture.allOf(
                subscriptionIds.entrySet()
                        .stream()
                        .map(entry -> {
                            final CompletableFuture<Void> future = new CompletableFuture<>();
                            unsubscribe(entry.getValue(), entry.getKey().stop(), entry.getKey().stopAck(), future);
                            return future;
                        })
                        .toArray(CompletableFuture[]::new)
        );
    }

    @Nullable
    private static String calculateMessagePath(final Message<?> message) {
        final ThingId thingId = message.getThingEntityId();
        final String subject = message.getSubject();

        final MessageDirection direction;
        try {
            direction = message.getDirection();
        } catch (final IllegalStateException e) {
            LOGGER.info("Received message with missing direction - ignoring: <{}>", message);
            return null;
        }
        // Direction is set to "TO" -> Message is put in the "inbox"
        // Direction is set to "FROM" -> Message is put in the "outbox"
        final String inboxOutbox = direction == MessageDirection.TO ? "inbox" : "outbox";

        final String messagePath;
        if (KnownMessageSubjects.CLAIM_SUBJECT.equals(subject)) {
            messagePath =
                    MessageFormat.format("/things/{0}/inbox/messages/{1}", thingId, KnownMessageSubjects.CLAIM_SUBJECT);
        } else {
            // Feature/Thing Message
            messagePath = message.getFeatureId()
                    .map(featureId -> MessageFormat.format("/things/{0}/features/{1}/{2}/messages/{3}", thingId,
                            featureId, inboxOutbox, subject))
                    .orElseGet(
                            () -> MessageFormat.format("/things/{0}/{1}/messages/{2}", thingId, inboxOutbox, subject));
        }
        return messagePath;
    }

    /*
     * ###### Section
     * ###### "live" Messages
     */

    @Override
    public <T> PendingMessage<T> message() {
        return PendingMessageImpl.of(LOGGER, outgoingMessageFactory, messageSerializerRegistry, PROTOCOL_ADAPTER,
                messagingProvider, channel);
    }

    @Override
    public <T, U> void registerForMessage(final String registrationId,
            final String subject,
            final Class<T> type,
            final Consumer<RepliableMessage<T, U>> handler) {

        checkRegistrationId(registrationId);
        checkSubject(subject);
        argumentNotNull(type, "type");
        checkHandler(handler);

        LiveMessagesUtil.checkSerializerExistForMessageType(messageSerializerRegistry, type, subject);

        // selector for thing messages:
        final JsonPointerSelector thingSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/'{direction}'/messages/'{subject}'")
                : SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/'{direction}'/messages/{0}", subject);
        // selector for feature messages:
        final JsonPointerSelector featureSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER,
                "/things/'{thingId}'/features/'{featureId}'/'{direction}'/messages/'{subject}'")
                : SelectorUtil.formatJsonPointer(LOGGER,
                "/things/'{thingId}'/features/'{featureId}'/'{direction}'/messages/{0}",
                subject);

        getHandlerRegistry().register(registrationId, SelectorUtil.or(thingSelector, featureSelector),
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry, type, handler));
    }

    private static void checkRegistrationId(final String registrationId) {
        argumentNotNull(registrationId, "registrationId");
    }

    private static void checkSubject(final String subject) {
        argumentNotNull(subject, "subject");
    }

    private static void checkHandler(final Consumer<?> handler) {
        argumentNotNull(handler, "handler");
    }

    @Override
    public <U> void registerForMessage(final String registrationId, final String subject,
            final Consumer<RepliableMessage<?, U>> handler) {

        checkRegistrationId(registrationId);
        checkSubject(subject);
        checkHandler(handler);

        // selector for thing messages:
        final JsonPointerSelector thingSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/'{direction}'/messages/'{subject}'")
                : SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/'{direction}'/messages/{0}", subject);
        // selector for feature messages:
        final JsonPointerSelector featureSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER,
                "/things/'{thingId}'/features/'{featureId}'/'{direction}'/messages/'{subject}'")
                : SelectorUtil.formatJsonPointer(LOGGER,
                "/things/'{thingId}'/features/'{featureId}'/'{direction}'/messages/{0}",
                subject);

        getHandlerRegistry().register(registrationId, SelectorUtil.or(thingSelector, featureSelector),
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry, handler));
    }

    @Override
    public <T, U> void registerForClaimMessage(final String registrationId, final Class<T> type,
            final Consumer<RepliableMessage<T, U>> handler) {

        checkRegistrationId(registrationId);
        argumentNotNull(type, "type");
        checkHandler(handler);

        LiveMessagesUtil.checkSerializerExistForMessageType(messageSerializerRegistry, type);

        final JsonPointerSelector selector =
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/inbox/messages/{0}",
                        KnownMessageSubjects.CLAIM_SUBJECT);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry,
                        type, handler));
    }

    @Override
    public <U> void registerForClaimMessage(final String registrationId,
            final Consumer<RepliableMessage<?, U>> handler) {

        checkRegistrationId(registrationId);
        checkHandler(handler);

        final JsonPointerSelector selector =
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/inbox/messages/{0}",
                        KnownMessageSubjects.CLAIM_SUBJECT);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(PROTOCOL_ADAPTER, getMessagingProvider(),
                        getOutgoingMessageFactory(), messageSerializerRegistry, handler));
    }

    /*
     * ###### Section
     * ###### Emit "live" Events
     */

    @Override
    public void emitEvent(final Function<GlobalEventFactory, Event<?>> eventFunction) {
        argumentNotNull(eventFunction);
        final GlobalEventFactory globalEventFactory = ImmutableGlobalEventFactory.getInstance(schemaVersion);
        final Event<?> eventToEmit = eventFunction.apply(globalEventFactory);
        getMessagingProvider().emit(signalToJsonString(adjustHeadersForLiveSignal(eventToEmit)));
    }

    /*
     * ###### Section
     * ###### Handle "live" Commands
     */

    @Override
    public void handleRetrieveThingsCommands(
            final Function<RetrieveThingsLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveThingsLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveThingsCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveThingsLiveCommand.class);
    }

    @Override
    public void handleCreateThingCommands(
            final Function<CreateThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(CreateThingLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingCreateThingCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(CreateThingLiveCommand.class);
    }

    @Override
    public void handleModifyThingCommands(
            final Function<ModifyThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(ModifyThingLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingModifyThingCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(ModifyThingLiveCommand.class);
    }

    @Override
    public void handleDeleteThingCommands(
            final Function<DeleteThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(DeleteThingLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingDeleteThingCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(DeleteThingLiveCommand.class);
    }

    @Override
    public void handleRetrieveThingCommandsFunction(
            final Function<RetrieveThingLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveThingLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveThingCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveThingLiveCommand.class);
    }

    @Override
    public void handleModifyAttributesCommands(
            final Function<ModifyAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(ModifyAttributesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingModifyAttributesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(ModifyAttributesLiveCommand.class);
    }

    @Override
    public void handleDeleteAttributesCommands(
            final Function<DeleteAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(DeleteAttributesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingDeleteAttributesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(DeleteAttributesLiveCommand.class);
    }

    @Override
    public void handleModifyAttributeCommands(
            final Function<ModifyAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(ModifyAttributeLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingModifyAttributeCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(ModifyAttributeLiveCommand.class);
    }

    @Override
    public void handleDeleteAttributeCommands(
            final Function<DeleteAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {

        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(DeleteAttributeLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingDeleteAttributeCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(DeleteAttributeLiveCommand.class);
    }

    @Override
    public void handleRetrieveAttributesCommands(
            final Function<RetrieveAttributesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {

        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveAttributesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveAttributesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveAttributesLiveCommand.class);
    }

    @Override
    public void handleRetrieveAttributeCommand(
            final Function<RetrieveAttributeLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {

        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveAttributeLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveAttributeCommand() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveAttributeLiveCommand.class);
    }

    @Override
    public void handleModifyFeaturesCommands(
            final Function<ModifyFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(ModifyFeaturesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingModifyFeaturesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(ModifyFeaturesLiveCommand.class);
    }

    @Override
    public void handleDeleteFeaturesCommands(
            final Function<DeleteFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(DeleteFeaturesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingDeleteFeaturesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(DeleteFeaturesLiveCommand.class);
    }

    @Override
    public void handleModifyFeatureCommands(
            final Function<ModifyFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(ModifyFeatureLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingModifyFeatureCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(ModifyFeatureLiveCommand.class);
    }

    @Override
    public void handleDeleteFeatureCommands(
            final Function<DeleteFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(DeleteFeatureLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingDeleteFeatureCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(DeleteFeatureLiveCommand.class);
    }

    @Override
    public void handleModifyFeaturePropertiesCommands(
            final Function<ModifyFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(ModifyFeaturePropertiesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingModifyFeaturePropertiesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(ModifyFeaturePropertiesLiveCommand.class);
    }

    @Override
    public void handleDeleteFeaturePropertiesCommands(
            final Function<DeleteFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(DeleteFeaturePropertiesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingDeleteFeaturePropertiesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(DeleteFeaturePropertiesLiveCommand.class);
    }

    @Override
    public void handleModifyFeaturePropertyCommands(
            final Function<ModifyFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(ModifyFeaturePropertyLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingModifyFeaturePropertyCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(ModifyFeaturePropertyLiveCommand.class);
    }

    @Override
    public void handleDeleteFeaturePropertyCommands(
            final Function<DeleteFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(DeleteFeaturePropertyLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingDeleteFeaturePropertyCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(DeleteFeaturePropertyLiveCommand.class);
    }

    @Override
    public void handleRetrieveFeaturesCommands(
            final Function<RetrieveFeaturesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveFeaturesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveFeaturesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveFeaturesLiveCommand.class);
    }

    @Override
    public void handleRetrieveFeatureCommands(
            final Function<RetrieveFeatureLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveFeatureLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveFeatureCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveFeatureLiveCommand.class);
    }

    @Override
    public void handleRetrieveFeaturePropertyCommands(
            final Function<RetrieveFeaturePropertyLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveFeaturePropertyLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveFeaturePropertyCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveFeaturePropertyLiveCommand.class);
    }

    @Override
    public void handleRetrieveFeaturePropertiesCommands(
            final Function<RetrieveFeaturePropertiesLiveCommand, LiveCommandAnswerBuilder.BuildStep> handler) {
        argumentNotNull(handler);
        registerLiveCommandToAnswerBuilderFunction(RetrieveFeaturePropertiesLiveCommand.class, handler);
    }

    @Override
    public void stopHandlingRetrieveFeaturePropertiesCommands() {
        unregisterLiveCommandToAnswerBuilderFunction(RetrieveFeaturePropertiesLiveCommand.class);
    }

    private void registerLiveCommandToAnswerBuilderFunction(final Class<? extends LiveCommand> liveCommandClass,
            final Function<? extends LiveCommand, LiveCommandAnswerBuilder.BuildStep> function) {
        liveCommandsFunctions.compute(liveCommandClass, (key, value) -> {
            if (value != null) {
                throw new IllegalStateException(
                        "A Function for '" + liveCommandClass.getSimpleName() + "' is already " +
                                "defined. Stop the registered handler before registering a new handler.");
            } else {
                return function;
            }
        });
    }

    private void unregisterLiveCommandToAnswerBuilderFunction(final Class<? extends LiveCommand> liveCommandClass) {
        liveCommandsFunctions.remove(liveCommandClass);
    }

    @Override
    public boolean processLiveCommand(final LiveCommand liveCommand) {
        return Arrays.stream(liveCommand.getClass().getInterfaces())
                .map(liveCommandsFunctions::get)
                .filter(Objects::nonNull)
                .map(function -> (Function<LiveCommand, LiveCommandAnswerBuilder.BuildStep>) function)
                .map(function -> {
                    try {
                        final LiveCommandAnswerBuilder.BuildStep builder = function.apply(liveCommand);
                        processLiveCommandAnswer(builder.build());
                        return true;
                    } catch (final RuntimeException e) {
                        LOGGER.error(
                                "User defined function which processed LiveCommand '{}' threw RuntimeException: {}",
                                liveCommand.getType(), e.getMessage(), e);
                        return false;
                    }
                })
                .findAny()
                .orElse(false);
    }

    private void processLiveCommandAnswer(final LiveCommandAnswer liveCommandAnswer) {
        liveCommandAnswer.getResponse()
                .ifPresent(r -> getMessagingProvider()
                        .emitAdaptable(PROTOCOL_ADAPTER.toAdaptable(adjustHeadersForLiveSignal(r))));
        liveCommandAnswer.getEvent().ifPresent(e -> getMessagingProvider().emitAdaptable(adaptOutgoingLiveSignal(e)));
    }

    private void handleLiveCommandOrResponse(final Adaptable adaptable) {
        if (adaptable.getPayload().getStatus().isPresent()) {
            // is live command response; just publish.
            messagingProvider.getAdaptableBus()
                    .publish(ProtocolFactory.wrapAsJsonifiableAdaptable(adaptable).toJsonString());
        } else {
            // throw ClassCastException when called on signal of incorrect type
            final ThingCommand<?> command = (ThingCommand<?>) PROTOCOL_ADAPTER.fromAdaptable(adaptable);
            handleLiveCommand(LiveCommandFactory.getInstance().getLiveCommand(command));
        }
    }

    private void handleLiveCommand(final LiveCommand<?, ?> liveCommand) {
        boolean handled = false;

        final ThingId thingId = liveCommand.getThingEntityId();
        if (liveCommand instanceof WithFeatureId) {
            final String featureId = ((WithFeatureId) liveCommand).getFeatureId();
            handled = getFeatureHandle(thingId, featureId)
                    .filter(h -> h instanceof LiveCommandProcessor)
                    .map(h -> (LiveCommandProcessor) h)
                    .map(h -> h.processLiveCommand(liveCommand))
                    .orElse(false);
            LOGGER.debug("Live command of type '{}' handled with specific feature handle: {}",
                    liveCommand.getType(), handled);
        }

        if (!handled) {
            handled = getThingHandle(thingId)
                    .filter(h -> h instanceof LiveCommandProcessor)
                    .map(h -> (LiveCommandProcessor) h)
                    .map(h -> h.processLiveCommand(liveCommand))
                    .orElse(false);
            LOGGER.debug("Live command of type '{}' handled with specific thing handle: {}",
                    liveCommand.getType(), handled);
        }

        if (!handled) {
            handled = processLiveCommand(liveCommand);
            LOGGER.debug("Live command of type '{}' handled with global handle: {}",
                    liveCommand.getType(),
                    handled);
        }

        if (!handled) {
            LOGGER.warn("Incoming live command of type '{}'  was not processed.",
                    liveCommand.getType());
        }

    }

    private static Message<?> adaptableAsLiveMessage(final Adaptable adaptable) {
        final Signal<?> signal = PROTOCOL_ADAPTER.fromAdaptable(adaptable);
        if (signal instanceof MessageCommand) {
            return ((MessageCommand<?, ?>) signal).getMessage();
        } else {
            // ClassCastException on incorrect type
            return ((MessageCommandResponse<?, ?>) signal).getMessage();
        }
    }
}
