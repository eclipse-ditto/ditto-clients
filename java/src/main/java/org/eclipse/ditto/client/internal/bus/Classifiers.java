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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.Signal;

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
        return Optional::of;
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
     * Create a classification key from the correlation ID of a signal.
     * Signals without correlation IDs are not classified to anything meaningful.
     *
     * @param signal the signal whose correlation ID is used for classification.
     * @return the classification key.
     */
    public static Object forCorrelationId(final Signal<?> signal) {
        return signal.getDittoHeaders()
                .getCorrelationId()
                .map(Classifiers::forCorrelationId)
                .orElseGet(Object::new);
    }

    /**
     * Create a correlation ID classification key.
     *
     * @param correlationId the correlation ID.
     * @return the key for the correlation ID.
     * @throws java.lang.NullPointerException if the argument is null.
     */
    public static Object forCorrelationId(@Nullable final String correlationId) {
        return new CorrelationId(checkNotNull(correlationId, "correlationId"));
    }

    /**
     * The classified streaming types.
     */
    public enum StreamingType {
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

    private static final class CorrelationId {

        private final String correlationId;

        private CorrelationId(final String correlationId) {
            this.correlationId = checkNotNull(correlationId, "correlationId");
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), correlationId);
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof CorrelationId) {
                return Objects.equals(correlationId, ((CorrelationId) o).correlationId);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + correlationId + "]";
        }
    }

    private static final class StreamingTypeClassifier implements Classifier<Adaptable> {

        @Override
        public Optional<Object> classify(final Adaptable message) {
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

    private static final class Instances {

        private static final Classifier<Adaptable> CORRELATION_ID_CLASSIFIER = adaptable ->
                adaptable.getHeaders()
                        .flatMap(DittoHeaders::getCorrelationId)
                        .map(CorrelationId::new);

        private static final Classifier<Adaptable> STREAMING_TYPE_CLASSIFIER = new StreamingTypeClassifier();
    }
}
