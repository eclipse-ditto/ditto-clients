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

import org.eclipse.ditto.json.JsonPointer;
import org.junit.Test;

/**
 * Tests functionality of {@link SelectorUtil}.
 */
public final class SelectorUtilTest {

    @Test
    public void testMapUnmapOfSimpleThingIdEmptyNamespace() {
        final String string = ":myThing-1";

        final String mapped = SelectorUtil.quoteRegexCharacters(string);
        final String unmapped = SelectorUtil.unquoteRegexCharacters(mapped);

        assertThat(unmapped).isEqualTo(string);
    }

    @Test
    public void testMapUnmapOfSimpleThingId() {
        final String string = "org.eclipse.ditto:myThing-1";

        final String mapped = SelectorUtil.quoteRegexCharacters(string);
        final String unmapped = SelectorUtil.unquoteRegexCharacters(mapped);

        assertThat(unmapped).isEqualTo(string);
    }

    @Test
    public void testMapUnmapOfComplexRegexfilledString() {
        final String string = "org.eclipse.ditto:my.?Thing[]containing(a|lot|of|crazy)*stuff{}";

        final String mapped = SelectorUtil.quoteRegexCharacters(string);

        final String descriptionPattern = "Quoted string should not contain '%s'";

        assertThat(mapped)
                .as(descriptionPattern, ".").doesNotContain(".")
                .as(descriptionPattern, "?").doesNotContain("?")
                .as(descriptionPattern, "[").doesNotContain("[")
                .as(descriptionPattern, "|").doesNotContain("|");

        final String unmapped = SelectorUtil.unquoteRegexCharacters(mapped);

        assertThat(unmapped).isEqualTo(string);
    }

    @Test
    public void testMapUnmapOfThingIdWithManyDots() {
        final String string = "foo:my.thing.is.the.greatest.of.the.greatest.in.the.world";

        final String mapped = SelectorUtil.quoteRegexCharacters(string);

        assertThat(mapped).as("Quoted string should not contain '.'").doesNotContain(".");

        final String unmapped = SelectorUtil.unquoteRegexCharacters(mapped);

        assertThat(unmapped).isEqualTo(string);
    }

    @Test
    public void testOrSelectorWhenFirstSelectorMatches() {
        final JsonPointerSelector selector1 = JsonPointerSelectors.jsonPointer(JsonPointer.of("sel1"));
        final JsonPointerSelector selector2 = JsonPointerSelectors.jsonPointer(JsonPointer.of("sel2"));

        final JsonPointerSelector orSelector = SelectorUtil.or(selector1, selector2);

        assertThat(orSelector.matches(JsonPointer.of("sel1"))).isTrue();
    }

    @Test
    public void testOrSelectorWhenSecondSelectorMatches() {
        final JsonPointerSelector selector1 = JsonPointerSelectors.jsonPointer(JsonPointer.of("sel1"));
        final JsonPointerSelector selector2 = JsonPointerSelectors.jsonPointer(JsonPointer.of("sel2"));

        final JsonPointerSelector orSelector = SelectorUtil.or(selector1, selector2);

        assertThat(orSelector.matches(JsonPointer.of("sel2"))).isTrue();
    }

    @Test
    public void testOrSelectorWhenNoSelectorMatches() {
        final JsonPointerSelector selector1 = JsonPointerSelectors.jsonPointer(JsonPointer.of("noMatch"));
        final JsonPointerSelector selector2 = JsonPointerSelectors.jsonPointer(JsonPointer.of("noMatch2"));

        final JsonPointerSelector orSelector = SelectorUtil.or(selector1, selector2);

        assertThat(orSelector.matches(JsonPointer.of("sel"))).isFalse();
    }

}
