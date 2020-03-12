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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.FeatureChange;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.changes.internal.ImmutableChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeatureChange;
import org.eclipse.ditto.client.changes.internal.ImmutableFeaturesChange;
import org.eclipse.ditto.client.changes.internal.ImmutableThingChange;
import org.eclipse.ditto.client.internal.bus.AdaptableBus;
import org.eclipse.ditto.client.internal.bus.Classifiers;
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
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessageDirection;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.CreateThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommandResponse;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThings;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThingsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link CommonManagement}.
 *
 * @param <T> the type of {@link ThingHandle} for handling {@code Things}s
 * @param <F> the type of {@link FeatureHandle} for handling {@code Feature}s
 * @since 1.0.0
 */
public abstract class CommonManagementImpl<T extends ThingHandle<F>, F extends FeatureHandle>
        extends AbstractHandle
        implements CommonManagement<T, F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonManagementImpl.class);

    protected final OutgoingMessageFactory outgoingMessageFactory;

    private final ResponseForwarder responseForwarder;
    private final HandlerRegistry<T, F> handlerRegistry;
    private final PointerBus bus;

    protected CommonManagementImpl(
            final TopicPath.Channel channel,
            final MessagingProvider messagingProvider,
            final ResponseForwarder responseForwarder,
            final OutgoingMessageFactory outgoingMessageFactory,
            final HandlerRegistry<T, F> handlerRegistry,
            final PointerBus bus) {

        super(messagingProvider, channel);
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
                .filter(option -> !option.getName().equals(OptionName.Consumption.EXTRA_FIELDS))
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
        options.getExtraFields().ifPresent(extraFields ->
                subscriptionConfig.put(CONSUMPTION_PARAM_EXTRA_FIELDS, extraFields.toString()));

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
    public T forId(final ThingId thingId) {
        argumentNotNull(thingId);
        return handlerRegistry.thingHandleForThingId(thingId, () -> createThingHandle(thingId));
    }

    /**
     * Creates a {@link ThingHandle} for the given thing id.
     *
     * @param thingId the thing id
     * @return the thing handle
     */
    protected abstract T createThingHandle(final ThingId thingId);

    /**
     * Returns the created {@link ThingHandle} for the passed {@code thingId} if one was created.
     *
     * @param thingId the Thing ID to look for in the already created ThingHandles.
     * @return the created thing handle
     */
    protected Optional<T> getThingHandle(final ThingId thingId) {
        return handlerRegistry.getThingHandle(thingId);
    }

    @Override
    public F forFeature(final ThingId thingId, final String featureId) {
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
    protected abstract F createFeatureHandle(final ThingId thingId, final String featureId);

    /**
     * Returns the created {@link FeatureHandle} for the passed {@code thingId} and {@code featureId} if one was
     * created.
     *
     * @param thingId the Thing ID to look for in the already created FeatureHandles.
     * @param featureId the Feature ID to look for in the already created FeatureHandles.
     * @return the created feature handle
     */
    protected Optional<F> getFeatureHandle(final ThingId thingId, final String featureId) {
        return handlerRegistry.getFeatureHandle(thingId, featureId);
    }

    @Override
    public CompletableFuture<Thing> create(final Option<?>... options) {
        // as the backend adds the default namespace, we can here simply use the empty namespace.
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(ThingId.generateRandom())
                .build();
        return create(thing, options);
    }

    @Override
    public CompletableFuture<Thing> create(final ThingId thingId, final Option<?>... options) {
        argumentNotNull(thingId);
        argumentNotEmpty(thingId);

        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(ThingId.of(thingId))
                .build();
        return create(thing, options);
    }

    @Override
    public CompletableFuture<Thing> create(final Thing thing, final Option<?>... options) {
        return processCreate(thing, null, options);
    }

    @Override
    public CompletableFuture<Thing> create(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Optional<JsonObject> initialPolicy = getInlinePolicyFromThingJson(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return processCreate(thing, initialPolicy.orElse(null), options);
    }

    @Override
    public CompletableFuture<Thing> create(final Policy policy, final Option<?>... options) {
        // as the backend adds the default namespace, we can here simply use the empty namespace.
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(ThingId.generateRandom())
                .build();
        return processCreate(thing, policy.toJson(), options);
    }

    @Override
    public CompletableFuture<Thing> create(final ThingId thingId, final JsonObject initialPolicy,
            final Option<?>... options) {
        argumentNotNull(thingId);
        argumentNotEmpty(thingId);
        argumentNotNull(initialPolicy);

        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(ThingId.of(thingId))
                .build();
        return processCreate(thing, initialPolicy, options);
    }


    @Override
    public CompletableFuture<Thing> create(final ThingId thingId, final Policy initialPolicy,
            final Option<?>... options) {
        argumentNotNull(thingId);
        argumentNotEmpty(thingId);
        argumentNotNull(initialPolicy);

        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(ThingId.of(thingId))
                .build();
        return processCreate(thing, initialPolicy.toJson(), options);
    }

    @Override
    public CompletableFuture<Thing> create(final JsonObject jsonObject, final JsonObject initialPolicy,
            final Option<?>... options) {
        argumentNotNull(jsonObject);
        argumentNotNull(initialPolicy);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);

        return processCreate(thing, initialPolicy, options);
    }

    @Override
    public CompletableFuture<Thing> create(final JsonObject jsonObject, final Policy initialPolicy,
            final Option<?>... options) {
        argumentNotNull(jsonObject);
        argumentNotNull(initialPolicy);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);

        return processCreate(thing, initialPolicy.toJson(), options);
    }

    @Override
    public CompletableFuture<Thing> create(final Thing thing, final JsonObject initialPolicy,
            final Option<?>... options) {
        return processCreate(thing, initialPolicy, options);
    }


    @Override
    public CompletableFuture<Thing> create(final Thing thing, final Policy initialPolicy,
            final Option<?>... options) {
        return processCreate(thing, initialPolicy.toJson(), options);
    }

    private CompletableFuture<Thing> processCreate(final Thing thing, @Nullable final JsonObject initialPolicy,
            final Option<?>... options) {
        argumentNotNull(thing);
        assertThatThingHasId(thing);

        final CreateThing command = outgoingMessageFactory.createThing(thing, initialPolicy, options);
        return askThingCommand(command, CreateThingResponse.class, response ->
                response.getThingCreated().orElseGet(() -> ThingsModelFactory.newThing(JsonFactory.nullObject())))
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final Thing thing, final Option<?>... options) {
        return processPut(thing, null, options);
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Optional<JsonObject> initialPolicy = getInlinePolicyFromThingJson(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return processPut(thing, initialPolicy.orElse(null), options);
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final JsonObject jsonObject, final JsonObject initialPolicy,
            final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return processPut(thing, initialPolicy, options);
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final JsonObject jsonObject, final Policy initialPolicy,
            final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return processPut(thing, initialPolicy.toJson(), options);
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final Thing thing, final JsonObject initialPolicy,
            final Option<?>... options) {
        return processPut(thing, initialPolicy, options);
    }

    @Override
    public CompletableFuture<Optional<Thing>> put(final Thing thing, final Policy initialPolicy,
            final Option<?>... options) {
        return processPut(thing, initialPolicy.toJson(), options);
    }

    private CompletableFuture<Optional<Thing>> processPut(final Thing thing, @Nullable final JsonObject initialPolicy,
            final Option<?>... options) {
        argumentNotNull(thing);
        assertThatThingHasId(thing);
        return askThingCommand(outgoingMessageFactory.putThing(thing, initialPolicy, options),
                // response could be either CreateThingResponse or ModifyThingResponse.
                ThingModifyCommandResponse.class,
                response -> response.getEntity(response.getImplementedSchemaVersion())
                        .map(JsonValue::asObject)
                        .map(ThingsModelFactory::newThing)
        ).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> update(final Thing thing, final Option<?>... options) {
        argumentNotNull(thing);
        assertThatThingHasId(thing);

        return askThingCommand(outgoingMessageFactory.updateThing(thing, options), ModifyThingResponse.class,
                this::toVoid).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> update(final JsonObject jsonObject, final Option<?>... options) {
        argumentNotNull(jsonObject);

        final Thing thing = ThingsModelFactory.newThing(jsonObject);
        return update(thing, options);
    }

    @Override
    public CompletableFuture<Void> delete(final ThingId thingId, final Option<?>... options) {
        argumentNotNull(thingId);

        final DeleteThing command = outgoingMessageFactory.deleteThing(thingId, options);
        return askThingCommand(command, DeleteThingResponse.class, this::toVoid).toCompletableFuture();
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final Iterable<ThingId> thingIds) {
        argumentNotNull(thingIds);

        return sendRetrieveThingsMessage(outgoingMessageFactory.retrieveThings(thingIds));
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final ThingId thingId, final ThingId... thingIds) {
        argumentNotNull(thingId);
        argumentNotNull(thingIds);

        final Collection<ThingId> thingIdList = new ArrayList<>(1 + thingIds.length);
        thingIdList.add(thingId);
        Collections.addAll(thingIdList, thingIds);

        return sendRetrieveThingsMessage(outgoingMessageFactory.retrieveThings(thingIdList));
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final JsonFieldSelector fieldSelector, final ThingId thingId,
            final ThingId... thingIds) {

        argumentNotNull(thingId);
        argumentNotNull(thingIds);

        final Collection<ThingId> thingIdList = new ArrayList<>(1 + thingIds.length);
        thingIdList.add(thingId);
        Collections.addAll(thingIdList, thingIds);

        return retrieve(fieldSelector, thingIdList);
    }

    @Override
    public CompletableFuture<List<Thing>> retrieve(final JsonFieldSelector fieldSelector,
            final Iterable<ThingId> thingIds) {

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
                (change, value, path, params) -> new ImmutableChange(change.getEntityId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null))
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
                (change, value, path, params) -> new ImmutableChange(change.getEntityId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null))
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
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/features/{0}", featureId),
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
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'/features"),
                FeaturesChange.class, handler, (change, value, path, params) -> {
                    final Features features = value != null ? ThingsModelFactory.newFeatures(value.asObject()) : null;
                    return new ImmutableFeaturesChange(change.getEntityId(), change.getAction(), features, path,
                            change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null));
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
                (change, value, path, params) -> new ImmutableChange(change.getEntityId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null))
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
                (change, value, path, params) -> new ImmutableChange(change.getEntityId(), change.getAction(), path,
                        value, change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null))
        );
    }

    @Override
    public void registerForThingChanges(final String registrationId, final Consumer<ThingChange> handler) {
        argumentNotNull(handler);
        SelectorUtil.registerForChanges(handlerRegistry, registrationId,
                SelectorUtil.formatJsonPointer(LOGGER, "/things/'{thingId}'"),
                ThingChange.class, handler, (change, value, path, params) -> {
                    final Thing thing = null != value ? ThingsModelFactory.newThing(value.asObject()) : null;
                    return new ImmutableThingChange(change.getEntityId(), change.getAction(), thing, path,
                            change.getRevision(), change.getTimestamp().orElse(null), change.getExtra().orElse(null));
                });
    }

    /**
     * Request a subscription for a streaming type.
     *
     * @param previousSubscriptionId the previous subscription ID if any exists.
     * @param streamingType the streaming type.
     * @param protocolCommand the command to start the subscription.
     * @param protocolCommandAck the expected acknowledgement.
     * @param futureToCompleteOrFailAfterAck the future to complete or fail after receiving the expected acknowledgement
     * or not.
     * @return the subscription ID.
     */
    protected AdaptableBus.SubscriptionId subscribe(
            @Nullable final AdaptableBus.SubscriptionId previousSubscriptionId,
            final Classifiers.StreamingType streamingType,
            final String protocolCommand,
            final String protocolCommandAck,
            final CompletableFuture<Void> futureToCompleteOrFailAfterAck,
            final Function<Adaptable, Message<?>> adaptableToMessage) {

        return subscribeAndPublishMessage(previousSubscriptionId, streamingType, protocolCommand, protocolCommandAck,
                futureToCompleteOrFailAfterAck, adaptable -> bus -> {
                    final Message<?> message = adaptableToMessage.apply(adaptable);
                    bus.notify(message.getSubject(), message);
                });
    }

    protected AdaptableBus.SubscriptionId subscribeAndPublishMessage(
            @Nullable final AdaptableBus.SubscriptionId previousSubscriptionId,
            final Classifiers.StreamingType streamingType,
            final String protocolCommand,
            final String protocolCommandAck,
            final CompletableFuture<Void> futureToCompleteOrFailAfterAck,
            final Function<Adaptable, NotifyMessage> adaptableToNotifier) {

        final AdaptableBus adaptableBus = messagingProvider.getAdaptableBus();
        if (previousSubscriptionId != null) {
            // remove previous subscription without going through back-end because subscription will be replaced
            adaptableBus.unsubscribe(previousSubscriptionId);
        }
        final AdaptableBus.SubscriptionId subscriptionId =
                adaptableBus.subscribeForAdaptable(streamingType,
                        adaptable -> adaptableToNotifier.apply(adaptable).accept(getBus()));
        // TODO: configure timeout
        adjoin(adaptableBus.subscribeOnceForString(protocolCommandAck, Duration.ofSeconds(60L)),
                futureToCompleteOrFailAfterAck);
        messagingProvider.emit(protocolCommand);
        return subscriptionId;
    }

    /**
     * Remove a subscription.
     *
     * @param subscriptionId the subscription ID.
     * @param protocolCommand the command to stop the subscription.
     * @param protocolCommandAck the expected acknowledgement.
     * @param futureToCompleteOrFailAfterAck the future to complete or fail after receiving the expected acknowledgement
     * or not.
     */
    protected void unsubscribe(
            @Nullable final AdaptableBus.SubscriptionId subscriptionId,
            final String protocolCommand,
            final String protocolCommandAck,
            final CompletableFuture<Void> futureToCompleteOrFailAfterAck) {

        final AdaptableBus adaptableBus = messagingProvider.getAdaptableBus();
        if (adaptableBus.unsubscribe(subscriptionId)) {
            // TODO: configure timeout
            adjoin(adaptableBus.subscribeOnceForString(protocolCommandAck, Duration.ofSeconds(60L)),
                    futureToCompleteOrFailAfterAck);
            messagingProvider.emit(protocolCommand);
        } else {
            futureToCompleteOrFailAfterAck.complete(null);
        }
    }

    // TODO: test signal enrichment
    protected static Message<?> asThingMessage(final Adaptable adaptable) {
        final Signal<?> signal = PROTOCOL_ADAPTER.fromAdaptable(adaptable);
        final ThingId thingId = ThingId.of(signal.getEntityId());
        final MessageHeaders messageHeaders =
                MessageHeaders.newBuilder(MessageDirection.FROM, thingId, signal.getType())
                        .correlationId(signal.getDittoHeaders().getCorrelationId().orElse(null))
                        .build();
        return Message.newBuilder(messageHeaders)
                .payload(signal)
                .extra(adaptable.getPayload().getExtra().orElse(null))
                .build();
    }

    private static void adjoin(final CompletionStage<?> stage, final CompletableFuture<Void> future) {
        stage.thenAccept(ignored -> future.complete(null))
                .exceptionally(error -> {
                    future.completeExceptionally(error);
                    return null;
                });
    }

    private static Optional<JsonObject> getInlinePolicyFromThingJson(final JsonObject jsonObject) {
        return jsonObject.getValue(CreateThing.JSON_INLINE_POLICY.getPointer())
                .filter(JsonValue::isObject)
                .map(JsonValue::asObject);
    }

    private static void assertThatThingHasId(final Thing thing) {
        if (!thing.getEntityId().isPresent()) {
            final String msgPattern = "Mandatory field <{0}> is missing!";
            throw new IllegalArgumentException(MessageFormat.format(msgPattern, Thing.JsonFields.ID.getPointer()));
        }
    }

    private CompletableFuture<List<Thing>> sendRetrieveThingsMessage(final RetrieveThings command) {
        return askThingCommand(command, RetrieveThingsResponse.class, RetrieveThingsResponse::getThings)
                .toCompletableFuture();
    }

    @FunctionalInterface
    protected interface NotifyMessage {

        void accept(final PointerBus pointerBus);
    }
}
