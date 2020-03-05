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
package org.eclipse.ditto.client.streaming;

import org.reactivestreams.Subscription;

/**
 * TODO
 */
public final class FailedSubscription implements Subscription {

    private FailedSubscription() {}

    // TODO: javadoc
    public static Subscription of() {
        // create new object each time to trigger rule 2.5
        return new FailedSubscription();
    }

    @Override
    public void request(final long n) {
        // do nothing - rule 3.6
    }

    @Override
    public void cancel() {
        // do nothing - rule 3.7
    }
}
