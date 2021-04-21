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
package org.eclipse.ditto.client.live.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.ditto.client.ack.Acknowledgeable;
import org.eclipse.ditto.client.ack.internal.ImmutableAcknowledgementRequestHandle;
import org.eclipse.ditto.client.changes.AcknowledgementRequestHandle;
import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.AcknowledgementRequest;
import org.eclipse.ditto.model.base.entity.id.WithEntityId;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.WithThingId;
import org.eclipse.ditto.signals.base.Signal;

/**
 * Acknowledgeable of a live command.
 *
 * @param <L> the type of live commands. MUST be an interface satisfying the recursive type bound.
 * @since 1.2.0
 */
public final class LiveCommandAcknowledgeable<L extends LiveCommand<L, B>, B extends LiveCommandAnswerBuilder>
        implements Acknowledgeable {

    private final L liveCommand;
    private final Consumer<Signal<?>> signalPublisher;

    private LiveCommandAcknowledgeable(final L liveCommand, final Consumer<Signal<?>> signalPublisher) {
        this.liveCommand = liveCommand;
        this.signalPublisher = signalPublisher;
    }

    public static <L extends LiveCommand<L, B>, B extends LiveCommandAnswerBuilder> LiveCommandAcknowledgeable<L, B> of(
            final L liveCommand,
            final Consumer<Signal<?>> signalPublisher) {
        return new LiveCommandAcknowledgeable<>(liveCommand, signalPublisher);
    }

    /**
     * Return the live command.
     *
     * @return the live command.
     */
    public L getLiveCommand() {
        return liveCommand;
    }

    /**
     * Create a live command answer builder.
     *
     * @return the live command answer builder.
     */
    public B answer() {
        return liveCommand.answer();
    }

    @Override
    public void handleAcknowledgementRequests(
            final Consumer<Collection<AcknowledgementRequestHandle>> acknowledgementHandles) {

        acknowledgementHandles.accept(getHandles());
    }

    @Override
    public void handleAcknowledgementRequest(final AcknowledgementLabel acknowledgementLabel,
            final Consumer<AcknowledgementRequestHandle> acknowledgementHandle) {
        final DittoHeaders dittoHeaders = liveCommand.getDittoHeaders();
        if (dittoHeaders.getAcknowledgementRequests().contains(AcknowledgementRequest.of(acknowledgementLabel))) {
            WithEntityId.getEntityIdOfType(ThingId.class, liveCommand).ifPresent(thingId ->
                acknowledgementHandle.accept(new ImmutableAcknowledgementRequestHandle(
                        acknowledgementLabel,
                        ((WithThingId) liveCommand).getEntityId(),
                        dittoHeaders,
                        signalPublisher::accept
                ))
            );
        }
    }

    private Collection<AcknowledgementRequestHandle> getHandles() {
        return WithEntityId.getEntityIdOfType(ThingId.class, liveCommand)
                .map(thingId -> liveCommand.getDittoHeaders()
                        .getAcknowledgementRequests()
                        .stream()
                        .map(ackRequest -> (AcknowledgementRequestHandle) new ImmutableAcknowledgementRequestHandle(
                                ackRequest.getLabel(),
                                thingId,
                                liveCommand.getDittoHeaders(),
                                signalPublisher::accept)
                        ).collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }
}
