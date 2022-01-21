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
package org.eclipse.ditto.client.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.eclipse.ditto.client.internal.bus.JsonPointerSelector;
import org.eclipse.ditto.client.internal.bus.PointerBus;
import org.eclipse.ditto.client.internal.bus.PointerWithData;
import org.eclipse.ditto.client.internal.bus.Registration;
import org.eclipse.ditto.client.registration.DuplicateRegistrationIdException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests {@link HandlerRegistry}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HandlerRegistryTest {

    private static final String KNOWN_REG_ID = "regId";
    @Mock
    private PointerBus busMock;
    @Mock
    private JsonPointerSelector selectorMock;
    @Mock
    private Consumer<PointerWithData<?>> consumerMock;
    @Mock
    private Registration<Consumer<? extends PointerWithData<?>>> registrationMock;
    private HandlerRegistry<?, ?> registry;


    @Before
    public void before() {
        registry = new HandlerRegistry<>(busMock);

        when(busMock.on(any(JsonPointerSelector.class), any(Consumer.class))).thenReturn(registrationMock);
    }

    @Test(expected = NullPointerException.class)
    public void constructorWithNullBus() {
        new HandlerRegistry<>(null);
    }

    @Test
    public void registerWithNewRegistrationId() {
        // test
        registry.register(KNOWN_REG_ID, selectorMock, consumerMock);

        // verify
        verify(busMock).on(selectorMock, consumerMock);
    }

    @Test
    public void registerWithAlreadyExistingRegistrationId() {
        // prepare
        registry.register(KNOWN_REG_ID, selectorMock, consumerMock);
        reset(busMock);

        // test
        try {
            registry.register(KNOWN_REG_ID, selectorMock, consumerMock);
            fail("Expected: " + DuplicateRegistrationIdException.class.getName());
        } catch (final DuplicateRegistrationIdException e) {
            // expected
        }

        // verify
        verify(busMock, never()).on(selectorMock, consumerMock);
    }

    @Test
    public void registerTwoConsumersWithDifferentRegistrationId() {
        // test
        registry.register(KNOWN_REG_ID, selectorMock, consumerMock);
        registry.register("reg2", selectorMock, consumerMock);

        // verify
        verify(busMock, times(2)).on(selectorMock, consumerMock);
    }

    @Test
    public void deregisterWithKnownRegistrationId() {
        // prepare
        registry.register(KNOWN_REG_ID, selectorMock, consumerMock);

        // test
        final boolean deregistered = registry.deregister(KNOWN_REG_ID);

        // verify
        assertTrue(deregistered);
        verify(registrationMock).cancel();
    }

    @Test
    public void deregisterWithUnknownRegistrationId() {
        // test
        final boolean deregistered = registry.deregister("unknown");

        // verify
        assertFalse(deregistered);
    }

    @Test
    public void deregisterAndRegisterAgain() {
        // prepare
        registry.register(KNOWN_REG_ID, selectorMock, consumerMock);
        assertTrue(registry.deregister(KNOWN_REG_ID));
        reset(busMock);

        // test
        registry.register(KNOWN_REG_ID, selectorMock, consumerMock);

        // verify
        verify(busMock).on(selectorMock, consumerMock);
    }
}
