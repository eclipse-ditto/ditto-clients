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
package org.eclipse.ditto.client.internal;

import java.text.MessageFormat;

/**
 * Defines patterns of bus addresses used for the notification of thing events.
 */
enum BusAddressPatterns {

    THING_PATTERN("/things/{0}"),
    ATTRIBUTES_PATTERN("/things/{0}/attributes"),
    ATTRIBUTE_PATTERN("/things/{0}/attributes{1}"),
    DEFINITION_PATTERN("/things/{0}/definition"),
    POLICY_ID_PATTERN("/things/{0}/policyId"),
    FEATURES_PATTERN("/things/{0}/features"),
    FEATURE_PATTERN("/things/{0}/features/{1}"),
    FEATURE_DEFINITION_PATTERN("/things/{0}/features/{1}/definition"),
    FEATURE_PROPERTIES_PATTERN("/things/{0}/features/{1}/properties"),
    FEATURE_PROPERTY_PATTERN("/things/{0}/features/{1}/properties{2}"),
    FEATURE_DESIRED_PROPERTIES_PATTERN("/things/{0}/features/{1}/desiredProperties"),
    FEATURE_DESIRED_PROPERTY_PATTERN("/things/{0}/features/{1}/desiredProperties{2}");

    private final String pattern;

    BusAddressPatterns(final String pattern) {
        this.pattern = pattern;
    }

    /**
     * Formats this bus address pattern with the given arguments.
     *
     * @param arguments the arguments used for formatting
     * @return the formatted address
     */
    String format(final Object... arguments) {
        return MessageFormat.format(pattern, arguments);
    }
}
