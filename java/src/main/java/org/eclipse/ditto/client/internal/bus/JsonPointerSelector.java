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

import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonKey;
import org.eclipse.ditto.json.JsonPointer;

/**
 * Holds a JsonPointer and is capable to {@code match} or {@code test} against other JsonPointers.
 * <p>
 * Hierarchically matches "nested" JsonPointers against JsonPointer templates also considering placeholders. For example
 * the following {@code target} JsonPointer {@code /things/namespace:foo.bar/attributes/maker} would be positively
 * matched to various {@code template}s:
 * </p>
 * <ul>
 * <li>{@code /things/namespace:foo.bar}</li>
 * <li>{@code /things/namespace:foo.bar/attributes}</li>
 * <li>{@code /things/namespace:foo.bar/attributes/maker}</li>
 * <li>{@code /things/{thingId}}</li>
 * <li>{@code /things/{thingId}/attributes}</li>
 * <li>{@code /things/{thingId}/attributes/maker}</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface JsonPointerSelector extends Predicate<JsonPointer> {

    /**
     * @return the JsonPointer this selector holds.
     */
    JsonPointer getPointer();

    /**
     * Matches the JsonPointer this selector holds against the passed {@code pointer}.
     *
     * @param pointer the Pointer to match against.
     * @return true if the pointers match.
     */
    boolean matches(JsonPointer pointer);

    /**
     * Calculates whether the passed in {@code target} Pointer matches the passed in {@code template} Pointer either
     * exactly or partially, also considering used template strings (e.g. {@code "{thingId}"}) in the {@code template}
     * Pointer which would also match if at the same place in the {@code target} there would be an arbitrary value.
     *
     * @param target the target Pointer to check against the template
     * @param template the template to check against - may contain placeholders like e.g. {@code {thingId}}
     * @return whether the {@code target} matches the {@code template}
     */
    static boolean doesTargetMatchTemplate(final JsonPointer target, final JsonPointer template) {
        if (target.isEmpty()) {
            // if the target is empty, it always matches the template:
            return true;
        } else if (template.isEmpty()) {
            // if the template is empty, it never matches:
            return false;
        } else {
            // otherwise, we have to do some calculation
            for (int i = 0; i < template.getLevelCount(); i++) {
                final JsonKey targetKeyOnLevel =
                        target.get(i).orElse(null); // e.g.: /things/org.eclipse.ditto:my1/attributes/foo
                final JsonKey configuredKeyOnLevel =
                        template.get(i).orElse(null); // e.g.: /things/{thingId}/attributes/{attributeKey}

                final boolean match = matchesExactly(targetKeyOnLevel, configuredKeyOnLevel)
                        || matchesTemplateParam(targetKeyOnLevel, configuredKeyOnLevel);
                if (!match) {
                    return false;
                }
            }
            // if we passed the loop without "mismatch", we do have a match:
            return true;
        }
    }

    /*
     * Applies First rule: if both configured and target targetKey on the same level are equal we have a match.
     */
    static boolean matchesExactly(@Nullable final JsonKey targetKeyOnLevel,
            @Nullable final JsonKey configuredKeyOnLevel) {
        return (targetKeyOnLevel == null && configuredKeyOnLevel == null) ||
                (targetKeyOnLevel != null && targetKeyOnLevel.equals(configuredKeyOnLevel));
    }

    /*
     * Applies second rule: if the target JsonPointer has any targetKey at this level
     * and the configured JsonPointer has a "{variable}" placeholder as targetKey we have a match.
     */
    static boolean matchesTemplateParam(@Nullable final JsonKey targetKeyOnLevel,
            @Nullable final JsonKey configuredKeyOnLevel) {
        return targetKeyOnLevel != null && configuredKeyOnLevel != null
                && configuredKeyOnLevel.toString().matches("^\\{.*}$");
    }

}
