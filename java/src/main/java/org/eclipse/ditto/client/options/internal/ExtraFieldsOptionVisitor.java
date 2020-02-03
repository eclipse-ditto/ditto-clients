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
package org.eclipse.ditto.client.options.internal;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.json.JsonFieldSelector;

/**
 * This visitor fetches and provides the value as {@code JsonFieldSelector} for the option with name {@link
 * org.eclipse.ditto.client.options.OptionName.Consumption#EXTRA_FIELDS} from the user provided options.
 *
 * @since 1.1.0
 */
@ThreadSafe
final class ExtraFieldsOptionVisitor extends AbstractOptionVisitor<JsonFieldSelector> {

    /**
     * Constructs a new {@code ExtraFieldsOptionVisitor} object.
     */
    ExtraFieldsOptionVisitor() {
        super(OptionName.Consumption.EXTRA_FIELDS);
    }

    @Override
    protected JsonFieldSelector getValueFromOption(final Option<?> option) {
        return option.getValueAs(JsonFieldSelector.class);
    }

}
