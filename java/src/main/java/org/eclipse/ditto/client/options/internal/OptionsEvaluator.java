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

import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.ThingId;

/**
 * This class provides convenient access to the values of known options out of an array of user provided options.
 *
 * @since 1.0.0
 */
@Immutable
public final class OptionsEvaluator {

    private final UserProvidedOptions userProvidedOptions;

    private OptionsEvaluator(final UserProvidedOptions theUserProvidedOptions) {
        userProvidedOptions = theUserProvidedOptions;
    }

    /**
     * Returns a new instance of {@link OptionsEvaluator.Global} with the given options.
     * <p>
     * Furthermore it validates the integrity of user provided options, for example if an option name was given more
     * than once or if mutually exclusive options were provided.
     * </p>
     *
     * @param options the user provided options.
     * @return the new OptionEvaluator for global operation options.
     * @throws NullPointerException if {@code options} is {@code null}.
     * @throws IllegalArgumentException if two options have the same name but different values.
     */
    public static Global forGlobalOptions(final Option<?>[] options) {
        return new OptionsEvaluator(UserProvidedOptions.of(options)).new Global();
    }

    /**
     * Returns a new instance of {@link OptionsEvaluator.Modify} with the given options.
     * <p>
     * Furthermore it validates the integrity of user provided options, for example if an option name was given more
     * than once or if mutually exclusive options were provided.
     * </p>
     *
     * @param options the user provided options.
     * @return the new OptionEvaluator for modify operation options.
     * @throws NullPointerException if {@code options} is {@code null}.
     * @throws IllegalArgumentException if two options have the same name but different values.
     */
    public static Modify forModifyOptions(final Option<?>[] options) {
        return new OptionsEvaluator(UserProvidedOptions.of(options)).new Modify();
    }

    /**
     * Returns a new instance of {@link OptionsEvaluator.Consumption} with the given options.
     * <p>
     * Furthermore it validates the integrity of user provided options, for example if an option name was given more
     * than once or if mutually exclusive options were provided.
     * </p>
     *
     * @param options the user provided options.
     * @return the new OptionEvaluator for modify operation options.
     * @throws NullPointerException if {@code options} is {@code null}.
     * @throws IllegalArgumentException if two options have the same name but different values.
     * @since 1.0.0
     */
    public static Consumption forConsumptionOptions(final Option<?>[] options) {
        return new OptionsEvaluator(UserProvidedOptions.of(options)).new Consumption();
    }

    private <T> Optional<T> getValue(final OptionVisitor<T> optionVisitor) {
        userProvidedOptions.accept(optionVisitor);
        return optionVisitor.getValue();
    }

    /**
     * An evaluator for global operations options.
     */
    @Immutable
    public final class Global {

        private Global() {
            super();
        }

        /**
         * Returns the DittoHeaders to send along for commands/messages to the backend.
         *
         * @return the DittoHeaders to send along for commands/messages to the backend.
         * @since 1.1.0
         */
        public Optional<DittoHeaders> getDittoHeaders() {
            return getValue(new DittoHeadersOptionVisitor());
        }
    }

    /**
     * An evaluator for modifying operations options.
     */
    @Immutable
    public final class Modify {

        private Modify() {
            super();
        }

        /**
         * Returns whether a response is required for a modifying operation or not.
         *
         * @return whether a response is required for a modifying operation or not.
         */
        public Optional<Boolean> isResponseRequired() {
            return getValue(new ResponseRequiredOptionVisitor());
        }

        /**
         * Returns whether the object affected by the modifying operation must exist or whether it must not exist.
         *
         * @return an Optional holding a {@link Boolean} which specifies whether the object must exist or not exist; an
         * empty Optional, if not specified.
         */
        public Optional<Boolean> exists() {
            return getValue(new ExistsOptionVisitor());
        }

        /**
         * Returns whether a Policy for a new Thing should be copied for the modify action.
         * @return an Optional holding the {@link PolicyId} to copy from.
         * @since 1.1.0
         */
        public Optional<PolicyId> copyPolicy() {
            return getValue(new CopyPolicyOptionVisitor());
        }

        /**
         * Returns whether a Policy for a new Thing should be copied from another Thing for the modify action.
         * @return an Optional holding the {@link ThingId} to copy the Policy from.
         * @since 1.1.0
         */
        public Optional<ThingId> copyPolicyFromThingId() {
            return getValue(new CopyPolicyFromThingOptionVisitor());
        }

    }

    /**
     * An evaluator for consumption options.
     *
     * @since 1.0.0
     */
    @Immutable
    public final class Consumption {

        private Consumption() {
            super();
        }

        /**
         * Returns for which namespaces the consumption of messages, (twin/live) events and live commands should be
         * started for.
         *
         * @return for which namespaces the consumption of messages, (twin/live) events and live commands should be
         * started for.
         */
        public Optional<Iterable<CharSequence>> getNamespaces() {
            return getValue(new NamespacesOptionVisitor());
        }

        /**
         * Returns the RQL expression / filter which must match in the backend for events to be delivered to this
         * client.
         *
         * @return the RQL expression / filter which must match in the backend for events to be delivered to this
         * client.
         */
        public Optional<CharSequence> getFilter() {
            return getValue(new FilterOptionVisitor());
        }

        /**
         * Returns the extra fields for events to be delivered to this client.
         *
         * @return the extra fields for events to be delivered to this client.
         */
        public Optional<JsonFieldSelector> getExtraFields() {
            return getValue(new ExtraFieldsOptionVisitor());
        }

    }

}
