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

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import org.eclipse.ditto.client.configuration.internal.InternalConfiguration;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.SendTerminator;
import org.eclipse.ditto.client.internal.bus.JsonPointerSelector;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.live.LiveCommandProcessor;
import org.eclipse.ditto.client.live.LiveFeatureHandle;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.live.events.ThingEventFactory;
import org.eclipse.ditto.client.live.events.internal.ImmutableThingEventFactory;
import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.PendingMessageWithThingId;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.client.live.messages.internal.ImmutableMessageSender;
import org.eclipse.ditto.client.management.internal.ThingHandleImpl;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.KnownMessageSubjects;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.protocoladapter.TopicPath;
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
import org.eclipse.ditto.signals.events.base.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link LiveThingHandle}.
 *
 * @since 1.0.0
 */
@ParametersAreNonnullByDefault
public final class LiveThingHandleImpl extends ThingHandleImpl<LiveThingHandle, LiveFeatureHandle>
        implements LiveThingHandle,
        LiveCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveThingHandleImpl.class);

    private final InternalConfiguration configuration;
    private final MessageSerializerRegistry serializerRegistry;
    private final String clientSessionId;
    private final JsonSchemaVersion schemaVersion;
    private final Map<Class<? extends LiveCommand>, Function<? extends LiveCommand, LiveCommandAnswerBuilder.BuildStep>>
            liveCommandsFunctions;

    LiveThingHandleImpl(final InternalConfiguration configuration, final String thingId) {
        super(TopicPath.Channel.LIVE, thingId,
                configuration.getLiveMessagingProviderOrFail(),
                configuration.getResponseForwarder(),
                OutgoingMessageFactory.newInstance(configuration.getLiveConfigurationOrFail()),
                configuration.getLiveHandlerRegistryOrFail());

        this.configuration = configuration;
        serializerRegistry = configuration
                .getLiveConfigurationOrFail()
                .getMessageSerializerConfiguration()
                .getMessageSerializerRegistry();
        clientSessionId = configuration
                .getLiveConfigurationOrFail()
                .getProviderConfiguration()
                .getAuthenticationProvider()
                .getClientSessionId();
        schemaVersion = configuration
                .getLiveConfigurationOrFail().getSchemaVersion();

        liveCommandsFunctions = new IdentityHashMap<>();
    }

    @Override
    protected LiveFeatureHandleImpl createFeatureHandle(final String thingId, final String featureId) {
        return new LiveFeatureHandleImpl(configuration, thingId, featureId);
    }

    /*
     * ###### Section
     * ###### "live" Messages
     */

    @Override
    public <T> PendingMessageWithThingId<T> message() {

        return new PendingMessageWithThingId<T>() {
            @Override
            public MessageSender.SetSubject<T> from() {
                return ImmutableMessageSender.<T>newInstance()
                        .from(this::sendMessage)
                        .thingId(getThingId());
            }

            @Override
            public MessageSender.SetSubject<T> to() {
                return ImmutableMessageSender.<T>newInstance()
                        .to(this::sendMessage)
                        .thingId(getThingId());
            }

            private void sendMessage(final Message<T> message) {
                final Message<?> toBeSentMessage = getOutgoingMessageFactory().sendMessage(serializerRegistry, message);
                LOGGER.trace("Message about to send: {}", toBeSentMessage);
                new SendTerminator<>(getMessagingProvider(), getResponseForwarder(), toBeSentMessage).send();
            }
        };
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

        LiveMessagesUtil.checkSerializerExistForMessageType(serializerRegistry, type, subject);

        // selector for thing messages:
        final JsonPointerSelector thingSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/'{subject}'", getThingId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/{1}", getThingId(), subject);
        // selector for feature messages:
        final JsonPointerSelector featureSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER,
                "/things/{0}/features/'{featureId}'/'{direction}'/messages/'{subject}'", getThingId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/'{featureId}'/'{direction}'/messages/{0}",
                        getThingId(), subject);

        getHandlerRegistry().register(registrationId, SelectorUtil.or(thingSelector, featureSelector),
                LiveMessagesUtil.createEventConsumerForRepliableMessage(configuration, getOutgoingMessageFactory(),
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
                ? SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/'{subject}'", getThingId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/'{direction}'/messages/{1}", getThingId(), subject);
        // selector for feature messages:
        final JsonPointerSelector featureSelector = "*".equals(subject)
                ? SelectorUtil.formatJsonPointer(LOGGER,
                "/things/{0}/features/'{featureId}'/'{direction}'/messages/'{subject}'",
                getThingId())
                :
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/'{featureId}'/'{direction}'/messages/{1}",
                        getThingId(), subject);

        getHandlerRegistry().register(registrationId, SelectorUtil.or(thingSelector, featureSelector),
                LiveMessagesUtil.createEventConsumerForRepliableMessage(configuration, getOutgoingMessageFactory(),
                        handler));
    }

    @Override
    public <T, U> void registerForClaimMessage(final String registrationId, final Class<T> type,
            final Consumer<RepliableMessage<T, U>> handler) {

        argumentNotNull(registrationId, "registrationId");
        argumentNotNull(type, "type");
        argumentNotNull(handler, "handler");

        LiveMessagesUtil.checkSerializerExistForMessageType(serializerRegistry, type);

        final JsonPointerSelector selector =
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/inbox/messages/{1}", getThingId(),
                        KnownMessageSubjects.CLAIM_SUBJECT);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(configuration, getOutgoingMessageFactory(),
                        type, handler));
    }

    @Override
    public <U> void registerForClaimMessage(final String registrationId,
            final Consumer<RepliableMessage<?, U>> handler) {
        argumentNotNull(registrationId, "registrationId");
        argumentNotNull(handler, "handler");

        final JsonPointerSelector selector =
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/inbox/messages/{1}", getThingId(),
                        KnownMessageSubjects.CLAIM_SUBJECT);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(configuration, getOutgoingMessageFactory(),
                        handler));
    }

    /*
     * ###### Section
     * ###### Emit "live" Events
     */

    @Override
    public void emitEvent(final Function<ThingEventFactory, Event<?>> eventFunction) {
        argumentNotNull(eventFunction);

        final ThingEventFactory thingEventFactory = ImmutableThingEventFactory.getInstance(clientSessionId,
                schemaVersion, getThingId());
        final Event<?> eventToEmit = eventFunction.apply(thingEventFactory);
        getMessagingProvider().emitEvent(eventToEmit, TopicPath.Channel.LIVE);
    }

    /*
     * ###### Section
     * ###### Handle "live" Commands
     */

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
        if (liveCommandsFunctions.containsKey(liveCommandClass)) {
            throw new IllegalStateException("A Function for '" + liveCommandClass.getSimpleName() + "' is already " +
                    "defined. Stop the registered handler before registering a new handler.");
        } else {
            liveCommandsFunctions.put(liveCommandClass, function);
        }
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
                .ifPresent(r -> getMessagingProvider().sendCommandResponse(r, TopicPath.Channel.LIVE));
        liveCommandAnswer.getEvent().ifPresent(e -> getMessagingProvider().emitEvent(e, TopicPath.Channel.LIVE));
    }

}
