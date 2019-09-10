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


import { BasicAuth, DittoURL, ImmutableURL } from '@eclipse-ditto/ditto-javascript-client-api_0';
import { NodeBase64Encoder, NodeHttpBasicAuth, NodeWebSocketBasicAuth } from '../src/node-auth';

const USERNAME = 'ditto';
const PASSWORD = 'foo$bar';
const AUTH_DECODED = `${USERNAME}:${PASSWORD}`;
const AUTH_BASE64_ENCODED = 'ZGl0dG86Zm9vJGJhcg==';

const DEFAULT_KEY = 'foo';
const DEFAULT_VAL = 'bar';
const DEFAULT_URL_PARAM = `${DEFAULT_KEY}=${DEFAULT_VAL}`;

describe('NodeBase64Encoder', () => {

  it('encodes base64', () => {
    const encoder = new NodeBase64Encoder();
    expect(encoder.encodeBase64(AUTH_DECODED)).toEqual(AUTH_BASE64_ENCODED);
  });

});

describe('NodeHttpBasicAuth', () => {

  it('adds basic auth header', () => {
    expectAddsBasicAuthHeader(NodeHttpBasicAuth.newInstance(USERNAME, PASSWORD));
  });

  it('leaves url as it is', () => {
    expectLeavesUrlAsItIs(NodeHttpBasicAuth.newInstance(USERNAME, PASSWORD));
  });

});

describe('NodeWebSocketBasicAuth', () => {

  it('adds basic auth header', () => {
    expectAddsBasicAuthHeader(NodeWebSocketBasicAuth.newInstance(USERNAME, PASSWORD));
  });

  it('leaves url as it is', () => {
    expectLeavesUrlAsItIs(NodeWebSocketBasicAuth.newInstance(USERNAME, PASSWORD));
  });

});

const expectAddsBasicAuthHeader = (implementation: BasicAuth) => {
  const dittoHeaders = implementation.authenticateWithHeaders(defaultHeaders());
  expect(dittoHeaders.size).toEqual(2);
  expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
  expect(dittoHeaders.get('Authorization')).toEqual(`Basic ${AUTH_BASE64_ENCODED}`);
};

const expectLeavesUrlAsItIs = (implementation: BasicAuth) => {
  const expected = defaultUrl();

  const actual = implementation.authenticateWithUrl(defaultUrl());

  expectEquals(actual, expected);
};

const defaultHeaders = () => {
  return new Map().set(DEFAULT_KEY, DEFAULT_VAL);
};

const defaultUrl = () => {
  return ImmutableURL.newInstance('http', 'ditto.eclipse.org', '/api/2', [DEFAULT_URL_PARAM]);
};

const expectEquals = (actual: DittoURL, toEqual: DittoURL) => {
  expect(actual.domain).toEqual(toEqual.domain);
  expect(actual.path).toEqual(toEqual.path);
  expect(actual.protocol).toEqual(toEqual.protocol);
  expect(actual.queryParams).toEqual(toEqual.queryParams);
};
