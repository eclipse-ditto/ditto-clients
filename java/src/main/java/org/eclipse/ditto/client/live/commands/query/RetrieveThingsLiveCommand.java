/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.live.commands.query;

import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.signals.commands.base.WithNamespace;
import org.eclipse.ditto.things.model.signals.commands.WithSelectedFields;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThings;

/**
 * {@link RetrieveThings} live command giving access to the command and all of its special accessors.
 * Also the entry point for creating a {@link RetrieveThingsLiveCommandAnswerBuilder} capable of
 * answering incoming commands.
 *
 * @since 2.0.0
 */
public interface RetrieveThingsLiveCommand
        extends LiveCommand<RetrieveThingsLiveCommand, RetrieveThingsLiveCommandAnswerBuilder>,
        WithNamespace, WithSelectedFields {

    /**
     * Returns the identifiers of the {@code Thing}s to be retrieved.
     *
     * @return the identifiers
     */
    @Nonnull
    List<ThingId> getEntityIds();

}
