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

import org.eclipse.ditto.signals.base.Signal;

/**
 * Opaque type of classifications.
 */
public interface Classification {

    /**
     * Create a classification key from a string representing the string itself.
     *
     * @param string the string.
     * @return the classification key.
     */
    static Classification forString(final String string) {
        return new Identity<>(string);
    }

    /**
     * Create a classification key from the correlation ID of a signal.
     * Signals without correlation IDs are not classified to anything meaningful.
     *
     * @param signal the signal whose correlation ID is used for classification.
     * @return the classification key.
     * @throws NullPointerException if the signal has no correlation ID.
     */
    static Classification forCorrelationId(final Signal<?> signal) {
        return forCorrelationId(signal.getDittoHeaders().getCorrelationId().orElse(null));
    }

    /**
     * Create a correlation ID classification key.
     *
     * @param correlationId the correlation ID.
     * @return the key for the correlation ID.
     * @throws NullPointerException if the argument is null (but the argument is marked
     * {@code @Nullable} to centralize throwing of {@code NullPointerException}).
     */
    static Classification forCorrelationId(@Nullable final String correlationId) {
        return new CorrelationId(checkNotNull(correlationId, "correlationId"));
    }

    /**
     * Create a search-protocol subscription ID classification key.
     *
     * @param searchSubscriptionId the search-protocol subscription ID.
     * @return the key.
     */
    static Classification forThingsSearch(final String searchSubscriptionId) {
        return new SearchSubscriptionId(searchSubscriptionId);
    }

    /**
     * Check whether subscribers for this classification requires sequential dispatching.
     *
     * @return whether sequential dispatching is required.
     */
    default boolean mustBeSequential() {
        return false;
    }

    /**
     * The classified streaming types.
     */
    enum StreamingType implements Classification {
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

    abstract class Literal<T> implements Classification {

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
            if (o != null && o.getClass() == getClass()) {
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

    final class CorrelationId extends Literal<String> {

        private CorrelationId(final String correlationId) {
            super(correlationId);
        }
    }

    final class SearchSubscriptionId extends Literal<String> {

        private SearchSubscriptionId(final String searchSubscriptionId) {
            super(searchSubscriptionId);
        }

        @Override
        public boolean mustBeSequential() {
            // rule 1.3: onSubscribe, onNext, onError and onComplete signaled to a subscriber must be signaled serially.
            return true;
        }
    }

    final class Identity<T> extends Literal<T> {

        private Identity(final T value) {
            super(value);
        }

        static <T> Optional<Classification> of(final T value) {
            return Optional.of(new Identity<>(value));
        }
    }
}
