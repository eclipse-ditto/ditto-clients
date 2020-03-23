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
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_ID;
import static org.eclipse.ditto.client.TestConstants.Policy.POLICY_JSON_OBJECT;
import static org.eclipse.ditto.client.assertions.ClientAssertions.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.messaging.mock.MockMessagingProvider;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonMissingFieldException;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.policies.modify.CreatePolicyResponse;
import org.eclipse.ditto.signals.commands.policies.modify.DeletePolicyResponse;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyResponse;
import org.junit.Test;

public class DittoClientPoliciesTest extends AbstractDittoClientTest {

    @Test
    public void verifyClientDefaultsToSchemaVersion2ForPolicyCommands() throws InterruptedException {
        final MockMessagingProvider mockProvider = new MockMessagingProvider(JsonSchemaVersion.V_1);
        final DittoClient client = DittoClients.newInstance(mockProvider);

        final CountDownLatch latch = new CountDownLatch(1);
        mockProvider.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasSchemaVersion(JsonSchemaVersion.V_2)
                    .hasType("policies.commands:retrievePolicy");

            latch.countDown();
        });

        client.policies().retrieve(POLICY_ID);
        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
    }

    @Test
    public void testCreatePolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasType("policies.commands:createPolicy");

            messaging.receiveResponse(CreatePolicyResponse.of(POLICY_ID, POLICY, c.getDittoHeaders()));
            latch.countDown();
        });

        final CompletableFuture<Policy> policyCompletableFuture = client.policies().create(POLICY);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
        Assertions.assertThat(policyCompletableFuture).isCompletedWithValue(POLICY);
    }

    @Test
    public void testCreatePolicyJsonObject() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasType("policies.commands:createPolicy");

            messaging.receiveResponse(CreatePolicyResponse.of(POLICY_ID, POLICY, c.getDittoHeaders()));
            latch.countDown();
        });

        final CompletableFuture<Policy> policyCompletableFuture = client.policies().create(POLICY_JSON_OBJECT);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
        Assertions.assertThat(policyCompletableFuture).isCompletedWithValue(POLICY);
    }

    @Test
    public void createPolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> client.policies()
                        .create(POLICY, Options.Modify.exists(false))
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testPutPolicyJsonObject() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final LinkedList<Function<PolicyCommand<?>, ModifyPolicyResponse>> responseList = new LinkedList<>(Arrays.asList(
                c -> ModifyPolicyResponse.created(POLICY_ID, POLICY, c.getDittoHeaders()),
                c -> ModifyPolicyResponse.modified(POLICY_ID, c.getDittoHeaders()),
                c -> ModifyPolicyResponse.created(POLICY_ID, POLICY, c.getDittoHeaders())
        ));

        messaging.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasType("policies.commands:modifyPolicy");
            final ModifyPolicyResponse response = responseList.pop().apply(c);
            messaging.receiveResponse(response);
            latch.countDown();
        });

        final CompletableFuture<Optional<Policy>> createdResponse = client.policies().put(POLICY_JSON_OBJECT);
        final CompletableFuture<Optional<Policy>> modifiedResponse = client.policies().put(POLICY_JSON_OBJECT, Options.Modify.exists(true));
        final CompletableFuture<Optional<Policy>> createdResponse2 = client.policies().put(POLICY_JSON_OBJECT, Options.Modify.exists(false));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
        Assertions.assertThat(createdResponse).isCompletedWithValue(Optional.of(POLICY));
        Assertions.assertThat(modifiedResponse).isCompletedWithValue(Optional.empty());
        Assertions.assertThat(createdResponse2).isCompletedWithValue(Optional.of(POLICY));
    }

    @Test
    public void testPutPolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final LinkedList<Function<PolicyCommand<?>, ModifyPolicyResponse>> responseList = new LinkedList<>(Arrays.asList(
                c -> ModifyPolicyResponse.created(POLICY_ID, POLICY, c.getDittoHeaders()),
                c -> ModifyPolicyResponse.modified(POLICY_ID, c.getDittoHeaders()),
                c -> ModifyPolicyResponse.created(POLICY_ID, POLICY, c.getDittoHeaders())
        ));

        messaging.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasType("policies.commands:modifyPolicy");

            final ModifyPolicyResponse response = responseList.pop().apply(c);
            messaging.receiveResponse(response);
            latch.countDown();
        });

        final CompletableFuture<Optional<Policy>> createdResponse = client.policies().put(POLICY);
        final CompletableFuture<Optional<Policy>> modifiedResponse = client.policies().put(POLICY, Options.Modify.exists(true));
        final CompletableFuture<Optional<Policy>> createdResponse2 = client.policies().put(POLICY, Options.Modify.exists(false));

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
        Assertions.assertThat(createdResponse).isCompletedWithValue(Optional.of(POLICY));
        Assertions.assertThat(modifiedResponse).isCompletedWithValue(Optional.empty());
        Assertions.assertThat(createdResponse2).isCompletedWithValue(Optional.of(POLICY));
    }

    @Test
    public void testUpdatePolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasType("policies.commands:modifyPolicy");

            messaging.receiveResponse(ModifyPolicyResponse.modified(POLICY_ID, c.getDittoHeaders()));
            latch.countDown();
        });

        final CompletableFuture<Void> modifiedResponse = client.policies().update(POLICY);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
        Assertions.assertThat(modifiedResponse).isCompleted();
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

        messaging.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasType("policies.commands:deletePolicy");
            messaging.receiveResponse(DeletePolicyResponse.of(POLICY_ID, c.getDittoHeaders()));
            latch.countDown();
        });

        final CompletableFuture<Void> deletedResponse = client.policies().delete(POLICY_ID);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
        Assertions.assertThat(deletedResponse).isCompleted();
    }

    @Test
    public void deletePolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> client.policies()
                        .delete(POLICY_ID, Options.Modify.exists(false))
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testRetrievePolicy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        messaging.onPolicyCommand(c -> {
            assertThat(c)
                    .hasPolicyId(POLICY_ID)
                    .hasType("policies.commands:retrievePolicy");
            messaging.receiveResponse(RetrievePolicyResponse.of(POLICY_ID, POLICY, c.getDittoHeaders()));
            latch.countDown();
        });

        final CompletableFuture<Policy> retrievePolicyResponse = client.policies().retrieve(POLICY_ID);

        Assertions.assertThat(latch.await(TIMEOUT, TIME_UNIT)).isTrue();
        Assertions.assertThat(retrievePolicyResponse).isCompletedWithValue(POLICY);
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
