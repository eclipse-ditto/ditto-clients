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
import org.eclipse.ditto.client.live.events.FeatureEventFactory;
import org.eclipse.ditto.client.live.events.internal.ImmutableFeatureEventFactory;
import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.PendingMessageWithFeatureId;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.client.live.messages.internal.ImmutableMessageSender;
import org.eclipse.ditto.client.management.internal.FeatureHandleImpl;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.live.base.LiveCommand;
import org.eclipse.ditto.signals.commands.live.base.LiveCommandAnswer;
import org.eclipse.ditto.signals.commands.live.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.DeleteFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertiesLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertyLiveCommand;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturesLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeatureLiveCommand;
import org.eclipse.ditto.signals.commands.live.query.RetrieveFeaturesLiveCommand;
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

    private final InternalConfiguration configuration;
    private final MessageSerializerRegistry serializerRegistry;
    private final String clientSessionId;
    private final JsonSchemaVersion schemaVersion;
    private final Map<Class<? extends LiveCommand>, Function<? extends LiveCommand, LiveCommandAnswerBuilder.BuildStep>>
            liveCommandsFunctions;

    LiveFeatureHandleImpl(final InternalConfiguration configuration, final ThingId thingId, final String featureId) {
        super(TopicPath.Channel.LIVE, thingId, featureId,
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

    /*
     * ###### Section
     * ###### "live" Messages
     */

    @Override
    public <T> PendingMessageWithFeatureId<T> message() {
        return new PendingMessageWithFeatureId<T>() {
            @Override
            public MessageSender.SetSubject<T> from() {
                return ImmutableMessageSender.<T>newInstance()
                        .from(this::sendMessage)
                        .thingId(getThingEntityId())
                        .featureId(getFeatureId());
            }

            @Override
            public MessageSender.SetSubject<T> to() {
                return ImmutableMessageSender.<T>newInstance()
                        .to(this::sendMessage)
                        .thingId(getThingEntityId())
                        .featureId(getFeatureId());
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

        argumentNotNull(subject);
        argumentNotNull(type);
        argumentNotNull(handler);

        LiveMessagesUtil.checkSerializerExistForMessageType(serializerRegistry, type, subject);

        final JsonPointerSelector selector = "*".equals(subject) ?
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/'{direction}'/messages/'{subject}'",
                        getThingEntityId(), getFeatureId())
                : SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/'{direction}'/messages/{2}",
                getThingEntityId(), getFeatureId(), subject);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(configuration, getOutgoingMessageFactory(),
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
                getThingEntityId(), getFeatureId())
                : SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/'{direction}'/messages/{2}",
                getThingEntityId(), getFeatureId(), subject);

        getHandlerRegistry().register(registrationId, selector,
                LiveMessagesUtil.createEventConsumerForRepliableMessage(configuration, getOutgoingMessageFactory(),
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
                ImmutableFeatureEventFactory.getInstance(clientSessionId, schemaVersion, getThingEntityId(),
                        getFeatureId());
        final Event<?> eventToEmit = eventFunction.apply(featureEventFactory);
        getMessagingProvider().emitEvent(eventToEmit, TopicPath.Channel.LIVE);
    }

    /*
     * ###### Section
     * ###### Handle "live" Commands
     */

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
    @SuppressWarnings("unchecked")
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
