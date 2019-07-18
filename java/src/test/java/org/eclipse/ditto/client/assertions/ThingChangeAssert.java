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
package org.eclipse.ditto.client.assertions;

import org.eclipse.ditto.client.changes.ThingChange;

/**
 * Custom assertions for {@link ThingChange}.
 */
public final class ThingChangeAssert extends AbstractThingChangeAssert<ThingChangeAssert, ThingChange> {

    /**
     * Constructs a new {@code ThingChangeAssert} object.
     *
     * @param actual the {@code ThingChange} to be checked.
     */
    public ThingChangeAssert(final ThingChange actual) {
        super(actual, ThingChangeAssert.class);
    }

}
