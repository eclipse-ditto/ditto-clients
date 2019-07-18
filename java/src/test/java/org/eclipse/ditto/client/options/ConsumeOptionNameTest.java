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
package org.eclipse.ditto.client.options;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link OptionName.Modify}.
 */
public final class ConsumeOptionNameTest {

    /**
     *
     */
    @Test
    public void testAgainstValidStringValueReturnsTrue() {
        final String optionNameValue = OptionName.Modify.RESPONSE_REQUIRED.toString();

        assertThat(OptionName.Modify.RESPONSE_REQUIRED.test(optionNameValue)).isTrue();
    }

    /**
     *
     */
    @Test
    public void testAgainstInvalidStringValueReturnsFalse() {
        assertThat(OptionName.Modify.RESPONSE_REQUIRED.test("Fnord")).isFalse();
    }

    /**
     *
     */
    @Test
    public void testAgainstSameOptionNameReturnsTrue() {
        final OptionName optionName = OptionName.Modify.RESPONSE_REQUIRED;

        assertThat(optionName.test(optionName)).isTrue();
    }

}
