/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
import static org.eclipse.ditto.client.options.internal.MockOptionFactory.createOptionMock;

import org.eclipse.ditto.client.options.OptionName;
import org.eclipse.ditto.policies.model.PolicyId;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link CopyPolicyOptionVisitor}.
 */
public final class CopyPolicyOptionVisitorTest {

    private CopyPolicyOptionVisitor underTest = null;


    @Before
    public void setUp() {
        underTest = new CopyPolicyOptionVisitor();
    }

    @Test
    public void tryToVisitNullOption() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> underTest.visit(null))
                .withMessageContaining("option to be visited")
                .withMessageContaining("null");
    }

    @Test
    public void getNoValueIfOptionNameIsUnexpected() {
        final String value = "Booh!";

        final boolean isFinished = underTest.visit(createOptionMock(new OptionName() {
            @Override
            public boolean test(final Object o) {
                return false;
            }
        }, value));

        assertThat(isFinished).isFalse();
        assertThat(underTest.getValue()).isEmpty();
    }

    @Test
    public void optionValueTypeDiffersFromExpectedType() {
        final String value = "Booh!";

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> underTest.visit(createOptionMock(OptionName.Modify.COPY_POLICY, value)))
                .withMessage(String.format("The option value <%s> is not of expected type!", value))
                .withCauseInstanceOf(ClassCastException.class);
    }

    @Test
    public void optionValueIsExpected() {
        final PolicyId policyId = PolicyId.inNamespaceWithRandomName("org.eclipse.ditto");

        final boolean isFinished =
                underTest.visit(createOptionMock(OptionName.Modify.COPY_POLICY, policyId));

        assertThat(isFinished).isTrue();
        assertThat(underTest.getValue()).contains(policyId);
    }

}
