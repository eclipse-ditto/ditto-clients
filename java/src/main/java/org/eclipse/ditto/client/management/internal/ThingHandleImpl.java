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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkArgument;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.FeatureChange;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.changes.internal.ImmutableChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeatureChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeaturesChange;
import org.eclipse.ditto.client.changes.internal.ImmutableThingChange;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.internal.SendTerminator;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.management.FeatureHandle;
import org.eclipse.ditto.client.management.PolicyHandle;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link ThingHandle}.
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Things}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public abstract class ThingHandleImpl<T extends ThingHandle, F extends FeatureHandle> implements ThingHandle<F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingHandleImpl.class);

    private final TopicPath.Channel channel;
    private final ThingId thingId;
    private final MessagingProvider messagingProvider;
    private final ResponseForwarder responseForwarder;
    private final OutgoingMessageFactory outgoingMessageFactory;
    private final HandlerRegistry<T, F> handlerRegistry;

    protected ThingHandleImpl(
            final TopicPath.Channel channel,
            final ThingId thingId,
            final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<T, F> handlerRegistry) {
        this.channel = channel;
        this.thingId = thingId;
        this.messagingProvider = messagingProvider;
        this.responseForwarder = responseForwarder;
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
     * Returns the ResponseForwarder this ThingHandle uses.
     *
     * @return the ResponseForwarder this ThingHandle uses.
     */
    protected ResponseForwarder getResponseForwarder() {
        return responseForwarder;
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
    public ThingId getThingEntityId() {
        return thingId;
    }

    @Override
    public F forFeature(final String featureId) {
        argumentNotNull(featureId);
        return handlerRegistry.featureHandleForFeatureId(thingId, featureId, () ->
                createFeatureHandle(thingId, featureId));
    }

    @Override
    public PolicyHandle forPolicy(final String policyId) {
        return new PolicyHandleImpl(
                outgoingMessageFactory,
                PolicyId.of(policyId),
                messagingProvider,
                responseForwarder);
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
    public CompletableFuture<Void> delete(final Option<?>[] options) {
        final ThingCommand command = outgoingMessageFactory.deleteThing(thingId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> putAttribute(final JsonPointer path, final boolean value,
            final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putAttribute(final JsonPointer path, final double value,
            final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putAttribute(final JsonPointer path, final int value, final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putAttribute(final JsonPointer path, final long value, final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putAttribute(final JsonPointer path, final JsonValue value,
            final Option<?>... options) {
        argumentNotNull(path);
        argumentNotNull(value);
        checkArgument(path, p -> !p.isEmpty(), () -> "The path is not allowed to be empty! " +
                "If you want to update the whole attributes object, please use the setAttributes(JsonObject) method.");

        final ThingCommand command = outgoingMessageFactory.setAttribute(thingId, path, value, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> setAttributes(final JsonObject attributes, final Option<?>... options) {
        argumentNotNull(attributes);
        checkArgument(attributes, v -> v.isObject() || v.isNull(),
                () -> "The root attributes entry can only be a JSON" + " object or JSON NULL literal!");

        final ThingCommand command = outgoingMessageFactory.setAttributes(thingId, attributes, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> setFeatures(final Features features, final Option<?>... options) {
        argumentNotNull(features);

        final ThingCommand command = outgoingMessageFactory.setFeatures(thingId, features, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> setPolicyId(final PolicyId policyId, final Option<?>... options) {
        argumentNotNull(policyId);

        final ModifyPolicyId command = outgoingMessageFactory.setPolicyId(thingId, policyId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> putFeature(final Feature feature, final Option<?>... options) {
        argumentNotNull(feature);

        final ThingCommand command = outgoingMessageFactory.setFeature(thingId, feature, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> deleteFeature(final String featureId, final Option<?>... options) {
        argumentNotNull(featureId);

        final ThingCommand command = outgoingMessageFactory.deleteFeature(thingId, featureId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> deleteFeatures(final Option<?>... options) {
        final ThingCommand command = outgoingMessageFactory.deleteFeatures(thingId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }


    @Override
    public CompletableFuture<Void> putAttribute(final JsonPointer path, final String value,
            final Option<?>... options) {
        return putAttribute(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> deleteAttribute(final JsonPointer path, final Option<?>... options) {
        argumentNotNull(path);
        checkArgument(path, p -> !p.isEmpty(), () -> "The root attributes object cannot be deleted!");

        final ThingCommand command = outgoingMessageFactory.deleteAttribute(thingId, path, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> deleteAttributes(final Option<?>... options) {
        final ThingCommand command = outgoingMessageFactory.deleteAttributes(thingId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
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
                Change.class, handler, (change, value, path, params) ->
                        new ImmutableChange(change.getEntityId(), change.getAction(), path, value, change.getRevision(),
                                change.getTimestamp().orElse(null), change.getExtra().orElse(null))
        );
    }

    @Override
    public void registerForAttributeChanges(final String registrationId, final JsonPointer attrPath,
            final Consumer<Change> handler) {
        argumentNotNull(attrPath);
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/attributes{1}", thingId, attrPath),
                Change.class, handler, (change, value, path, params) ->
                        new ImmutableChange(change.getEntityId(), change.getAction(), path, value, change.getRevision(),
                                change.getTimestamp().orElse(null), change.getExtra().orElse(null))
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
                    return new ImmutableFeatureChange(change.getEntityId(), change.getAction(), feature, path,
                            change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null));
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
                    return new ImmutableFeatureChange(change.getEntityId(), change.getAction(), feature, path,
                            change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null));
                });
    }

    @Override
    public void registerForFeaturesChanges(final String registrationId, final Consumer<FeaturesChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features", thingId), FeaturesChange.class, handler,
                (change, value, path, params) -> {
                    final Features features = value != null ? ThingsModelFactory.newFeatures(value.asObject()) : null;
                    return new ImmutableFeaturesChange(change.getEntityId(), change.getAction(), features, path,
                            change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null));
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
                    return new ImmutableThingChange(change.getEntityId(), change.getAction(), thing, path,
                            change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null));
                });
    }

    @Override
    public CompletableFuture<Thing> retrieve() {
        final ThingCommand command = outgoingMessageFactory.retrieveThing(thingId);
        return new SendTerminator<Thing>(messagingProvider, responseForwarder, channel, command).applyView(tvr ->
        {
            if (tvr != null) {
                return ThingsModelFactory.newThing(tvr.getEntity(tvr.getImplementedSchemaVersion()).asObject());
            } else {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Thing> retrieve(final JsonFieldSelector fieldSelector) {
        argumentNotNull(fieldSelector);

        final ThingCommand command = outgoingMessageFactory.retrieveThing(thingId, fieldSelector.getPointers());
        return new SendTerminator<Thing>(messagingProvider, responseForwarder, channel, command).applyView(tvr ->
        {
            if (tvr != null) {
                return ThingsModelFactory.newThing(tvr.getEntity(tvr.getImplementedSchemaVersion()).asObject());
            } else {
                return null;
            }
        });
    }

}
