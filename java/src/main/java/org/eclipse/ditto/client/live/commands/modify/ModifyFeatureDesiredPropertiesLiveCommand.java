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
package org.eclipse.ditto.client.live.commands.modify;

import javax.annotation.Nonnull;

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.things.model.FeatureProperties;
import org.eclipse.ditto.base.model.signals.WithFeatureId;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDesiredProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.ThingModifyCommand;

/**
 * {@link ModifyFeatureDesiredProperties} live command giving access to the command and all of its special accessors.
 * Also the entry point for creating a {@link ModifyFeatureDesiredPropertiesLiveCommandAnswerBuilder} as answer for
 * an incoming command.
 *
 * @since 2.0.0
 */
public interface ModifyFeatureDesiredPropertiesLiveCommand extends LiveCommand<ModifyFeatureDesiredPropertiesLiveCommand,
        ModifyFeatureDesiredPropertiesLiveCommandAnswerBuilder>, ThingModifyCommand<ModifyFeatureDesiredPropertiesLiveCommand>,
        WithFeatureId {

    /**
     * Returns the {desired properties to modify.
     *
     * @return the Properties to modify.
     * @see org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDesiredProperties#getDesiredProperties() ()
     */
    @Nonnull
    FeatureProperties getDesiredProperties();

}
