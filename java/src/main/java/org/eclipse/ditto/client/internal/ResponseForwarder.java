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

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.base.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds a map of correlation-id to response promises to quickly select the correct Bus for received responses. The
 * standard consumer registry is very slow for large number of registrations. Until we optimize the Registry we use this
 * HashMap for better performance.
 *
 * @since 1.0.0
 */
@ThreadSafe
public final class ResponseForwarder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseForwarder.class);

    private final ConcurrentMap<String, CompletableFuture<CommandResponse>> map;

    private ResponseForwarder() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Returns an instance of {@code ResponseForwarder}.
     *
     * @return the instance.
     */
    public static ResponseForwarder getInstance() {
        return new ResponseForwarder();
    }

    /**
     * Associates the specified response promise with the specified correlation-id. If this correlation-id is already
     * associated with another response promise the association gets replaced and the previous promise is completed with
     * an IllegalStateException.
     *
     * @param correlationId the correlationId of the request.
     * @param responsePromise the response promise for the request with the given correlation-id.
     * @return {@code responsePromise} if no conflict occurred or the previously associated response promise.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code correlationId} is empty.
     */
    public CompletableFuture<CommandResponse> put(final CharSequence correlationId,
            final CompletableFuture<CommandResponse> responsePromise) {

        argumentNotEmpty(correlationId, "correlationId");
        checkNotNull(responsePromise, "response promise");

        final CompletableFuture<CommandResponse> previous = map.put(correlationId.toString(), responsePromise);

        if (null != previous && !responsePromise.equals(previous)) {
            final String msgTemplate = "A new response promise was associated with correlation-id <{0}>!";
            previous.completeExceptionally(new IllegalStateException(MessageFormat.format(msgTemplate, correlationId)));
            return previous;
        }

        return responsePromise;
    }

    /**
     * Handles the specified response.
     *
     * @param response the received event containing the response for a recently sent request.
     * @return the <em>completed</em> response promise which is associated to {@code response} or an empty Optional.
     * @throws NullPointerException if {@code response} is {@code null}.
     */
    public Optional<CompletableFuture<CommandResponse>> handle(final CommandResponse<?> response) {
        checkNotNull(response, "ThingCommandResponse to be handled");

        final DittoHeaders dittoHeaders = response.getDittoHeaders();
        final Optional<String> correlationIdOptional = dittoHeaders.getCorrelationId();
        if (!correlationIdOptional.isPresent()) {
            LOGGER.trace("DittoHeaders did not contain a correlation-id. Not going to handle response.");
            return Optional.empty();
        }

        final String correlationId = correlationIdOptional.get();
        LOGGER.trace("Received response for correlation-id <{}>.", correlationId);
        final CompletableFuture<CommandResponse> responsePromise = map.remove(correlationId);
        if (null != responsePromise) {
            tryToCompleteResponsePromise(response, responsePromise);
        } else {
            LOGGER.debug("No promise found for response with correlation-id <{}>!", correlationId);
        }

        return Optional.ofNullable(responsePromise);
    }

    private static void tryToCompleteResponsePromise(final CommandResponse<?> response,
            final CompletableFuture<CommandResponse> responsePromise) {

        try {
            completeResponsePromise(response, responsePromise);
        } catch (final Exception e) {
            responsePromise.completeExceptionally(e);
        }
    }

    private static void completeResponsePromise(final CommandResponse<?> response,
            final CompletableFuture<CommandResponse> responsePromise) {

        if (response instanceof ErrorResponse) {
            responsePromise.completeExceptionally(((ErrorResponse<?>) response).getDittoRuntimeException());
        } else {
            responsePromise.complete(response);
        }
    }

}
