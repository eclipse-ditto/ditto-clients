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

/**
 * This interface defines the entrance point to the fluent API to create and send a new {@link
 * org.eclipse.ditto.model.messages.Message}.
 *
 * @param <T> the type of the Message's payload.
 * @since 1.0.0
 */
public interface PendingMessageWithFeatureId<T> {

    /**
     * Sets the {@link org.eclipse.ditto.model.messages.Message} as being sent <em>FROM</em> the {@link
     * org.eclipse.ditto.model.things.Feature}.
     *
     * @return fluent API builder that provides the functionality to set the subject of the Message.
     */
    MessageSender.SetSubject<T> from();

    /**
     * Sets the {@link org.eclipse.ditto.model.messages.Message} as being sent <em>TO</em> the {@link
     * org.eclipse.ditto.model.things.Feature}.
     *
     * @return fluent API builder that provides the functionality to set the subject of the Message.
     */
    MessageSender.SetSubject<T> to();

}
