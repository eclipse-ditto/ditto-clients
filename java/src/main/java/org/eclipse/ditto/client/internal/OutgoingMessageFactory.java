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

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;
import static org.eclipse.ditto.client.options.OptionName.Modify.CONDITION;
import static org.eclipse.ditto.client.options.OptionName.Modify.EXISTS;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.headers.DittoHeadersBuilder;
import org.eclipse.ditto.base.model.headers.entitytag.EntityTagMatcher;
import org.eclipse.ditto.base.model.headers.entitytag.EntityTagMatchers;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.live.messages.MessageSerializationException;
import org.eclipse.ditto.client.live.messages.MessageSerializer;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.MessageSerializers;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.client.options.internal.OptionsEvaluator;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.messages.model.MessageBuilder;
import org.eclipse.ditto.messages.model.MessageHeaders;
import org.eclipse.ditto.messages.model.MessagesModelFactory;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.policies.model.PolicyIdInvalidException;
import org.eclipse.ditto.policies.model.signals.commands.modify.CreatePolicy;
import org.eclipse.ditto.policies.model.signals.commands.modify.DeletePolicy;
import org.eclipse.ditto.policies.model.signals.commands.modify.ModifyPolicy;
import org.eclipse.ditto.policies.model.signals.commands.query.RetrievePolicy;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.FeatureDefinition;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.things.model.signals.commands.modify.CreateThing;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteThing;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyPolicyId;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeature;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThings;

/**
 * Creates outgoing Commands sent from the client.
 *
 * @since 1.0.0
 */
public final class OutgoingMessageFactory {

    private static final String ARGUMENT_THING = "thing";
    private static final EntityTagMatchers ASTERISK =
            EntityTagMatchers.fromList(Collections.singletonList(EntityTagMatcher.asterisk()));

    private final JsonSchemaVersion jsonSchemaVersion;

    private OutgoingMessageFactory(final JsonSchemaVersion jsonSchemaVersion) {
        this.jsonSchemaVersion = jsonSchemaVersion;
    }

    /**
     * Creates a new {@code OutgoingMessageFactory}.
     *
     * @param jsonSchemaVersion the version in which messages should be created by this factory.
     * @return the factory.
     * @throws NullPointerException if {@code configuration} is {@code null}.
     */
    public static OutgoingMessageFactory newInstance(final JsonSchemaVersion jsonSchemaVersion) {
        checkNotNull(jsonSchemaVersion, "jsonSchemaVersion");
        return new OutgoingMessageFactory(jsonSchemaVersion);
    }

    /**
     * @param thing the thing to be created.
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy.
     * @param options options to be applied configuring behaviour of this method.
     * @return the ThingCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code thing} has no identifier.
     */
    public CreateThing createThing(final Thing thing, @Nullable JsonObject initialPolicy, final Option<?>... options) {
        validateOptions(initialPolicy, options);

        final DittoHeaders dittoHeaders = buildDittoHeaders(Collections.emptySet(), options);

        return getPolicyIdOrPlaceholder(options)
                .map(policyIdOrPlaceHolder -> CreateThing.withCopiedPolicy(thing, policyIdOrPlaceHolder, dittoHeaders))
                .orElseGet(() -> CreateThing.of(thing, initialPolicy, dittoHeaders));
    }

    /**
     * @param thing the thing to be put (which may be created or updated).
     * @param initialPolicy a custom policy to use for the Thing instead of the default Policy. This will only apply if
     * the Thing does not already exist.
     * @param options options to be applied configuring behaviour of this method.
     * @return the ThingCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code thing} has no identifier.
     */
    public ModifyThing putThing(final Thing thing,
            @Nullable final JsonObject initialPolicy,
            final Option<?>... options) {

        checkNotNull(thing, ARGUMENT_THING);
        final ThingId thingId = thing.getEntityId().orElseThrow(() -> new IllegalArgumentException("Thing had no ID!"));

        validateOptions(initialPolicy, options);

        final DittoHeaders dittoHeaders = buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options);

        final Optional<String> optionalPolicyIdOrPlaceHolder = getPolicyIdOrPlaceholder(options);

        return optionalPolicyIdOrPlaceHolder
                .map(policyIdOrPlaceHolder -> ModifyThing.withCopiedPolicy(thingId, thing, policyIdOrPlaceHolder,
                        dittoHeaders))
                .orElseGet(() -> ModifyThing.of(thingId, thing, initialPolicy, dittoHeaders));
    }

    /**
     * @param thing the thing to be updated.
     * @param options options to be applied configuring behaviour of this method.
     * @return the ThingCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code thing} has no identifier.
     * @throws UnsupportedOperationException if an invalid option has been specified.
     */
    public ModifyThing updateThing(final Thing thing, final Option<?>... options) {
        checkNotNull(thing, ARGUMENT_THING);
        final ThingId thingId = thing.getEntityId().orElseThrow(() -> new IllegalArgumentException("Thing had no ID!"));

        final DittoHeaders headersWithoutIfMatch = buildDittoHeaders(EnumSet.of(CONDITION), options);
        final DittoHeaders headers = headersWithoutIfMatch.toBuilder()
                .ifMatch(ASTERISK)
                .build();

        return ModifyThing.of(thingId, thing, null, headers);
    }

    /**
     * @param thingId the thing to be merged.
     * @param thing which should be used for merged.
     * @param options options to be applied configuring behaviour of this method.
     * @return the ThingCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code thing} has no identifier.
     * @throws UnsupportedOperationException if an invalid option has been specified.
     */
    MergeThing mergeThing(final ThingId thingId, final Thing thing, final Option<?>[] options) {
        checkNotNull(thing, ARGUMENT_THING);

        final DittoHeaders headers = DittoHeaders.newBuilder(buildDittoHeaders(EnumSet.of(CONDITION), options))
                .ifMatch(ASTERISK)
                .build();

        return MergeThing.withThing(thingId, thing, headers);
    }

    public RetrieveThing retrieveThing(final CharSequence thingId) {
        return RetrieveThing.of(ThingId.of(thingId), buildDittoHeaders(EnumSet.of(CONDITION)));
    }

    public RetrieveThing retrieveThing(final CharSequence thingId, final Iterable<JsonPointer> fields) {
        return RetrieveThing.getBuilder(ThingId.of(thingId), buildDittoHeaders(EnumSet.of(CONDITION)))
                .withSelectedFields(JsonFactory.newFieldSelector(fields))
                .build();
    }

    public RetrieveThings retrieveThings(final Iterable<ThingId> thingIds) {
        return RetrieveThings.getBuilder(makeList(thingIds))
                .dittoHeaders(buildDittoHeaders(Collections.emptySet()))
                .build();
    }

    private static <E> List<E> makeList(final Iterable<E> iter) {
        final List<E> list = new ArrayList<>();
        for (final E item : iter) {
            list.add(item);
        }
        return list;
    }

    public RetrieveThings retrieveThings(final Iterable<ThingId> thingIds, final Iterable<JsonPointer> fields) {
        return RetrieveThings.getBuilder(makeList(thingIds))
                .selectedFields(JsonFactory.newFieldSelector(fields))
                .dittoHeaders(buildDittoHeaders(Collections.emptySet()))
                .build();
    }

    public DeleteThing deleteThing(final ThingId thingId, final Option<?>... options) {
        return DeleteThing.of(thingId, buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    /**
     * Build a command for creating a Policy.
     *
     * @param policy the policy to create.
     * @param options options to be applied configuring behaviour of this method.
     * @return The {@link CreatePolicy} command.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws PolicyIdInvalidException if the {@link Policy}'s ID is not valid.
     * @since 1.1.0
     */
    public CreatePolicy createPolicy(final Policy policy, final Option<?>... options) {
        return CreatePolicy.of(policy, buildDittoHeaders(Collections.emptySet(), options));
    }

    /**
     * @param policy the policy to be put (which may be created or updated).
     * @param options options to be applied configuring behaviour of this method.
     * @return the PolicyCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code policy} has no identifier.
     * @since 1.1.0
     */
    public ModifyPolicy putPolicy(final Policy policy, final Option<?>... options) {
        checkNotNull(policy, "policy");
        final PolicyId policyId =
                policy.getEntityId().orElseThrow(() -> new IllegalArgumentException("Policy had no ID!"));

        final DittoHeaders headers = buildDittoHeaders(EnumSet.of(EXISTS), options);
        return ModifyPolicy.of(policyId, policy, headers);
    }

    /**
     * @param policy the policy to be updated.
     * @param options options to be applied configuring behaviour of this method.
     * @return the PolicyCommand
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code policy} has no identifier.
     * @since 1.1.0
     */
    public ModifyPolicy updatePolicy(final Policy policy, final Option<?>... options) {
        checkNotNull(policy, "policy");
        final PolicyId policyId =
                policy.getEntityId().orElseThrow(() -> new IllegalArgumentException("Policy had no ID!"));

        final DittoHeaders headers = DittoHeaders.newBuilder(buildDittoHeaders(Collections.emptySet(), options))
                .ifMatch(ASTERISK)
                .build();
        return ModifyPolicy.of(policyId, policy, headers);
    }

    /**
     * Builds a command to retrieve the policy with ID {@code policyId}.
     *
     * @param policyId the policy to retrieve.
     * @return the {@link RetrievePolicy} command.
     * @throws NullPointerException if the policyId is {@code null}.
     * @since 1.1.0
     */
    public RetrievePolicy retrievePolicy(final PolicyId policyId) {
        return RetrievePolicy.of(policyId, buildDittoHeaders(Collections.emptySet()));
    }

    /**
     * Builds a command to delete the policy with ID {@code policyId}.
     *
     * @param policyId the policy to delete.
     * @param options options to be applied configuring behaviour of this method.
     * @return the {@link DeletePolicy} command.
     * @throws NullPointerException if the policyId is {@code null}.
     * @since 1.1.0
     */
    public DeletePolicy deletePolicy(final PolicyId policyId, final Option<?>... options) {
        return DeletePolicy.of(policyId, buildDittoHeaders(Collections.emptySet(), options));
    }

    public ModifyAttribute setAttribute(final ThingId thingId,
            final JsonPointer path,
            final JsonValue value,
            final Option<?>... options) {

        return ModifyAttribute.of(thingId, path, value, buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public MergeThing mergeAttribute(final ThingId thingId,
            final JsonPointer path,
            final JsonValue value,
            final Option<?>... options) {

        return MergeThing.withAttribute(thingId,
                path,
                value,
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public ModifyAttributes setAttributes(final ThingId thingId,
            final JsonObject attributes,
            final Option<?>... options) {

        return ModifyAttributes.of(thingId,
                ThingsModelFactory.newAttributes(attributes),
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public MergeThing mergeAttributes(final ThingId thingId, final JsonObject attributes, final Option<?>[] options) {
        return MergeThing.withAttributes(thingId,
                ThingsModelFactory.newAttributes(attributes),
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public DeleteAttribute deleteAttribute(final ThingId thingId, final JsonPointer path, final Option<?>... options) {
        return DeleteAttribute.of(thingId, path, buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    public DeleteAttributes deleteAttributes(final ThingId thingId, final Option<?>... options) {
        return DeleteAttributes.of(thingId, buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    public ModifyFeature setFeature(final ThingId thingId, final Feature feature, final Option<?>... options) {
        return ModifyFeature.of(thingId, feature, buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public MergeThing mergeFeature(final ThingId thingId, final Feature feature, final Option<?>... options) {
        return MergeThing.withFeature(thingId, feature, buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public ModifyFeatures setFeatures(final ThingId thingId, final Features features, final Option<?>... options) {
        return ModifyFeatures.of(thingId, features, buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public MergeThing mergeFeatures(final ThingId thingId, final Features features, final Option<?>[] options) {
        return MergeThing.withFeatures(thingId, features, buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public ModifyPolicyId setPolicyId(final ThingId thingId, final PolicyId policyId, final Option<?>... options) {
        return ModifyPolicyId.of(thingId, policyId, buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public MergeThing mergePolicyId(final ThingId thingId, final PolicyId policyId, final Option<?>... options) {
        return MergeThing.withPolicyId(thingId, policyId, buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public RetrieveFeature retrieveFeature(final ThingId thingId, final String featureId, final Option<?>... options) {
        return RetrieveFeature.of(thingId, featureId, buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    public RetrieveFeature retrieveFeature(final ThingId thingId,
            final String featureId,
            final Iterable<JsonPointer> fields,
            final Option<?>... options) {

        return RetrieveFeature.of(thingId,
                featureId,
                JsonFactory.newFieldSelector(fields),
                buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    public DeleteFeature deleteFeature(final ThingId thingId, final String featureId, final Option<?>... options) {
        return DeleteFeature.of(thingId, featureId, buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    public DeleteFeatures deleteFeatures(final ThingId thingId, final Option<?>... options) {
        return DeleteFeatures.of(thingId, buildDittoHeaders(EnumSet.of(CONDITION), options));
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
    public ModifyFeatureDefinition setFeatureDefinition(final ThingId thingId,
            final String featureId,
            final FeatureDefinition featureDefinition,
            final Option<?>... options) {

        return ModifyFeatureDefinition.of(thingId,
                featureId,
                featureDefinition,
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    /**
     * Creates a new {@link MergeThing} object to merge FeatureDefinition.
     *
     * @param thingId ID of the thing to which the feature belongs to.
     * @param featureId ID of the feature to set the definition for.
     * @param featureDefinition the FeatureDefinition to be merged.
     * @param options options to be applied configuring behaviour of this method, see {@link Option}s.
     * @return the command object.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public MergeThing mergeFeatureDefinition(final ThingId thingId,
            final String featureId,
            final FeatureDefinition featureDefinition,
            final Option<?>... options) {

        return MergeThing.withFeatureDefinition(thingId,
                featureId,
                featureDefinition,
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
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
    public DeleteFeatureDefinition deleteFeatureDefinition(final ThingId thingId,
            final String featureId,
            final Option<?>... options) {

        return DeleteFeatureDefinition.of(thingId, featureId, buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    public ModifyFeatureProperty setFeatureProperty(final ThingId thingId,
            final String featureId,
            final JsonPointer path,
            final JsonValue value,
            final Option<?>... options) {

        return ModifyFeatureProperty.of(thingId,
                featureId,
                path,
                value,
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public MergeThing mergeFeatureProperty(final ThingId thingId,
            final String featureId,
            final JsonPointer path,
            final JsonValue value,
            final Option<?>... options) {

        return MergeThing.withFeatureProperty(thingId,
                featureId,
                path,
                value,
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public ModifyFeatureProperties setFeatureProperties(final ThingId thingId,
            final String featureId,
            final JsonObject properties,
            final Option<?>... options) {

        return ModifyFeatureProperties.of(thingId,
                featureId,
                ThingsModelFactory.newFeatureProperties(properties),
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public MergeThing mergeFeatureProperties(final ThingId thingId,
            final String featureId,
            final JsonObject properties,
            final Option<?>... options) {

        return MergeThing.withFeatureProperties(thingId,
                featureId,
                ThingsModelFactory.newFeatureProperties(properties),
                buildDittoHeaders(EnumSet.of(EXISTS, CONDITION), options));
    }

    public DeleteFeatureProperty deleteFeatureProperty(final ThingId thingId,
            final String featureId,
            final JsonPointer path,
            final Option<?>... options) {

        return DeleteFeatureProperty.of(thingId, featureId, path, buildDittoHeaders(EnumSet.of(CONDITION), options));
    }

    public DeleteFeatureProperties deleteFeatureProperties(final ThingId thingId,
            final String featureId,
            final Option<?>... options) {

        return DeleteFeatureProperties.of(thingId, featureId, buildDittoHeaders(EnumSet.of(CONDITION), options));
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
                    message.getExtra().ifPresent(builder::extra);

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

        return messageBuilder.build();
    }

    private DittoHeaders buildDittoHeaders(final Collection<? extends OptionName> allowedOptions,
            final Option<?>... options) {

        final OptionsEvaluator.Global global = OptionsEvaluator.forGlobalOptions(options);
        final OptionsEvaluator.Modify modify = OptionsEvaluator.forModifyOptions(options);

        final DittoHeaders additionalHeaders = global.getDittoHeaders().orElseGet(DittoHeaders::empty);
        final DittoHeadersBuilder<?, ?> headersBuilder = DittoHeaders.newBuilder(additionalHeaders)
                .correlationId(additionalHeaders.getCorrelationId()
                        .orElseGet(() -> UUID.randomUUID().toString()))
                .schemaVersion(jsonSchemaVersion)
                .responseRequired(modify.isResponseRequired().orElse(true));

        modify.exists().ifPresent(exists -> {
            validateIfOptionIsAllowed(EXISTS, allowedOptions);
            if (Boolean.TRUE.equals(exists)) {
                headersBuilder.ifMatch(ASTERISK);
            } else {
                headersBuilder.ifNoneMatch(ASTERISK);
            }
        });
        modify.condition().ifPresent(condition -> {
            validateIfOptionIsAllowed(CONDITION, allowedOptions);
            headersBuilder.condition(condition);
        });

        return headersBuilder.build();
    }

    private static void validateIfOptionIsAllowed(final OptionName option,
            final Collection<? extends OptionName> allowedOptions) {

        if (!allowedOptions.contains(option)) {
            final String pattern = "Option ''{0}'' is not allowed for this operation.";
            final String lowerCaseOptionName = option.toString().toLowerCase(Locale.ENGLISH);
            throw new IllegalArgumentException(MessageFormat.format(pattern, lowerCaseOptionName));
        }
    }

    /**
     * Validates the options together with the initial policy and throws an exception if something isn't valid.
     *
     * @param initialPolicy The initial policy.
     * @param options The options to validate.
     * @throws IllegalArgumentException when the options aren't valid.
     */
    private void validateOptions(@Nullable final JsonObject initialPolicy, final Option<?>... options) {
        final OptionsEvaluator.Modify optionsEvaluator = OptionsEvaluator.forModifyOptions(options);
        final boolean isCopyPolicy = optionsEvaluator.copyPolicy().isPresent();
        final boolean isCopyPolicyFromThing = optionsEvaluator.copyPolicyFromThingId().isPresent();

        if (isCopyPolicy && isCopyPolicyFromThing) {
            throw new IllegalArgumentException(
                    "It is not allowed to set option \"COPY_POLICY\" and \"COPY_POLICY_FROM_THING\" at the same time");
        } else if (null != initialPolicy && (isCopyPolicy || isCopyPolicyFromThing)) {
            throw new IllegalArgumentException(
                    "It is not allowed to set option \"COPY_POLICY\" or \"COPY_POLICY_FROM_THING\" and a initialPolicy at the same time");
        }
    }

    private Optional<String> getPolicyIdOrPlaceholder(final Option<?>... options) {
        final OptionsEvaluator.Modify optionsEvaluator = OptionsEvaluator.forModifyOptions(options);
        final Optional<String> copyPolicy = optionsEvaluator.copyPolicy().map(PolicyId::toString);

        return ifPresentOrElse(copyPolicy,
                () -> optionsEvaluator.copyPolicyFromThingId()
                        .map(ThingId::toString)
                        .map(thingId -> "{{ ref:things/" + thingId + "/policyId }}"));
    }

    private static <T> Optional<T> ifPresentOrElse(final Optional<T> optional, final Supplier<Optional<T>> otherwise) {
        if (optional.isPresent()) {
            return optional;
        }
        return otherwise.get();
    }

}
