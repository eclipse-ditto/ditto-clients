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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.eclipse.ditto.base.model.common.ConditionChecker;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.headers.DittoHeadersBuilder;
import org.eclipse.ditto.base.model.headers.entitytag.EntityTagMatcher;
import org.eclipse.ditto.base.model.headers.entitytag.EntityTagMatchers;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.client.options.internal.OptionsEvaluator;

/**
 * This class provides the means to build {@link DittoHeaders} for a particular outgoing message.
 */
final class OptionsToDittoHeaders {

    private static final EntityTagMatchers ASTERISK_ENTITY_TAG_MATCHER =
            EntityTagMatchers.fromList(Collections.singletonList(EntityTagMatcher.asterisk()));

    private final JsonSchemaVersion jsonSchemaVersion;
    private final Set<? extends OptionName> allowedOptions;
    private final OptionsEvaluator.Global globalOptionsEvaluator;
    private final OptionsEvaluator.Modify modifyOptionsEvaluator;

    private final DittoHeadersBuilder<?, ?> headersBuilder;

    private OptionsToDittoHeaders(final JsonSchemaVersion jsonSchemaVersion,
            final Collection<? extends OptionName> allowedOptions,
            final OptionsEvaluator.Global globalOptionsEvaluator,
            final OptionsEvaluator.Modify modifyOptionsEvaluator) {

        this.jsonSchemaVersion = jsonSchemaVersion;
        this.allowedOptions = Collections.unmodifiableSet(new HashSet<>(allowedOptions));
        this.globalOptionsEvaluator = globalOptionsEvaluator;
        this.modifyOptionsEvaluator = modifyOptionsEvaluator;

        headersBuilder = DittoHeaders.newBuilder();
    }

    /**
     * Returns {@link DittoHeaders} that are based on the specified arguments.
     *
     * @param schemaVersion the JSON schema version to be used.
     * @param allowedOptions the options that are allowed for a particular outgoing message.
     * @param options the user provided options.
     * @return the {@code DittoHeaders}.
     * @throws NullPointerException if any argument is {@code null}.
     */
    static DittoHeaders getDittoHeaders(final JsonSchemaVersion schemaVersion,
            final Collection<? extends OptionName> allowedOptions,
            final Option<?>[] options) {

        final OptionsToDittoHeaders optionsToDittoHeaders = new OptionsToDittoHeaders(
                ConditionChecker.checkNotNull(schemaVersion, "schemaVersion"),
                ConditionChecker.checkNotNull(allowedOptions, "allowedOptions"),
                OptionsEvaluator.forGlobalOptions(options),
                OptionsEvaluator.forModifyOptions(options)
        );
        return optionsToDittoHeaders.getDittoHeaders();
    }

    private DittoHeaders getDittoHeaders() {
        final DittoHeaders additionalHeaders = getAdditionalHeaders();
        putAdditionalHeaders(additionalHeaders);
        setRandomCorrelationIdIfMissing(additionalHeaders);
        setSchemaVersion();
        setResponseRequired();
        setEntityTagMatchers();
        setCondition();
        return buildDittoHeaders();
    }

    private void putAdditionalHeaders(final DittoHeaders additionalHeaders) {
        headersBuilder.putHeaders(additionalHeaders);
    }

    private DittoHeaders getAdditionalHeaders() {
        return globalOptionsEvaluator.getDittoHeaders().orElseGet(DittoHeaders::empty);
    }

    private void setRandomCorrelationIdIfMissing(final DittoHeaders additionalHeaders) {
        final Optional<String> correlationIdOptional = additionalHeaders.getCorrelationId();
        if (!correlationIdOptional.isPresent()) {
            headersBuilder.randomCorrelationId();
        }
    }

    private void setSchemaVersion() {
        headersBuilder.schemaVersion(jsonSchemaVersion);
    }

    private void setResponseRequired() {
        headersBuilder.responseRequired(modifyOptionsEvaluator.isResponseRequired().orElse(true));
    }

    private void setEntityTagMatchers() {
        modifyOptionsEvaluator.exists()
                .ifPresent(exists -> {
                    validateIfOptionIsAllowed(OptionName.Modify.EXISTS);
                    if (exists) {
                        headersBuilder.ifMatch(ASTERISK_ENTITY_TAG_MATCHER);
                    } else {
                        headersBuilder.ifNoneMatch(ASTERISK_ENTITY_TAG_MATCHER);
                    }
                });
    }

    private void validateIfOptionIsAllowed(final OptionName option) {
        if (!allowedOptions.contains(option)) {
            final String pattern = "Option ''{0}'' is not allowed for this operation.";
            final String lowerCaseOptionName = option.toString().toLowerCase(Locale.ENGLISH);
            throw new IllegalArgumentException(MessageFormat.format(pattern, lowerCaseOptionName));
        }
    }

    private void setCondition() {
        globalOptionsEvaluator.condition()
                .ifPresent(condition -> {
                    validateIfOptionIsAllowed(OptionName.Global.CONDITION);
                    headersBuilder.condition(condition);
                });
    }

    private DittoHeaders buildDittoHeaders() {
        return headersBuilder.build();
    }

}
