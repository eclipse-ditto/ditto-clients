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

import { GenericResponse } from '../../api/src/model/response';
import { HttpRequester } from '../../api/src/client/request-factory/http-request-sender';
import * as http from 'http';
import * as https from 'https';
import { ProxyAgent } from './proxy-settings';

/**
 * NodeJs implementation of a Http Requester.
 */
export class NodeRequester implements HttpRequester {
  private readonly proxyAgent: any;

  public constructor(agent: ProxyAgent) {
    this.proxyAgent = agent.proxyAgent;
  }

  private static createResponseHandler(resolve: (value?: (PromiseLike<GenericResponse> | GenericResponse)) => void,
                                       reject: (reason?: any) => void):
    (response: http.IncomingMessage) => void {
    return response => {
      this.handleResponse(resolve, reject, response);
    };
  }

  private static handleResponse(resolve: (value?: (PromiseLike<GenericResponse> | GenericResponse)) => void,
                                reject: (reason?: any) => void,
                                response: http.IncomingMessage): void {
    let data = '';
    response.on('data', chunk => {
      data += chunk;
    });
    response.on('end', () => {
      let body: object;
      if (data === '') {
        body = {};
      } else {
        body = JSON.parse(data);
      }
      const status: number = response.statusCode !== undefined ? response.statusCode : 0;
      if (status < 200 || status >= 300) {
        reject(body);
      }
      const headersObj = response.headers;
      const headers = Object.keys(headersObj).reduce((map, name) => {
        map.set(name, headersObj[name] as string);
        return map;
      }, new Map<string, string>());
      resolve({ status, headers, body });
    });
  }

  public async doRequest(method: string, url: string, header: Map<string, string>, payload: string): Promise<GenericResponse> {
    return new Promise<GenericResponse>(((resolve, reject) => {
      const parsedUrl = new URL(url);

      const client = (parsedUrl.protocol === 'http:') ? http : https;
      const requestOptions = this.buildRequest(method, parsedUrl, header, payload);
      const req = client.request(requestOptions, NodeRequester.createResponseHandler(resolve, reject));
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
   * @param parsedUrl - The Url to send the request to.
   * @param header - The headers of the request.
   * @param body - The payload to send with the request.
   * @return the builder.
   */
  private buildRequest(method: string, parsedUrl: URL, header: Map<string, string>, body: string): object {
    if (body !== undefined && body !== '') {
      header.set('Content-Length', Buffer.byteLength(body).toString());
    }
    const headers: { [key: string]: any } = {};
    header.forEach((v, k) => headers[k] = v);
    const request: { [key: string]: any } = {
      method,
      headers,
      hostname: parsedUrl.hostname,
      port: parsedUrl.port,
      path: parsedUrl.pathname
    };
    if (this.proxyAgent !== undefined && this.proxyAgent.options.path !== undefined) {
      request['agent'] = this.proxyAgent;
    }
    return request;
  }
}
