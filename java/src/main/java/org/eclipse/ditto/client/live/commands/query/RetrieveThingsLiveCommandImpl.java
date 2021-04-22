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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.commands.base.AbstractLiveCommand;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.things.model.signals.commands.ThingCommand;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThings;

/**
 * An immutable implementation of {@link RetrieveThingsLiveCommand}.
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@Immutable
final class RetrieveThingsLiveCommandImpl extends AbstractLiveCommand<RetrieveThingsLiveCommand,
        RetrieveThingsLiveCommandAnswerBuilder> implements RetrieveThingsLiveCommand {

    private final List<ThingId> thingIds;
    @Nullable private final String namespace;

    private RetrieveThingsLiveCommandImpl(final RetrieveThings command) {
        super(command);
        thingIds = command.getEntityIds();
        namespace = command.getNamespace().orElse(null);
    }

    /**
     * Returns an instance of {@code RetrieveThingsLiveCommandImpl}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of {@link RetrieveThings}.
     */
    @Nonnull
    public static RetrieveThingsLiveCommandImpl of(final Command<?> command) {
        return new RetrieveThingsLiveCommandImpl((RetrieveThings) command);
    }

    @Nonnull
    @Override
    public List<ThingId> getEntityIds() {
        return thingIds;
    }

    @Nonnull
    @Override
    public Optional<String> getNamespace() {
        return Optional.ofNullable(namespace);
    }

    @Override
    public String getTypePrefix() {
        return ThingCommand.TYPE_PREFIX;
    }

    @Override
    public Category getCategory() {
        return Category.QUERY;
    }

    @Override
    public RetrieveThingsLiveCommand setDittoHeaders(final DittoHeaders dittoHeaders) {
        final RetrieveThings retrieveThingsCommand = RetrieveThings.getBuilder(getEntityIds())
                .dittoHeaders(dittoHeaders)
                .selectedFields(getSelectedFields().orElse(null))
                .build();

        return RetrieveThingsLiveCommandImpl.of(retrieveThingsCommand);
    }

    @Nonnull
    @Override
    public RetrieveThingsLiveCommandAnswerBuilder answer() {
        return RetrieveThingsLiveCommandAnswerBuilderImpl.newInstance(this);
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + super.toString() + ", namespace=" + namespace + "]";
    }

    @Override
    public String getResourceType() {
        return ThingCommand.RESOURCE_TYPE;
    }
}
