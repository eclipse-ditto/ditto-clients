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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;
import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.FeatureChange;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.changes.internal.ImmutableChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeatureChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeaturesChange;
import org.eclipse.ditto.client.changes.internal.ImmutableThingChange;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.internal.bus.SelectorUtil;
import org.eclipse.ditto.client.management.CommonManagement;
import org.eclipse.ditto.client.management.FeatureHandle;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.client.options.internal.OptionsEvaluator;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link CommonManagement}.
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Things}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public abstract class CommonManagementImpl<T extends ThingHandle, F extends FeatureHandle> implements
        CommonManagement<T, F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonManagementImpl.class);

    private final TopicPath.Channel channel;
    private final MessagingProvider messagingProvider;
    private final ResponseForwarder responseForwarder;
    private final OutgoingMessageFactory outgoingMessageFactory;
    private final HandlerRegistry<T, F> handlerRegistry;
    private final PointerBus bus;

    protected CommonManagementImpl(
            final TopicPath.Channel channel,
            final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<T, F> handlerRegistry,
            final PointerBus bus) {

        this.channel = channel;
        this.messagingProvider = messagingProvider;
        this.responseForwarder = responseForwarder;
        this.outgoingMessageFactory = outgoingMessageFactory;
        this.handlerRegistry = handlerRegistry;
        this.bus = bus;
    }

    @Override
    public CompletableFuture<Void> startConsumption() {
        return doStartConsumption(Collections.emptyMap());
    }

    @Override
    public CompletableFuture<Void> startConsumption(final Option<?>... consumptionOptions) {

        // only accept "Consumption" related options here:
        final Optional<Option<?>> unknownOptionIncluded = Arrays.stream(consumptionOptions)
                .filter(option -> !option.getName().equals(OptionName.Consumption.NAMESPACES))
                .filter(option -> !option.getName().equals(OptionName.Consumption.FILTER))
                .findFirst();
        if (unknownOptionIncluded.isPresent()) {
            final Option<?> unknownOption = unknownOptionIncluded.get();
            throw new IllegalArgumentException("Unsupported Option <" + unknownOption + ">. " +
                    "The only supported options for startConsumption() are: " +
                    "Options.Consumption.namespaces() and Options.Consumption.filter()");
        }

        final OptionsEvaluator.Consumption options = OptionsEvaluator.forConsumptionOptions(consumptionOptions);
        final Map<String, String> subscriptionConfig = new HashMap<>();
        options.getNamespaces().ifPresent(namespaces ->
                subscriptionConfig.put(CONSUMPTION_PARAM_NAMESPACES, String.join(",", namespaces)));
        options.getFilter().ifPresent(filter ->
                subscriptionConfig.put(CONSUMPTION_PARAM_FILTER, filter.toString()));

        return doStartConsumption(subscriptionConfig);
    }

    /**
     * Starts the consumption of twin events / messages / live events and commands.
     *
     * @param consumptionConfig the configuration Map to apply for the consumption.
     * @return a CompletableFuture that terminates when the start operation was successful.
     */
    protected abstract CompletableFuture<Void> doStartConsumption(Map<String, String> consumptionConfig);

    /**
     * Returns the MessagingProvider this CommonManagement uses.
     *
     * @return the MessagingProvider this CommonManagement uses.
     */
    protected MessagingProvider getMessagingProvider() {
        return messagingProvider;
    }

    /**
     * Returns the ResponseForwarder this CommonManagement uses.
     *
     * @return the ResponseForwarder this CommonManagement uses.
     */
    protected ResponseForwarder getResponseForwarder() {
        return responseForwarder;
    }

    /**
     * Returns the OutgoingMessageFactory this CommonManagement uses.
     *
     * @return the OutgoingMessageFactory this CommonManagement uses.
     */
    protected OutgoingMessageFactory getOutgoingMessageFactory() {
        return outgoingMessageFactory;
    }

    /**
     * Returns the HandlerRegistry this CommonManagement uses.
     *
     * @return the HandlerRegistry this CommonManagement uses.
     */
    protected HandlerRegistry<T, F> getHandlerRegistry() {
        return handlerRegistry;
    }

    /**
     * Returns the Bus this CommonManagement uses.
     *
     * @return the Bus this CommonManagement uses.
     */
    protected PointerBus getBus() {
        return bus;
    }

    @Override
    public boolean deregister(final String registrationId) {
        return handlerRegistry.deregister(registrationId);
    }

    @Override
    public T forId(final String thingId) {
        argumentNotNull(thingId);
        return handlerRegistry.thingHandleForThingId(thingId, () -> createThingHandle(thingId));
    }

    /**
     * Creates a {@link ThingHandle} for the given thing id.
     *
     * @param thingId the thing id
     * @return the thing handle
     */
    protected abstract T createThingHandle(final String thingId);

    /**
     * Returns the created {@link ThingHandle} for the passed {@code thingId} if one was created.
     *
     * @param thingId the Thing ID to look for in the already created ThingHandles.
     * @return the created thing handle
     */
    protected Optional<T> getThingHandle(final String thingId) {
        return handlerRegistry.getThingHandle(thingId);
    }

    @Override
    public F forFeature(final String thingId, final String featureId) {
        argumentNotNull(thingId);
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
    protected abstract F createFeatureHandle(final String thingId, final String featureId);

    /**
     * Returns the created {@link FeatureHandle} for the passed {@code thingId} and {@code featureId} if one was
     * created.
     *
     * @param thingId the Thing ID to look for in the already created FeatureHandles.
     * @param featureId the Feature ID to look for in the already created FeatureHandles.
     * @return the created feature handle
     */
    protected Optional<F> getFeatureHandle(final String thingId, final String featureId) {
        return handlerRegistry.getFeatureHandle(thingId, featureId);
    }

    @Override
    public CompletableFuture<Thing> create(final Option<?>... options) {
        // as the backend adds the default namespace, we can here simply use the empty namespace:
        final String thingId = ":" + UUID.randomUUID().toString();
        final Thing thing = ThingsModelFactory.newThingBuilder().setId(thingId).build();
        return create(thing, options);
    }

    @Override
    public CompletableFuture<Thing> create(final String thingId, final Option<?>... options) {
        argumentNotNull(thingId);
        argumentNotEmpty(thingId);

        final Thing thing = ThingsModelFactory.newThingBuilder().setId(thingId).build();
        return create(thing, options);
    }

    @Override
    public CompletableFuture<Thing> create(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return create(thing, options);
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final Thing thing, final Option<?>... options) {
        argumentNotNull(thing);
        getThingIdOrThrow(thing);

        return new SendTerminator<Optional<Thing>>(messagingProvider, responseForwarder, channel,
                outgoingMessageFactory.putThing(thing, options)).applyModify(response -> {
            if (response != null) {
                final Optional<JsonValue> responseEntityOpt =
                        response.getEntity(response.getImplementedSchemaVersion());
                if (responseEntityOpt.isPresent()) {
                    final Thing createdThing = ThingsModelFactory.newThing(responseEntityOpt.get().asObject());
                    return Optional.of(createdThing);
                } else {
                    return Optional.empty();
                }
            } else {
                throw new IllegalStateException("Response is always expected!");
            }
        });
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return put(thing, options);
    }

    @Override
    public CompletableFuture<Void> update(final Thing thing, final Option<?>... options) {
        argumentNotNull(thing);
        getThingIdOrThrow(thing);

        return new SendTerminator<Void>(messagingProvider, responseForwarder, channel,
                outgoingMessageFactory.updateThing(thing, options)).applyVoid();
    }

    @Override
    public CompletableFuture<Void> update(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return update(thing, options);
    }

    @Override
    public CompletableFuture<Thing> create(final Thing thing, final Option<?>... options) {
        argumentNotNull(thing);

        getThingIdOrThrow(thing);

        final ThingCommand command = outgoingMessageFactory.createThing(thing, options);

        return new SendTerminator<Thing>(messagingProvider, responseForwarder, channel, command)
                .applyModify(response -> {
                    if (response != null) {
                        return ThingsModelFactory.newThing(response.getEntity(response.getImplementedSchemaVersion())
                                .orElse(JsonFactory.nullObject()).asObject());
                    } else {
                        return null;
                    }
                });
    }

    @Override
    public CompletableFuture<Void> delete(final String thingId, final Option<?>... options) {
        argumentNotNull(thingId);

        final ThingCommand command = outgoingMessageFactory.deleteThing(thingId, options);
        return new SendTerminator<Void>(messagingProvider, responseForwarder, channel, command).applyVoid();
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final Iterable<String> thingIds) {
        argumentNotNull(thingIds);

        return sendRetrieveThingsMessage(outgoingMessageFactory.retrieveThings(thingIds));
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final String thingId, final String... thingIds) {
        argumentNotNull(thingId);
        argumentNotNull(thingIds);

        final Collection<String> thingIdList = new ArrayList<>(1 + thingIds.length);
        thingIdList.add(thingId);
        Collections.addAll(thingIdList, thingIds);

        return sendRetrieveThingsMessage(outgoingMessageFactory.retrieveThings(thingIdList));
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final JsonFieldSelector fieldSelector, final String thingId,
            final String... thingIds) {

        argumentNotNull(thingId);
        argumentNotNull(thingIds);

        final Collection<String> thingIdList = new ArrayList<>(1 + thingIds.length);
        thingIdList.add(thingId);
        Collections.addAll(thingIdList, thingIds);

        return retrieve(fieldSelector, thingIdList);
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final JsonFieldSelector fieldSelector,
            final Iterable<String> thingIds) {

        argumentNotNull(fieldSelector);
        argumentNotNull(thingIds);

        return sendRetrieveThingsMessage(outgoingMessageFactory.retrieveThings(thingIds, fieldSelector.getPointers()));
    }

    @Override
    public void registerForAttributesChanges(final String registrationId, final Consumer<Change> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/attributes"),
                Change.class, handler,
                (change, value, path, params) -> new ImmutableChange(change.getThingId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null))
        );
    }

    @Override
    public void registerForAttributeChanges(final String registrationId, final JsonPointer attrPath,
            final Consumer<Change> handler) {

        argumentNotNull(attrPath);
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/attributes{0}", attrPath), Change.class,
                handler,
                (change, value, path, params) -> new ImmutableChange(change.getThingId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null))
        );
    }

    @Override
    public void registerForFeatureChanges(final String registrationId, final Consumer<FeatureChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/features/'{featureId}'"),
                FeatureChange.class, handler,
                (change, value, path, params) -> {
                    final Feature feature = value != null ?
                            ThingsModelFactory.newFeatureBuilder(value.asObject())
                                    .useId(params.get("{featureId}"))
                                    .build() : null;
                    return new ImmutableFeatureChange(change.getThingId(), change.getAction(), feature, path,
                            change.getRevision(), change.getTimestamp().orElse(null));
                });
    }

    @Override
    public void registerForFeatureChanges(final String registrationId, final String featureId,
            final Consumer<FeatureChange> handler) {

        argumentNotNull(featureId);
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/features/{0}", featureId),
                FeatureChange.class, handler, (change, value, path, params) -> {
                    final Feature feature = value != null ?
                            ThingsModelFactory.newFeatureBuilder(value.asObject()).useId(featureId).build() : null;
                    return new ImmutableFeatureChange(change.getThingId(), change.getAction(), feature, path,
                            change.getRevision(), change.getTimestamp().orElse(null));
                });
    }

    @Override
    public void registerForFeaturesChanges(final String registrationId, final Consumer<FeaturesChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/features"),
                FeaturesChange.class, handler, (change, value, path, params) -> {
                    final Features features = value != null ? ThingsModelFactory.newFeatures(value.asObject()) : null;
                    return new ImmutableFeaturesChange(change.getThingId(), change.getAction(), features, path,
                            change.getRevision(), change.getTimestamp().orElse(null));
                });
    }

    @Override
    public void registerForFeaturePropertyChanges(final String registrationId, final String featureId,
            final Consumer<Change> handler) {

        argumentNotNull(featureId);
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/features/{0}/properties", featureId),
                Change.class,
                handler,
                (change, value, path, params) -> new ImmutableChange(change.getThingId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null))
        );
    }

    @Override
    public void registerForFeaturePropertyChanges(final String registrationId,
            final String featureId,
            final JsonPointer propertyPath,
            final Consumer<Change> handler) {

        argumentNotNull(featureId);
        argumentNotNull(propertyPath);
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/features/{0}/properties{1}", featureId,
                        propertyPath),
                Change.class, handler,
                (change, value, path, params) -> new ImmutableChange(change.getThingId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null))
        );
    }

    @Override
    public void registerForThingChanges(final String registrationId, final Consumer<ThingChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'"),
                ThingChange.class, handler, (change, value, path, params) -> {
                    final Thing thing = null != value ? ThingsModelFactory.newThing(value.asObject()) : null;
                    return new ImmutableThingChange(change.getThingId(), change.getAction(), thing, path,
                            change.getRevision(), change.getTimestamp().orElse(null));
                });
    }

    private static String getThingIdOrThrow(final Thing thing) {
        return thing.getId().orElseThrow(() -> {
            final String msgPattern = "Mandatory field <{0}> is missing!";
            return new IllegalArgumentException(MessageFormat.format(msgPattern, Thing.JsonFields.ID.getPointer()));
        });
    }

    private CompletableFuture<List<Thing>> sendRetrieveThingsMessage(final ThingCommand command) {
        return new SendTerminator<List<Thing>>(messagingProvider, responseForwarder, channel, command)
                .applyView(tvr -> {
                    if (tvr != null) {
                        return tvr.getEntity(tvr.getImplementedSchemaVersion())
                                .asArray()
                                .stream()
                                .map(JsonValue::asObject)
                                .map(ThingsModelFactory::newThing)
                                .collect(Collectors.toList());
                    } else {
                        return null;
                    }
                });
    }

}
