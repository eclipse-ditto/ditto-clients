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
package org.eclipse.ditto.client.internal;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.signals.base.Signal;

/**
 * Factory class for classifiers.
 * TODO: javadoc
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

    public static Object forCorrelationId(final Signal<?> signal) {
        return forCorrelationId(signal.getDittoHeaders().getCorrelationId()
                .orElseThrow(() -> new IllegalArgumentException("No correlation ID found: " + signal)));
    }

    public static Object forCorrelationId(final String correlationId) {
        return new CorrelationId(correlationId);
    }

    public static Classifier<Adaptable> correlationId() {
        return Instances.CORRELATION_ID_CLASSIFIER;
    }

    private static final class CorrelationId {

        private final String correlationId;

        private CorrelationId(final String correlationId) {
            this.correlationId = correlationId;
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

    private static final class Instances {

        private static final Classifier<Adaptable> CORRELATION_ID_CLASSIFIER =
                adaptable -> adaptable.getHeaders()
                        .flatMap(DittoHeaders::getCorrelationId)
                        .map(CorrelationId::new);
    }
}
