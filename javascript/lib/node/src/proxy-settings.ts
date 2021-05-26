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

const { UrlWithStringQuery, parse } = require('url');
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

/**
 * Provider of an Agent that establishes a proxy connection.
 */
export class ProxyAgent {
  /** The Agent that provides the proxy connection. */
  public readonly proxyAgent?: typeof HttpsProxyAgent;
  public readonly httpProxyAgent?: typeof HttpProxyAgent.HttpProxyAgent;

  public constructor(options?: ProxyOptions | undefined) {
    this.httpProxyAgent = buildHttpProxyAgent(options);
    this.proxyAgent = buildHttpsProxyAgent(options);
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
  /** If proxy environment variables HTTPS_PROXY, https_proxy, HTTP_PROXY and http_proxy should be ignored. */
  ignoreProxyFromEnv?: boolean;
}
