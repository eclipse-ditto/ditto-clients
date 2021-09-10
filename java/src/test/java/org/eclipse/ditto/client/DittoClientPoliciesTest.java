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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.client.internal.AbstractDittoClientTest;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonMissingFieldException;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.policies.model.signals.commands.PolicyCommand;
import org.eclipse.ditto.policies.model.signals.commands.PolicyCommandResponse;
import org.eclipse.ditto.policies.model.signals.commands.PolicyErrorResponse;
import org.eclipse.ditto.policies.model.signals.commands.exceptions.PolicyNotAccessibleException;
import org.eclipse.ditto.policies.model.signals.commands.modify.CreatePolicy;
import org.eclipse.ditto.policies.model.signals.commands.modify.CreatePolicyResponse;
import org.eclipse.ditto.policies.model.signals.commands.modify.DeletePolicy;
import org.eclipse.ditto.policies.model.signals.commands.modify.DeletePolicyResponse;
import org.eclipse.ditto.policies.model.signals.commands.modify.ModifyPolicy;
import org.eclipse.ditto.policies.model.signals.commands.modify.ModifyPolicyResponse;
import org.eclipse.ditto.policies.model.signals.commands.query.RetrievePolicy;
import org.eclipse.ditto.policies.model.signals.commands.query.RetrievePolicyResponse;
import org.junit.Test;

/**
 * Test the policies interface.
 */
public final class DittoClientPoliciesTest extends AbstractDittoClientTest {

    @Test
    public void testCreatePolicy() throws Exception {
        final CompletableFuture<Policy> policyCompletableFuture =
                client.policies().create(POLICY).toCompletableFuture();
        final CreatePolicy command = expectMsgClass(CreatePolicy.class);
        reply(CreatePolicyResponse.of(command.getEntityId(), command.getPolicy(), command.getDittoHeaders()));
        policyCompletableFuture.get(TIMEOUT, TIME_UNIT);
        Assertions.assertThat(policyCompletableFuture).isCompletedWithValue(POLICY);
    }

    @Test
    public void testCreatePolicyJsonObject() throws Exception {
        final CompletableFuture<Policy> policyCompletableFuture =
                client.policies().create(POLICY_JSON_OBJECT).toCompletableFuture();
        final CreatePolicy command = expectMsgClass(CreatePolicy.class);
        reply(CreatePolicyResponse.of(command.getEntityId(), command.getPolicy(), command.getDittoHeaders()));
        policyCompletableFuture.get(TIMEOUT, TIME_UNIT);
        Assertions.assertThat(policyCompletableFuture).isCompletedWithValue(POLICY);
    }

    @Test
    public void createPolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> client.policies()
                        .create(POLICY, Options.Modify.exists(false))
                        .toCompletableFuture()
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testPutPolicyJsonObject() throws Exception {
        final LinkedList<Function<PolicyCommand<?>, PolicyCommandResponse<?>>> responseList =
                new LinkedList<>(Arrays.asList(
                        c -> ModifyPolicyResponse.created(POLICY_ID, POLICY, c.getDittoHeaders()),
                        c -> ModifyPolicyResponse.modified(POLICY_ID, c.getDittoHeaders()),
                        c -> CreatePolicyResponse.of(POLICY_ID, POLICY, c.getDittoHeaders())
                ));

        final CompletableFuture<Optional<Policy>> createdResponse =
                client.policies().put(POLICY_JSON_OBJECT).toCompletableFuture();
        final CompletableFuture<Optional<Policy>> modifiedResponse =
                client.policies().put(POLICY_JSON_OBJECT, Options.Modify.exists(true)).toCompletableFuture();
        final CompletableFuture<Optional<Policy>> createdResponse2 =
                client.policies().put(POLICY_JSON_OBJECT, Options.Modify.exists(false)).toCompletableFuture();
        final List<PolicyCommand<?>> commands = Arrays.asList(
                expectMsgClass(ModifyPolicy.class),
                expectMsgClass(ModifyPolicy.class),
                expectMsgClass(ModifyPolicy.class)
        );
        for (int i = 0; i < 3; ++i) {
            reply(responseList.get(i).apply(commands.get(i)));
        }

        CompletableFuture.allOf(createdResponse, modifiedResponse, createdResponse2).get(TIMEOUT, TIME_UNIT);

        Assertions.assertThat(createdResponse).isCompletedWithValue(Optional.of(POLICY));
        Assertions.assertThat(modifiedResponse).isCompletedWithValue(Optional.empty());
        Assertions.assertThat(createdResponse2).isCompletedWithValue(Optional.of(POLICY));
    }

    @Test
    public void testPutPolicy() throws Exception {
        final LinkedList<Function<PolicyCommand<?>, ModifyPolicyResponse>> responseList =
                new LinkedList<>(Arrays.asList(
                        c -> ModifyPolicyResponse.created(POLICY_ID, POLICY, c.getDittoHeaders()),
                        c -> ModifyPolicyResponse.modified(POLICY_ID, c.getDittoHeaders()),
                        c -> ModifyPolicyResponse.created(POLICY_ID, POLICY, c.getDittoHeaders())
                ));

        final CompletableFuture<Optional<Policy>> createdResponse = client.policies().put(POLICY).toCompletableFuture();
        final CompletableFuture<Optional<Policy>> modifiedResponse =
                client.policies().put(POLICY, Options.Modify.exists(true)).toCompletableFuture();
        final CompletableFuture<Optional<Policy>> createdResponse2 =
                client.policies().put(POLICY, Options.Modify.exists(false)).toCompletableFuture();
        final List<PolicyCommand<?>> commands = Arrays.asList(
                expectMsgClass(ModifyPolicy.class),
                expectMsgClass(ModifyPolicy.class),
                expectMsgClass(ModifyPolicy.class)
        );
        for (int i = 0; i < 3; ++i) {
            reply(responseList.get(i).apply(commands.get(i)));
        }

        CompletableFuture.allOf(createdResponse, modifiedResponse, createdResponse2).get(TIMEOUT, TIME_UNIT);

        Assertions.assertThat(createdResponse).isCompletedWithValue(Optional.of(POLICY));
        Assertions.assertThat(modifiedResponse).isCompletedWithValue(Optional.empty());
        Assertions.assertThat(createdResponse2).isCompletedWithValue(Optional.of(POLICY));
    }

    @Test
    public void testUpdatePolicy() {
        assertEventualCompletion(client.policies().update(POLICY));
        reply(ModifyPolicyResponse.modified(POLICY_ID, expectMsgClass(ModifyPolicy.class).getDittoHeaders()));
    }

    @Test
    public void updatePolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                client.policies()
                        .update(POLICY, Options.Modify.exists(false))
                        .toCompletableFuture()
                        .get(TIMEOUT, TIME_UNIT)
        );
    }

    @Test
    public void testDeletePolicy() {
        assertEventualCompletion(client.policies().delete(POLICY_ID));
        reply(DeletePolicyResponse.of(POLICY_ID, expectMsgClass(DeletePolicy.class).getDittoHeaders()));
    }

    @Test
    public void deletePolicyFailsWithExistsOption() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> client.policies()
                        .delete(POLICY_ID, Options.Modify.exists(false))
                        .toCompletableFuture()
                        .get(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void testRetrievePolicy() throws Exception {
        final CompletableFuture<Policy> retrievePolicyResponse = client.policies()
                .retrieve(POLICY_ID)
                .toCompletableFuture();
        reply(RetrievePolicyResponse.of(POLICY_ID, POLICY, expectMsgClass(RetrievePolicy.class).getDittoHeaders()));
        retrievePolicyResponse.get(TIMEOUT, TIME_UNIT);
        Assertions.assertThat(retrievePolicyResponse).isCompletedWithValue(POLICY);
    }

    @Test
    public void testRetrievePolicyFails() {
        assertEventualCompletion(client.policies().retrieve(POLICY_ID).handle((response, error) -> {
            assertThat(error)
                    .describedAs("Expect failure with %s, got response=%s, error=%s",
                            PolicyNotAccessibleException.class.getSimpleName(), response, error)
                    .isInstanceOf(CompletionException.class)
                    .hasCauseInstanceOf(PolicyNotAccessibleException.class);
            return null;
        }));
        final RetrievePolicy retrievePolicy = expectMsgClass(RetrievePolicy.class);
        reply(PolicyErrorResponse.of(PolicyNotAccessibleException.newBuilder(POLICY_ID)
                .dittoHeaders(retrievePolicy.getDittoHeaders())
                .build()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePolicyWithMissingId() {
        client.policies().create(JsonFactory.newObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePolicyWithMissingId() {
        client.policies().update(JsonFactory.newObject());
    }
}
