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

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const { UrlWithStringQuery, parse } = require('url');
import { isIP } from 'net';
const HttpsProxyAgent = require('https-proxy-agent');
const HttpProxyAgent = require('http-proxy-agent');

class ProxyAgentOptionsBuilder {

  private proxyUrl: typeof UrlWithStringQuery = {};
  private proxyCredentials: any = {};
  private constructor() {
    /* intentionally empty */
  }

  static newInstance(options: ProxyOptions | undefined, environmentProxy: string | undefined): ProxyAgentOptionsBuilder {
    return new ProxyAgentOptionsBuilder()
      .parseUrlFromEnvironment(environmentProxy, options)
      .parseUrlFromOptions(options)
      .parseCredentialsFromOptions(options);
  }

  parseUrlFromOptions(options: ProxyOptions | undefined): ProxyAgentOptionsBuilder {
    if (options !== undefined && options.url !== undefined) {
      this.proxyUrl = parse(options.url);
    }
    return this;
  }

  parseUrlFromEnvironment(environmentProxy: string | undefined, options: ProxyOptions | undefined): ProxyAgentOptionsBuilder {
    // tslint:disable-next-line:strict-boolean-expressions
    const shouldIgnoreProxyFromEnv = options !== undefined && options.ignoreProxyFromEnv;
    // tslint:disable-next-line:strict-boolean-expressions
    if (environmentProxy !== undefined && !shouldIgnoreProxyFromEnv) {
      this.proxyUrl =  parse(environmentProxy);
    }
    return this;
  }

  parseCredentialsFromOptions(options: ProxyOptions | undefined): ProxyAgentOptionsBuilder {
    if (options !== undefined && options.username !== undefined && options.password !== undefined) {
      const credentials = `${options.username}:${options.password}`;
      this.proxyCredentials = { headers: { 'Proxy-Authorization': `Basic ${Buffer.from(credentials).toString('base64')}` } };
    }
    return this;
  }

  isEmpty(): boolean {
    // can ignore proxyCredentials here, as we can't send credentials if we don't know a proxy location
    return 0 === Object.keys(this.proxyUrl).length;
  }

  getOptions(): any {
    return {
      ...this.proxyUrl,
      ...this.proxyCredentials
    };
  }
}

function buildHttpsProxyAgent(options: ProxyOptions | undefined): typeof HttpsProxyAgent | undefined {
  /* tslint:disable-next-line:strict-boolean-expressions */
  const environmentProxy = process.env.https_proxy || process.env.HTTPS_PROXY;
  const proxyOptions = ProxyAgentOptionsBuilder.newInstance(options, environmentProxy);
  return proxyOptions.isEmpty() ? undefined : new HttpsProxyAgent(proxyOptions.getOptions());
}

function buildHttpProxyAgent(options: ProxyOptions | undefined): typeof HttpProxyAgent | undefined {
  /* tslint:disable-next-line:strict-boolean-expressions */
  const environmentProxy = process.env.http_proxy || process.env.HTTP_PROXY;
  const proxyOptions = ProxyAgentOptionsBuilder.newInstance(options, environmentProxy);
  return proxyOptions.isEmpty() ? undefined : new HttpProxyAgent(proxyOptions.getOptions());
}

interface ProxyExclusion {
  host: string;
  subdomainsOnly: boolean;
  ip: boolean;
  port?: string;
}

/** Parses once per client; unsupported entries must not silently become literal hostnames. */
function parseExclusions(value: string): ProxyExclusion[] {
  const exclusions: ProxyExclusion[] = [];
  let unsupported = false;
  for (const entry of value.toLowerCase().split(/[\s,]+/).filter(Boolean)) {
    // A bare IPv6 address has no port. Brackets are required when specifying one.
    const normalizedEntry = isIP(entry) === 6 ? `[${entry}]` : entry;
    const match = /^(\[[^\]]+\]|[^:]+)(?::(\d+))?$/.exec(normalizedEntry);
    if (match === null) {
      unsupported = true;
      continue;
    }
    let host = match[1].replace(/^\*\./, '.').replace(/\.$/, '');
    const subdomainsOnly = host.startsWith('.');
    if (subdomainsOnly) {
      host = host.slice(1);
    }
    if (!host || /[/@?#]/.test(host) || (host !== '*' && host.includes('*'))
      || (match[2] !== undefined && (Number(match[2]) < 1 || Number(match[2]) > 65535))) {
      unsupported = true;
      continue;
    }
    try {
      // Use the same canonical spelling for IDNs and IPv6 as the destination URL.
      host = host === '*' ? host : new URL(`http://${host}`).hostname;
      const ip = isIP(host.replace(/^\[|\]$/g, '')) !== 0;
      if (subdomainsOnly && (ip || host === '*')) {
        unsupported = true;
        continue;
      }
      exclusions.push({ host, subdomainsOnly, ip, port: match[2] });
    } catch {
      unsupported = true;
    }
  }
  if (unsupported) {
    console.warn('Ignoring unsupported NO_PROXY entries; CIDR ranges, URLs and arbitrary wildcards are not supported.');
  }
  return exclusions;
}

/**
 * Provider of an Agent that establishes a proxy connection.
 */
export class ProxyAgent {
  /** The Agent that provides the proxy connection. */
  public readonly proxyAgent?: typeof HttpsProxyAgent;
  public readonly httpProxyAgent?: typeof HttpProxyAgent.HttpProxyAgent;
  private readonly exclusions: ProxyExclusion[];

  public constructor(options?: ProxyOptions | undefined) {
    this.httpProxyAgent = buildHttpProxyAgent(options);
    this.proxyAgent = buildHttpsProxyAgent(options);
    this.exclusions = parseExclusions(options?.ignoreProxyFromEnv
      ? '' : process.env.no_proxy || process.env.NO_PROXY || '');
  }

  /** Selects a proxy per destination, respecting exclusions unless environment settings are disabled. */
  public getAgentForUrl(url: URL): typeof HttpsProxyAgent | undefined {
    if (this.isExcluded(url)) {
      return undefined;
    }
    return url.protocol === 'https:' || url.protocol === 'wss:' ? this.proxyAgent : this.httpProxyAgent;
  }

  private isExcluded(url: URL): boolean {
    const hostname = url.hostname.toLowerCase().replace(/\.$/, '');
    const port = url.port || (url.protocol === 'https:' || url.protocol === 'wss:' ? '443' : '80');
    return this.exclusions.some(entry => {
      if (entry.port !== undefined && Number(entry.port) !== Number(port)) {
        return false;
      }
      return entry.host === '*' || (!entry.subdomainsOnly && hostname === entry.host)
        || (!entry.ip && hostname.endsWith(`.${entry.host}`));
    });
  }
}

/**
 * Options for establishing a proxy connection.
 */
export interface ProxyOptions {
  /** The url and port of the proxy server to connect to. It needs to be set like this: URL:PORT */
  url?: string;
  /** The username to authenticate to the proxy server with. */
  username?: string;
  /** The password to authenticate to the proxy server with. */
  password?: string;
  /** If proxy environment variables, including NO_PROXY and no_proxy, should be ignored. */
  ignoreProxyFromEnv?: boolean;
}
