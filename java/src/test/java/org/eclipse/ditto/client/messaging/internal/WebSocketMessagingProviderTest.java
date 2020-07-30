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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Tests the error handling behavior of websocket messaging provider.
 */
public final class WebSocketMessagingProviderTest {

    @Test(timeout = 5000)
    public void connectToPort0() throws Exception {
        final BlockingQueue<ServerSocket> serverSocket = new LinkedBlockingQueue<>();
        CompletableFuture.runAsync(() -> {
            try (final ServerSocket s = new ServerSocket(0)) {
                serverSocket.add(s);
                final Socket ss = s.accept();
                ss.close();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
        final ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        final MessagingConfiguration config = WebSocketMessagingConfiguration.newBuilder()
                .jsonSchemaVersion(JsonSchemaVersion.V_2)
                .reconnectEnabled(true)
                .endpoint("ws://127.0.0.1:" + serverSocket.take().getLocalPort())
                .connectionErrorHandler(errors::add)
                .reconnectEnabled(true)
                .build();
        final AuthenticationProvider<WebSocket> auth = AuthenticationProviders.basic(
                BasicAuthenticationConfiguration.newBuilder()
                        .username("username")
                        .password("password")
                        .build()
        );
        final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor();
        final WebSocketMessagingProvider underTest =
                WebSocketMessagingProvider.newInstance(config, auth, callbackExecutor);

        // WHEN: websocket connect to a nonsense address
        // THEN: the calling thread should not block nor throw any exception
        underTest.initialize();

        // THEN: the error handler is notified exactly once
        Thread.sleep(2500L);
        assertThat(errors.size()).isEqualTo(1L);
    }
}