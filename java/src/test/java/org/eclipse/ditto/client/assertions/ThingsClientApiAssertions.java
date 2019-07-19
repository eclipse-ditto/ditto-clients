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
package org.eclipse.ditto.client.assertions;

import org.eclipse.ditto.client.options.Option;

/**
 * Custom test assertions for the Ditto Client API.
 */
public class ThingsClientApiAssertions {

    public static <T> OptionAssert<T> assertThat(final Option<T> option) {
        return new OptionAssert<>(option);
    }

}
