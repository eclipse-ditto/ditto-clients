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

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.signals.commands.modify.ThingModifyCommand;

/**
 * {@link org.eclipse.ditto.things.model.signals.commands.modify.MergeThing} live command giving access to the command and all of
 * its special accessors. Also the entry point
 * for creating a {@link ModifyThingLiveCommandAnswerBuilder} capable of answering incoming commands.
 *
 * @since 2.0.0
 */
public interface MergeThingLiveCommand extends LiveCommand<MergeThingLiveCommand,
        MergeThingLiveCommandAnswerBuilder>, ThingModifyCommand<MergeThingLiveCommand> {

    /**
     * @return the path where the changes are applied.
     */
    JsonPointer getPath();

    /**
     * @return the value describing the changes that are applied to the existing thing.
     */
    JsonValue getValue();

}
