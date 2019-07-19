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

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;

/**
 * Custom assertions for {@link Change}.
 */
public final class ChangeAssert
        extends AbstractThingChangeAssert<ChangeAssert, Change> {

    /**
     * Constructs a new {@code ThingAttributeChangeAssert} object.
     *
     * @param actual the {@code ThingChange} to be checked.
     */
    public ChangeAssert(final Change actual) {
        super(actual, ChangeAssert.class);
    }

    public ChangeAssert hasPath(final JsonPointer expectedPath) {
        return assertThatEqual(expectedPath, actual.getPath(), "path");
    }

    public ChangeAssert hasAttributeValue(final JsonValue expectedValue) {
        return assertThatEqual(expectedValue, actual.getValue(), "attribute value");
    }

}
