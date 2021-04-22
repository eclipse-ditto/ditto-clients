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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveAttributes;

/**
 * An immutable implementation of {@link RetrieveAttributesLiveCommand}.
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@Immutable
final class RetrieveAttributesLiveCommandImpl extends AbstractQueryLiveCommand<RetrieveAttributesLiveCommand,
        RetrieveAttributesLiveCommandAnswerBuilder> implements RetrieveAttributesLiveCommand {

    private RetrieveAttributesLiveCommandImpl(final RetrieveAttributes command) {
        super(command);
    }

    /**
     * Returns an instance of {@code RetrieveAttributesLiveCommandImpl}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of {@link RetrieveAttributes}.
     */
    @Nonnull
    public static RetrieveAttributesLiveCommandImpl of(final Command<?> command) {
        return new RetrieveAttributesLiveCommandImpl((RetrieveAttributes) command);
    }

    @Override
    public RetrieveAttributesLiveCommand setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(RetrieveAttributes.of(getEntityId(), getSelectedFields().orElse(null), dittoHeaders));
    }

    @Nonnull
    @Override
    public RetrieveAttributesLiveCommandAnswerBuilder answer() {
        return RetrieveAttributesLiveCommandAnswerBuilderImpl.newInstance(this);
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + super.toString() + "]";
    }

}
