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

import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.TopicPath;
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
        return Classification.Identity::of;
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

    private static final class StreamingTypeClassifier implements Classifier<Adaptable> {

        @Override
        public Optional<Classification> classify(final Adaptable message) {
            final TopicPath topicPath = message.getTopicPath();
            if (topicPath.getGroup() == TopicPath.Group.THINGS) {
                switch (topicPath.getChannel()) {
                    case LIVE:
                        switch (topicPath.getCriterion()) {
                            case COMMANDS:
                                return Optional.of(Classification.StreamingType.LIVE_COMMAND);
                            case EVENTS:
                                return Optional.of(Classification.StreamingType.LIVE_EVENT);
                            case MESSAGES:
                                return Optional.of(Classification.StreamingType.LIVE_MESSAGE);
                        }
                    case TWIN:
                        if (topicPath.getCriterion() == TopicPath.Criterion.EVENTS) {
                            return Optional.of(Classification.StreamingType.TWIN_EVENT);
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
                    .map(Classification::forThingsSearch);
        }
    }

    private static final class Instances {

        private static final Classifier<Adaptable> CORRELATION_ID_CLASSIFIER = adaptable ->
                adaptable.getHeaders()
                        .flatMap(DittoHeaders::getCorrelationId)
                        .map(Classification::forCorrelationId);

        private static final Classifier<Adaptable> STREAMING_TYPE_CLASSIFIER = new StreamingTypeClassifier();

        private static final Classifier<Adaptable> THINGS_SEARCH_CLASSIFIER = new ThingsSearchClassifier();
    }
}
