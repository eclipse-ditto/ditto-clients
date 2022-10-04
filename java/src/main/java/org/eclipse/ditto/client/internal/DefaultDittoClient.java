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
package org.eclipse.ditto.client.internal;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.eclipse.ditto.base.model.acks.AcknowledgementLabelNotDeclaredException;
import org.eclipse.ditto.base.model.acks.AcknowledgementLabelNotUniqueException;
import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.base.model.headers.DittoHeadersBuilder;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.base.model.signals.acks.Acknowledgement;
import org.eclipse.ditto.base.model.signals.commands.ErrorResponse;
import org.eclipse.ditto.client.DisconnectedDittoClient;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.internal.ImmutableChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeatureChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeaturesChange;
import org.eclipse.ditto.client.changes.internal.ImmutableThingChange;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.BusFactory;
import org.eclipse.ditto.client.internal.bus.Classification;
import org.eclipse.ditto.client.internal.bus.JsonPointerSelectors;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.live.Live;
import org.eclipse.ditto.client.live.internal.LiveImpl;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.policies.Policies;
import org.eclipse.ditto.client.policies.internal.PoliciesImpl;
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.client.twin.internal.TwinImpl;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.messages.model.MessageDirection;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.protocol.adapter.ProtocolAdapter;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.events.AttributeCreated;
import org.eclipse.ditto.things.model.signals.events.AttributeDeleted;
import org.eclipse.ditto.things.model.signals.events.AttributeModified;
import org.eclipse.ditto.things.model.signals.events.AttributesCreated;
import org.eclipse.ditto.things.model.signals.events.AttributesDeleted;
import org.eclipse.ditto.things.model.signals.events.AttributesModified;
import org.eclipse.ditto.things.model.signals.events.FeatureCreated;
import org.eclipse.ditto.things.model.signals.events.FeatureDeleted;
import org.eclipse.ditto.things.model.signals.events.FeatureDesiredPropertiesCreated;
import org.eclipse.ditto.things.model.signals.events.FeatureDesiredPropertiesDeleted;
import org.eclipse.ditto.things.model.signals.events.FeatureDesiredPropertiesModified;
import org.eclipse.ditto.things.model.signals.events.FeatureDesiredPropertyCreated;
import org.eclipse.ditto.things.model.signals.events.FeatureDesiredPropertyDeleted;
import org.eclipse.ditto.things.model.signals.events.FeatureDesiredPropertyModified;
import org.eclipse.ditto.things.model.signals.events.FeatureModified;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertiesModified;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturePropertyModified;
import org.eclipse.ditto.things.model.signals.events.FeaturesCreated;
import org.eclipse.ditto.things.model.signals.events.FeaturesDeleted;
import org.eclipse.ditto.things.model.signals.events.FeaturesModified;
import org.eclipse.ditto.things.model.signals.events.ThingCreated;
import org.eclipse.ditto.things.model.signals.events.ThingDeleted;
import org.eclipse.ditto.things.model.signals.events.ThingMerged;
import org.eclipse.ditto.things.model.signals.events.ThingModified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link DittoClient}.
 *
 * @since 1.0.0
 */
public final class DefaultDittoClient implements DittoClient, DisconnectedDittoClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDittoClient.class);

    private static final String SELECTOR_INCOMING_MESSAGE = "incoming-message";

    private final TwinImpl twin;
    private final LiveImpl live;
    private final PoliciesImpl policies;

    private DefaultDittoClient(final TwinImpl twin, final LiveImpl live, final PoliciesImpl policies) {
        this.twin = twin;
        this.live = live;
        this.policies = policies;
        twin.getMessagingProvider().registerChannelCloser(() -> {
            LOGGER.info("Closing <twin> channel..");
            twin.getMessagingProvider().close();
            twin.getBus().close();
        });
        live.getMessagingProvider().registerChannelCloser(() -> {
            LOGGER.info("Closing <live> channel..");
            live.getMessagingProvider().close();
            live.getBus().close();
        });
        policies.getMessagingProvider().registerChannelCloser(() -> {
            LOGGER.info("Closing <policies> channel..");
            policies.getMessagingProvider().close();
            policies.getBus().close();
        });
        logVersionInformation();
        handleSpontaneousErrors();
    }

    /**
     * Create a Ditto client object but do not attempt to connect to the configured back-end.
     *
     * @param twinMessagingProvider the messaging provider to use for the {@code Twin} aspect.
     * @param liveMessagingProvider the messaging provider to use for the {@code Live} aspect.
     * @param policyMessagingProvider the messaging provider for the {@code Policy} part of the client.
     * @param messageSerializerRegistry registry for all serializers of live messages.
     * @return the disconnected client.
     */
    public static DisconnectedDittoClient newDisconnectedInstance(final MessagingProvider twinMessagingProvider,
            final MessagingProvider liveMessagingProvider,
            final MessagingProvider policyMessagingProvider,
            final MessageSerializerRegistry messageSerializerRegistry) {

        final TwinImpl twin = configureTwin(twinMessagingProvider);
        final LiveImpl live = configureLive(liveMessagingProvider, messageSerializerRegistry);
        final PoliciesImpl policy = configurePolicyClient(policyMessagingProvider);
        return new DefaultDittoClient(twin, live, policy);
    }

    @Override
    public Twin twin() {
        return twin;
    }

    @Override
    public Live live() {
        return live;
    }

    @Override
    public Policies policies() {
        return policies;
    }

    @Override
    public CompletionStage<Adaptable> sendDittoProtocol(final Adaptable dittoProtocolAdaptable) {

        final TopicPath.Group group = dittoProtocolAdaptable.getTopicPath().getGroup();
        switch (group) {
            case THINGS:
                return sendDittoProtocolForThingsGroup(dittoProtocolAdaptable);
            case POLICIES:
                return sendDittoProtocolForPoliciesGroup(dittoProtocolAdaptable);
            default:
                throw new IllegalArgumentException("Unknown group: " + group);
        }
    }

    private CompletionStage<Adaptable> sendDittoProtocolForThingsGroup(final Adaptable dittoProtocolAdaptable) {
        final TopicPath.Channel channel = dittoProtocolAdaptable.getTopicPath().getChannel();
        switch (channel) {
            case TWIN:
                return twin.getMessagingProvider().sendAdaptable(dittoProtocolAdaptable);
            case LIVE:
                return live.getMessagingProvider().sendAdaptable(dittoProtocolAdaptable);
            default:
                throw new IllegalArgumentException("Unsupported channel for things group: " + channel);
        }
    }

    private CompletionStage<Adaptable> sendDittoProtocolForPoliciesGroup(final Adaptable dittoProtocolAdaptable) {
        final TopicPath.Channel channel = dittoProtocolAdaptable.getTopicPath().getChannel();
        if (TopicPath.Channel.NONE.equals(channel)) {
            return policies.getMessagingProvider().sendAdaptable(dittoProtocolAdaptable);
        }
        throw new IllegalArgumentException("Unsupported channel for policies group: " + channel);
    }

    @Override
    public void destroy() {
        twin.getMessagingProvider().close();
        twin.getBus().close();
        live.getMessagingProvider().close();
        live.getBus().close();
        policies.getMessagingProvider().close();
        policies.getBus().close();
    }

    private static void logVersionInformation() {
        final String clientVersion = VersionReader.determineClientVersion();
        final String buildTimeStamp = VersionReader.determineBuildTimeStamp();
        LOGGER.info("Ditto Client [{}//{}] initialized successfully", clientVersion, buildTimeStamp);
    }

    private static TwinImpl configureTwin(final MessagingProvider messagingProvider) {
        final String name = TopicPath.Channel.TWIN.getName();
        final PointerBus bus = BusFactory.createPointerBus(name, messagingProvider.getExecutorService());
        init(bus, messagingProvider);
        final MessagingConfiguration messagingConfiguration = messagingProvider.getMessagingConfiguration();
        final JsonSchemaVersion schemaVersion = messagingConfiguration.getJsonSchemaVersion();
        final OutgoingMessageFactory messageFactory = OutgoingMessageFactory.newInstance(schemaVersion);
        return TwinImpl.newInstance(messagingProvider, messageFactory, bus);
    }

    private static LiveImpl configureLive(final MessagingProvider messagingProvider,
            final MessageSerializerRegistry messageSerializerRegistry) {
        final String name = TopicPath.Channel.LIVE.getName();
        final PointerBus bus = BusFactory.createPointerBus(name, messagingProvider.getExecutorService());
        init(bus, messagingProvider);
        final JsonSchemaVersion schemaVersion = messagingProvider.getMessagingConfiguration().getJsonSchemaVersion();
        final OutgoingMessageFactory messageFactory = OutgoingMessageFactory.newInstance(schemaVersion);
        return LiveImpl.newInstance(messagingProvider, messageFactory, bus, schemaVersion,
                messageSerializerRegistry);
    }

    private static PoliciesImpl configurePolicyClient(final MessagingProvider messagingProvider) {
        final String busName = TopicPath.Channel.NONE.getName();
        final PointerBus bus = BusFactory.createPointerBus(busName, messagingProvider.getExecutorService());
        init(bus, messagingProvider);
        final OutgoingMessageFactory messageFactory = getOutgoingMessageFactoryForPolicies(messagingProvider);
        return PoliciesImpl.newInstance(messagingProvider, messageFactory, bus);
    }

    private static OutgoingMessageFactory getOutgoingMessageFactoryForPolicies(
            final MessagingProvider messagingProvider) {
        final JsonSchemaVersion schemaVersion = messagingProvider.getMessagingConfiguration().getJsonSchemaVersion();
        return OutgoingMessageFactory.newInstance(schemaVersion);
    }

    private static void init(final PointerBus bus, final MessagingProvider messagingProvider) {
        registerKeyBasedDistributorForIncomingEvents(bus);
        registerKeyBasedHandlersForIncomingEvents(bus, messagingProvider, AbstractHandle.PROTOCOL_ADAPTER);
        messagingProvider.getAdaptableBus().subscribeForAdaptable(Classification.forErrors(), errorAdaptable ->
                messagingProvider.onDittoProtocolError(asDittoRuntimeException(errorAdaptable))
        );
    }

    private static void registerKeyBasedDistributorForIncomingEvents(final PointerBus bus) {
        bus.on(JsonPointerSelectors.jsonPointer(SELECTOR_INCOMING_MESSAGE), e -> {
            final Message<?> message = (Message<?>) e.getData();

            LOGGER.trace("Received Message: '{}'", message);

            final String subject = message.getSubject();
            final ThingId thingId = message.getEntityId();
            final MessageDirection direction = message.getDirection();

            // Direction is set to "TO" -> Message is put in the "inbox"
            // Direction is set to "FROM" -> Message is put in the "outbox"
            final String inboxOutbox = direction == MessageDirection.TO ? "inbox" : "outbox";

            final String key = message.getFeatureId()
                    .map(featureId -> MessageFormat.format("/things/{0}/features/{1}/{2}/messages/{3}", thingId,
                            featureId, inboxOutbox, subject))
                    .orElse(MessageFormat.format("/things/{0}/{1}/messages/{2}", thingId, inboxOutbox, subject));

            final JsonPointer keyPointer = JsonPointer.of(key);
            LOGGER.trace("Notifying bus at address '{}' with obj: {}", keyPointer, message);
            bus.notify(keyPointer, message);
        });
    }

    private static <T extends Signal<?>> Consumer<T> emitSignal(final MessagingProvider messagingProvider,
            final ProtocolAdapter protocolAdapter,
            final DittoHeaderDefinition... headersToRemove) {

        return signal -> {
            final DittoHeadersBuilder<?, ?> headersBuilder = signal.getDittoHeaders().toBuilder();
            for (final DittoHeaderDefinition definition : headersToRemove) {
                headersBuilder.removeHeader(definition.getKey());
            }
            final Signal<?> signalToEmit = signal.setDittoHeaders(headersBuilder.build());
            messagingProvider.emitAdaptable(protocolAdapter.toAdaptable(signalToEmit));
        };
    }

    private static void registerKeyBasedHandlersForIncomingEvents(final PointerBus bus,
            final MessagingProvider messagingProvider,
            final ProtocolAdapter protocolAdapter) {

        final Consumer<Acknowledgement> emitAcknowledgement = emitSignal(
                messagingProvider,
                protocolAdapter,
                DittoHeaderDefinition.READ_SUBJECTS,
                DittoHeaderDefinition.AUTHORIZATION_CONTEXT,
                DittoHeaderDefinition.RESPONSE_REQUIRED
        );

        /*
         * Merged Events are distinguished by their resource path. There is only one command for all existing
         * resource paths.
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingMerged.TYPE, ThingMerged.class,
                e -> BusAddressFactory.forThingMergedEvent(e.getEntityId(), e.getResourcePath()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.MERGED,
                        e.getResourcePath(), e.getValue(), e.getRevision(), e.getTimestamp().orElse(null),
                        extra, e.getDittoHeaders(), emitAcknowledgement));

        /*
         * Thing
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingCreated.TYPE, ThingCreated.class,
                e -> BusAddressFactory.forThing(e.getEntityId()),
                (e, extra) -> new ImmutableThingChange(e.getEntityId(), ChangeAction.CREATED, e.getThing(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingModified.TYPE, ThingModified.class,
                e -> BusAddressFactory.forThing(e.getEntityId()),
                (e, extra) -> new ImmutableThingChange(e.getEntityId(), ChangeAction.UPDATED, e.getThing(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingDeleted.TYPE, ThingDeleted.class,
                e -> BusAddressFactory.forThing(e.getEntityId()),
                (e, extra) -> new ImmutableThingChange(e.getEntityId(), ChangeAction.DELETED, null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        /*
         * Attributes
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesCreated.TYPE, AttributesCreated.class,
                e -> BusAddressFactory.forAttributes(e.getEntityId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        e.getCreatedAttributes(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesModified.TYPE, AttributesModified.class,
                e -> BusAddressFactory.forAttributes(e.getEntityId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        e.getModifiedAttributes(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesDeleted.TYPE, AttributesDeleted.class,
                e -> BusAddressFactory.forAttributes(e.getEntityId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        /*
         * Attribute
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeCreated.TYPE, AttributeCreated.class,
                e -> BusAddressFactory.forAttribute(e.getEntityId(), e.getAttributePointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.CREATED,
                        e.getAttributePointer(),
                        e.getAttributeValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement));

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeModified.TYPE, AttributeModified.class,
                e -> BusAddressFactory.forAttribute(e.getEntityId(), e.getAttributePointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.UPDATED,
                        e.getAttributePointer(),
                        e.getAttributeValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement));

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeDeleted.TYPE, AttributeDeleted.class,
                e -> BusAddressFactory.forAttribute(e.getEntityId(), e.getAttributePointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.DELETED,
                        e.getAttributePointer(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement));

        /*
         * Features
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesCreated.TYPE, FeaturesCreated.class,
                e -> BusAddressFactory.forFeatures(e.getEntityId()),
                (e, extra) -> new ImmutableFeaturesChange(e.getEntityId(), ChangeAction.CREATED,
                        e.getFeatures(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesModified.TYPE, FeaturesModified.class,
                e -> BusAddressFactory.forFeatures(e.getEntityId()),
                (e, extra) -> new ImmutableFeaturesChange(e.getEntityId(), ChangeAction.UPDATED,
                        e.getFeatures(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesDeleted.TYPE, FeaturesDeleted.class,
                e -> BusAddressFactory.forFeatures(e.getEntityId()),
                (e, extra) -> new ImmutableFeaturesChange(e.getEntityId(), ChangeAction.DELETED,
                        null,
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        /*
         * Feature
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureCreated.TYPE, FeatureCreated.class,
                e -> BusAddressFactory.forFeature(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableFeatureChange(e.getEntityId(), ChangeAction.CREATED,
                        e.getFeature(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureModified.TYPE, FeatureModified.class,
                e -> BusAddressFactory.forFeature(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableFeatureChange(e.getEntityId(), ChangeAction.UPDATED,
                        e.getFeature(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDeleted.TYPE, FeatureDeleted.class,
                e -> BusAddressFactory.forFeature(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableFeatureChange(e.getEntityId(), ChangeAction.DELETED,
                        null,
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        /*
         * Feature Properties
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesCreated.TYPE,
                FeaturePropertiesCreated.class,
                e -> BusAddressFactory.forFeatureProperties(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        e.getProperties().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesModified.TYPE,
                FeaturePropertiesModified.class,
                e -> BusAddressFactory.forFeatureProperties(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        e.getProperties().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesDeleted.TYPE,
                FeaturePropertiesDeleted.class,
                e -> BusAddressFactory.forFeatureProperties(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        /*
         * Feature Property
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyCreated.TYPE,
                FeaturePropertyCreated.class,
                e -> BusAddressFactory.forFeatureProperty(e.getEntityId(), e.getFeatureId(),
                        e.getPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.CREATED,
                        e.getPropertyPointer(),
                        e.getPropertyValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyModified.TYPE,
                FeaturePropertyModified.class,
                e -> BusAddressFactory.forFeatureProperty(e.getEntityId(), e.getFeatureId(),
                        e.getPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.UPDATED,
                        e.getPropertyPointer(),
                        e.getPropertyValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyDeleted.TYPE,
                FeaturePropertyDeleted.class,
                e -> BusAddressFactory.forFeatureProperty(e.getEntityId(), e.getFeatureId(),
                        e.getPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.DELETED,
                        e.getPropertyPointer(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        /*
         * Feature Desired Properties
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDesiredPropertiesCreated.TYPE,
                FeatureDesiredPropertiesCreated.class,
                e -> BusAddressFactory.forFeatureDesiredProperties(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        e.getDesiredProperties().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDesiredPropertiesModified.TYPE,
                FeatureDesiredPropertiesModified.class,
                e -> BusAddressFactory.forFeatureDesiredProperties(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        e.getDesiredProperties().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDesiredPropertiesDeleted.TYPE,
                FeatureDesiredPropertiesDeleted.class,
                e -> BusAddressFactory.forFeatureDesiredProperties(e.getEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        /*
         * Feature Desired Property
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDesiredPropertyCreated.TYPE,
                FeatureDesiredPropertyCreated.class,
                e -> BusAddressFactory.forFeatureDesiredProperty(e.getEntityId(), e.getFeatureId(),
                        e.getDesiredPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.CREATED,
                        e.getDesiredPropertyPointer(),
                        e.getDesiredPropertyValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDesiredPropertyModified.TYPE,
                FeatureDesiredPropertyModified.class,
                e -> BusAddressFactory.forFeatureDesiredProperty(e.getEntityId(), e.getFeatureId(),
                        e.getDesiredPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.UPDATED,
                        e.getDesiredPropertyPointer(),
                        e.getDesiredPropertyValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDesiredPropertyDeleted.TYPE,
                FeatureDesiredPropertyDeleted.class,
                e -> BusAddressFactory.forFeatureDesiredProperty(e.getEntityId(), e.getFeatureId(),
                        e.getDesiredPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getEntityId(), ChangeAction.DELETED,
                        e.getDesiredPropertyPointer(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra, e.getDittoHeaders(),
                        emitAcknowledgement)
        );
    }

    @Override
    public CompletionStage<DittoClient> connect() {
        return twin.messagingProvider.initializeAsync()
                .thenCompose(result -> live.messagingProvider.initializeAsync())
                .thenCompose(result -> policies.messagingProvider.initializeAsync())
                .thenApply(result -> this);
    }

    private void handleSpontaneousErrors() {
        handleSpontaneousErrors(twin.messagingProvider);
        if (live.messagingProvider != twin.messagingProvider) {
            handleSpontaneousErrors(live.messagingProvider);
        }
        if (policies.messagingProvider != twin.messagingProvider) {
            handleSpontaneousErrors(policies.messagingProvider);
        }
    }

    /**
     * Handle {@code DittoRuntimeException}s from the back-end that are not replies of anything.
     *
     * @param provider the messaging provider.
     */
    private static void handleSpontaneousErrors(final MessagingProvider provider) {
        final Optional<Consumer<Throwable>> connectionErrorHandler =
                provider.getMessagingConfiguration().getConnectionErrorHandler();
        if (connectionErrorHandler.isPresent()) {
            final AdaptableBus adaptableBus = provider.getAdaptableBus();
            final Consumer<Throwable> consumer = connectionErrorHandler.get();

            final Classification ackLabelNotUnique =
                    Classification.forErrorCode(AcknowledgementLabelNotUniqueException.ERROR_CODE);
            final Classification ackLabelNotDeclared =
                    Classification.forErrorCode(AcknowledgementLabelNotDeclaredException.ERROR_CODE);

            adaptableBus.subscribeForAdaptableExclusively(ackLabelNotUnique,
                    adaptable -> consumer.accept(asDittoRuntimeException(adaptable)));
            adaptableBus.subscribeForAdaptableExclusively(ackLabelNotDeclared,
                    adaptable -> consumer.accept(asDittoRuntimeException(adaptable)));
        }
    }

    private static Throwable asDittoRuntimeException(final Adaptable adaptable) {
        final Signal<?> signal = AbstractHandle.PROTOCOL_ADAPTER.fromAdaptable(adaptable);
        if (signal instanceof ErrorResponse) {
            return ((ErrorResponse<?>) signal).getDittoRuntimeException();
        } else {
            return new ClassCastException("Expect an error response, got: " +
                    ProtocolFactory.wrapAsJsonifiableAdaptable(adaptable).toJsonString());
        }
    }

}
