/*
 * Copyright Bosch Software Innovations GmbH 2019
 *
 * All rights reserved, also regarding any disposal, exploitation,
 * reproduction, editing, distribution, as well as in the event of
 * applications for industrial property rights.
 *
 * This software is the confidential and proprietary information
 * of Bosch Software Innovations GmbH. You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you
 * entered into with Bosch Software Innovations GmbH.
 */
package org.eclipse.ditto.client.configuration.internal;

import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link DummyAuthenticationConfiguration}.
 */
public final class DummyAuthenticationConfigurationTest {

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DummyAuthenticationConfiguration.class)
                .usingGetClass()
                .verify();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(DummyAuthenticationConfiguration.class,
                areImmutable());
    }

}