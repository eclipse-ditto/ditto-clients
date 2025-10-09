/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.options.internal;

import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.json.JsonPointer;


/**
 * This visitor fetches and provides the value as {@code Map<JsonPointer, String>} for the option with name
 * {@link OptionName.Global#MERGE_THING_PATCH_CONDITIONS} from the user provided options.
 *
 * @since 3.8.0
 */
@ThreadSafe
final class MergeThingPatchConditionsOptionVisitor extends AbstractOptionVisitor<Map<JsonPointer, String>> {

    /**
     * Constructs a {@code MergeThingPatchConditionsOptionVisitor} object.
     */
    MergeThingPatchConditionsOptionVisitor() {
        super(OptionName.Global.MERGE_THING_PATCH_CONDITIONS);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<JsonPointer, String> getValueFromOption(final Option<?> option) {
        return option.getValueAs(Map.class);
    }

}
