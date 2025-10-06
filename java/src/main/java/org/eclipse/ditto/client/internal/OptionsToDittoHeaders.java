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
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ditto.base.model.common.ConditionChecker;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.headers.DittoHeadersBuilder;
import org.eclipse.ditto.base.model.headers.entitytag.EntityTagMatcher;
import org.eclipse.ditto.base.model.headers.entitytag.EntityTagMatchers;
import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.client.options.internal.OptionsEvaluator;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;

/**
 * This class provides the means to build {@link DittoHeaders} for a particular outgoing message.
 */
final class OptionsToDittoHeaders {

    private static final EntityTagMatchers ASTERISK_ENTITY_TAG_MATCHER =
            EntityTagMatchers.fromList(Collections.singletonList(EntityTagMatcher.asterisk()));

    private final JsonSchemaVersion jsonSchemaVersion;
    private final SortedSet<? extends OptionName> allowedOptions;
    private final OptionsEvaluator.Global globalOptionsEvaluator;
    private final OptionsEvaluator.Modify modifyOptionsEvaluator;

    private final DittoHeadersBuilder<?, ?> headersBuilder;

    private OptionsToDittoHeaders(final JsonSchemaVersion jsonSchemaVersion,
            final Collection<? extends OptionName> explicitlyAllowedOptions,
            final OptionsEvaluator.Global globalOptionsEvaluator,
            final OptionsEvaluator.Modify modifyOptionsEvaluator) {

        this.jsonSchemaVersion = jsonSchemaVersion;

        final SortedSet<OptionName> allAllowedOptions = new TreeSet<>(Comparator.comparing(Object::toString));
        allAllowedOptions.addAll(explicitlyAllowedOptions);
        allAllowedOptions.add(OptionName.Global.DITTO_HEADERS);
        allAllowedOptions.add(OptionName.Modify.RESPONSE_REQUIRED);
        allowedOptions = Collections.unmodifiableSortedSet(allAllowedOptions);

        this.globalOptionsEvaluator = globalOptionsEvaluator;
        this.modifyOptionsEvaluator = modifyOptionsEvaluator;

        headersBuilder = DittoHeaders.newBuilder();
    }

    /**
     * Returns {@link DittoHeaders} that are based on the specified arguments.
     *
     * @param schemaVersion the JSON schema version to be used.
     * @param explicitlyAllowedOptions the options that are explicitly allowed for a particular outgoing message.
     * @param options the user provided options.
     * @return the {@code DittoHeaders}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code options} contains items that are not within
     * {@code explicitlyAllowedOptions} or that are not implicitly allowed.
     */
    static DittoHeaders getDittoHeaders(final JsonSchemaVersion schemaVersion,
            final Collection<? extends OptionName> explicitlyAllowedOptions,
            final Option<?>[] options) {

        final OptionsToDittoHeaders optionsToDittoHeaders = new OptionsToDittoHeaders(
                ConditionChecker.checkNotNull(schemaVersion, "schemaVersion"),
                ConditionChecker.checkNotNull(explicitlyAllowedOptions, "explicitlyAllowedOptions"),
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
        setLiveChannelCondition();
        setMergeThingPatchConditions();
        return buildDittoHeaders();
    }

    private void putAdditionalHeaders(final DittoHeaders additionalHeaders) {
        headersBuilder.putHeaders(additionalHeaders);
    }

    private DittoHeaders getAdditionalHeaders() {
        final DittoHeaders result;
        final Optional<DittoHeaders> dittoHeadersOptional = globalOptionsEvaluator.getDittoHeaders();
        if (dittoHeadersOptional.isPresent()) {
            validateIfOptionIsAllowed(OptionName.Global.DITTO_HEADERS);
            result = dittoHeadersOptional.get();
        } else {
            result = DittoHeaders.empty();
        }
        return result;
    }

    private void validateIfOptionIsAllowed(final OptionName optionName) {
        if (!allowedOptions.contains(optionName)) {
            final String pattern = "Option ''{0}'' is not allowed. This operation only allows {1}.";
            throw new IllegalArgumentException(MessageFormat.format(pattern, optionName, allowedOptions));
        }
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

    private void setCondition() {
        globalOptionsEvaluator.condition()
                .ifPresent(condition -> {
                    validateIfOptionIsAllowed(OptionName.Global.CONDITION);
                    headersBuilder.condition(condition);
                });
    }

    private void setLiveChannelCondition() {
        globalOptionsEvaluator.getLiveChannelCondition()
                .ifPresent(liveChannelCondition -> {
                    validateIfOptionIsAllowed(OptionName.Global.LIVE_CHANNEL_CONDITION);
                    headersBuilder.liveChannelCondition(liveChannelCondition);
                });
    }

    private void setMergeThingPatchConditions() {
        globalOptionsEvaluator.getMergeThingPatchConditions()
                .ifPresent(patchConditions -> {
                    validateIfOptionIsAllowed(OptionName.Global.MERGE_THING_PATCH_CONDITIONS);
                    final JsonObjectBuilder builder = JsonObject.newBuilder();
                    for (final Map.Entry<JsonPointer, String> entry : patchConditions.entrySet()) {
                        builder.set(entry.getKey().toString(), entry.getValue());
                    }
                    headersBuilder.putHeader("merge-thing-patch-conditions", builder.build().toString());
                });
    }

    private DittoHeaders buildDittoHeaders() {
        return headersBuilder.build();
    }

}
