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

import { RequestOptions } from '../../options/request.options';
import { FetchRequest, RequestSender, RequestSenderFactory } from './request-sender';
import { AuthProvider, DittoHeaders, DittoURL, authenticateWithUrl } from '../../auth/auth-provider';
import { GenericResponse } from '../../model/response';

/**
 * Handle to send HTTP requests.
 */
export class HttpRequestSender extends RequestSender {

  public constructor(private readonly requester: HttpRequester,
                     private readonly baseUrl: DittoURL,
                     private readonly authenticationProviders: AuthProvider[]) {
    super();
  }

  public fetchRequest(options: FetchRequest): Promise<GenericResponse> {
    return this.requester.doRequest(options.verb, this.buildUrl(options.id, options.path, options.requestOptions),
      this.buildHeader(options.requestOptions), JSON.stringify(options.payload))
      .then(response => {
        if (response.status >= 200 && response.status < 300) {
          return response;
        }
        return Promise.reject(response.body);
      });
  }

  /**
   * Builds a URL to make HTTP calls with.
   *
   * @param id - The id of the basic entity the request is for.
   * @param path - The path to the entity the request is about from the basic entity.
   * @param options - The options provided in the request.
   * @returns The request URL
   */
  private buildUrl(id: string | undefined, path: string | undefined, options: RequestOptions | undefined): string {
    let urlSuffix = id === undefined ? '' : `/${id}`;
    urlSuffix = path === undefined ? urlSuffix : `${urlSuffix}/${path}`;
    if (options !== undefined && options.getOptions().size > 0) {
      const values = options.getOptions();
      let result = '';
      values.forEach((v, k) => {
        result += `&${k}=${v}`;
      });
      urlSuffix = `${urlSuffix}?${result.substr(1)}`;
    }
    const baseUrlWithSuffix = this.baseUrl.withPath(`${this.baseUrl.path}${urlSuffix}`);
    const authenticatedBaseUrl = authenticateWithUrl(baseUrlWithSuffix, this.authenticationProviders);
    return authenticatedBaseUrl.toString();
  }

  /**
   * Builds headers to make HTTP calls with by combining authentication and options headers.
   * Options headers will override authentication headers.
   *
   * @param options - The options to provided in the request.
   * @returns The combined headers
   */
  private buildHeader(options: RequestOptions | undefined): Map<string, string> {
    const headers: DittoHeaders = new Map();
    if (options) {
      options.getHeaders().forEach((v, k) => headers.set(k, v));
    }
    if (!headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }
    let authenticatedHeaders = headers;
    for (const authenticationProvider of this.authenticationProviders) {
      authenticatedHeaders = authenticationProvider.authenticateWithHeaders(authenticatedHeaders);
    }
    return authenticatedHeaders;
  }
}

/**
 * Requester to execute HTTP requests.
 */
export interface HttpRequester {

  /**
   * Executes a HTTP request and returns the response.
   *
   * @param method - The method to use for the request (eg. GET).
   * @param url - The request URL.
   * @param header - The header to use for the request.
   * @param body - The request body.
   * @returns The response
   */
  doRequest(method: string, url: string, header: Map<string, string>, body: string): Promise<GenericResponse>;
}


/**
 * A Factory for a HttpRequestSender.
 */
export class HttpRequestSenderBuilder implements RequestSenderFactory {

  public constructor(private readonly requester: HttpRequester,
                     private readonly url: DittoURL,
                     private readonly authProviders: AuthProvider[]) {
  }

  public buildInstance(group: string): HttpRequestSender {
    return new HttpRequestSender(this.requester,
      this.url.withPath(`${this.url.path}/${group}`),
      this.authProviders);
  }
}
