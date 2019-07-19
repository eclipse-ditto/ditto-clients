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
package org.eclipse.ditto.client.rule;

import java.util.Queue;

import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Fails the Unit Tests if any exception occurred.
 */
public class FailOnExceptionRule implements TestRule {

    private final Queue<Throwable> caught;

    public FailOnExceptionRule(final Queue<Throwable> queue) {
        caught = queue;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // run test
                base.evaluate();

                // check if exception reported
                if (!caught.isEmpty()) {
                    Assert.fail(caught.size() + " exception(s) caught in test, failed!");
                }
            }
        };
    }
}
