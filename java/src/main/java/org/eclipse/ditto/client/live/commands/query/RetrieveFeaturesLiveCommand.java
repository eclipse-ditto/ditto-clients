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

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatures;
import org.eclipse.ditto.things.model.signals.commands.query.ThingQueryCommand;

/**
 * {@link RetrieveFeatures} live command giving access to the command and all of its special accessors.
 * Also the entry point for creating a {@link RetrieveFeaturesLiveCommandAnswerBuilder} capable of
 * answering incoming commands.
 *
 * @since 2.0.0
 */
public interface RetrieveFeaturesLiveCommand extends LiveCommand<RetrieveFeaturesLiveCommand,
        RetrieveFeaturesLiveCommandAnswerBuilder>, ThingQueryCommand<RetrieveFeaturesLiveCommand> {
}
