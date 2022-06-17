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
package org.eclipse.ditto.client.live.commands.modify;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.signals.commands.modify.CreateThing;

/**
 * An immutable implementation of {@link CreateThingLiveCommand}.
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@Immutable
final class CreateThingLiveCommandImpl
        extends AbstractModifyLiveCommand<CreateThingLiveCommand, CreateThingLiveCommandAnswerBuilder>
        implements CreateThingLiveCommand {

    private final Thing thing;

    private CreateThingLiveCommandImpl(final CreateThing command) {
        super(command);
        thing = command.getThing();
    }

    /**
     * Returns a new instance of {@code CreateThingLiveCommandImpl}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of {@link CreateThing}.
     */
    @Nonnull
    public static CreateThingLiveCommand of(final Command<?> command) {
        return new CreateThingLiveCommandImpl((CreateThing) command);
    }

    @Override
    public ThingId getEntityId() {
        return thing.getEntityId().orElseThrow(() -> new NullPointerException("Thing has no ID!"));
    }

    @Override
    public Thing getThing() {
        return thing;
    }

    @Override
    public Category getCategory() {
        return Category.CREATE;
    }

    @Override
    public CreateThingLiveCommand setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(CreateThing.of(thing, null, dittoHeaders));
    }

    @Override
    public boolean changesAuthorization() {
        return false;
    }

    @Nonnull
    @Override
    public CreateThingLiveCommandAnswerBuilder answer() {
        return CreateThingLiveCommandAnswerBuilderImpl.newInstance(this);
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + super.toString() + "]";
    }

}
