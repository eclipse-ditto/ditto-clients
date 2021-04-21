/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.live.messages;

import org.eclipse.ditto.things.model.ThingId;

/**
 * This interface defines the entrance point to the fluent API to create and send a new {@link
 * org.eclipse.ditto.model.messages.Message}.
 *
 * @param <T> the type of the Message's payload.
 * @since 1.0.0
 */
public interface PendingMessage<T> {

    /**
     * Sets the {@link org.eclipse.ditto.model.messages.Message} as being sent <em>FROM</em> the specified {@link
     * org.eclipse.ditto.things.model.Thing}.
     *
     * @param thingId the ID of the Thing from which the Message will be sent.
     * @return fluent API builder that provides the functionality to <em>optionally</em> set the ID of the Feature from
     * which the Message will be sent, or to leave the Feature ID empty and set the subject of the Message.
     */
    MessageSender.SetFeatureIdOrSubject<T> from(ThingId thingId);

    /**
     * Sets the {@link org.eclipse.ditto.model.messages.Message} as being sent <em>TO</em> the specified {@link
     * org.eclipse.ditto.things.model.Thing}.
     *
     * @param thingId the identifier of the Thing to which the Message will be sent.
     * @return fluent API builder that provides the functionality to <em>optionally</em> set the ID of the Feature to
     * which the Message will be sent, or to leave the Feature ID empty and set the subject of the Message.
     */
    MessageSender.SetFeatureIdOrSubject<T> to(ThingId thingId);

}
