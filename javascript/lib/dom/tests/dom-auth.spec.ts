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


import { DomBase64Encoder, DomHttpBasicAuth, DomWebSocketBasicAuth } from '../src/dom-auth';
import { DittoURL, ImmutableURL } from '@eclipse-ditto/ditto-javascript-client-api_0';

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
