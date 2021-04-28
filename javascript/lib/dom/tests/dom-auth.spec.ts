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


import { DefaultTokenSupplier, TokenSupplier } from '../../api/src/auth/bearer-auth';
import { DittoURL, ImmutableURL } from '../../api/src/auth/auth-provider';

import { DomBase64Encoder, DomHttpBasicAuth, DomHttpBearerAuth, DomWebSocketBasicAuth, DomWebSocketBearerAuth } from '../src/dom-auth';


const USERNAME = 'ditto';
const PASSWORD = 'foo$bar';
const AUTH_DECODED = `${USERNAME}:${PASSWORD}`;
const AUTH_URL_ENCODED = `${USERNAME}:foo%24bar`;
const AUTH_BASE64_ENCODED = 'ZGl0dG86Zm9vJGJhcg==';

const DEFAULT_KEY = 'foo';
const DEFAULT_VAL = 'bar';
const DEFAULT_URL_PARAM = `${DEFAULT_KEY}=${DEFAULT_VAL}`;

describe('DomBase64Encoder', () => {

  it('encodes base64', () => {
    const encoder = new DomBase64Encoder();
    expect(encoder.encodeBase64(AUTH_DECODED)).toEqual(AUTH_BASE64_ENCODED);
  });

});

describe('DomHttpBasicAuth', () => {

  it('adds basic auth header', () => {
    const basicAuth = DomHttpBasicAuth.newInstance(USERNAME, PASSWORD);

    const dittoHeaders = basicAuth.authenticateWithHeaders(defaultHeaders());
    expect(dittoHeaders.size).toEqual(2);
    expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
    expect(dittoHeaders.get('Authorization')).toEqual(`Basic ${AUTH_BASE64_ENCODED}`);
  });

  it('leaves url as it is', () => {
    const basicAuth = DomHttpBasicAuth.newInstance(USERNAME, PASSWORD);
    const expected = defaultUrl();

    const actual = basicAuth.authenticateWithUrl(defaultUrl());

    expectEquals(actual, expected);
  });

});
describe('DomWebSocketBasicAuth', () => {

  it('leaves headers as they are', () => {
    const basicAuth = DomWebSocketBasicAuth.newInstance(USERNAME, PASSWORD);

    const dittoHeaders = basicAuth.authenticateWithHeaders(defaultHeaders());
    expect(dittoHeaders.size).toEqual(1);
    expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
  });

  it('enhances url with username and password', () => {
    const basicAuth = DomWebSocketBasicAuth.newInstance(USERNAME, PASSWORD);
    const expected = defaultUrl().withDomain(`${AUTH_URL_ENCODED}@${defaultUrl().domain}`);

    const actual = basicAuth.authenticateWithUrl(defaultUrl());

    expectEquals(actual, expected);
  });

});

describe('DomHttpBearerAuth', () => {


  const exampleToken1 = 'bGLYQpCUgchwipBMEXNeyqGglINUbh';
  const exampleToken2 = 'ClEXwgYsJGPfrmRVuKpnmsXekuyhbx';

  it('should leave urls as they are', () => {
    const bearerAuth = DomHttpBearerAuth.newInstance(new DefaultTokenSupplier(exampleToken1));

    const expected = defaultUrl();
    const actual = bearerAuth.authenticateWithUrl(defaultUrl());

    expectEquals(actual, expected);
  });

  it('should add a Authorization header', () => {
    const bearerAuth = DomHttpBearerAuth.newInstance(new DefaultTokenSupplier(exampleToken1));

    const dittoHeaders = bearerAuth.authenticateWithHeaders(defaultHeaders());
    expect(dittoHeaders.size).toEqual(2);
    expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
    expect(dittoHeaders.get('Authorization')).toEqual(`Bearer ${exampleToken1}`);
  });

  it('should use the token provided by the TokenSupplier', () => {
    const testSupplier = new TestTokenSupplier();
    testSupplier.testToken = exampleToken1;
    const bearerAuth = DomHttpBearerAuth.newInstance(testSupplier);

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


describe('DomWebSocketBearerAuth', () => {

  const exampleToken1 = 'bGLYQpCUgchwipBMEXNeyqGglINUbh';
  const exampleToken2 = 'ClEXwgYsJGPfrmRVuKpnmsXekuyhbx';

  it('should add access_token parameter', () => {
    const bearerAuth = DomWebSocketBearerAuth.newInstance(new DefaultTokenSupplier(exampleToken1));
    const baseUrl = defaultUrl().toString();
    const expected = `${baseUrl}&access_token=${exampleToken1}`;
    const actual = bearerAuth.authenticateWithUrl(defaultUrl()).toString();

    expect(actual).toEqual(expected);
  });

  it('should not add Authorization header', () => {
    const bearerAuth = DomWebSocketBearerAuth.newInstance(new DefaultTokenSupplier(exampleToken1));

    const dittoHeaders = bearerAuth.authenticateWithHeaders(defaultHeaders());
    expect(dittoHeaders.size).toEqual(1);
    expect(dittoHeaders.get(DEFAULT_KEY)).toEqual(DEFAULT_VAL);
  });

  it('should use the token provided by the TokenSupplier', () => {
    const testSupplier = new TestTokenSupplier();
    testSupplier.testToken = exampleToken1;
    const bearerAuth = DomWebSocketBearerAuth.newInstance(testSupplier);

    let authenticatedUrl = bearerAuth.authenticateWithUrl(defaultUrl());
    expect(authenticatedUrl.queryParams.length).toEqual(2);
    expect(authenticatedUrl.queryParams[1]).toEqual(`access_token=${exampleToken1}`);

    testSupplier.testToken = exampleToken2;

    authenticatedUrl = bearerAuth.authenticateWithUrl(defaultUrl());
    expect(authenticatedUrl.queryParams.length).toEqual(2);
    expect(authenticatedUrl.queryParams[1]).toEqual(`access_token=${exampleToken2}`);
  });

});


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
