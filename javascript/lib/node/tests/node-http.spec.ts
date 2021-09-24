/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import { ProxyAgent } from '../src/proxy-settings';
import { NodeRequester } from '../src/node-http';
import nock = require('nock');

describe('NodeHttp', () => {

  afterAll(nock.restore);

  afterEach(nock.cleanAll);

  beforeEach(() => {
    delete process.env.HTTPS_PROXY;
    delete process.env.https_proxy;
    delete process.env.HTTP_PROXY;
    delete process.env.http_proxy;
  });

  it('sends requests', () => {
    const payload = 'hello';
    const expectedResponsePayload = { foo: 'bar' };
    const proxyAgent = new ProxyAgent();

    nock('http://localhost:8080')
      .get('/get', payload)
      .reply(200, function () {
        if ((this.req as any).options.agent) {
          return '"Request was using a proxy agent where it shouldn\'t"';
        }
        return expectedResponsePayload;
      });

    const underTest = new NodeRequester(proxyAgent);

    const request = underTest.doRequest('GET', 'http://localhost:8080/get', new Map(), payload);
    return request
      .then(response => {
        expect(response.body).toEqual(expectedResponsePayload);
      }, rejected => {
        fail(rejected);
      });
  });

  it('uses the http proxy agent for http requests', () => {
    const payload = 'hello';
    const expectedResponsePayload = { foo: 'bar' };

    process.env.HTTP_PROXY = 'http://http-proxy';
    const proxyAgent = new ProxyAgent();

    nock('http://localhost:8080')
      .get('/get', payload)
      .reply(200, function () {
        if ((this.req as any).options.agent === proxyAgent.httpProxyAgent) {
          return expectedResponsePayload;
        }
        return '"Request wasn\'t using the expected http proxy agent"';
      });

    const underTest = new NodeRequester(proxyAgent);

    const request = underTest.doRequest('GET', 'http://localhost:8080/get', new Map(), payload);
    return request
      .then(response => {
        expect(response.body).toEqual(expectedResponsePayload);
      }, rejected => {
        fail(rejected);
      });
  });

  it('uses the https proxy agent for https requests', () => {
    const payload = 'hello';
    const expectedResponsePayload = { foo: 'bar' };

    process.env.HTTPS_PROXY = 'http://https-proxy';
    const proxyAgent = new ProxyAgent();

    nock('https://localhost:8080')
      .get('/get', payload)
      .reply(200, function () {
        if ((this.req as any).options.agent === proxyAgent.proxyAgent) {
          return expectedResponsePayload;
        }
        return '"Request wasn\'t using the expected https proxy agent"';
      });

    const underTest = new NodeRequester(proxyAgent);

    const request = underTest.doRequest('GET', 'https://localhost:8080/get', new Map(), payload);
    return request
      .then(response => {
        expect(response.body).toEqual(expectedResponsePayload);
      }, rejected => {
        fail(rejected);
      });
  });

  it('sends query params', () => {
    const payload = 'hello';
    const expectedResponsePayload = { foo: 'bar' };


    nock('https://localhost:8080')
      .get('/get?bum=baz', payload)
      .reply(200, expectedResponsePayload);

    const underTest = new NodeRequester(new ProxyAgent({}));

    const request = underTest.doRequest('GET', 'https://localhost:8080/get?bum=baz', new Map(), payload);
    return request
      .then(response => {
        expect(response.body).toEqual(expectedResponsePayload);
      }, rejected => {
        fail(rejected);
      });
  });

});
