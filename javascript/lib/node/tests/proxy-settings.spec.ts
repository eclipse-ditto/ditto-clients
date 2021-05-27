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
import { parse } from 'url';

function expectContainsUrl(agent: any, stringUrl: string) {
  const url = parse(stringUrl);
  expect(agent.proxy.host).toEqual(url.hostname);
  expect(`${agent.proxy.port}`).toEqual(url.port);
  expect(agent.proxy.protocol).toEqual(url.protocol);
}

function expectContainsCredentials(agent: any, username: string, password: string) {
  const encodedCredentials = Buffer.from(`${username}:${password}`).toString('base64');
  expect(agent.proxy.headers['Proxy-Authorization']).toEqual(`Basic ${encodedCredentials}`);
}

const PROXY_URL = 'http://localhost:3128';

describe('ProxyAgent', () => {

  describe('proxyAgent (https)', () => {
    beforeEach(() => {
      delete process.env.HTTPS_PROXY;
      delete process.env.https_PROXY;
    });

    it('has undefined agent if options are empty', () => {
      const underTest = new ProxyAgent();
      expect(underTest.proxyAgent).toBeUndefined();
    });

    it('builds agent with url', () => {
      const underTest = new ProxyAgent({ url: PROXY_URL });
      expect(underTest.proxyAgent).toBeDefined();
      expectContainsUrl(underTest.proxyAgent, PROXY_URL);
    });


    it('builds agent with url and credentials', () => {
      const username = 'user';
      // tslint:disable-next-line:no-hardcoded-credentials
      const password = 'pass';
      const underTest = new ProxyAgent({ username, password, url: PROXY_URL });
      expect(underTest.proxyAgent).toBeDefined();
      expectContainsUrl(underTest.proxyAgent, PROXY_URL);
      expectContainsCredentials(underTest.proxyAgent, username, password);
    });

    it('doesnt build agent with only credentials', () => {
      const username = 'user';
      // tslint:disable-next-line:no-hardcoded-credentials
      const password = 'pass';
      const underTest = new ProxyAgent({ username, password });
      expect(underTest.proxyAgent).toBeUndefined();
    });

    it('builds agent from HTTPS_PROXY environment variable', () => {
      process.env.HTTPS_PROXY = PROXY_URL;
      const underTest = new ProxyAgent();
      expect(underTest.proxyAgent).toBeDefined();
      expectContainsUrl(underTest.proxyAgent, PROXY_URL);
    });

    it('builds agent from https_proxy environment variable', () => {
      process.env.https_proxy = PROXY_URL;
      const underTest = new ProxyAgent();
      expect(underTest.proxyAgent).toBeDefined();
      expectContainsUrl(underTest.proxyAgent, PROXY_URL);
    });

    it('doesnt build agent from environment if disabled', () => {
      process.env.HTTPS_PROXY = PROXY_URL;
      const underTest = new ProxyAgent({ ignoreProxyFromEnv: true });
      expect(underTest.proxyAgent).toBeUndefined();
    });

  });
  describe('httpProxyAgent', () => {
    beforeEach(() => {
      delete process.env.HTTP_PROXY;
      delete process.env.http_PROXY;
    });

    it('has undefined agent if options are empty', () => {
      const underTest = new ProxyAgent();
      expect(underTest.httpProxyAgent).toBeUndefined();
    });

    it('builds agent with url', () => {
      const underTest = new ProxyAgent({ url: PROXY_URL });
      expect(underTest.httpProxyAgent).toBeDefined();
      expectContainsUrl(underTest.httpProxyAgent, PROXY_URL);
    });


    it('builds agent with url and credentials', () => {
      const username = 'user';
      // tslint:disable-next-line:no-hardcoded-credentials
      const password = 'pass';
      const underTest = new ProxyAgent({ username, password, url: PROXY_URL });
      expect(underTest.httpProxyAgent).toBeDefined();
      expectContainsUrl(underTest.httpProxyAgent, PROXY_URL);
      expectContainsCredentials(underTest.httpProxyAgent, username, password);
    });

    it('doesnt build agent with only credentials', () => {
      const username = 'user';
      // tslint:disable-next-line:no-hardcoded-credentials
      const password = 'pass';
      const underTest = new ProxyAgent({ username, password });
      expect(underTest.httpProxyAgent).toBeUndefined();
    });

    it('builds agent from HTTP_PROXY environment variable', () => {
      process.env.HTTP_PROXY = PROXY_URL;
      const underTest = new ProxyAgent();
      expect(underTest.httpProxyAgent).toBeDefined();
      expectContainsUrl(underTest.httpProxyAgent, PROXY_URL);
    });

    it('builds agent from http_proxy environment variable', () => {
      process.env.http_proxy = PROXY_URL;
      const underTest = new ProxyAgent();
      expect(underTest.httpProxyAgent).toBeDefined();
      expectContainsUrl(underTest.httpProxyAgent, PROXY_URL);
    });

    it('doesnt build agent from environment if disabled', () => {
      process.env.HTTP_PROXY = PROXY_URL;
      const underTest = new ProxyAgent({ ignoreProxyFromEnv: true });
      expect(underTest.httpProxyAgent).toBeUndefined();
    });

  });


});
