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
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.FeatureDefinition;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeature;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureResponse;
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

    protected final OutgoingMessageFactory outgoingMessageFactory;

    private final ThingId thingId;
    private final String featureId;
    private final HandlerRegistry<T, F> handlerRegistry;

    protected FeatureHandleImpl(final TopicPath.Channel channel,
            final ThingId thingId,
            final String featureId,
            final MessagingProvider messagingProvider,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<T, F> handlerRegistry) {

        super(messagingProvider, channel);
        this.thingId = thingId;
        this.featureId = featureId;
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
    public ThingId getEntityId() {
        return thingId;
    }

    @Override
    public String getFeatureId() {
        return featureId;
    }

    @Override
    public CompletionStage<Void> delete(final Option<?>... options) {
        final DeleteFeature command = outgoingMessageFactory.deleteFeature(thingId, featureId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Feature> retrieve() {
        return retrieve(new Option<?>[0]);
    }

    @Override
    public CompletionStage<Feature> retrieve(final Option<?>... options) {
        ConditionChecker.checkNotNull(options, "options");
        final RetrieveFeature command = outgoingMessageFactory.retrieveFeature(thingId, featureId, options);
        return askThingCommand(command, RetrieveFeatureResponse.class, RetrieveFeatureResponse::getFeature);
    }

    @Override
    public CompletionStage<Feature> retrieve(final JsonFieldSelector fieldSelector) {
        return retrieve(fieldSelector, new Option<?>[0]);
    }

    @Override
    public CompletionStage<Feature> retrieve(final JsonFieldSelector fieldSelector, final Option<?>... options) {
        ConditionChecker.checkNotNull(fieldSelector, "fieldSelector");
        ConditionChecker.checkNotNull(options, "options");
        final RetrieveFeature command =
                outgoingMessageFactory.retrieveFeature(thingId, featureId, fieldSelector, options);
        return askThingCommand(command, RetrieveFeatureResponse.class, RetrieveFeatureResponse::getFeature);
    }

    @Override
    public CompletionStage<Void> setDefinition(final FeatureDefinition featureDefinition,
            final Option<?>... options) {
        final ModifyFeatureDefinition command =
                outgoingMessageFactory.setFeatureDefinition(thingId, featureId, featureDefinition, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergeDefinition(final FeatureDefinition featureDefinition,
            final Option<?>... options) {

        final MergeThing command =
                outgoingMessageFactory.mergeFeatureDefinition(thingId, featureId, featureDefinition, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> deleteDefinition(final Option<?>... options) {
        final DeleteFeatureDefinition command =
                outgoingMessageFactory.deleteFeatureDefinition(thingId, featureId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> putProperty(final JsonPointer path, final boolean value,
            final Option<?>... options) {

        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putProperty(final JsonPointer path, final double value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putProperty(final JsonPointer path, final int value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putProperty(final JsonPointer path, final long value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putProperty(final JsonPointer path, final String value, final Option<?>... options) {
        return putProperty(path, JsonFactory.newValue(value), options);
    }

    @Override
    public CompletionStage<Void> putProperty(final JsonPointer path, final JsonValue value,
            final Option<?>... options) {

        argumentNotNull(path, "Path");
        checkArgument(path, p -> !p.isEmpty(), () -> "The path is not allowed to be empty! " +
                "If you want to update the whole properties object, please use the setProperties(JsonObject) method.");

        final ModifyFeatureProperty command =
                outgoingMessageFactory.setFeatureProperty(thingId, featureId, path, value, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergeProperty(final JsonPointer path, final JsonValue value,
            final Option<?>... options) {

        argumentNotNull(path, "Path");
        checkArgument(path, p -> !p.isEmpty(), () -> "The path is not allowed to be empty! " +
                "If you want to update the whole properties object, please use the FeatureHandleImpl.mergeProperties method.");

        final MergeThing command =
                outgoingMessageFactory.mergeFeatureProperty(thingId, featureId, path, value, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> setProperties(final JsonObject value, final Option<?>... options) {
        final ModifyFeatureProperties
                command = outgoingMessageFactory.setFeatureProperties(thingId, featureId, value, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> mergeProperties(final JsonObject value, final Option<?>... options) {
        final MergeThing command = outgoingMessageFactory.mergeFeatureProperties(thingId, featureId, value, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> deleteProperty(final JsonPointer path, final Option<?>... options) {
        final DeleteFeatureProperty
                command = outgoingMessageFactory.deleteFeatureProperty(thingId, featureId, path, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public CompletionStage<Void> deleteProperties(final Option<?>... options) {
        final DeleteFeatureProperties
                command = outgoingMessageFactory.deleteFeatureProperties(thingId, featureId, options);
        return askThingCommand(command, CommandResponse.class, this::toVoid);
    }

    @Override
    public void registerForPropertyChanges(final String registrationId, final Consumer<Change> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/properties", thingId, featureId),
                Change.class, handler, (change, value, path, params) -> change.withPathAndValue(path, value)
        );
    }

    @Override
    public void registerForPropertyChanges(final String registrationId, final JsonPointer propertyPath,
            final Consumer<Change> handler) {

        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/{0}/features/{1}/properties{2}", thingId, featureId,
                        propertyPath), Change.class, handler, (change, value, path, params) ->
                        change.withPathAndValue(path, value)
        );
    }

    @Override
    public boolean deregister(final String registrationId) {
        return handlerRegistry.deregister(registrationId);
    }
}
