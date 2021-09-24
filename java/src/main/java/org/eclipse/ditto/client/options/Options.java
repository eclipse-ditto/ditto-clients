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

import java.util.Arrays;

import org.eclipse.ditto.base.model.common.ConditionChecker;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.client.management.CommonManagement;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.things.model.ThingId;

/**
 * This utility class allows creating {@link Option}s with custom values the Ditto Client is aware of.
 *
 * @since 1.0.0
 */
public final class Options {

    private Options() {
        throw new AssertionError();
    }

    /**
     * Creates an option for specifying additional/custom DittoHeaders to send along together with any command/message
     * accepting options.
     * <p>
     * DittoHeader passed in here will be overwritten by more specific {@code Options}, when specified.
     * </p>
     *
     * @param dittoHeaders the additional DittoHeaders to send along with an operation.
     * @return the new option.
     * @since 1.1.0
     */
    public static Option<DittoHeaders> headers(final DittoHeaders dittoHeaders) {
        return DefaultOption.newInstance(OptionName.Global.DITTO_HEADERS, dittoHeaders);
    }

    /**
     * Creates an option for specifying whether the operation should be performed on back-end based on the given
     * condition.
     * <p>
     * The returned option has the name {@link OptionName.Global#CONDITION} and the given argument value.
     * </p>
     * <p>
     * If this {@code Option} is not specified, the operation will be performed no matter what.
     * </p>
     *
     * @param condition the RQL condition that determines whether the operation will be performed.
     * @return the new option.
     * @throws NullPointerException if {@code condition} is {@code null}.
     * @throws IllegalArgumentException if {@code condition} is empty.
     * @since 2.1.0
     */
    public static Option<String> condition(final CharSequence condition) {
        ConditionChecker.argumentNotEmpty(condition, "condition");
        return DefaultOption.newInstance(OptionName.Global.CONDITION, condition.toString());
    }

    /**
     * The {@code Modify} class provides static factory methods for creating Options which are related to modifying
     * operations.
     *
     * @since 1.0.0
     */
    public static final class Modify {

        private Modify() {
            throw new AssertionError();
        }

        /**
         * Creates an option for specifying whether a response to a modifying operation is required or not.
         * <p>
         * The returned option has the name {@link OptionName.Modify#RESPONSE_REQUIRED} and the given {@code boolean}
         * value.
         * </p>
         * <p>
         * The default if this Option is not specified is {@code true}, you'll always get a response message even if you
         * don't consume the CompletableFuture.
         * </p>
         *
         * @param responseRequired whether a response is required for the modifying operation.
         * @return the new option.
         */
        public static Option<Boolean> responseRequired(final boolean responseRequired) {
            return DefaultOption.newInstance(OptionName.Modify.RESPONSE_REQUIRED, responseRequired);
        }

        /**
         * Creates an option for specifying whether the object affected by the modifying operation must exist or whether
         * it must not exist.
         * <p>
         * This option is only supported for set- and put-operations. That means it is <strong>not</strong> supported
         * for all other operations (retrieve, create, update and delete). In the latter case, an {@link
         * IllegalArgumentException} will be thrown.
         * </p>
         * <p>
         * The returned option has the name {@link OptionName.Modify#EXISTS} and the given {@code boolean} value.
         * </p>
         * <p>
         * If this Option is not specified, it does not matter whether the object exists.
         * </p>
         *
         * @param exists whether the object affected by the modifying operation must exist or whether it must not
         * exist.
         * @return the new option.
         */
        public static Option<Boolean> exists(final boolean exists) {
            return DefaultOption.newInstance(OptionName.Modify.EXISTS, exists);
        }

        /**
         * Creates an option for specifying whether the created policy should copied from an already existing policy
         * <p>
         * The returned option has the name {@link OptionName.Modify#COPY_POLICY} and the given {@code boolean} value.
         * </p>
         * <p>
         * If this Option is not specified, it does not matter whether the object exists.
         * </p>
         *
         * @param copyPolicyFrom existing policy which should be copied.
         * @return the new option.
         * @since 1.1.0
         */
        public static Option<PolicyId> copyPolicy(final PolicyId copyPolicyFrom) {
            return DefaultOption.newInstance(OptionName.Modify.COPY_POLICY, copyPolicyFrom);
        }

        /**
         * Creates an option for specifying whether the created policy should be copied from an already existing thing
         * <p>
         * The returned option has the name {@link OptionName.Modify#COPY_POLICY_FROM_THING} and the given {@code boolean} value.
         * </p>
         * <p>
         * If this Option is not specified, it does not matter whether the object exists.
         * </p>
         *
         * @param thingToCopyPolicyFrom existing thing from which the policy are used.
         * @return the new option.
         * @since 1.1.0
         */
        public static Option<ThingId> copyPolicyFromThing(final ThingId thingToCopyPolicyFrom) {
            return DefaultOption.newInstance(OptionName.Modify.COPY_POLICY_FROM_THING, thingToCopyPolicyFrom);
        }

    }

    /**
     * The Consumption class provides static factory methods for creating Options which are related to
     * {@link CommonManagement#startConsumption() startConsumption()}.
     *
     * @since 1.0.0
     */
    public static final class Consumption {

        private Consumption() {
            throw new AssertionError();
        }

        /**
         * Creates an option for specifying for which namespaces the consumption of messages, (twin/live) events and
         * live commands should be started for.
         * <p>
         * The default if this Option is that no namespaces are specified meaning that the started consumption will
         * receive messages, (twin/live) events and live commands for all namespaces.
         * </p>
         *
         * @param namespaces the namespaces to include for the consumption.
         * @return the new option.
         */
        public static Option<Iterable<CharSequence>> namespaces(final CharSequence... namespaces) {
            return DefaultOption.newInstance(OptionName.Consumption.NAMESPACES, Arrays.asList(namespaces));
        }

        /**
         * Creates an option for specifying an RQL expression / filter which must match in the backend for events to be
         * delivered to this client.
         * <p>
         * This option is only applied for twin and live events.
         * </p>
         * <p>
         * If this Option is not specified, no filtering of events in the backend is done.
         * </p>
         *
         * @param rqlExpression the RQL expression / filter which must match for this consumption.
         * @return the new option.
         */
        public static Option<CharSequence> filter(final CharSequence rqlExpression) {
            return DefaultOption.newInstance(OptionName.Consumption.FILTER, rqlExpression);
        }

        /**
         * Creates an option for specifying extra fields to send for events to be delivered to this client.
         * <p>
         * This option is only applied for twin and live events.
         * </p>
         * <p>
         * If this Option is not specified, no extra fields will be sent back from the backend.
         * </p>
         *
         * @param jsonFieldSelector the JsonFieldSelector to apply for selecting extra fields.
         * @return the new option.
         */
        public static Option<JsonFieldSelector> extraFields(final JsonFieldSelector jsonFieldSelector) {
            return DefaultOption.newInstance(OptionName.Consumption.EXTRA_FIELDS, jsonFieldSelector);
        }

    }

}
