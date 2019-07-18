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

import org.eclipse.ditto.model.messages.Message;

/**
 * A {@link Message} which can be replied to.
 *
 * @param <T> the type of the message's payload.
 * @param <U> the type of the response's payload.
 * @since 1.0.0
 */
public interface RepliableMessage<T, U> extends Message<T> {

    /**
     * Respond to this message.
     *
     * @return fluent API to respond to this message.
     */
    MessageSender.SetPayloadOrSend<U> reply();

}
