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

const Url = require('url');
const HttpsProxyAgent = require('https-proxy-agent');

/**
 * Provider of an Agent that establishes a proxy connection.
 */
export class ProxyAgent {
  /** The Agent that provides the proxy connection. */
  public readonly proxyAgent;

  public constructor(options: ProxyOptions) {
    const environmentProxy = process.env.https_proxy || process.env.HTTPS_PROXY;
    let proxyOptions = environmentProxy ? Url.parse(environmentProxy) : {};
    if (options !== undefined) {
      if (options.url) {
        proxyOptions = Url.parse(options.url);
      }
      if (options.username && options.password) {
        const credentials = `${options.username}:${options.password}`;
        proxyOptions = Object.assign(proxyOptions,
          { headers: { 'Proxy-Authorization': `Basic ${Buffer.from(credentials).toString('base64')}` } });
      }
    }
    this.proxyAgent = new HttpsProxyAgent(proxyOptions);
  }
}

/**
 * Options for establishing a proxy connection.
 */
export interface ProxyOptions {
  /** The url and port of the proxy server to connect to. It needs to be set like this: UTL:PORT */
  url?: string;
  /** The username to authenticate to the proxy server with. */
  username?: string;
  /** The password to authenticate to the proxy server with. */
  password?: string;
}
