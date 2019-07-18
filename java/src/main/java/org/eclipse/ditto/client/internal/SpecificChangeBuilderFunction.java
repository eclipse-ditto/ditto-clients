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
package org.eclipse.ditto.client.internal;

import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;

/**
 * Provides a FunctionInterface with a method which takes a {@link Change originating Change}, {@link JsonValue changed
 * Value}, {@link JsonPointer change Path} and a Map of resolved template parameters (the key is the template name, e.g.
 * "{featureId}" and the value the resolved value of this key).
 *
 * @param <C> the type of the specific {@link Change} which should be build by this Function
 * @since 1.0.0
 */
@FunctionalInterface
public interface SpecificChangeBuilderFunction<C extends Change> {

    /**
     * Builds a specific change of type {@code <C>}
     *
     * @param originatingChange the originating/root {@link Change}
     * @param changedValue the changed {@link JsonValue} which originates in the {@link Change#getValue()} but contains
     * the "plain" value
     * @param changePath the changed path
     * @param templateParams the resolved template parameters (the key is the template name, e.g. "{featureId}" and the
     * value the resolved value of this key)
     * @return the specific Change of type {@code <C>}
     */
    C buildSpecificChange(Change originatingChange, @Nullable JsonValue changedValue, JsonPointer changePath,
            Map<String, String> templateParams);

}
