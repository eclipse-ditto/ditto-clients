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

import java.util.Optional;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.model.things.ThingId;

/**
 *
 */
public abstract class AbstractThingChangeAssert<S extends AbstractAssert<S, T>, T extends Change>
        extends AbstractAssert<S, T> {

    /**
     * Constructs a new {@code AbstractThingChangeAssert} object.
     *
     * @param actual the {@code ThingChange} to be checked.
     */
    protected AbstractThingChangeAssert(final T actual, final Class<S> selfType) {
        super(actual, selfType);
    }

    public S hasThingId(final ThingId expectedThingId) {
        return assertThatEqual(expectedThingId, actual.getEntityId(), "Thing identifier");
    }

    public S isAdded() {
        return assertThatEqual(ChangeAction.CREATED, actual.getAction(), "action");
    }

    public S isUpdated() {
        return assertThatEqual(ChangeAction.UPDATED, actual.getAction(), "action");
    }

    public S isDeleted() {
        return assertThatEqual(ChangeAction.DELETED, actual.getAction(), "action");
    }

    protected <P> S assertThatEqual(final P expected, final P actual, final String propertyName) {
        isNotNull();
        Assertions.assertThat(actual)
                .overridingErrorMessage("Expected change to have %s \n<%s> but it had \n<%s>", propertyName, expected,
                        actual)
                .isEqualTo(expected);
        return myself;
    }

    protected <P> S assertThatEqual(final P expected, final Optional<P> actual, final String propertyName) {
        isNotNull();
        Assertions.assertThat(actual)
                .overridingErrorMessage("Expected change to have %s \n<Optional[%s]> but it had \n<%s>", propertyName,
                        expected, actual)
                .contains(expected);
        return myself;
    }

}
