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
package org.eclipse.ditto.client.options;

import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.ditto.client.management.CommonManagement;

/**
 * Framework-level interface defining the name of an Option.
 *
 * @since 1.0.0
 */
public interface OptionName extends Predicate<Object> {

    @Override
    default boolean test(final Object o) {
        return equals(o) || Objects.equals(toString(), o.toString());
    }

    /**
     * An enumeration of known option names for all operations.
     *
     * @since 1.0.0
     */
    enum Global implements OptionName {

        /**
         * Name of the option for defining whether the thing command should be applied, based on the specified
         * condition.
         *
         * @since 2.1.0
         */
        CONDITION,

        /**
         * Name of the option for defining the DittoHeaders to send along with a command/message to the Ditto backend.
         *
         * @since 1.1.0
         */
        DITTO_HEADERS,

        /**
         * Name of the option for defining a "live channel condition", which, when evaluating to {@code true}, shall
         * switch the channel to use to {@code "live"}.
         * The condition is defined via an RQL expression, which is evaluated in the Things service persistence.
         * If the condition evaluates to {@code true}, then the live channel is used instead of the regular
         * twin channel.
         *
         * @since 2.3.0
         */
        LIVE_CHANNEL_CONDITION,

        /**
         * Name of the option for defining merge thing patch conditions, which map JSON pointer paths to RQL condition expressions.
         * These conditions determine which parts of a merge payload should be applied based on the current state of the Thing.
         *
         * @since 3.8.0
         */
        MERGE_THING_PATCH_CONDITIONS,

    }

    /**
     * An enumeration of known option names for modifying operations.
     *
     * @since 1.0.0
     */
    enum Modify implements OptionName {

        /**
         * Name of the option for defining whether a response for a modifying operation is required or not.
         */
        RESPONSE_REQUIRED,

        /**
         * Name of the option for defining whether the object affected by the modifying operation must exist or whether
         * it must not exist.
         * <p>
         * This option is only supported for set/put operations. That means it is <strong>not</strong> supported for all
         * other operations (retrieve, create, update and delete). In the latter case, an
         * {@link UnsupportedOperationException} will be thrown.
         * </p>
         * <p>If the option is not specified, it does not matter whether the object exists.
         * </p>
         */
        EXISTS,

        /**
         * Name of the option for defining whether the policy should be copied from another policy when creating a
         * thing.
         *
         * @since 1.1.0
         */
        COPY_POLICY,

        /**
         * Name of the option for defining whether the policy should be copied from another thing when creating a thing.
         *
         * @since 1.1.0
         */
        COPY_POLICY_FROM_THING

    }

    /**
     * An enumeration of known option names for {@link CommonManagement#startConsumption() startConsumption()}.
     *
     * @since 1.0.0
     */
    enum Consumption implements OptionName {

        /**
         * Name of the option for defining for which namespaces (as comma separated list) to start consumption for.
         */
        NAMESPACES,

        /**
         * Name of the option for defining which RQL expression / filter should match in the backend for events to be
         * delivered to this client.
         */
        FILTER,

        /**
         * Name of the option for selecting extra fields for events to be delivered to this client.
         */
        EXTRA_FIELDS

    }

}
