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
package org.eclipse.ditto.client.options.internal;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;


/**
 * This visitor fetches and provides the value as {@code Iterable<CharSequence>} for the option with name {@link
 * OptionName.Consumption#NAMESPACES} from the user provided options.
 *
 * @since 1.0.0
 */
@ThreadSafe
final class NamespacesOptionVisitor extends AbstractOptionVisitor<Iterable<CharSequence>> {

    /**
     * Constructs a new {@code NamespacesOptionVisitor} object.
     */
    NamespacesOptionVisitor() {
        super(OptionName.Consumption.NAMESPACES);
    }

    @Override
    protected Iterable<CharSequence> getValueFromOption(final Option<?> option) {
        return option.getValueAs(Iterable.class);
    }

}
