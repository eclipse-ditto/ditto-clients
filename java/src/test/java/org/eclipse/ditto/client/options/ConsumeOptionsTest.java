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

import static org.eclipse.ditto.client.assertions.ThingsClientApiAssertions.assertThat;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.junit.Test;

/**
 * Unit test for {@link Options.Modify}.
 */
public final class ConsumeOptionsTest {

    /**
     *
     */
    @Test
    public void assertImmutability() {
        assertInstancesOf(Options.Modify.class, areImmutable());
    }

    /**
     *
     */
    @Test
    public void responseTimeoutWithDurationReturnsExpectedOption() {
        final boolean responseRequired = false;
        final Option<Boolean> option = Options.Modify.responseRequired(responseRequired);

        assertThat(option).hasName(OptionName.Modify.RESPONSE_REQUIRED).hasValue(responseRequired);
    }

}
