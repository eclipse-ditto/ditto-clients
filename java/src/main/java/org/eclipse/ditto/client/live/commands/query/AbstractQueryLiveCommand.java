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

import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.commands.base.AbstractLiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveCommandAnswerBuilder;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.query.ThingQueryCommand;

/**
 * Abstract base implementation for LiveCommands wrapping {@link ThingQueryCommand}s.
 *
 * @param <T> the type of the LiveCommand; currently needed as return type for {@link #setDittoHeaders(DittoHeaders)}.
 * @param <B> the type of the LiveCommandAnswerBuilder to be returned for {@link #answer()}.
 *
 * @since 2.0.0
 */
@Immutable
abstract class AbstractQueryLiveCommand<T extends LiveCommand<T, B> & ThingQueryCommand<T>, B extends LiveCommandAnswerBuilder>
        extends AbstractLiveCommand<T, B> implements ThingQueryCommand<T> {

    private final ThingQueryCommand<?> thingQueryCommand;

    /**
     * Constructs a new {@code AbstractQueryLiveCommand} object.
     *
     * @param thingQueryCommand the command to be wrapped by the returned object.
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    protected AbstractQueryLiveCommand(final ThingQueryCommand<?> thingQueryCommand) {
        super(thingQueryCommand);
        this.thingQueryCommand = thingQueryCommand;
    }

    @Override
    public ThingId getEntityId() {
        return thingQueryCommand.getEntityId();
    }

    @Override
    public Optional<JsonFieldSelector> getSelectedFields() {
        return thingQueryCommand.getSelectedFields();
    }

    @Override
    public Category getCategory() {
        return Category.QUERY;
    }

}
