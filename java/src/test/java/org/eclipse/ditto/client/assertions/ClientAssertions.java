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
package org.eclipse.ditto.client.assertions;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.policies.model.signals.commands.PolicyCommand;

/**
 * Custom assertions for testing the CR Integration Client.
 */
public final class ClientAssertions extends Assertions {

    public static MessageAssert assertThat(final Message<?> outgoingMessage) {
        return new MessageAssert(outgoingMessage);
    }

    public static ChangeAssert assertThat(final Change thingAttributeChange) {
        return new ChangeAssert(thingAttributeChange);
    }

    public static ThingChangeAssert assertThat(final ThingChange thingChange) {
        return new ThingChangeAssert(thingChange);
    }

    public static PolicyAssert assertThat(final PolicyCommand policyCommand) {
        return new PolicyAssert(policyCommand);
    }

}
