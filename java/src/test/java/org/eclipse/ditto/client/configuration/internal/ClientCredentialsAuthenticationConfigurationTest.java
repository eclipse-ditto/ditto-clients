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
package org.eclipse.ditto.client.configuration.internal;

import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration}.
 */
public final class ClientCredentialsAuthenticationConfigurationTest {

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ClientCredentialsAuthenticationConfiguration.class)
                .usingGetClass()
                .verify();
    }

}