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
package org.eclipse.ditto.client.management.internal;

import static org.eclipse.ditto.base.model.common.ConditionChecker.argumentNotNull;
import static org.eclipse.ditto.base.model.common.ConditionChecker.checkArgument;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.eclipse.ditto.base.model.common.ConditionChecker;
import org.eclipse.ditto.base.model.signals.commands.CommandResponse;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.FeatureChange;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeatureChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeaturesChange;
import org.eclipse.ditto.client.changes.internal.ImmutableThingChange;
import org.eclipse.ditto.client.internal.AbstractHandle;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.management.FeatureHandle;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteThing;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyPolicyId;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link ThingHandle}.
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Things}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public abstract class ThingHandleImpl<T extends ThingHandle<F>, F extends FeatureHandle>
        extends AbstractHandle
        implements ThingHandle<F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingHandleImpl.class);

    protected final OutgoingMessageFactory outgoingMessageFactory;

    private final ThingId thingId;
    private final HandlerRegistry<T, F> handlerRegistry;

    protected ThingHandleImpl(
            final TopicPath.Channel channel,
            final ThingId thingId,
            final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<T, F> handlerRegistry) {
        super(messagingProvider, channel);
        this.thingId = thingId;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Returns the MessagingProvider this ThingHandle uses.
     *
     * @return the MessagingProvider this ThingHandle uses.
     */
    protected MessagingProvider getMessagingProvider() {
        return messagingProvider;
    }

    /**
     * Returns the OutgoingMessageFactory this ThingHandle uses.
     *
     * @return the OutgoingMessageFactory this ThingHandle uses.
     */
    protected OutgoingMessageFactory getOutgoingMessageFactory() {
        return outgoingMessageFactory;
    }

    /**
     * Returns the HandlerRegistry this ThingHandle uses.
     *
     * @return the HandlerRegistry this ThingHandle uses.
     */
    protected HandlerRegistry<T, F> getHandlerRegistry() {
        return handlerRegistry;
    }

    @Override
    public ThingId getEntityId() {
        return thingId;
    }

    @Override
    public F forFeature(final String featureId) {
        argumentNotNull(featureId);
        return handlerRegistry.featureHandleForFeatureId(thingId, featureId, () ->
                createFeatureHandle(thingId, featureId));
    }

    /**
     * Creates a {@link FeatureHandle} for the given thing and feature ids.
     *
     * @param thingId the thing id
     * @param featureId the feature id
     * @return the feature handle
     */
    protected abstract F createFeatureHandle(final ThingId thingId, final String featureId);


    @Override
    public CompletionStage<Void> delete(final Option<?>[] options) {
        final DeleteThing command = outgoingMessageFactory.deleteThing(thingId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> putAttribute(final JsonPointer path, final boolean value,
            final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putAttribute(final JsonPointer path, final double value,
            final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putAttribute(final JsonPointer path, final int value, final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putAttribute(final JsonPointer path, final long value, final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putAttribute(final JsonPointer path, final JsonValue value,
            final Option<?>... options) {
        argumentNotNull(path);
        argumentNotNull(value);
        checkArgument(path, p -> !p.isEmpty(), () -> "The path is not allowed to be empty! " +
                "If you want to update the whole attributes object, please use the setAttributes(JsonObject) method.");

        final ModifyAttribute command = outgoingMessageFactory.setAttribute(thingId, path, value, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergeAttribute(final JsonPointer path, final JsonValue value,
            final Option<?>... options) {
        argumentNotNull(path);
        argumentNotNull(value);
        checkArgument(path, p -> !p.isEmpty(), () -> "The path is not allowed to be empty! " +
                "If you want to merge the whole attributes object, please use the ThingHandleImpl.mergeAttributes() " +
                "method.");

        final MergeThing command = outgoingMessageFactory.mergeAttribute(thingId, path, value, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> setAttributes(final JsonObject attributes, final Option<?>... options) {
        argumentNotNull(attributes);
        checkArgument(attributes, v -> v.isObject() || v.isNull(),
                () -> "The root attributes entry can only be a JSON" + " object or JSON NULL literal!");

        final ModifyAttributes command = outgoingMessageFactory.setAttributes(thingId, attributes, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergeAttributes(final JsonObject attributes, final Option<?>... options) {
        argumentNotNull(attributes);
        checkArgument(attributes, v -> v.isObject() || v.isNull(),
                () -> "The root attributes entry can only be a JSON" + " object or JSON NULL literal!");

        final MergeThing command = outgoingMessageFactory.mergeAttributes(thingId, attributes, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> setFeatures(final Features features, final Option<?>... options) {
        argumentNotNull(features);

        final ModifyFeatures command = outgoingMessageFactory.setFeatures(thingId, features, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergeFeatures(final Features features, final Option<?>... options) {
        argumentNotNull(features);

        final MergeThing command = outgoingMessageFactory.mergeFeatures(thingId, features, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> setPolicyId(final PolicyId policyId, final Option<?>... options) {
        argumentNotNull(policyId);

        final ModifyPolicyId command = outgoingMessageFactory.setPolicyId(thingId, policyId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergePolicyId(final PolicyId policyId, final Option<?>... options) {
        argumentNotNull(policyId);

        final MergeThing command = outgoingMessageFactory.mergePolicyId(thingId, policyId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> putFeature(final Feature feature, final Option<?>... options) {
        argumentNotNull(feature);

        final ModifyFeature command = outgoingMessageFactory.setFeature(thingId, feature, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergeFeature(final Feature feature, final Option<?>... options) {
        argumentNotNull(feature);

        final MergeThing command = outgoingMessageFactory.mergeFeature(thingId, feature, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> deleteFeature(final String featureId, final Option<?>... options) {
        argumentNotNull(featureId);

        final DeleteFeature command = outgoingMessageFactory.deleteFeature(thingId, featureId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> deleteFeatures(final Option<?>... options) {
        final DeleteFeatures command = outgoingMessageFactory.deleteFeatures(thingId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> putAttribute(final JsonPointer path, final String value, final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> deleteAttribute(final JsonPointer path, final Option<?>... options) {
        argumentNotNull(path);
        checkArgument(path, p -> !p.isEmpty(), () -> "The root attributes object cannot be deleted!");

        final DeleteAttribute command = outgoingMessageFactory.deleteAttribute(thingId, path, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> deleteAttributes(final Option<?>... options) {
        final DeleteAttributes command = outgoingMessageFactory.deleteAttributes(thingId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public boolean deregister(final String registrationId) {
        return handlerRegistry.deregister(registrationId);
    }

    @Override
    public void registerForAttributesChanges(final String registrationId, final Consumer<Change> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/attributes", thingId),
                Change.class, handler, (change, value, path, params) -> change.withPathAndValue(path, value)
        );
    }

    @Override
    public void registerForAttributeChanges(final String registrationId, final JsonPointer attrPath,
            final Consumer<Change> handler) {
        argumentNotNull(attrPath);
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/attributes{1}", thingId, attrPath),
                Change.class, handler, (change, value, path, params) -> change.withPathAndValue(path, value)
        );
    }

    @Override
    public void registerForFeatureChanges(final String registrationId, final Consumer<FeatureChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/'{featureId}'", thingId),
                FeatureChange.class, handler, (change, value, path, params) -> {
                    final Feature feature = value != null ?
                            ThingsModelFactory.newFeatureBuilder(value.asObject())
                                    .useId(params.get("{featureId}"))
                                    .build() : null;
                    return new ImmutableFeatureChange(change.withPathAndValue(path, value), feature);
                });
    }

    @Override
    public void registerForFeatureChanges(final String registrationId, final String featureId,
            final Consumer<FeatureChange> handler) {
        argumentNotNull(featureId);
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}", thingId, featureId),
                FeatureChange.class, handler, (change, value, path, params) -> {
                    final Feature feature = value != null ?
                            ThingsModelFactory.newFeatureBuilder(value.asObject()).useId(featureId).build() : null;
                    return new ImmutableFeatureChange(change.withPathAndValue(path, value), feature);
                });
    }

    @Override
    public void registerForFeaturesChanges(final String registrationId, final Consumer<FeaturesChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features", thingId), FeaturesChange.class, handler,
                (change, value, path, params) -> {
                    final Features features = value != null ? ThingsModelFactory.newFeatures(value.asObject()) : null;
                    return new ImmutableFeaturesChange(change.withPathAndValue(path, value), features);
                });
    }

    @Override
    public void registerForThingChanges(final String registrationId, final Consumer<ThingChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}", thingId), ThingChange.class, handler,
                (change, value, path, params) -> {
                    final Thing thing =
                            value != null ? ThingsModelFactory.newThingBuilder(value.asObject()).build() : null;
                    return new ImmutableThingChange(change.withPathAndValue(path, value), thing);
                });
    }

    @Override
    public CompletionStage<Thing> retrieve() {
        return retrieve(new Option<?>[0]);
    }

    @Override
    public CompletionStage<Thing> retrieve(final Option<?>... options) {
        ConditionChecker.checkNotNull(options, "options");
        final RetrieveThing command = outgoingMessageFactory.retrieveThing(thingId, options);
        return askThingCommand(command, RetrieveThingResponse.class, RetrieveThingResponse::getThing);
    }

    @Override
    public CompletionStage<Thing> retrieve(final JsonFieldSelector fieldSelector) {
        return retrieve(fieldSelector, new Option<?>[0]);
    }

    @Override
    public CompletionStage<Thing> retrieve(final JsonFieldSelector fieldSelector, final Option<?>... options) {
        ConditionChecker.checkNotNull(fieldSelector, "fieldSelector");
        ConditionChecker.checkNotNull(options, "options");

        final RetrieveThing command =
                outgoingMessageFactory.retrieveThing(thingId, fieldSelector.getPointers(), options);
        return askThingCommand(command, RetrieveThingResponse.class, RetrieveThingResponse::getThing);
    }

}
