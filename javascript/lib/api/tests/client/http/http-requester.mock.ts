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

import { HttpRequester } from '../../../src/client/request-factory/http-request-sender';
import { GenericResponse } from '../../../src/model/response';

export class TestRequester implements HttpRequester {
  private readonly requests: Map<Request, GenericResponse> = new Map();

  public constructor(private readonly errorUrl: string,
                     private readonly errorResponse: GenericResponse) {
  }

  public doRequest(method: string, url: string, header: Map<string, string>, body: string): Promise<GenericResponse> {
    return new Promise<GenericResponse>((resolve, reject) => {
      if (url.startsWith(this.errorUrl)) {
        resolve(this.errorResponse);
      }
      this.requests.forEach((response, request) => {
        if (this.matches(method, url, header, body, request)) {
          this.requests.delete(request);
          resolve(response);
        }
      });
      reject('TestRequest was not registered');
    });
  }

  private matches(method: string, url: string, header: Map<string, string>, body: string, request: Request): boolean {
    if (method.toLowerCase() === request.method && url === request.url && body === request.body) {
      let hasHeaders = true;
      if (request.header !== undefined) {
        request.header.forEach((v, k) => {
          if (header.get(k) !== v) {
            hasHeaders = false;
          }
        });
      }
      return hasHeaders;
    }
    return false;
  }

  public addResponse(method: string, url: string, header: Map<string, string>, body: string, response: GenericResponse): void {
    if (method === undefined) {
      throw Error('TestRequest-method needs to be defined!');
    }
    if (url === undefined) {
      throw Error('TestRequest-url needs to be defined!');
    }
    if (response === undefined) {
      throw Error('TestRequest-response needs to be defined!');
    }
    const request = { method, url };
    request['header'] = header === undefined ? new Map() : header;
    request['body'] = body === '' ? undefined : body;
    this.requests.set({ method, url, header, body }, response);
  }
}

interface Request {
  method: string;
  url: string;
  header: Map<string, string>;
  body: string;
}
