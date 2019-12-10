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
package org.eclipse.ditto.client;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.client.TestConstants.Policy.JSON_OBJECT;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_ID;
import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonMissingFieldException;
import org.eclipse.ditto.model.policies.PolicyId;
import org.junit.Test;

public class DittoClientPoliciesTest extends AbstractDittoClientTest {

    @Test
    public void testCreatePolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID))
                    .hasType("policies.commands:createPolicy");

            latch.countDown();
        });

        client.policies().create(PolicyId.of(POLICY_ID));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void createPolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> client.policies()
                        .create(PolicyId.of(POLICY_ID), Options.Modify.exists(false))
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testPutPolicyWithoutExistsOption() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID))
                    .hasType("policies.commands:modifyPolicy");

            latch.countDown();
        });

        client.policies().put(JSON_OBJECT);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testPutPolicyWithExistsOptionFalse() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID))
                    .hasType("policies.commands:modifyPolicy");

            latch.countDown();
        });

        client.policies().put(JSON_OBJECT, Options.Modify.exists(false));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testPutPolicyWithExistsOptionTrue() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID));

            latch.countDown();
        });

        client.policies().put(POLICY, Options.Modify.exists(true));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testUpdatePolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID))
                    .hasType("policies.commands:modifyPolicy");

            latch.countDown();
        });

        client.policies().update(POLICY);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void updatePolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(
                        () -> client.policies().update(POLICY, Options.Modify.exists(false)).get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testDeletePolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID))
                    .hasType("policies.commands:deletePolicy");

            latch.countDown();
        });

        client.policies().delete(PolicyId.of(POLICY_ID));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testRetrievePolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID))
                    .hasType("policies.commands:retrievePolicy");

            latch.countDown();
        });

        client.policies().retrieve(PolicyId.of(POLICY_ID));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testRetrievePolicyFromTwinChannel() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onCommandSend(c -> {
            assertThat(c)
                    .hasPolicyId(PolicyId.of(POLICY_ID))
                    .hasType("policies.commands:retrievePolicy");

            latch.countDown();
        });

        client.twin().forPolicy(PolicyId.of(POLICY_ID)).retrieve();

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void deletePolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> client.policies()
                        .delete(PolicyId.of(POLICY_ID), Options.Modify.exists(false))
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test(expected = JsonMissingFieldException.class)
    public void testCreatePolicyWithMissingId() {
        client.policies().create(JsonFactory.newObject());
    }

    @Test(expected = JsonMissingFieldException.class)
    public void testUpdatePolicyWithMissingId() {
        client.policies().update(JsonFactory.newObject());
    }
}
