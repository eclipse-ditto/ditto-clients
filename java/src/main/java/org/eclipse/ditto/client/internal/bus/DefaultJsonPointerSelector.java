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
package org.eclipse.ditto.client.internal.bus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonPointer;

/**
 * Default implementation of {@link JsonPointerSelector} which is capable of dealing with {@link JsonPointer}s as well
 * as the wrapper object {@link JsonPointerWithChangePaths}.
 *
 * @since 1.0.0
 */
final class DefaultJsonPointerSelector implements JsonPointerSelector {

    private final JsonPointer jsonPointer;

    private DefaultJsonPointerSelector(final JsonPointer jsonPointer) {
        this.jsonPointer = jsonPointer;
    }

    /**
     * Creates a new instance of {@code JsonPointSelector} with the passed in String which is interpreted as {@link
     * JsonPointer}.
     *
     * @param jsonPointer the string representation of a JsonPointer.
     * @return the created JsonPointerSelector
     */
    static DefaultJsonPointerSelector jsonPointerSelector(final CharSequence jsonPointer) {
        return new DefaultJsonPointerSelector(JsonPointer.of(jsonPointer));
    }

    @Override
    public JsonPointer getPointer() {
        return jsonPointer;
    }

    @Override
    public boolean test(final JsonPointer target) {
        return matches(target);
    }

    @Override
    public boolean matches(@Nullable final JsonPointer pointer) {
        // method to be implemented for JsonPointerSelector mechanism - has to check whether the passed in "target"
        // (which could be e.g. a string) matches the configured "object key" (accessed by "getObject()").
        if (null == pointer) {
            return false;
        }

        final Class<?> type = pointer.getClass();
        if (JsonPointerWithChangePaths.class.isAssignableFrom(type)) // only JsonPointerWithChangePaths class
        {
            final JsonPointerWithChangePaths target = (JsonPointerWithChangePaths) pointer;
            final JsonPointer targetPointer = target.getTargetPath();

            // we need to add the target path for modification on empty objects
            final List<JsonPointer> targetPaths = new ArrayList<>(target.getChangePaths());
            targetPaths.add(JsonPointer.empty());

            return targetPaths.stream() // iterate over all configured "changePaths"
                    .anyMatch(
                            targetPath -> // and check if the "changePath" appended to the initial "targetPath" matches
                                    // the template key looked up via "getObject()":
                                    JsonPointerSelector.doesTargetMatchTemplate(targetPointer.append(targetPath),
                                            getPointer()));
        } else if (JsonPointer.class.isAssignableFrom(type)) // or JsonPointer class are supported
        {
            return JsonPointerSelector.doesTargetMatchTemplate(pointer, getPointer());
        } else {
            // for all other classes (e.g. String) we can very quickly respond:
            return false;
        }
    }

}
