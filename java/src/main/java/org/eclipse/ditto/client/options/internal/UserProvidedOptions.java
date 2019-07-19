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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.options.Option;

/**
 * This class wraps the options that a user provided by calling a create method for example. It utilizes the Visitor
 * Pattern to make evaluation of those options more convenient than having to iterate over and analyze the plain option
 * array.
 * <p>
 * Furthermore this class validates the integrity of user provided options, for example if an option name was given more
 * than once or if mutually exclusive options were provided.
 *
 * @since 1.0.0
 */
@Immutable
final class UserProvidedOptions {

    private final Set<Option<?>> options;

    private UserProvidedOptions(final Set<Option<?>> theOptions) {
        options = theOptions;
    }

    /**
     * Returns a new instance of {@code UserProvidedOptions} which is based on the given options.
     *
     * @param options the options that were originally provided by the user.
     * @return the new {@code UserProvidedOptions}.
     * @throws NullPointerException if {@code options} is {@code null}.
     * @throws IllegalArgumentException if two options have the same name but different values.
     */
    public static UserProvidedOptions of(final Option<?>[] options) {
        checkNotNull(options, "options");

        final Set<Option<?>> optionsAsSet = new LinkedHashSet<>(options.length);
        Collections.addAll(optionsAsSet, options);

        final Consumer<Option<?>[]> optionsValidator = new OptionsValidator();
        optionsValidator.accept(optionsAsSet.toArray(new Option<?>[optionsAsSet.size()]));

        return new UserProvidedOptions(optionsAsSet);
    }

    /**
     * This method accepts the given visitor. The visitor is provided with each user provided option. The option then is
     * evaluated by the visitor. If the visitor found what it was interested in it returns the boolean {@code true} to
     * stop looping over the remaining options.
     *
     * @param visitor a visitor which is interested in at at least one user provided option.
     * @throws NullPointerException if {@code visitor} is {@code null}.
     */
    public void accept(final OptionVisitor<?> visitor) {
        checkNotNull(visitor, "visitor");

        final Iterator<Option<?>> optionIterator = options.iterator();
        boolean isVisitorFinished = false;
        while (optionIterator.hasNext() && !isVisitorFinished) {
            final Option<?> option = optionIterator.next();
            isVisitorFinished = visitor.visit(option);
        }
    }

}
