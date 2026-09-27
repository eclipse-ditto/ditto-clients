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
import { clearProxyEnvironment } from './proxy-environment';

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
  let restoreEnvironment: () => void;

  beforeEach(() => {
    restoreEnvironment = clearProxyEnvironment();
  });

  afterEach(() => {
    restoreEnvironment();
    jest.restoreAllMocks();
  });

  describe('destination exclusions', () => {
    beforeEach(() => {
      process.env.HTTP_PROXY = PROXY_URL;
      process.env.HTTPS_PROXY = PROXY_URL;
    });

    it.each([
      ['localhost', 'http://localhost', true],
      ['localhost', 'https://localhost', true],
      ['localhost', 'ws://localhost', true],
      ['localhost', 'wss://localhost', true],
      ['localhost', 'http://notlocalhost', false],
      ['example.org', 'https://example.org.evil.test', false],
      ['example.org', 'https://sub.example.org', true],
      ['corp.example.com', 'https://ditto.corp.example.com', true],
      ['example.org', 'https://badexample.org', false],
      ['.example.org', 'https://sub.example.org', true],
      ['*.example.org', 'https://sub.example.org', true],
      ['.example.org', 'https://example.org', false],
      ['.example.org', 'https://badexample.org', false],
      [' EXAMPLE.ORG, other.test ', 'http://example.org', true],
      ['*', 'https://anything.test', true],
      ['*:8080', 'http://anything.test:8080', true],
      ['*:8080', 'http://anything.test:8081', false],
      ['localhost:8080', 'http://localhost:8080', true],
      ['localhost:8080', 'http://localhost:8081', false],
      ['localhost:80', 'http://localhost', true],
      ['localhost:443', 'https://localhost', true],
      ['localhost:80', 'ws://localhost', true],
      ['localhost:443', 'wss://localhost', true],
      ['localhost:80', 'https://localhost', false],
      ['127.0.0.1', 'http://127.0.0.1', true],
      ['[::1]', 'http://[::1]', true],
      ['::1', 'http://[::1]', true],
      ['2001:db8::1', 'http://[2001:db8::1]:8080', true],
      ['[::1]:8080', 'http://[::1]:8080', true],
      ['[::1]:8080', 'http://[::1]:8081', false],
      ['', 'http://localhost', false],
      ['localhost:invalid', 'http://localhost', false],
      ['127.0.0.0/8', 'http://127.0.0.1', false],
      ['example.org', 'http://example.org.', true],
      ['example.org.', 'http://example.org', true]
    ])('NO_PROXY=%s for %s has bypass=%s', (exclusion, destination, bypass) => {
      process.env.NO_PROXY = exclusion as string;
      const agent = new ProxyAgent();
      expect(agent.getAgentForUrl(new URL(destination as string)) === undefined).toBe(bypass);
    });

    it('warns once for unsupported entries, without logging their values or reparsing per request', () => {
      const warning = jest.spyOn(console, 'warn').mockImplementation(() => { /* captured below */ });
      process.env.NO_PROXY = '10.0.0.0/8,http://example.org,*.,host:invalid,localhost';
      const agent = new ProxyAgent();
      expect(warning).toHaveBeenCalledTimes(1);
      expect(warning.mock.calls[0][0]).not.toContain('10.0.0.0');
      expect(agent.getAgentForUrl(new URL('http://localhost'))).toBeUndefined();
      expect(agent.getAgentForUrl(new URL('http://10.1.2.3'))).toBe(agent.httpProxyAgent);
      process.env.NO_PROXY = '*';
      expect(agent.getAgentForUrl(new URL('http://remote.test'))).toBe(agent.httpProxyAgent);
      expect(warning).toHaveBeenCalledTimes(1);
    });

    it('canonicalizes IDN hostnames and IPv6 literals', () => {
      process.env.NO_PROXY = 'müller.example,[0:0:0:0:0:0:0:1]:8080';
      const agent = new ProxyAgent();
      expect(agent.getAgentForUrl(new URL('http://xn--mller-kva.example'))).toBeUndefined();
      expect(agent.getAgentForUrl(new URL('http://[::1]:8080'))).toBeUndefined();
      expect(agent.getAgentForUrl(new URL('http://[::1]:8081'))).toBe(agent.httpProxyAgent);
    });

    it('keeps the native environment object when restoring proxy keys', () => {
      const original = process.env;
      const restore = clearProxyEnvironment();
      process.env.HTTP_PROXY = 'http://temporary.test';
      restore();
      expect(process.env).toBe(original);
      expect(process.env.HTTP_PROXY).toBe(PROXY_URL);
    });

    it('prefers nonempty lowercase no_proxy and evaluates each destination separately', () => {
      process.env.no_proxy = 'localhost';
      process.env.NO_PROXY = '*';
      const agent = new ProxyAgent();
      expect(agent.getAgentForUrl(new URL('http://localhost'))).toBeUndefined();
      expect(agent.getAgentForUrl(new URL('http://remote.test'))).toBe(agent.httpProxyAgent);
      expect(agent.getAgentForUrl(new URL('https://remote.test'))).toBe(agent.proxyAgent);
      expect(agent.getAgentForUrl(new URL('ws://remote.test'))).toBe(agent.httpProxyAgent);
      expect(agent.getAgentForUrl(new URL('wss://remote.test'))).toBe(agent.proxyAgent);
    });

    it('applies environment exclusions to explicit proxies too', () => {
      process.env.NO_PROXY = '*';
      const agent = new ProxyAgent({ url: PROXY_URL });
      expect(agent.getAgentForUrl(new URL('http://localhost'))).toBeUndefined();
      expect(agent.getAgentForUrl(new URL('https://localhost'))).toBeUndefined();
    });

    it('falls back to uppercase NO_PROXY when lowercase no_proxy is empty', () => {
      process.env.no_proxy = '';
      process.env.NO_PROXY = 'localhost';
      expect(new ProxyAgent().getAgentForUrl(new URL('http://localhost'))).toBeUndefined();
    });

    it('ignores all proxy environment settings when requested', () => {
      process.env.NO_PROXY = '*';
      const agent = new ProxyAgent({ ignoreProxyFromEnv: true });
      expect(agent.getAgentForUrl(new URL('http://localhost'))).toBeUndefined();
      const explicit = new ProxyAgent({ ignoreProxyFromEnv: true, url: PROXY_URL });
      expect(explicit.getAgentForUrl(new URL('http://localhost'))).toBe(explicit.httpProxyAgent);
    });
  });

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
