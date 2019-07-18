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

import java.util.concurrent.CompletableFuture;

import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributeResponse;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommand;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link SendTerminator}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class SendTerminatorTest {

    private static final String CORRELATION_ID = "0815-4711-2342-3311";
    private static final String THING_ID = "com.example:my-thing";

    private static DittoHeaders dittoHeaders;

    @Mock
    private MessagingProvider messagingProvider;

    @Mock
    private ThingModifyCommand command;

    private ResponseForwarder responseForwarder;

    @BeforeClass
    public static void initDittoHeaders() {
        dittoHeaders = DittoHeaders.newBuilder().correlationId(CORRELATION_ID).build();
    }

    @Before
    public void setUp() {
        Mockito.when(command.getDittoHeaders()).thenReturn(dittoHeaders);
        responseForwarder = ResponseForwarder.getInstance();
    }

    @Test
    public void tryToSendMessageWithoutMessageBeingSet() {
        final SendTerminator underTest =
                new SendTerminator(messagingProvider, responseForwarder, TopicPath.Channel.TWIN, command);

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(underTest::send)
                .withMessage("A message must be present in order to call send!")
                .withNoCause();
    }

    @Test
    public void sendMessageWorksAsExpected() {
        final Message<?> message = Mockito.mock(Message.class);
        final SendTerminator<?> underTest = new SendTerminator<>(messagingProvider, responseForwarder, message);

        underTest.send();

        Mockito.verify(messagingProvider).send(Mockito.eq(message), Mockito.eq(TopicPath.Channel.LIVE));
    }

    @Test
    public void sendModifyCommandWithResponseRequiredAndHandleResponse() {
        final TopicPath.Channel channel = TopicPath.Channel.TWIN;
        final ThingCommandResponse commandResponse =
                DeleteAttributeResponse.of(THING_ID, JsonPointer.of("/foo"), dittoHeaders);
        final SendTerminator<String> underTest =
                new SendTerminator<>(messagingProvider, responseForwarder, channel, command);

        final CompletableFuture<String> promise = underTest.applyModify(ThingCommandResponse::getName);
        final boolean isPromiseDoneBeforeBeingHandled = promise.isDone();
        responseForwarder.handle(commandResponse);

        Mockito.verify(messagingProvider).sendCommand(Mockito.eq(command), Mockito.eq(channel));
        assertThat(isPromiseDoneBeforeBeingHandled).isFalse();
        assertThat(promise).isCompletedWithValue(commandResponse.getName());
    }

    @Test
    public void sendModifyCommandWithoutResponseRequired() {
        final TopicPath.Channel channel = TopicPath.Channel.TWIN;
        final DittoHeaders dittoHeadersWithoutResponseRequired = SendTerminatorTest.dittoHeaders.toBuilder()
                .responseRequired(false)
                .build();
        Mockito.when(command.getDittoHeaders()).thenReturn(dittoHeadersWithoutResponseRequired);
        final SendTerminator<String> underTest =
                new SendTerminator<>(messagingProvider, responseForwarder, channel, command);

        final CompletableFuture<String> promise = underTest.applyModify(response -> null);

        Mockito.verify(messagingProvider).sendCommand(Mockito.eq(command), Mockito.eq(channel));
        assertThat(promise).isCompletedWithValue(null);
    }

    @Test
    public void applyCommandWithVoidSemantics() {
        final TopicPath.Channel channel = TopicPath.Channel.TWIN;
        final ThingCommandResponse commandResponse = Mockito.mock(ThingCommandResponse.class);
        Mockito.when(commandResponse.getDittoHeaders()).thenReturn(dittoHeaders);
        final SendTerminator<String> underTest =
                new SendTerminator<>(messagingProvider, responseForwarder, channel, command);

        final CompletableFuture<Void> promise = underTest.applyVoid();
        final boolean isPromiseDoneBeforeBeingHandled = promise.isDone();
        responseForwarder.handle(commandResponse);

        Mockito.verify(messagingProvider).sendCommand(Mockito.eq(command), Mockito.eq(channel));
        assertThat(isPromiseDoneBeforeBeingHandled).isFalse();
        assertThat(promise).isCompletedWithValue(null);
    }

}
