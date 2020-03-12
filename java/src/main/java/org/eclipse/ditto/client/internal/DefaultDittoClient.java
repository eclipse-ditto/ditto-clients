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
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.internal.ImmutableChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeatureChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeaturesChange;
import org.eclipse.ditto.client.changes.internal.ImmutableThingChange;
import org.eclipse.ditto.client.internal.bus.BusFactory;
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
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.events.things.AclEntryCreated;
import org.eclipse.ditto.signals.events.things.AclEntryDeleted;
import org.eclipse.ditto.signals.events.things.AclEntryModified;
import org.eclipse.ditto.signals.events.things.AclModified;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributeDeleted;
import org.eclipse.ditto.signals.events.things.AttributeModified;
import org.eclipse.ditto.signals.events.things.AttributesCreated;
import org.eclipse.ditto.signals.events.things.AttributesDeleted;
import org.eclipse.ditto.signals.events.things.AttributesModified;
import org.eclipse.ditto.signals.events.things.FeatureCreated;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.things.FeatureModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertyCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.FeaturesCreated;
import org.eclipse.ditto.signals.events.things.FeaturesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturesModified;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingModified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link DittoClient}.
 *
 * @since 1.0.0
 */
public final class DefaultDittoClient implements DittoClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDittoClient.class);

    private static final String SELECTOR_INCOMING_MESSAGE = "incoming-message";

    private static final String THING_PATTERN = "/things/{0}";
    private static final String ACL_PATTERN = THING_PATTERN + "/acl/{1}";
    private static final String ATTRIBUTES_PATTERN = THING_PATTERN + "/attributes";
    private static final String ATTRIBUTE_PATTERN = THING_PATTERN + "/attributes{1}";
    private static final String FEATURES_PATTERN = THING_PATTERN + "/features";
    private static final String FEATURE_PATTERN = THING_PATTERN + "/features/{1}";
    private static final String FEATURE_PROPERTIES_PATTERN = THING_PATTERN + "/features/{1}/properties";
    private static final String FEATURE_PROPERTY_PATTERN = THING_PATTERN + "/features/{1}/properties{2}";

    private final TwinImpl twin;
    private final LiveImpl live;
    private final PoliciesImpl policies;

    private DefaultDittoClient(final TwinImpl twin, final LiveImpl live, final PoliciesImpl policies) {
        this.twin = twin;
        this.live = live;
        this.policies = policies;
        logVersionInformation();
    }

    /**
     * Creates a new {@link org.eclipse.ditto.client.DittoClient}.
     *
     * @param twinMessagingProvider the messaging provider to use for the {@code Twin} aspect.
     * @param liveMessagingProvider the messaging provider to use for the {@code Live} aspect.
     * @param policyMessagingProvider the messaging provider for the {@code Policy} part of the client.
     * @param responseForwarder forwarder used to optimize response performance.
     * @param messageSerializerRegistry registry for all serializers of live messages.
     * @return the client.
     */
    public static DittoClient newInstance(final MessagingProvider twinMessagingProvider,
            final MessagingProvider liveMessagingProvider,
            final MessagingProvider policyMessagingProvider,
            final ResponseForwarder responseForwarder,
            final MessageSerializerRegistry messageSerializerRegistry) {
        final TwinImpl twin = configureTwin(twinMessagingProvider, responseForwarder);
        final LiveImpl live = configureLive(liveMessagingProvider, responseForwarder, messageSerializerRegistry);
        final PoliciesImpl policy = configurePolicyClient(policyMessagingProvider, responseForwarder);
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
    public CompletableFuture<Adaptable> sendDittoProtocol(final Adaptable dittoProtocolAdaptable) {

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

    private CompletableFuture<Adaptable> sendDittoProtocolForThingsGroup(final Adaptable dittoProtocolAdaptable) {
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

    private CompletableFuture<Adaptable> sendDittoProtocolForPoliciesGroup(final Adaptable dittoProtocolAdaptable) {
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

    private static TwinImpl configureTwin(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder) {
        final String name = TopicPath.Channel.TWIN.getName();
        final PointerBus bus = BusFactory.createPointerBus(name, messagingProvider.getExecutorService());
        init(bus, messagingProvider);
        final JsonSchemaVersion schemaVersion = messagingProvider.getMessagingConfiguration().getJsonSchemaVersion();
        final OutgoingMessageFactory messageFactory = OutgoingMessageFactory.newInstance(schemaVersion);
        return TwinImpl.newInstance(messagingProvider, responseForwarder, messageFactory, bus);
    }

    private static LiveImpl configureLive(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder, final MessageSerializerRegistry messageSerializerRegistry) {
        final String name = TopicPath.Channel.LIVE.getName();
        final PointerBus bus = BusFactory.createPointerBus(name, messagingProvider.getExecutorService());
        init(bus, messagingProvider);
        final String sessionId = messagingProvider.getAuthenticationConfiguration().getSessionId();
        final JsonSchemaVersion schemaVersion = messagingProvider.getMessagingConfiguration().getJsonSchemaVersion();
        final OutgoingMessageFactory messageFactory = OutgoingMessageFactory.newInstance(schemaVersion);
        return LiveImpl.newInstance(messagingProvider, responseForwarder, messageFactory, bus, schemaVersion,
                messageSerializerRegistry);
    }

    private static PoliciesImpl configurePolicyClient(final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder) {
        final String busName = TopicPath.Channel.NONE.getName();
        final PointerBus bus = BusFactory.createPointerBus(busName, messagingProvider.getExecutorService());
        init(bus, messagingProvider);
        final OutgoingMessageFactory messageFactory = getOutgoingMessageFactoryForPolicies(messagingProvider);
        return PoliciesImpl.newInstance(messagingProvider, responseForwarder, messageFactory, bus);
    }

    private static OutgoingMessageFactory getOutgoingMessageFactoryForPolicies(
            final MessagingProvider messagingProvider) {
        final JsonSchemaVersion schemaVersion = messagingProvider.getMessagingConfiguration().getJsonSchemaVersion();
        if (JsonSchemaVersion.V_1.equals(schemaVersion)) {
            LOGGER.warn("The MessagingProvider was configured with JsonSchemaVersion V_1 which is invalid for policy" +
                    " commands. Therefore defaulting to V_2 for all policy commands." +
                    " Please consider upgrading to JsonSchemaVersion V_2 as V_1 is deprecated and will be removed" +
                    " in an upcoming release.");
            return OutgoingMessageFactory.newInstance(JsonSchemaVersion.V_2);
        }
        return OutgoingMessageFactory.newInstance(schemaVersion);
    }

    private static void init(final PointerBus bus, final MessagingProvider messagingProvider) {
        registerKeyBasedDistributorForIncomingEvents(bus);
        registerKeyBasedHandlersForIncomingEvents(bus);
        messagingProvider.initialize();
    }

    private static void registerKeyBasedDistributorForIncomingEvents(final PointerBus bus) {
        bus.on(JsonPointerSelectors.jsonPointer(SELECTOR_INCOMING_MESSAGE), e -> {
            final Message<?> message = (Message<?>) e.getData();

            LOGGER.trace("Received Message: '{}'", message);

            final String subject = message.getSubject();
            final ThingId thingId = message.getThingEntityId();
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

    private static void registerKeyBasedHandlersForIncomingEvents(final PointerBus bus) {
        /*
         * Thing
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingCreated.TYPE, ThingCreated.class,
                e -> MessageFormat.format(THING_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableThingChange(e.getThingEntityId(), ChangeAction.CREATED, e.getThing(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingModified.TYPE, ThingModified.class,
                e -> MessageFormat.format(THING_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableThingChange(e.getThingEntityId(), ChangeAction.UPDATED, e.getThing(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingDeleted.TYPE, ThingDeleted.class,
                e -> MessageFormat.format(THING_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableThingChange(e.getThingEntityId(), ChangeAction.DELETED, null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        /*
         * ACL - v1 only
         * @deprecated as part of deprecated API 1
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclModified.TYPE, AclModified.class,
                e -> MessageFormat.format("/things/{0}/acl", e.getThingEntityId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        e.getAccessControlList().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclEntryCreated.TYPE, AclEntryCreated.class,
                e -> MessageFormat.format(ACL_PATTERN, e.getThingEntityId(),
                        e.getAclEntry().getAuthorizationSubject().getId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        e.getAclEntry().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclEntryModified.TYPE, AclEntryModified.class,
                e -> MessageFormat.format(ACL_PATTERN, e.getThingEntityId(),
                        e.getAclEntry().getAuthorizationSubject().getId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        e.getAclEntry().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclEntryDeleted.TYPE, AclEntryDeleted.class,
                e -> MessageFormat.format(ACL_PATTERN, e.getThingEntityId(),
                        e.getAuthorizationSubject().getId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        /*
         * Attributes
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesCreated.TYPE, AttributesCreated.class,
                e -> MessageFormat.format(ATTRIBUTES_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        e.getCreatedAttributes(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesModified.TYPE, AttributesModified.class,
                e -> MessageFormat.format(ATTRIBUTES_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        e.getModifiedAttributes(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesDeleted.TYPE, AttributesDeleted.class,
                e -> MessageFormat.format(ATTRIBUTES_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        /*
         * Attribute
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeCreated.TYPE, AttributeCreated.class,
                e -> MessageFormat.format(ATTRIBUTE_PATTERN, e.getThingEntityId(),
                        e.getAttributePointer()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.CREATED,
                        e.getAttributePointer(),
                        e.getAttributeValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra));

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeModified.TYPE, AttributeModified.class,
                e -> MessageFormat.format(ATTRIBUTE_PATTERN, e.getThingEntityId(),
                        e.getAttributePointer()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        e.getAttributePointer(),
                        e.getAttributeValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra));

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeDeleted.TYPE, AttributeDeleted.class,
                e -> MessageFormat.format(ATTRIBUTE_PATTERN, e.getThingEntityId(),
                        e.getAttributePointer()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.DELETED,
                        e.getAttributePointer(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra));

        /*
         * Features
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesCreated.TYPE, FeaturesCreated.class,
                e -> MessageFormat.format(FEATURES_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableFeaturesChange(e.getThingEntityId(), ChangeAction.CREATED,
                        e.getFeatures(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesModified.TYPE, FeaturesModified.class,
                e -> MessageFormat.format(FEATURES_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableFeaturesChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        e.getFeatures(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesDeleted.TYPE, FeaturesDeleted.class,
                e -> MessageFormat.format(FEATURES_PATTERN, e.getThingEntityId()),
                (e, extra) -> new ImmutableFeaturesChange(e.getThingEntityId(), ChangeAction.DELETED,
                        null,
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        /*
         * Feature
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureCreated.TYPE, FeatureCreated.class,
                e -> MessageFormat.format(FEATURE_PATTERN, e.getThingEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableFeatureChange(e.getThingEntityId(), ChangeAction.CREATED,
                        e.getFeature(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureModified.TYPE, FeatureModified.class,
                e -> MessageFormat.format(FEATURE_PATTERN, e.getThingEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableFeatureChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        e.getFeature(),
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDeleted.TYPE, FeatureDeleted.class,
                e -> MessageFormat.format(FEATURE_PATTERN, e.getThingEntityId(), e.getFeatureId()),
                (e, extra) -> new ImmutableFeatureChange(e.getThingEntityId(), ChangeAction.DELETED,
                        null,
                        JsonPointer.empty(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        /*
         * Feature Properties
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesCreated.TYPE,
                FeaturePropertiesCreated.class,
                e -> MessageFormat.format(FEATURE_PROPERTIES_PATTERN, e.getThingEntityId(),
                        e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        e.getProperties().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesModified.TYPE,
                FeaturePropertiesModified.class,
                e -> MessageFormat.format(FEATURE_PROPERTIES_PATTERN, e.getThingEntityId(),
                        e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        e.getProperties().toJson(e.getImplementedSchemaVersion()),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesDeleted.TYPE,
                FeaturePropertiesDeleted.class,
                e -> MessageFormat.format(FEATURE_PROPERTIES_PATTERN, e.getThingEntityId(),
                        e.getFeatureId()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        /*
         * Feature Property
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyCreated.TYPE,
                FeaturePropertyCreated.class,
                e -> MessageFormat.format(FEATURE_PROPERTY_PATTERN, e.getThingEntityId(),
                        e.getFeatureId(), e.getPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.CREATED,
                        e.getPropertyPointer(),
                        e.getPropertyValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyModified.TYPE,
                FeaturePropertyModified.class,
                e -> MessageFormat.format(FEATURE_PROPERTY_PATTERN, e.getThingEntityId(),
                        e.getFeatureId(), e.getPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.UPDATED,
                        e.getPropertyPointer(),
                        e.getPropertyValue(),
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyDeleted.TYPE,
                FeaturePropertyDeleted.class,
                e -> MessageFormat.format(FEATURE_PROPERTY_PATTERN, e.getThingEntityId(),
                        e.getFeatureId(), e.getPropertyPointer()),
                (e, extra) -> new ImmutableChange(e.getThingEntityId(), ChangeAction.DELETED,
                        e.getPropertyPointer(),
                        null,
                        e.getRevision(), e.getTimestamp().orElse(null), extra)
        );
    }

}
