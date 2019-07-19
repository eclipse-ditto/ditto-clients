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
package org.eclipse.ditto.client.options.internal;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.mockito.Mockito;

final class MockOptionFactory {

    private MockOptionFactory() {
        throw new AssertionError();
    }

    public static Option<?> createOptionMock(final OptionName optionName, final Object optionValue) {
        final Option<?> result = Mockito.mock(Option.class);
        Mockito.when(result.getName()).thenReturn(optionName);
        Mockito.<Object>when(result.getValue()).thenReturn(optionValue);
        Mockito.when(result.getValueAs(Mockito.any())).thenReturn(optionValue);
        return result;
    }
}
