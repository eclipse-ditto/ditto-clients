/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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


/**
 * This visitor fetches and provides the value as {@code String} for the option with name
 * {@link OptionName.Global#CONDITION} from the user provided options.
 *
 * @since 2.1.0
 */
@ThreadSafe
final class ConditionOptionVisitor extends AbstractOptionVisitor<String> {

    /**
     * Constructs a {@code ConditionOptionVisitor} object.
     */
    ConditionOptionVisitor() {
        super(OptionName.Global.CONDITION);
    }

    @Override
    protected String getValueFromOption(final Option<?> option) {
        return option.getValueAs(String.class);
    }

}
