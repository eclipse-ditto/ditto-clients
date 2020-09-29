/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingException;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.AfterClass;
import org.junit.Test;

import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.WebSocket;

/**
 * Tests the error handling behavior of websocket messaging provider.
 */
public final class WebSocketMessagingProviderTest {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @AfterClass
    public static void shutdownExecutor() {
        EXECUTOR.shutdownNow();
    }

    @Test
    public void connectToUnknownHostWithErrorConsumer() throws Exception {
        final BlockingQueue<Throwable> errors = new LinkedBlockingQueue<>();
        final AtomicReference<WebSocketMessagingProvider> messagingProviderReference = new AtomicReference<>();
        final ExecutorService e = Executors.newSingleThreadExecutor();
        final WebSocketMessagingProvider underTest =
                WebSocketMessagingProvider.newInstance(configOf("ws://unknown.host.invalid:80", error -> {
                            messagingProviderReference.get().close();
                            errors.add(error);
                        }),
                        dummyAuth(), e);
        messagingProviderReference.set(underTest);

        // WHEN: websocket connect to a nonsense address
        // THEN: the calling thread receives a CompletionException
        assertThatExceptionOfType(CompletionException.class).isThrownBy(underTest::initialize);

        // THEN: the error handler is notified exactly once
        assertThat(errors.poll(2L, TimeUnit.SECONDS))
                .isInstanceOf(MessagingException.class)
                .hasCauseInstanceOf(UnknownHostException.class);
        expectNoMsg(errors);
    }

    @Test(timeout = 10_000)
    public void serviceUnavailable() throws Exception {
        final int numberOfRecoverableErrors = 3;
        final BlockingQueue<ServerSocket> serverSocket = new LinkedBlockingQueue<>();
        CompletableFuture.runAsync(() -> {
            try (final ServerSocket s = new ServerSocket(0)) {
                serverSocket.add(s);
                for (int i = 0; i < numberOfRecoverableErrors; ++i) {
                    write(s.accept(), "HTTP/1.0 503 Server hurt itself in its confusion!");
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
        final BlockingQueue<Throwable> errors = new LinkedBlockingQueue<>();
        final MessagingConfiguration config =
                configOf("ws://127.0.0.1:" + serverSocket.take().getLocalPort(), errors::add);
        final WebSocketMessagingProvider underTest =
                WebSocketMessagingProvider.newInstance(config, dummyAuth(), EXECUTOR);

        // WHEN: websocket connect to an unavailable address
        final CompletableFuture<?> future = underTest.initializeAsync().toCompletableFuture();

        // THEN: the error handler is notified many times
        for (int i = 0; i < numberOfRecoverableErrors; ++i) {
            assertThat(errors.poll(2L, TimeUnit.SECONDS))
                    .isInstanceOf(MessagingException.class)
                    .hasCauseInstanceOf(OpeningHandshakeException.class);
        }

        // THEN: the calling thread of .initialize blocks until the client is destroyed,
        // upon which an exception is thrown
        underTest.close();
        assertThatExceptionOfType(ExecutionException.class)
                .isThrownBy(() -> future.get(5L, TimeUnit.SECONDS))
                .withCauseInstanceOf(MessagingException.class);
    }

    private MessagingConfiguration configOf(final String uri, final Consumer<Throwable> errorHandler) {
        return WebSocketMessagingConfiguration.newBuilder()
                .jsonSchemaVersion(JsonSchemaVersion.V_2)
                .reconnectEnabled(true)
                .initialConnectRetryEnabled(true)
                .endpoint(uri)
                .connectionErrorHandler(errorHandler)
                .build();
    }

    private AuthenticationProvider<WebSocket> dummyAuth() {
        return AuthenticationProviders.basic(
                BasicAuthenticationConfiguration.newBuilder().username("dummy").password("auth").build());
    }

    private static void write(final Socket socket, final String line) throws Exception {
        final PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.println(line);
        writer.println();
        writer.flush();
    }

    private static void expectNoMsg(final BlockingQueue<?> queue) {
        try {
            assertThat(queue.poll(2500, TimeUnit.MILLISECONDS))
                    .describedAs("Expect no more notifications, but got one.")
                    .isNull();
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

}
