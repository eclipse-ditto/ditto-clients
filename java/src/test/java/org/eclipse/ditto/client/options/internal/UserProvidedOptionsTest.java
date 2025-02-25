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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.ditto.client.options.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link UserProvidedOptions}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class UserProvidedOptionsTest {

    @Mock
    private Option<Boolean> responseRequiredOptionMock;

    private Option<?>[] options = null;
    private UserProvidedOptions underTest = null;


    @Before
    public void setUp() {
        options = new Option<?>[]{responseRequiredOptionMock};
        underTest = UserProvidedOptions.of(options);
    }
    
    @Test
    public void tryToCreateInstanceWithNullOptionArray() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> UserProvidedOptions.of(null))
                .withMessageContaining("options")
                .withMessageContaining("null");
    }

    @Test
    public void tryToAcceptNullVisitor() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> underTest.accept(null))
                .withMessageContaining("visitor")
                .withMessageContaining("null");
    }

    @Test
    public void visitorIsProvidedWithAllOptions() {
        final OptionVisitor<List<Option<?>>> visitor = new OptionCollectingVisitor();
        underTest.accept(visitor);

        assertThat(visitor.getValue()).contains(Arrays.asList(options));
    }

    @Test
    public void visitorIsFinishedAfterFirstOption() {
        final OptionVisitor<List<Option<?>>> visitor = new OptionCollectingVisitor(1);
        underTest.accept(visitor);

        final List<Option<?>> expectedOptions = Collections.singletonList(options[0]);

        assertThat(visitor.getValue()).contains(expectedOptions);
    }

    private static final class OptionCollectingVisitor implements OptionVisitor<List<Option<?>>> {

        private final List<Option<?>> options;
        private final int maxVisits;
        private int optionCount;

        public OptionCollectingVisitor() {
            this(Integer.MAX_VALUE);
        }

        private OptionCollectingVisitor(final int maxVisits) {
            this.maxVisits = maxVisits;
            options = new ArrayList<>();
            optionCount = 0;
        }

        @Override
        public boolean visit(final Option<?> option) {
            options.add(option);
            optionCount++;
            return optionCount >= maxVisits;
        }

        @Override
        public Optional<List<Option<?>>> getValue() {
            return Optional.of(options);
        }
    }

}
