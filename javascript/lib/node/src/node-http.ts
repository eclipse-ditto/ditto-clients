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

import * as http from 'http';
import * as https from 'https';
import { HttpRequester } from '../../api/src/client/request-factory/http-request-sender';
import { BasicErrorResponse, ErrorResponse, GenericResponse } from '../../api/src/model/response';
import { ProxyAgent } from './proxy-settings';

/**
 * NodeJs implementation of a Http Requester.
 */
export class NodeRequester implements HttpRequester {

  public constructor(private readonly agent: ProxyAgent) {
  }

  private static createResponseHandler(resolve: (value?: (PromiseLike<GenericResponse> | GenericResponse)) => void,
                                       reject: (reason?: ErrorResponse) => void):
    (response: http.IncomingMessage) => void {
    return response => {
      this.handleResponse(resolve, reject, response);
    };
  }

  private static handleResponse(resolve: (value?: (PromiseLike<GenericResponse> | GenericResponse)) => void,
                                reject: (reason?: ErrorResponse) => void,
                                response: http.IncomingMessage): void {
    let data = '';
    response.on('data', chunk => {
      data += chunk;
    });
    response.on('end', () => {
      let body: any;
      if (data === '') {
        body = {};
      } else {
        try {
          body = JSON.parse(data);
        } catch (e) {
          body = data;
        }
      }

      const headersObj = response.headers;
      const headers = Object.keys(headersObj).reduce((map, name) => {
        map.set(name, headersObj[name] as string);
        return map;
      }, new Map<string, string>());

      const status: number = response.statusCode !== undefined ? response.statusCode : 0;
      if (status < 200 || status >= 300) {
        reject(new BasicErrorResponse(body, status, headers));
      }
      resolve({ status, headers, body });
    });
  }

  public async doRequest(method: string, url: string, header: Map<string, string>, payload: string): Promise<GenericResponse> {
    return new Promise<GenericResponse>(((resolve, reject) => {
      const parsedUrl = new URL(url);
      const isSecureRequest = parsedUrl.protocol === 'https:';
      const client = isSecureRequest ? https : http;
      const requestOptions = this.buildRequest(method, parsedUrl, header, payload, isSecureRequest);
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
   * @param isSecureRequest - If the request is a secure HTTPS request.
   * @return the builder.
   */
  private buildRequest(method: string, parsedUrl: URL, header: Map<string, string>, body: string,
                       isSecureRequest: boolean): object {
    if (body !== undefined && body !== '') {
      header.set('Content-Length', Buffer.byteLength(body).toString());
    }
    const pathWithQueryParams = `${parsedUrl.pathname}${parsedUrl.search}`;
    const headers: { [key: string]: any } = {};
    header.forEach((v, k) => headers[k] = v);
    return {
      method,
      headers,
      hostname: parsedUrl.hostname,
      port: parsedUrl.port,
      path: pathWithQueryParams,
      agent: this.getAgentForRequestType(isSecureRequest)
    };
  }

  private getAgentForRequestType(isSecureRequest: boolean): http.Agent | undefined {
    return isSecureRequest ? this.agent.proxyAgent : this.agent.httpProxyAgent;
  }

}
