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
package org.eclipse.ditto.client.messaging.internal;

import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.junit.Test;

/**
 * Unit test for {@link ClientCredentialsJsonWebTokenSupplier}.
 */
public final class ClientCredentialsJsonWebTokenSupplierTest {

    @Test
    public void assertImmutability() {
        assertInstancesOf(ClientCredentialsJsonWebTokenSupplier.class, areImmutable(),
                provided(ClientCredentialsAuthenticationConfiguration.class).isAlsoImmutable());
    }

}