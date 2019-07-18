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

import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;


/**
 * This consumer checks the integrity of an array of user provided {@link Option}s. By now it only ensures that an
 * option name is provided only once. If the user provided options are invalid an {@code IllegalArgumentException} with
 * an appropriate detail message is thrown.
 *
 * @since 1.0.0
 */
@Immutable
final class OptionsValidator implements Consumer<Option<?>[]> {

    private static void checkIfAnyOptionNameAppearsMoreThanOnce(final Option<?>[] options) {
        for (int i = 0; i < options.length; i++) {
            for (int j = options.length - 1; j > i; j--) {
                checkIfNamesAreEqual(options[i], options[j]);
            }
        }
    }

    private static void checkIfNamesAreEqual(final Option<?> green, final Option<?> blue) {
        final OptionName greenName = green.getName();
        if (Objects.equals(greenName, blue.getName())) {
            final String msgTemplate = "You provided at least two options with name <{0}> but different value!";
            throw new IllegalArgumentException(MessageFormat.format(msgTemplate, greenName));
        }
    }

    @Override
    public void accept(final Option<?>[] options) {
        checkIfAnyOptionNameAppearsMoreThanOnce(options);
    }

}
