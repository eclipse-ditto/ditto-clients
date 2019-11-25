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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.ditto.client.live.messages.MessageSerializationException;
import org.eclipse.ditto.client.live.messages.MessageSerializer;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.MessageSerializers;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.internal.OptionsEvaluator;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersBuilder;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTagMatcher;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTagMatchers;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageBuilder;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeature;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeature;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThing;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeature;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThing;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates outgoing ThingCommands sent from the client.
 *
 * @since 1.0.0
 */
public final class OutgoingMessageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutgoingMessageFactory.class);

    private static final EntityTagMatchers ASTERISK =
            EntityTagMatchers.fromList(Collections.singletonList(EntityTagMatcher.asterisk()));

    private final JsonSchemaVersion jsonSchemaVersion;
    private final String sessionId;

    private OutgoingMessageFactory(final JsonSchemaVersion jsonSchemaVersion, final String sessionId) {
        this.jsonSchemaVersion = jsonSchemaVersion;
        this.sessionId = sessionId;
    }

    /**
     * Creates a new {@code OutgoingMessageFactory}.
     *
     * @param jsonSchemaVersion the version in which messages should be created by this factory.
     * @param sessionId the session id of the client.
     * @return the factory.
     * @throws NullPointerException if {@code configuration} is {@code null}.
     */
    public static OutgoingMessageFactory newInstance(final JsonSchemaVersion jsonSchemaVersion,
            final String sessionId) {
        checkNotNull(jsonSchemaVersion, "jsonSchemaVersion");
        checkNotNull(sessionId, "sessionId");
        return new OutgoingMessageFactory(jsonSchemaVersion, sessionId);
    }

    public ThingCommand createThing(final Thing thing, final Option<?>... options) {
        logWarningsForAclPolicyUsage(thing);
        return CreateThing.of(thing, null, buildDittoHeaders(false, options));
    }

    /**
     * @param thing the thing to be put (which may be created or updated).
     * @param options options to be applied configuring behaviour of this method.
     * @return the ThingCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code thing} has no identifier.
     */
    public ThingCommand putThing(final Thing thing, final Option<?>... options) {
        checkNotNull(thing, "thing");
        final ThingId thingId = thing.getEntityId().orElseThrow(() -> new IllegalArgumentException("Thing had no ID!"));

        logWarningsForAclPolicyUsage(thing);

        final DittoHeaders headers = buildDittoHeaders(true, options);
        return ModifyThing.of(thingId, thing, null, headers);
    }

    /**
     * @param thing the thing to be updated.
     * @param options options to be applied configuring behaviour of this method.
     * @return the ThingCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code thing} has no identifier.
     * @throws UnsupportedOperationException if an invalid option has been specified.
     */
    public ThingCommand updateThing(final Thing thing, final Option<?>... options) {
        checkNotNull(thing, "thing");
        final ThingId thingId = thing.getEntityId().orElseThrow(() -> new IllegalArgumentException("Thing had no ID!"));

        logWarningsForAclPolicyUsage(thing);

        final DittoHeaders headersWithoutIfMatch = buildDittoHeaders(false, options);
        final DittoHeaders headers = headersWithoutIfMatch.toBuilder()
                .ifMatch(ASTERISK)
                .build();
        return ModifyThing.of(thingId, thing, null, headers);
    }

    private void logWarningsForAclPolicyUsage(final Thing thing) {
        if (jsonSchemaVersion == JsonSchemaVersion.V_1 && thing.getPolicyEntityId().isPresent()) {
            LOGGER.warn("Creating/modifying a Thing with a defined 'policyId' when client was configured to use " +
                    "Ditto Protocol in 'schemaVersion' 1 (which is ACL based). That will most likely result in " +
                    "unexpected behavior.");
        }
        if (jsonSchemaVersion == JsonSchemaVersion.V_2 && thing.getAccessControlList().isPresent()) {
            LOGGER.warn("Creating/modifying a Thing with a defined 'acl' when client was configured to use " +
                    "Ditto Protocol in 'schemaVersion' 2 (which is policy based). That will most likely result in " +
                    "unexpected behavior.");
        }
    }

    public ThingCommand retrieveThing(final CharSequence thingId) {
        return RetrieveThing.of(ThingId.of(thingId), buildDittoHeaders(false));
    }

    public ThingCommand retrieveThing(final CharSequence thingId, final Iterable<JsonPointer> fields) {
        return RetrieveThing.getBuilder(ThingId.of(thingId), buildDittoHeaders(false))
                .withSelectedFields(JsonFactory.newFieldSelector(fields))
                .build();
    }

    public ThingCommand retrieveThings(final Iterable<ThingId> thingIds) {
        return RetrieveThings.getBuilder(makeList(thingIds))
                .dittoHeaders(buildDittoHeaders(false))
                .build();
    }

    private static <E> List<E> makeList(final Iterable<E> iter) {
        final List<E> list = new ArrayList<>();
        for (final E item : iter) {
            list.add(item);
        }
        return list;
    }

    public ThingCommand retrieveThings(final Iterable<ThingId> thingIds, final Iterable<JsonPointer> fields) {
        return RetrieveThings.getBuilder(makeList(thingIds))
                .selectedFields(JsonFactory.newFieldSelector(fields))
                .dittoHeaders(buildDittoHeaders(false))
                .build();
    }

    public ThingCommand deleteThing(final ThingId thingId, final Option<?>... options) {
        return DeleteThing.of(thingId, buildDittoHeaders(false, options));
    }

    public ThingCommand setAttribute(final ThingId thingId,
            final JsonPointer path,
            final JsonValue value,
            final Option<?>... options) {

        return ModifyAttribute.of(thingId, path, value, buildDittoHeaders(true, options));
    }

    public ThingCommand setAttributes(final ThingId thingId, final JsonObject attributes, final Option<?>... options) {
        return ModifyAttributes.of(thingId, ThingsModelFactory.newAttributes(attributes),
                buildDittoHeaders(true, options));
    }

    public ThingCommand deleteAttribute(final ThingId thingId, final JsonPointer path, final Option<?>... options) {
        return DeleteAttribute.of(thingId, path, buildDittoHeaders(false, options));
    }

    public ThingCommand deleteAttributes(final ThingId thingId, final Option<?>... options) {
        return DeleteAttributes.of(thingId, buildDittoHeaders(false, options));
    }

    public ThingCommand setFeature(final ThingId thingId, final Feature feature, final Option<?>... options) {
        return ModifyFeature.of(thingId, feature, buildDittoHeaders(true, options));
    }

    public ThingCommand setFeatures(final ThingId thingId, final Features features, final Option<?>... options) {
        return ModifyFeatures.of(thingId, features, buildDittoHeaders(true, options));
    }

    public ThingCommand retrieveFeature(final ThingId thingId, final String featureId, final Option<?>... options) {
        return RetrieveFeature.of(thingId, featureId, buildDittoHeaders(false, options));
    }

    public ThingCommand retrieveFeature(final ThingId thingId,
            final String featureId,
            final Iterable<JsonPointer> fields,
            final Option<?>... options) {

        return RetrieveFeature.of(thingId, featureId, JsonFactory.newFieldSelector(fields),
                buildDittoHeaders(false, options));
    }

    public ThingCommand deleteFeature(final ThingId thingId, final String featureId, final Option<?>... options) {
        return DeleteFeature.of(thingId, featureId, buildDittoHeaders(false, options));
    }

    public ThingCommand deleteFeatures(final ThingId thingId, final Option<?>... options) {
        return DeleteFeatures.of(thingId, buildDittoHeaders(false, options));
    }

    /**
     * Creates a new {@link ModifyFeatureDefinition} object.
     *
     * @param thingId ID of the thing to which the feature belongs to.
     * @param featureId ID of the feature to set the definition for.
     * @param featureDefinition the FeatureDefinition to be set.
     * @param options options to be applied configuring behaviour of this method, see {@link Option}s.
     * @return the command object.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public ThingCommand<ModifyFeatureDefinition> setFeatureDefinition(final ThingId thingId,
            final String featureId,
            final FeatureDefinition featureDefinition,
            final Option<?>... options) {

        return ModifyFeatureDefinition.of(thingId, featureId, featureDefinition, buildDittoHeaders(true, options));
    }

    /**
     * Creates a new {@link DeleteFeatureDefinition} object.
     *
     * @param thingId ID of the thing to which the feature belongs to.
     * @param featureId ID of the feature to delete the definition from.
     * @param options options to be applied configuring behaviour of this method, see {@link Option}s.
     * @return the command object.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public ThingCommand<DeleteFeatureDefinition> deleteFeatureDefinition(final ThingId thingId, final String featureId,
            final Option<?>... options) {

        return DeleteFeatureDefinition.of(thingId, featureId, buildDittoHeaders(false, options));
    }

    public ThingCommand setFeatureProperty(final ThingId thingId,
            final String featureId,
            final JsonPointer path,
            final JsonValue value,
            final Option<?>... options) {

        return ModifyFeatureProperty.of(thingId, featureId, path, value, buildDittoHeaders(true, options));
    }

    public ThingCommand setFeatureProperties(final ThingId thingId,
            final String featureId,
            final JsonObject properties,
            final Option<?>... options) {

        return ModifyFeatureProperties.of(thingId, featureId, ThingsModelFactory.newFeatureProperties(properties),
                buildDittoHeaders(true, options));
    }

    public ThingCommand deleteFeatureProperty(final ThingId thingId,
            final String featureId,
            final JsonPointer path,
            final Option<?>... options) {

        return DeleteFeatureProperty.of(thingId, featureId, path, buildDittoHeaders(false, options));
    }

    public ThingCommand deleteFeatureProperties(final ThingId thingId, final String featureId,
            final Option<?>... options) {

        return DeleteFeatureProperties.of(thingId, featureId, buildDittoHeaders(false, options));
    }

    /**
     * Builds a Message.
     *
     * @param registry the MessageSerializerRegistry to lookup MessageSerializers in.
     * @param message message body as Message.
     * @param <T> the type of the payload.
     * @return a sendMessage message.
     */
    public <T> Message<T> sendMessage(final MessageSerializerRegistry registry, final Message<T> message) {

        final MessageHeaders messageHeaders = message.getHeaders().toBuilder()
                .correlationId(message.getHeaders().getCorrelationId().orElseGet(() -> UUID.randomUUID().toString()))
                .build();

        final MessageBuilder<T> messageBuilder = message.getPayload()
                .map(payload -> {
                    final Class<T> payloadType = (Class<T>) payload.getClass();
                    final String subject = message.getSubject();
                    final Optional<String> msgContentType = message.getContentType();

                    final MessageBuilder<T> builder;
                    // if no content-type was explicitly set, but a payload
                    if (!msgContentType.isPresent()) {
                        // find out the content-type by the payload java-type:
                        final String implicitContentType = registry.findKeyFor(payloadType, subject)
                                .orElseThrow(
                                        () -> new MessageSerializationException(
                                                "No content-type could be determined for payload of type '" +
                                                        payloadType + "'. "
                                                        +
                                                        "Ensure that a a MessageSerializer for that payload-type is registered"))
                                .getContentType();

                        final MessageHeaders adjustedHeaders =
                                messageHeaders.toBuilder().contentType(implicitContentType).build();
                        builder = MessagesModelFactory.newMessageBuilder(adjustedHeaders);
                    } else {
                        builder = MessagesModelFactory.newMessageBuilder(messageHeaders);
                    }
                    message.getPayload().ifPresent(builder::payload);

                    // if a content-type was explicitly set
                    final Optional<MessageSerializer<T>> contentTypeSerializer = msgContentType
                            // try to find a Serializer for that content-type (in combination with java-type and optional subject)
                            .map(contentType -> registry
                                    .findSerializerFor(contentType, payloadType, subject)
                                    .orElseThrow(() -> new MessageSerializationException(
                                            "No serializer " + "found for content-type '" + contentType + "'" +
                                                    " and payload-type '"
                                                    + payloadType + "'")));

                    final Optional<Charset> charsetOfContentType =
                            MessageSerializers.determineCharsetFromContentType(msgContentType);

                    // if no content-type was set try to "find out" the content-type by only using the Java-Type:
                    final MessageSerializer<T> messageSerializer = contentTypeSerializer
                            .orElseGet(() -> registry
                                    .findSerializerFor(payloadType, subject)
                                    .orElseGet(() -> registry.findSerializerFor(payloadType).orElseThrow(
                                            () -> new MessageSerializationException(
                                                    "No serializer found for payload type '" + payload.getClass() +
                                                            "'"))));
                    builder.rawPayload(messageSerializer.getSerializer()
                            .apply(payload, charsetOfContentType.orElse(StandardCharsets.UTF_8)));
                    return builder;
                }).orElseGet(() -> MessagesModelFactory.newMessageBuilder(messageHeaders));

        message.getResponseConsumer().ifPresent(messageBuilder::responseConsumer);
        return messageBuilder.build();
    }

    private DittoHeaders buildDittoHeaders(final boolean allowExists, final Option<?>... options) {
        final OptionsEvaluator.Modify modify = OptionsEvaluator.forModifyOptions(options);

        final DittoHeadersBuilder headersBuilder = DittoHeaders.newBuilder()
                .correlationId(UUID.randomUUID().toString())
                .schemaVersion(jsonSchemaVersion)
                .responseRequired(modify.isResponseRequired().orElse(true));
        modify.exists().ifPresent(exists -> {
            if (!allowExists) {
                throw new IllegalArgumentException("Option \"exists\" is not allowed for this operation.");
            }
            if (exists) {
                headersBuilder.ifMatch(ASTERISK);
            } else {
                headersBuilder.ifNoneMatch(ASTERISK);
            }
        });

        return headersBuilder.build();
    }

}
