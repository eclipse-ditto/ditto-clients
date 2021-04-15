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

import { BasicAuth } from '../../api/src/auth/basic-auth';
import { DefaultTokenSupplier, TokenSupplier } from '../../api/src/auth/bearer-auth';
import { DittoURL, ImmutableURL } from '../../api/src/auth/auth-provider';
import { NodeBase64Encoder, NodeHttpBasicAuth, NodeHttpBearerAuth, NodeWebSocketBasicAuth } from '../src/node-auth';

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

describe('NodeHttpBearerAuth', () => {

  const exampleToken1 = 'bGLYQpCUgchwipBMEXNeyqGglINUbh';
  const exampleToken2 = 'ClEXwgYsJGPfrmRVuKpnmsXekuyhbx';

  it('should leave urls as they are', () => {
    const bearerAuth = NodeHttpBearerAuth.newInstance(new DefaultTokenSupplier(exampleToken1));

    const expected = defaultUrl();
    const actual = bearerAuth.authenticateWithUrl(defaultUrl());

    expectEquals(actual, expected);
  });

  it('should add a Authorization header', () => {
    const bearerAuth = NodeHttpBearerAuth.newInstance(new DefaultTokenSupplier(exampleToken1));

    const dittoHeaders = bearerAuth.authenticateWithHeaders(defaultHeaders());
    expect(dittoHeaders.size).toEqual(2);
    expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
    expect(dittoHeaders.get('Authorization')).toEqual(`Bearer ${exampleToken1}`);
  });

  it('should use the token provided by the TokenSupplier', () => {
    const testSupplier = new TestTokenSupplier();
    testSupplier.testToken = exampleToken1;
    const bearerAuth = NodeHttpBearerAuth.newInstance(testSupplier);

    let dittoHeaders = bearerAuth.authenticateWithHeaders(defaultHeaders());
    expect(dittoHeaders.size).toEqual(2);
    expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
    expect(dittoHeaders.get('Authorization')).toEqual(`Bearer ${exampleToken1}`);

    testSupplier.testToken = exampleToken2;

    dittoHeaders = bearerAuth.authenticateWithHeaders(defaultHeaders());
    expect(dittoHeaders.size).toEqual(2);
    expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
    expect(dittoHeaders.get('Authorization')).toEqual(`Bearer ${exampleToken2}`);
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

/**
 * Used to test BearerAuth
 */
class TestTokenSupplier implements TokenSupplier {

  public testToken = '';

  getToken(): string {
    return this.testToken;
  }

}

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
