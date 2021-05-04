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
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.utils.jsr305.annotations.AllValuesAreNonnullByDefault;

/**
 * An immutable implementation of {@link DeleteFeatureDefinitionLiveCommand}.
 *
 * @since 2.0.0
 */
@AllValuesAreNonnullByDefault
@Immutable
final class DeleteFeatureDefinitionLiveCommandImpl
        extends AbstractModifyLiveCommand<DeleteFeatureDefinitionLiveCommand, DeleteFeatureDefinitionLiveCommandAnswerBuilder>
        implements DeleteFeatureDefinitionLiveCommand {

    private final String featureId;

    private DeleteFeatureDefinitionLiveCommandImpl(final DeleteFeatureDefinition command) {
        super(command);
        featureId = command.getFeatureId();
    }

    /**
     * Returns a new instance of {@code DeleteFeatureDefinitionLiveCommandImpl}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of {@link DeleteFeatureDefinition}.
     */
    @Nonnull
    public static DeleteFeatureDefinitionLiveCommandImpl of(final Command<?> command) {
        return new DeleteFeatureDefinitionLiveCommandImpl((DeleteFeatureDefinition) command);
    }

    @Override
    public String getFeatureId() {
        return featureId;
    }

    @Override
    public Category getCategory() {
        return Category.DELETE;
    }

    @Override
    public DeleteFeatureDefinitionLiveCommand setDittoHeaders(final DittoHeaders dittoHeaders) {
        return new DeleteFeatureDefinitionLiveCommandImpl(DeleteFeatureDefinition.of(getEntityId(), getFeatureId(),
                dittoHeaders));
    }

    @Override
    public boolean changesAuthorization() {
        return false;
    }

    @Nonnull
    @Override
    public DeleteFeatureDefinitionLiveCommandAnswerBuilder answer() {
        return DeleteFeatureDefinitionLiveCommandAnswerBuilderImpl.newInstance(this);
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + super.toString() + "]";
    }

}
