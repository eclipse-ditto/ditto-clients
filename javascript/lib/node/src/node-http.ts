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

import * as https from 'https';
import { HttpRequester, GenericResponse } from '@eclipse-ditto/ditto-javascript-client-api_0';
import { ProxyAgent } from './proxy-settings';

/**
 * NodeJs implementation of a Http Requester.
 */
export class NodeRequester implements HttpRequester {
  private readonly proxyAgent;

  public constructor(agent: ProxyAgent) {
    this.proxyAgent = agent.proxyAgent;
  }

  public async doRequest(method: string, url: string, header: Map<string, string>, payload: string): Promise<GenericResponse> {
    return new Promise<GenericResponse>(((resolve, reject) => {
      const req = https.request(this.buildRequest(method, url, header, payload), res => {
        let data = '';
        res.on('data', chunk => {
          data += chunk;
        });
        res.on('end', () => {
          let body: object;
          if (data === '') {
            body = {};
          } else {
            body = JSON.parse(data);
          }
          const status = res.statusCode;
          if (status < 200 || status >= 300) {
            reject(body);
          }
          const headersObj = res.headers;
          const headers = Object.keys(headersObj).reduce((map, name) => {
            map[name] = headersObj[name];
            return map;
          }, new Map<string, string>());
          resolve({ status, headers, body });
        });
      });
      req.on('error', e => {
        throw new Error(String(e));
      });
      if (payload !== undefined) {
        req.write(payload);
      }
      req.end();
    }));
  }

  /**
   * Builds an options object to perform a Http request with.
   *
   * @param method - The type of action to perform.
   * @param url - The Url to send the request to.
   * @param header - The headers of the request.
   * @param body - The payload to send with the request.
   * @return the builder.
   */
  private buildRequest(method: string, url: string, header: Map<string, string>, body: string): object {
    if (body !== undefined && body !== '') {
      header.set('Content-Length', Buffer.byteLength(body).toString());
    }
    const headers = new Object();
    header.forEach((v, k) => headers[k] = v);
    const parsedUrl = new URL(url);
    const request = {
      method,
      headers,
      host: parsedUrl.host,
      path: parsedUrl.pathname
    };
    if (this.proxyAgent !== undefined) {
      request['agent'] = this.proxyAgent;
    }
    return request;
  }
}
