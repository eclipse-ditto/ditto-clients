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
import org.eclipse.ditto.client.changes.internal.ImmutableChange;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.ResponseForwarder;
import org.eclipse.ditto.client.internal.SendTerminator;
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
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link FeatureHandle}.
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Things}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public abstract class FeatureHandleImpl<T extends ThingHandle, F extends FeatureHandle> implements FeatureHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureHandleImpl.class);

    private final TopicPath.Channel channel;
    private final ThingId thingId;
    private final String featureId;
    private final MessagingProvider messagingProvider;
    private final ResponseForwarder responseForwarder;
    private final OutgoingMessageFactory outgoingMessageFactory;
    private final HandlerRegistry<T, F> handlerRegistry;

    protected FeatureHandleImpl(final TopicPath.Channel channel,
            final ThingId thingId,
            final String featureId,
            final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<T, F> handlerRegistry) {

        this.channel = channel;
        this.thingId = thingId;
        this.featureId = featureId;
        this.messagingProvider = messagingProvider;
        this.responseForwarder = responseForwarder;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Returns the MessagingProvider this FeatureHandle uses.
     *
     * @return the MessagingProvider this FeatureHandle uses.
     */
    protected MessagingProvider getMessagingProvider() {
        return messagingProvider;
    }

    /**
     * Returns the ResponseForwarder this FeatureHandle uses.
     *
     * @return the ResponseForwarder this FeatureHandle uses.
     */
    protected ResponseForwarder getResponseForwarder() {
        return responseForwarder;
    }

    /**
     * Returns the OutgoingMessageFactory this FeatureHandle uses.
     *
     * @return the OutgoingMessageFactory this FeatureHandle uses.
     */
    protected OutgoingMessageFactory getOutgoingMessageFactory() {
        return outgoingMessageFactory;
    }

    /**
     * Returns the HandlerRegistry this FeatureHandle uses.
     *
     * @return the HandlerRegistry this FeatureHandle uses.
     */
    protected HandlerRegistry<T, F> getHandlerRegistry() {
        return handlerRegistry;
    }

    @Override
    public ThingId getThingEntityId() {
        return thingId;
    }

    @Override
    public String getFeatureId() {
        return featureId;
    }

    @Override
    public CompletableFuture<Void> delete(final Option<?>... options) {
        final ThingCommand command = outgoingMessageFactory.deleteFeature(thingId, featureId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Feature> retrieve() {
        final ThingCommand command = outgoingMessageFactory.retrieveFeature(thingId, featureId);
        return new SendTerminator<Feature>(messagingProvider, responseForwarder, channel, command).applyView(tvr -> {
            if (tvr != null) {
                return ThingsModelFactory.newFeatureBuilder(tvr.getEntity(tvr.getImplementedSchemaVersion()).asObject())
                        .useId(featureId)
                        .build();
            } else {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Feature> retrieve(final JsonFieldSelector fieldSelector) {
        final ThingCommand command =
                outgoingMessageFactory.retrieveFeature(thingId, featureId, fieldSelector.getPointers());

        return new SendTerminator<Feature>(messagingProvider, responseForwarder, channel, command).applyView(tvr -> {
            if (tvr != null) {
                return ThingsModelFactory.newFeatureBuilder(tvr.getEntity(tvr.getImplementedSchemaVersion()).asObject())
                        .useId(featureId)
                        .build();
            } else {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setDefinition(final FeatureDefinition featureDefinition,
            final Option<?>... options) {

        final ThingCommand command =
                outgoingMessageFactory.setFeatureDefinition(thingId, featureId, featureDefinition, options);

        return new SendTerminator<Void>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> deleteDefinition(final Option<?>... options) {
        final ThingCommand command = outgoingMessageFactory.deleteFeatureDefinition(thingId, featureId, options);
        return new SendTerminator<Void>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> putProperty(final JsonPointer path, final boolean value,
            final Option<?>... options) {

        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putProperty(final JsonPointer path, final double value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putProperty(final JsonPointer path, final int value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putProperty(final JsonPointer path, final long value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putProperty(final JsonPointer path, final String value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletableFuture<Void> putProperty(final JsonPointer path, final JsonValue value,
            final Option<?>... options) {

        argumentNotNull(path, "Path");
        checkArgument(path, p -> !p.isEmpty(), () -> "The path is not allowed to be empty! " +
                "If you want to update the whole properties object, please use the setProperties(JsonObject) method.");

        final ThingCommand command =
                outgoingMessageFactory.setFeatureProperty(thingId, featureId, path, value, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> setProperties(final JsonObject value, final Option<?>... options) {
        final ThingCommand command = outgoingMessageFactory.setFeatureProperties(thingId, featureId, value, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> deleteProperty(final JsonPointer path, final Option<?>... options) {
        final ThingCommand command = outgoingMessageFactory.deleteFeatureProperty(thingId, featureId, path, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<Void> deleteProperties(final Option<?>... options) {
        final ThingCommand command = outgoingMessageFactory.deleteFeatureProperties(thingId, featureId, options);
        return new SendTerminator<>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public void registerForPropertyChanges(final String registrationId, final Consumer<Change> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/properties", thingId, featureId),
                Change.class, handler, (change, value, path, params) ->
                        new ImmutableChange(change.getEntityId(), change.getAction(), path, value, change.getRevision(),
                                change.getTimestamp().orElse(null))
        );
    }

    @Override
    public void registerForPropertyChanges(final String registrationId, final JsonPointer propertyPath,
            final Consumer<Change> handler) {

        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/properties{2}", thingId, featureId,
                        propertyPath), Change.class, handler,
                (change, value, path, params) -> new ImmutableChange(change.getEntityId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null))
        );
    }

    @Override
    public boolean deregister(final String registrationId) {
        return handlerRegistry.deregister(registrationId);
    }

}
