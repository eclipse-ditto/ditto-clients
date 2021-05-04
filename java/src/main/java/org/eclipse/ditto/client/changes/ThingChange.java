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
package org.eclipse.ditto.client.changes;

import java.util.Optional;

import org.eclipse.ditto.things.model.Thing;

/**
 * Represents a change of a {@link Thing}.
 *
 * @since 1.0.0
 */
public interface ThingChange extends Change {

    /**
     * Returns the {@code Thing} which changed. May be empty if a sub-element was deleted.
     *
     * @return the {@code Thing} which changed.
     */
    Optional<Thing> getThing();

}
