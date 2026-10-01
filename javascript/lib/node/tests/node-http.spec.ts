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
import { HttpVerb } from '../../api/src/client/constants/http-verb';
import * as http from 'http';
import { AddressInfo } from 'net';
import { clearProxyEnvironment } from './proxy-environment';

describe('NodeHttp', () => {
  let restoreEnvironment: () => void;

  afterAll(nock.restore);

  afterEach(nock.cleanAll);
  afterEach(() => restoreEnvironment());

  beforeEach(() => {
    restoreEnvironment = clearProxyEnvironment();
  });

  it.each(['http', 'https'])('bypasses the environment proxy for NO_PROXY destinations over %s', async protocol => {
    process.env.HTTP_PROXY = 'http://proxy.invalid:3128';
    process.env.HTTPS_PROXY = 'http://proxy.invalid:3128';
    process.env.NO_PROXY = 'localhost';
    const proxyAgent = new ProxyAgent({ username: 'synthetic-user', password: 'synthetic-password' });
    let proxyAuthorization: unknown = 'unset';
    nock(`${protocol}://localhost:8080`)
      .get('/get')
      .reply(200, function () {
        proxyAuthorization = this.req.getHeader('proxy-authorization');
        return { proxied: Boolean((this.req as any).options.agent) };
      });
    const response = await new NodeRequester(proxyAgent)
      .doRequest(HttpVerb.GET, `${protocol}://localhost:8080/get`, new Map(), '');
    expect(response.body).toEqual({ proxied: false });
    expect(proxyAuthorization).toBeUndefined();
  });

  it.each([false, true])('reaches the destination without leaking credentials (explicit proxy=%s)', async explicit => {
    let proxyRequests = 0;
    let proxyAuthorization: unknown = 'unset';
    const destination = http.createServer((req, res) => {
      proxyAuthorization = req.headers['proxy-authorization'];
      res.end(JSON.stringify({ direct: true }));
    });
    const proxy = http.createServer((_req, res) => {
      proxyRequests++;
      res.writeHead(502);
      res.end();
    });
    try {
      await Promise.all([destination, proxy].map(server => new Promise<void>((resolve, reject) => {
        server.once('error', reject);
        server.listen(0, '127.0.0.1', resolve);
      })));
      const url = `http://127.0.0.1:${(destination.address() as AddressInfo).port}/get`;
      process.env.HTTP_PROXY = `http://127.0.0.1:${(proxy.address() as AddressInfo).port}`;
      process.env.NO_PROXY = '127.0.0.1';
      const options = {
        username: 'synthetic-user', password: 'synthetic-password',
        url: explicit ? process.env.HTTP_PROXY : undefined
      };
      const response = await new NodeRequester(new ProxyAgent(options))
        .doRequest(HttpVerb.GET, url, new Map(), '');
      expect(response.body).toEqual({ direct: true });
      expect(proxyAuthorization).toBeUndefined();
      expect(proxyRequests).toBe(0);
      delete process.env.NO_PROXY;
      await expect(new NodeRequester(new ProxyAgent(options))
        .doRequest(HttpVerb.GET, url, new Map(), '')).rejects.toBeDefined();
      expect(proxyRequests).toBe(1);
    } finally {
      await Promise.all([destination, proxy].map(server => new Promise<void>(resolve => server.close(() => resolve()))));
    }
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

    const request = underTest.doRequest(HttpVerb.GET, 'http://localhost:8080/get', new Map(), payload);
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

    const request = underTest.doRequest(HttpVerb.GET, 'http://localhost:8080/get', new Map(), payload);
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

    const request = underTest.doRequest(HttpVerb.GET, 'https://localhost:8080/get', new Map(), payload);
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

    const request = underTest.doRequest(HttpVerb.GET, 'https://localhost:8080/get?bum=baz', new Map(), payload);
    return request
      .then(response => {
        expect(response.body).toEqual(expectedResponsePayload);
      }, rejected => {
        fail(rejected);
      });
  });

});
