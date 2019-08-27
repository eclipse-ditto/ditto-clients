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

import { HttpRequester, GenericResponse } from '@eclipse-ditto/ditto-javascript-client-api_0';

export class FetchRequester implements HttpRequester {

  public doRequest(method: string, url: string, header: Map<string, string>, body: string): Promise<GenericResponse> {
    return fetch(this.prepareRequest(method, url, header, body))
      .then(response => {
        const headers = new Map<string, string>();
        response.headers.forEach((v, k) => headers[k] = v);
        return response.json()
          .then(
            json => ({ headers, status: response.status, body: json }),
            () => ({ headers, status: response.status, body: undefined })
          );
      });
  }

  /**
   * Builds a Request object to perform a fetch request with.
   *
   * @param method - The type of action to perform.
   * @param url - The Url to send the request to.
   * @param header - The headers of the request.
   * @param body - The payload to send with the request.
   * @return the builder.
   */
  private prepareRequest(method: string, url: string, header: Map<string, string>, body: string): Request {
    const headers = new Headers();
    header.forEach((v, k) => headers.append(k, v));

    return new Request(url, {
      headers,
      method,
      body,
      mode: 'cors'
    });
  }
}
