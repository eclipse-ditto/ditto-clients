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
import org.eclipse.ditto.client.internal.AbstractHandle;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.OutgoingMessageFactory;
import org.eclipse.ditto.client.internal.ResponseForwarder;
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
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeature;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturePropertiesResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeaturePropertyResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinitionResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturePropertiesResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturePropertyResponse;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeature;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link FeatureHandle}.
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Things}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public abstract class FeatureHandleImpl<T extends ThingHandle<F>, F extends FeatureHandle>
        extends AbstractHandle implements FeatureHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureHandleImpl.class);

    private final ThingId thingId;
    private final String featureId;
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

        super(messagingProvider, channel);
        this.thingId = thingId;
        this.featureId = featureId;
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
        final DeleteFeature command = outgoingMessageFactory.deleteFeature(thingId, featureId, options);
        return askThingCommand(command, DeleteFeatureResponse.class, this::toVoid).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Feature> retrieve() {
        final RetrieveFeature command = outgoingMessageFactory.retrieveFeature(thingId, featureId);
        return askThingCommand(command, RetrieveFeatureResponse.class, RetrieveFeatureResponse::getFeature)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Feature> retrieve(final JsonFieldSelector fieldSelector) {
        final RetrieveFeature command =
                outgoingMessageFactory.retrieveFeature(thingId, featureId, fieldSelector.getPointers());
        return askThingCommand(command, RetrieveFeatureResponse.class, RetrieveFeatureResponse::getFeature)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> setDefinition(final FeatureDefinition featureDefinition,
            final Option<?>... options) {
        final ModifyFeatureDefinition command =
                outgoingMessageFactory.setFeatureDefinition(thingId, featureId, featureDefinition, options);
        return askThingCommand(command, ModifyFeatureDefinitionResponse.class, this::toVoid).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> deleteDefinition(final Option<?>... options) {
        final DeleteFeatureDefinition
                command = outgoingMessageFactory.deleteFeatureDefinition(thingId, featureId, options);
        return askThingCommand(command, DeleteFeatureDefinitionResponse.class, this::toVoid).toCompletableFuture();
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

        final ModifyFeatureProperty command =
                outgoingMessageFactory.setFeatureProperty(thingId, featureId, path, value, options);
        return askThingCommand(command, ModifyFeaturePropertyResponse.class, this::toVoid).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> setProperties(final JsonObject value, final Option<?>... options) {
        final ModifyFeatureProperties
                command = outgoingMessageFactory.setFeatureProperties(thingId, featureId, value, options);
        return askThingCommand(command, ModifyFeaturePropertiesResponse.class, this::toVoid).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> deleteProperty(final JsonPointer path, final Option<?>... options) {
        final DeleteFeatureProperty
                command = outgoingMessageFactory.deleteFeatureProperty(thingId, featureId, path, options);
        return askThingCommand(command, DeleteFeaturePropertyResponse.class, this::toVoid).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> deleteProperties(final Option<?>... options) {
        final DeleteFeatureProperties
                command = outgoingMessageFactory.deleteFeatureProperties(thingId, featureId, options);
        return askThingCommand(command, DeleteFeaturePropertiesResponse.class, this::toVoid).toCompletableFuture();
    }

    @Override
    public void registerForPropertyChanges(final String registrationId, final Consumer<Change> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/properties", thingId, featureId),
                Change.class, handler, (change, value, path, params) ->
                        new ImmutableChange(change.getEntityId(), change.getAction(), path, value, change.getRevision(),
                                change.getTimestamp().orElse(null), change.getExtra().orElse(null))
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
                        value, change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null))
        );
    }

    @Override
    public boolean deregister(final String registrationId) {
        return handlerRegistry.deregister(registrationId);
    }
}
