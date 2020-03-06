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
package org.eclipse.ditto.client.options.internal;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.model.things.ThingId;


/**
 * This visitor fetches and provides the value as {@code ThingId} for the option with name {@link
 * OptionName.Modify#COPY_POLICY_FROM_THING} from the user provided options.
 *
 * @since 1.1.0
 */
@ThreadSafe
final class CopyPolicyFromThingOptionVisitor extends AbstractOptionVisitor<ThingId> {

    /**
     * Constructor.
     */
    CopyPolicyFromThingOptionVisitor() {
        super(OptionName.Modify.COPY_POLICY_FROM_THING);
    }

    @Override
    protected ThingId getValueFromOption(final Option<?> option) {
        return option.getValueAs(ThingId.class);
    }

}
