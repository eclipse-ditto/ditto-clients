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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.client.TestConstants.Thing.THING_ID;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotAccessibleException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link ResponseForwarder}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class ResponseForwarderTest {

    private static final String CORRELATION_ID = "0815-4711-2342-3311";

    private static DittoHeaders dittoHeaders;

    @Mock
    private ThingCommandResponse commandResponse;

    private CompletableFuture<ThingCommandResponse> responsePromise;
    private ResponseForwarder underTest;

    @BeforeClass
    public static void initDittoHeaders() {
        dittoHeaders = DittoHeaders.newBuilder().correlationId(CORRELATION_ID).build();
    }

    @Before
    public void setUp() {
        Mockito.when(commandResponse.getDittoHeaders()).thenReturn(dittoHeaders);
        responsePromise = new CompletableFuture<>();
        underTest = ResponseForwarder.getInstance();
    }

    @Test
    public void tryToPutResponsePromiseWithNullCorrelationId() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> underTest.put(null, new CompletableFuture<>()))
                .withMessage("The %s must not be null!", "correlationId")
                .withNoCause();
    }

    @Test
    public void tryToPutResponsePromiseWithEmptyCorrelationId() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> underTest.put("", new CompletableFuture<>()))
                .withMessage("The argument '%s' must not be empty!", "correlationId")
                .withNoCause();
    }

    @Test
    public void tryToPutNullResponsePromise() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> underTest.put(CORRELATION_ID, null))
                .withMessage("The %s must not be null!", "response promise")
                .withNoCause();
    }

    @Test
    public void putResponsePromiseWithoutConflictWorksAsExpected() {
        assertThat(underTest.put(CORRELATION_ID, responsePromise)).isEqualTo(responsePromise);
    }

    @Test
    public void putSameResponsePromiseTwiceReturnsTrue() {
        underTest.put(CORRELATION_ID, responsePromise);

        assertThat(underTest.put(CORRELATION_ID, responsePromise)).isEqualTo(responsePromise);
    }

    @Test
    public void putDifferentResponsePromisesForSameCorrelationIdTriggersConflictHandling() {
        final CompletableFuture<ThingCommandResponse> initialResponsePromise = responsePromise;
        final CompletableFuture<ThingCommandResponse> anotherResponsePromise = new CompletableFuture<>();
        underTest.put(CORRELATION_ID, initialResponsePromise);

        assertThat(underTest.put(CORRELATION_ID, anotherResponsePromise)).isEqualTo(initialResponsePromise);
        assertThat(initialResponsePromise)
                .hasFailedWithThrowableThat()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A new response promise was associated with correlation-id <%s>!", CORRELATION_ID);
    }

    @Test
    public void tryToHandleNullResponse() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> underTest.handle(null))
                .withMessage("The %s must not be null!", "ThingCommandResponse to be handled")
                .withNoCause();
    }

    @Test
    public void handleCommandResponseWhichHasNoPromiseReturnsEmptyOptional() {
        final Optional<CompletableFuture<ThingCommandResponse>> handledPromise = underTest.handle(commandResponse);

        assertThat(handledPromise).isEmpty();
    }

    @Test
    public void handleCommandResponseWithoutCorrelationIdReturnsEmptyOptional() {
        Mockito.when(commandResponse.getDittoHeaders()).thenReturn(DittoHeaders.empty());

        final Optional<CompletableFuture<ThingCommandResponse>> handledPromise = underTest.handle(commandResponse);

        assertThat(handledPromise).isEmpty();
    }

    @Test
    public void handleCommandResponseWithoutException() {
        underTest.put(CORRELATION_ID, responsePromise);

        final Optional<CompletableFuture<ThingCommandResponse>> handledPromise = underTest.handle(commandResponse);

        assertThat(handledPromise).contains(responsePromise);
    }

    @Test
    public void handleThingErrorResponse() {
        final DittoRuntimeException exception = ThingNotAccessibleException.newBuilder(THING_ID).build();
        final ThingErrorResponse thingErrorResponse = ThingErrorResponse.of(exception, dittoHeaders);
        underTest.put(CORRELATION_ID, responsePromise);

        final Optional<CompletableFuture<ThingCommandResponse>> handledPromise = underTest.handle(thingErrorResponse);

        assertThat(handledPromise).contains(responsePromise);
        assertThat(handledPromise.get())
                .hasFailedWithThrowableThat()
                .isEqualTo(exception);
    }

}
