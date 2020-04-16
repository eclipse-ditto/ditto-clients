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

import java.util.Optional;

/**
 * Classifier of a message.
 */
@FunctionalInterface
public interface Classifier<T> {

    /**
     * Classify a message for subscribers.
     *
     * @param message the message.
     * @return the classification of the message if any is known.
     */
    Optional<Classification> classify(T message);

}
