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
package org.eclipse.ditto.client.internal.bus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link org.eclipse.ditto.client.internal.bus.DefaultJsonPointerSelector}.
 */
public final class DefaultJsonPointerSelectorTest {

    /**
     * This test shall verify that DefaultJsonPointerSelector does not implement equals and hashCode, since it would
     * break functionality of {@link DefaultRegistry} at the current state.
     */
    @Test
    public void verifyInequality() {
        final DefaultJsonPointerSelector p1 = DefaultJsonPointerSelector.jsonPointerSelector("/any/things/attributes");
        final DefaultJsonPointerSelector p2 = DefaultJsonPointerSelector.jsonPointerSelector("/any/things/attributes");
        assertThat(p1).isNotEqualTo(p2);
    }

}
