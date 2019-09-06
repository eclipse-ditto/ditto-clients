/*!
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

module.exports = {
    roots: [
        "tests"
    ],
    transform: {
        "^.+\\.tsx?$": "ts-jest"
    },
    testEnvironment: "node", // use node env instead of jsdom for the node implementation tests
    collectCoverage: true,
    collectCoverageFrom: [
        "src/**/*.ts"
    ],
    coverageReporters: ["text"]
};
