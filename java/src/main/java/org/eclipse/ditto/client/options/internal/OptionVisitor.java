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

import java.util.Optional;

import org.eclipse.ditto.client.options.Option;

/**
 * The purpose of an option visitor is to get the value of a particular user provided option. The option of interest is
 * determined by its OptionName. The visitor supposes that the value of an option with a particular name has a
 * particular type; for example that the value of an option for a timeout is a {@code long} and nothing else.
 *
 * @since 1.0.0
 */
interface OptionVisitor<T> {

    /**
     * Examines the given option. If the option is the one this visitor is interested in, the option's value is obtained
     * appropriately typed.
     *
     * @param option the option to get the value from.
     * @return {@code true} the desired value is found and thus the work of this visitor is done, {@code false} else.
     * @throws IllegalArgumentException if the type of the {@code option}'s value is unexpected.
     */
    boolean visit(Option<?> option);

    /**
     * Returns the found option value as Optional.
     *
     * @return an Optional containing the found option value or being empty.
     */
    Optional<T> getValue();

}
