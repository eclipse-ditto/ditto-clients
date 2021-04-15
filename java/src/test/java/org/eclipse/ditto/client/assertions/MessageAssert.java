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
package org.eclipse.ditto.client.assertions;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.assertj.core.api.AbstractAssert;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTagMatcher;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * An Assert for {@link Message}.
 */
public final class MessageAssert extends AbstractAssert<MessageAssert, Message<?>> {

    private static final String ASTERISK = EntityTagMatcher.asterisk().toString();

    /**
     * Constructs a new {@code MessageAssert} object.
     *
     * @param actual the message to be checked.
     */
    public MessageAssert(final Message<?> actual) {
        super(actual, MessageAssert.class);
    }

    public MessageAssert hasSubject(final String expectedSubject) {
        return assertThatEqual(expectedSubject, actual.getSubject(), "subject");
    }

    public MessageAssert hasThingId(final ThingId expectedThingId) {
        return assertThatEqual(expectedThingId, actual.getEntityId(), "Thing identifier");
    }

    public MessageAssert hasInitialPolicy() {
        assertThat(((ThingCommand) actual.getPayload().get()).toJson().get(JsonPointer.of("initialPolicy")))
                .overridingErrorMessage("Thing does not have initial policy which is expected")
                .isNotEqualTo(JsonObject.of("{}"));
        return this;
    }

    public MessageAssert hasOptionCopyPolicy(final PolicyId policyId) {
        assertThat(((ThingCommand) actual.getPayload().get()).toJson().get(JsonPointer.of("policyIdOrPlaceholder")))
                .overridingErrorMessage("Thing does not have initial policy which is expected")
                .isEqualTo(JsonObject.of("{\"policyIdOrPlaceholder\":\"" + policyId + "\"}"));
        return this;
    }

    public MessageAssert hasOptionCopyPolicyFromThing(final ThingId thingId) {
        assertThat(((ThingCommand) actual.getPayload().get()).toJson().get(JsonPointer.of("policyIdOrPlaceholder")))
                .overridingErrorMessage("Thing does not have initial policy which is expected")
                .isEqualTo(JsonObject.of(
                        "{\"policyIdOrPlaceholder\":\"{{ ref:things/" + thingId + "/policyId }}\"}"));
        return this;
    }

    public MessageAssert hasFeatureId(final String expectedFeatureId) {
        return assertThatEqual(expectedFeatureId, actual.getFeatureId(), "Feature identifier");
    }

    private <T> MessageAssert assertThatEqual(final T expected, final T actual, final String propertyName) {
        isNotNull();
        assertThat(actual).overridingErrorMessage("Expected message to have %s \n<%s> but it had \n<%s>", propertyName,
                expected,
                actual).isEqualTo(expected);
        return this;
    }

    private <T> MessageAssert assertThatEqual(final T expected, final Optional<T> actual,
            final String propertyName) {
        isNotNull();
        assertThat(actual).overridingErrorMessage("Expected message to have %s \n<%s> but it had \n<%s>", propertyName,
                expected,
                actual).contains(expected);
        return this;

    }

    public MessageAssert bodyContains(final JsonValue expectedBody) {
        isNotNull();
        final Optional<ByteBuffer> actualBody = actual.getRawPayload();

        assertThat(actualBody).overridingErrorMessage("Expected message to contain a body but it did not").isPresent();

        final String bodyAsString = AbstractDittoClientTest.extractUtf8StringFromBody(actualBody);

        try {
            JSONAssert.assertEquals(expectedBody.toString(), bodyAsString, false);
        } catch (final JSONException e) {
            throw new AssertionError("JSONAssert failed to assert equality of actual and expected JSON string.", e);
        }

        return this;
    }

    public MessageAssert bodyHasSameJsonAs(final String jsonFilePath) {
        isNotNull();

        requireNonNull(jsonFilePath,
                "The path the the file containing the JSON string to be compared must not be " + "null!");


        final Optional<String> bodyAsStringOptional =
                Optional.ofNullable(AbstractDittoClientTest.extractUtf8StringFromBody(actual.getRawPayload()));

        assertThat(bodyAsStringOptional)
                .overridingErrorMessage("Expected message to have a string body but it did not")
                .isPresent();

        final String result;
        try {
            result = new String(
                    Files.readAllBytes(Paths.get(MessageAssert.class.getResource(jsonFilePath).toURI())),
                    UTF_8);
        } catch (final IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            JSONAssert.assertEquals(result, bodyAsStringOptional.orElse(""), false);
        } catch (final JSONException e) {
            throw new AssertionError("JSONAssert failed to assert equality of actual and expected JSON string.", e);
        }

        return this;
    }

    public MessageAssert hasNoConditionalHeaders() {
        isNotNull();

        assertThat(actual.getHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_MATCH.getKey());
        assertThat(actual.getHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_NONE_MATCH.getKey());

        return this;
    }

    public MessageAssert hasOnlyIfMatchHeader() {
        isNotNull();

        assertThat(actual.getHeaders()).containsEntry(DittoHeaderDefinition.IF_MATCH.getKey(), ASTERISK);
        assertThat(actual.getHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_NONE_MATCH.getKey());

        return this;
    }

    public MessageAssert hasOnlyIfNoneMatchHeader() {
        isNotNull();

        assertThat(actual.getHeaders()).doesNotContainKey(DittoHeaderDefinition.IF_MATCH.getKey());
        assertThat(actual.getHeaders()).containsEntry(DittoHeaderDefinition.IF_NONE_MATCH.getKey(), ASTERISK);

        return this;
    }
}
