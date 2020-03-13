/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.internal.bus;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionEvent;

/**
 * Factory class for classifiers.
 */
public final class Classifiers {

    private Classifiers() {}

    /**
     * Create an identity classifier.
     *
     * @return classifier that classifies each object as itself.
     */
    public static <T> Classifier<T> identity() {
        return Identity::of;
    }

    /**
     * Classify adaptables according to their correlation ID.
     *
     * @return the correlation ID classifier.
     */
    public static Classifier<Adaptable> correlationId() {
        return Instances.CORRELATION_ID_CLASSIFIER;
    }

    /**
     * Classify adaptables according to their streaming type: live commands, live events, live messages
     * and twin events. Live command responses are classified as live commands but should be handled by
     * a correlation-ID-based one-time subscriber in the normal case.
     *
     * @return the streaming type classifier.
     */
    public static Classifier<Adaptable> streamingType() {
        return Instances.STREAMING_TYPE_CLASSIFIER;
    }

    /**
     * Classify thing-search events.
     *
     * @return classifier for thing-search events.
     */
    public static Classifier<Adaptable> thingsSearch() {
        return Instances.THINGS_SEARCH_CLASSIFIER;
    }

    /**
     * Create a classification key from a string representing the string itself.
     *
     * @param string the string.
     * @return the classification key.
     */
    public static Classifier.Classification forString(final String string) {
        return new Identity<>(string);
    }

    /**
     * Create a classification key from the correlation ID of a signal.
     * Signals without correlation IDs are not classified to anything meaningful.
     *
     * @param signal the signal whose correlation ID is used for classification.
     * @return the classification key.
     * @throws java.lang.NullPointerException if the signal has no correlation ID.
     */
    public static Classifier.Classification forCorrelationId(final Signal<?> signal) {
        return forCorrelationId(signal.getDittoHeaders().getCorrelationId().orElse(null));
    }

    /**
     * Create a correlation ID classification key.
     *
     * @param correlationId the correlation ID.
     * @return the key for the correlation ID.
     * @throws java.lang.NullPointerException if the argument is null (but the argument is marked
     * {@code @Nullable} to centralize throwing of {@code NullPointerException}).
     */
    public static Classifier.Classification forCorrelationId(@Nullable final String correlationId) {
        return new CorrelationId(checkNotNull(correlationId, "correlationId"));
    }

    /**
     * Create a search-protocol subscription ID classification key.
     *
     * @param searchSubscriptionId the search-protocol subscription ID.
     * @return the key.
     */
    public static Classifier.Classification forThingsSearch(final String searchSubscriptionId) {
        return new SearchSubscriptionId(searchSubscriptionId);
    }

    /**
     * The classified streaming types.
     */
    public enum StreamingType implements Classifier.Classification {
        LIVE_COMMAND("START-SEND-LIVE-COMMANDS", "STOP-SEND-LIVE-COMMANDS"),
        LIVE_EVENT("START-SEND-LIVE-EVENTS", "STOP-SEND-LIVE-EVENTS"),
        LIVE_MESSAGE("START-SEND-MESSAGES", "STOP-SEND-MESSAGES"),
        TWIN_EVENT("START-SEND-EVENTS", "STOP-SEND-EVENTS");

        private final String startCommand;
        private final String stopCommand;
        private final String startAck;
        private final String stopAck;

        StreamingType(final String startCommand, final String stopCommand) {
            this.startCommand = startCommand;
            this.stopCommand = stopCommand;
            startAck = ack(startCommand);
            stopAck = ack(stopCommand);
        }

        /**
         * @return The protocol command to start streaming.
         */
        public String start() {
            return startCommand;
        }

        /**
         * @return The protocol command to stop streaming.
         */
        public String stop() {
            return stopCommand;
        }

        /**
         * @return The acknowledgement of {@code this#start()}.
         */
        public String startAck() {
            return startAck;
        }

        /**
         * @return The acknowledgement of {@code this#stop()}.
         */
        public String stopAck() {
            return stopAck;
        }


        private static String ack(final String command) {
            return command + ":ACK";
        }
    }

    private static abstract class Literal<T> implements Classifier.Classification {

        protected final T value;

        protected Literal(final T value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), value);
        }

        @Override
        public boolean equals(final Object o) {
            if (o.getClass() == getClass()) {
                return Objects.equals(value, getClass().cast(o).value);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + value + "]";
        }
    }

    private static final class CorrelationId extends Literal<String> {

        private CorrelationId(final String correlationId) {
            super(correlationId);
        }
    }

    private static final class SearchSubscriptionId extends Literal<String> {

        private SearchSubscriptionId(final String searchSubscriptionId) {
            super(searchSubscriptionId);
        }
    }

    private static final class Identity<T> extends Literal<T> {

        private Identity(final T value) {
            super(value);
        }

        private static <T> Optional<Classifier.Classification> of(final T value) {
            return Optional.of(new Identity<>(value));
        }
    }

    private static final class StreamingTypeClassifier implements Classifier<Adaptable> {

        @Override
        public Optional<Classification> classify(final Adaptable message) {
            final TopicPath topicPath = message.getTopicPath();
            if (topicPath.getGroup() == TopicPath.Group.THINGS) {
                switch (topicPath.getChannel()) {
                    case LIVE:
                        switch (topicPath.getCriterion()) {
                            case COMMANDS:
                                return Optional.of(StreamingType.LIVE_COMMAND);
                            case EVENTS:
                                return Optional.of(StreamingType.LIVE_EVENT);
                            case MESSAGES:
                                return Optional.of(StreamingType.LIVE_MESSAGE);
                        }
                    case TWIN:
                        if (topicPath.getCriterion() == TopicPath.Criterion.EVENTS) {
                            return Optional.of(StreamingType.TWIN_EVENT);
                        }
                }
            }
            return Optional.empty();
        }
    }

    private static final class ThingsSearchClassifier implements Classifier<Adaptable> {

        private static final EnumSet<TopicPath.SearchAction> SEARCH_EVENTS = EnumSet.of(
                TopicPath.SearchAction.HAS_NEXT,
                TopicPath.SearchAction.COMPLETE,
                TopicPath.SearchAction.FAILED
        );

        @Override
        public Optional<Classification> classify(final Adaptable message) {
            return message.getTopicPath().getSearchAction()
                    .filter(SEARCH_EVENTS::contains)
                    .flatMap(action -> message.getPayload().getValue())
                    .filter(JsonValue::isObject)
                    .flatMap(jsonValue -> jsonValue.asObject().getValue(SubscriptionEvent.JsonFields.SUBSCRIPTION_ID))
                    .map(SearchSubscriptionId::new);
        }
    }

    private static final class Instances {

        private static final Classifier<Adaptable> CORRELATION_ID_CLASSIFIER = adaptable ->
                adaptable.getHeaders()
                        .flatMap(DittoHeaders::getCorrelationId)
                        .map(CorrelationId::new);

        private static final Classifier<Adaptable> STREAMING_TYPE_CLASSIFIER = new StreamingTypeClassifier();

        private static final Classifier<Adaptable> THINGS_SEARCH_CLASSIFIER = new ThingsSearchClassifier();
    }
}
