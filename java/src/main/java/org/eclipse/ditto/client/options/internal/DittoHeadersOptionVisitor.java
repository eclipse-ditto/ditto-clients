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
import org.eclipse.ditto.model.base.headers.DittoHeaders;

/**
 * This visitor fetches and provides the value as {@code DittoHeaders} for the option with name
 * {@link OptionName.Global#DITTO_HEADERS} from the user provided options.
 *
 * @since 1.1.0
 */
@ThreadSafe
final class DittoHeadersOptionVisitor extends AbstractOptionVisitor<DittoHeaders> {

    /**
     * Constructs a new {@code DittoHeadersOptionVisitor} object.
     */
    DittoHeadersOptionVisitor() {
        super(OptionName.Global.DITTO_HEADERS);
    }

    @Override
    protected DittoHeaders getValueFromOption(final Option<?> option) {
        return option.getValueAs(DittoHeaders.class);
    }

}
