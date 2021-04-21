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

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.signals.base.WithFeatureId;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.ThingModifyCommand;

/**
 * {@link ModifyFeatureProperty} live command giving access to the command and all of its special accessors.
 * Also the entry point for creating a {@link ModifyFeaturePropertyLiveCommandAnswerBuilder} as answer for
 * an incoming command.
 *
 * @since 2.0.0
 */
public interface ModifyFeaturePropertyLiveCommand
        extends LiveCommand<ModifyFeaturePropertyLiveCommand, ModifyFeaturePropertyLiveCommandAnswerBuilder>,
        ThingModifyCommand<ModifyFeaturePropertyLiveCommand>, WithFeatureId {

    /**
     * Returns the JSON pointer of the Property to modify.
     *
     * @return the JSON pointer.
     * @see ModifyFeatureProperty#getPropertyPointer()
     */
    @Nonnull
    JsonPointer getPropertyPointer();

    /**
     * Returns the value of the Property to modify.
     *
     * @return the value.
     * @see ModifyFeatureProperty#getPropertyValue()
     */
    @Nonnull
    JsonValue getPropertyValue();

}
