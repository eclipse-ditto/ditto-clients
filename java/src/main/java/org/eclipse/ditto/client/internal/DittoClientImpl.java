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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.changes.internal.ImmutableChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeatureChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeaturesChange;
import org.eclipse.ditto.client.changes.internal.ImmutableThingChange;
import org.eclipse.ditto.client.configuration.CommonConfiguration;
import org.eclipse.ditto.client.configuration.internal.InternalConfiguration;
import org.eclipse.ditto.client.internal.bus.JsonPointerSelectors;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.live.Live;
import org.eclipse.ditto.client.live.internal.LiveImpl;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.client.twin.internal.TwinImpl;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
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
public final class DittoClientImpl implements DittoClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DittoClientImpl.class);

    private static final String SELECTOR_INCOMING_MESSAGE = "incoming-message";

    private static final String THING_PATTERN = "/things/{0}";
    private static final String ACL_PATTERN = THING_PATTERN + "/acl/{1}";
    private static final String ATTRIBUTES_PATTERN = THING_PATTERN + "/attributes";
    private static final String ATTRIBUTE_PATTERN = THING_PATTERN + "/attributes{1}";
    private static final String FEATURES_PATTERN = THING_PATTERN + "/features";
    private static final String FEATURE_PATTERN = THING_PATTERN + "/features/{1}";
    private static final String FEATURE_PROPERTIES_PATTERN = THING_PATTERN + "/features/{1}/properties";
    private static final String FEATURE_PROPERTY_PATTERN = THING_PATTERN + "/features/{1}/properties{2}";

    private final InternalConfiguration configuration;
    private final AtomicReference<Twin> twin = new AtomicReference<>(null);
    private final AtomicReference<Live> live = new AtomicReference<>(null);

    @SuppressWarnings("squid:S2629")
    DittoClientImpl(final InternalConfiguration configuration) {
        this.configuration = configuration;

        configuration.getTwinBus().ifPresent(bus -> {
            init(bus, configuration.getTwinConfigurationOrFail(), configuration.getTwinMessagingProviderOrFail());
            final TwinImpl twinImpl = new TwinImpl(configuration);
            twin.set(twinImpl);
        });

        configuration.getLiveBus().ifPresent(bus -> {
            init(bus, configuration.getLiveConfigurationOrFail(), configuration.getLiveMessagingProviderOrFail());
            live.set(new LiveImpl(configuration));
        });

        LOGGER.info("Ditto Client [{}//{}] initialized successfully", VersionReader.determineClientVersion(),
                VersionReader.determineBuildTimeStamp());
    }

    /**
     * Creates a new {@link DittoClient Ditto Client} instance with Optional {@code Twin} AND {@code Live}
     * configuration.
     *
     * @param twinConfiguration the Twin CommonConfiguration to use for this client
     * @param liveConfiguration the Live CommonConfiguration to use for this client
     * @return the {@link DittoClient Ditto Client} instance with the configured Live configuration
     */
    public static DittoClient newInstance(final Optional<CommonConfiguration> twinConfiguration,
            final Optional<CommonConfiguration> liveConfiguration) {

        final InternalConfiguration cfg = new InternalConfiguration(twinConfiguration, liveConfiguration);

        return new DittoClientImpl(cfg);
    }

    @Override
    public Twin twin() {
        final Twin result = twin.get();
        if (null == result) {
            throw new IllegalStateException("Twin client is not configured.");
        }
        return result;
    }

    @Override
    public Live live() {
        final Live result = live.get();
        if (null == result) {
            throw new IllegalStateException("Live client is not configured.");
        }
        return result;
    }

    @Override
    public void destroy() {
        configuration.getTwinMessagingProvider().ifPresent(MessagingProvider::close);
        configuration.getLiveMessagingProvider().ifPresent(MessagingProvider::close);
        configuration.getTwinBus().ifPresent(PointerBus::close);
        configuration.getLiveBus().ifPresent(PointerBus::close);
    }

    @Override
    public CompletableFuture<Adaptable> sendDittoProtocol(final Adaptable dittoProtocolAdaptable) {

        final TopicPath.Channel channel = dittoProtocolAdaptable.getTopicPath().getChannel();
        switch (channel) {
            case TWIN:
                return configuration.getTwinMessagingProviderOrFail().sendAdaptable(dittoProtocolAdaptable);
            case LIVE:
                return configuration.getLiveMessagingProviderOrFail().sendAdaptable(dittoProtocolAdaptable);
            default:
                throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    private void init(final PointerBus bus, final CommonConfiguration commonConfiguration,
            final MessagingProvider messagingProvider) {

        registerKeyBasedDistributorForIncomingEvents(bus);
        registerKeyBasedHandlersForIncomingEvents(bus);
        messagingProvider.registerReplyHandler(configuration.getResponseForwarder()::handle);
        messagingProvider.initialize(commonConfiguration, bus.getExecutor());
    }

    private static void registerKeyBasedDistributorForIncomingEvents(final PointerBus bus) {
        bus.on(JsonPointerSelectors.jsonPointer(SELECTOR_INCOMING_MESSAGE), e -> {
            final Message<?> message = (Message<?>) e.getData();

            LOGGER.trace("Received Message: '{}'", message);

            final String subject = message.getSubject();
            final String thingId = message.getThingId();
            final MessageDirection direction = message.getDirection();

            // Direction is set to "TO" -> Message is put in the "inbox"
            // Direction is set to "FROM" -> Message is put in the "outbox"
            final String inboxOutbox = (direction == MessageDirection.TO) ? "inbox" : "outbox";

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
                event -> MessageFormat.format(THING_PATTERN, event.getThingId()),
                event -> new ImmutableThingChange(event.getThingId(), ChangeAction.CREATED, event.getThing(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingModified.TYPE, ThingModified.class,
                event -> MessageFormat.format(THING_PATTERN, event.getThingId()),
                event -> new ImmutableThingChange(event.getThingId(), ChangeAction.UPDATED, event.getThing(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, ThingDeleted.TYPE, ThingDeleted.class,
                event -> MessageFormat.format(THING_PATTERN, event.getThingId()),
                event -> new ImmutableThingChange(event.getThingId(), ChangeAction.DELETED, null,
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        /*
         * ACL - v1 only
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclModified.TYPE, AclModified.class,
                event -> MessageFormat.format("/things/{0}/acl", event.getThingId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        event.getAccessControlList().toJson(event.getImplementedSchemaVersion()),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclEntryCreated.TYPE, AclEntryCreated.class,
                event -> MessageFormat.format(ACL_PATTERN, event.getThingId(),
                        event.getAclEntry().getAuthorizationSubject().getId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        event.getAclEntry().toJson(event.getImplementedSchemaVersion()),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclEntryModified.TYPE, AclEntryModified.class,
                event -> MessageFormat.format(ACL_PATTERN, event.getThingId(),
                        event.getAclEntry().getAuthorizationSubject().getId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        event.getAclEntry().toJson(event.getImplementedSchemaVersion()),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AclEntryDeleted.TYPE, AclEntryDeleted.class,
                event -> MessageFormat.format(ACL_PATTERN, event.getThingId(),
                        event.getAuthorizationSubject().getId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        /*
         * Attributes
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesCreated.TYPE, AttributesCreated.class,
                event -> MessageFormat.format(ATTRIBUTES_PATTERN, event.getThingId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        event.getCreatedAttributes(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesModified.TYPE, AttributesModified.class,
                event -> MessageFormat.format(ATTRIBUTES_PATTERN, event.getThingId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        event.getModifiedAttributes(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributesDeleted.TYPE, AttributesDeleted.class,
                event -> MessageFormat.format(ATTRIBUTES_PATTERN, event.getThingId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        /*
         * Attribute
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeCreated.TYPE, AttributeCreated.class,
                event -> MessageFormat.format(ATTRIBUTE_PATTERN, event.getThingId(),
                        event.getAttributePointer()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.CREATED,
                        event.getAttributePointer(),
                        event.getAttributeValue(),
                        event.getRevision(), event.getTimestamp().orElse(null)));

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeModified.TYPE, AttributeModified.class,
                event -> MessageFormat.format(ATTRIBUTE_PATTERN, event.getThingId(),
                        event.getAttributePointer()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.UPDATED,
                        event.getAttributePointer(),
                        event.getAttributeValue(),
                        event.getRevision(), event.getTimestamp().orElse(null)));

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, AttributeDeleted.TYPE, AttributeDeleted.class,
                event -> MessageFormat.format(ATTRIBUTE_PATTERN, event.getThingId(),
                        event.getAttributePointer()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.DELETED,
                        event.getAttributePointer(),
                        null,
                        event.getRevision(), event.getTimestamp().orElse(null)));

        /*
         * Features
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesCreated.TYPE, FeaturesCreated.class,
                event -> MessageFormat.format(FEATURES_PATTERN, event.getThingId()),
                event -> new ImmutableFeaturesChange(event.getThingId(), ChangeAction.CREATED,
                        event.getFeatures(),
                        JsonPointer.empty(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesModified.TYPE, FeaturesModified.class,
                event -> MessageFormat.format(FEATURES_PATTERN, event.getThingId()),
                event -> new ImmutableFeaturesChange(event.getThingId(), ChangeAction.UPDATED,
                        event.getFeatures(),
                        JsonPointer.empty(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturesDeleted.TYPE, FeaturesDeleted.class,
                event -> MessageFormat.format(FEATURES_PATTERN, event.getThingId()),
                event -> new ImmutableFeaturesChange(event.getThingId(), ChangeAction.DELETED,
                        null,
                        JsonPointer.empty(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        /*
         * Feature
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureCreated.TYPE, FeatureCreated.class,
                event -> MessageFormat.format(FEATURE_PATTERN, event.getThingId(), event.getFeatureId()),
                event -> new ImmutableFeatureChange(event.getThingId(), ChangeAction.CREATED,
                        event.getFeature(),
                        JsonPointer.empty(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureModified.TYPE, FeatureModified.class,
                event -> MessageFormat.format(FEATURE_PATTERN, event.getThingId(), event.getFeatureId()),
                event -> new ImmutableFeatureChange(event.getThingId(), ChangeAction.UPDATED,
                        event.getFeature(),
                        JsonPointer.empty(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeatureDeleted.TYPE, FeatureDeleted.class,
                event -> MessageFormat.format(FEATURE_PATTERN, event.getThingId(), event.getFeatureId()),
                event -> new ImmutableFeatureChange(event.getThingId(), ChangeAction.DELETED,
                        null,
                        JsonPointer.empty(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        /*
         * Feature Properties
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesCreated.TYPE,
                FeaturePropertiesCreated.class,
                event -> MessageFormat.format(FEATURE_PROPERTIES_PATTERN, event.getThingId(),
                        event.getFeatureId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.CREATED,
                        JsonPointer.empty(),
                        event.getProperties().toJson(event.getImplementedSchemaVersion()),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesModified.TYPE,
                FeaturePropertiesModified.class,
                event -> MessageFormat.format(FEATURE_PROPERTIES_PATTERN, event.getThingId(),
                        event.getFeatureId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.UPDATED,
                        JsonPointer.empty(),
                        event.getProperties().toJson(event.getImplementedSchemaVersion()),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertiesDeleted.TYPE,
                FeaturePropertiesDeleted.class,
                event -> MessageFormat.format(FEATURE_PROPERTIES_PATTERN, event.getThingId(),
                        event.getFeatureId()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.DELETED,
                        JsonPointer.empty(),
                        null,
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        /*
         * Feature Property
         */
        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyCreated.TYPE,
                FeaturePropertyCreated.class,
                event -> MessageFormat.format(FEATURE_PROPERTY_PATTERN, event.getThingId(),
                        event.getFeatureId(), event.getPropertyPointer()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.CREATED,
                        event.getPropertyPointer(),
                        event.getPropertyValue(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyModified.TYPE,
                FeaturePropertyModified.class,
                event -> MessageFormat.format(FEATURE_PROPERTY_PATTERN, event.getThingId(),
                        event.getFeatureId(), event.getPropertyPointer()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.UPDATED,
                        event.getPropertyPointer(),
                        event.getPropertyValue(),
                        event.getRevision(), event.getTimestamp().orElse(null))
        );

        SelectorUtil.addHandlerForThingEvent(LOGGER, bus, FeaturePropertyDeleted.TYPE,
                FeaturePropertyDeleted.class,
                event -> MessageFormat.format(FEATURE_PROPERTY_PATTERN, event.getThingId(),
                        event.getFeatureId(), event.getPropertyPointer()),
                event -> new ImmutableChange(event.getThingId(), ChangeAction.DELETED,
                        event.getPropertyPointer(),
                        null,
                        event.getRevision(), event.getTimestamp().orElse(null))
        );
    }

}
