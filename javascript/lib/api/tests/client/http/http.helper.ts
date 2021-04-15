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

import { TestRequester } from './http-requester.mock';
import { Helper } from '../test.helper';
import { HttpBuilderInitialStep, HttpClientBuilder } from '../../../src/client/http-client-builder';
import { DittoHttpClient } from '../../../src/client/ditto-client-http';
import { GenericResponse } from '../../../src/model/response';

export class HttpHelper extends Helper {
  private static readonly domain = 'test.web';
  private static readonly matcher = `http://${HttpHelper.domain}/api/`;
  private static readonly errorDomain = 'error.web';
  private static readonly errorUrl = `http://${HttpHelper.errorDomain}`;
  private static readonly errorResponse: GenericResponse = {
    status: 403,
    headers: new Map<string, string>(),
    body: HttpHelper.errorBody
  };
  // TODO: having this statically is causing problems between tests. it should really be not static
  private static readonly requester: TestRequester = new TestRequester(HttpHelper.errorUrl, HttpHelper.errorResponse);

  public static readonly thingsClient: DittoHttpClient = HttpHelper.buildHttpClient(HttpHelper.requester)
    .withoutTls()
    .withDomain(HttpHelper.domain)
    .withAuthProvider(HttpHelper.basicAuthProvider(HttpHelper.testName, HttpHelper.password))
    .build();

  public static readonly errorThingsClient: DittoHttpClient = HttpHelper.buildHttpClient(HttpHelper.requester)
    .withoutTls()
    .withDomain(HttpHelper.errorDomain)
    .withAuthProvider(HttpHelper.basicAuthProvider(HttpHelper.testName, HttpHelper.password))
    .build();

  public static test<T>(options: HttpTestOptions<T>): Promise<any> {
    const api = options.api === undefined ? 2 : options.api;
    const requestResponse: GenericResponse = {
      status: options.status,
      headers: options.responseHeaders,
      body: options.testBody
    };
    this.requester.addResponse(options.method,
      `${HttpHelper.matcher}${api}/${options.request}`, options.requestHeaders, options.payload, requestResponse);
    if (options.expected !== undefined) {
      return options.toTest()
        .then(response => {
          expect(response).toEqual(options.expected);
        })
        .catch(reason => {
          fail(`${options.method} ${options.request}: ${reason}`);
        });
    }
    return options.toTest()
      .catch(reason => {
        fail(`${options.method} ${options.request}: ${reason}`);
      });

  }

  private static buildHttpClient(requester: TestRequester): HttpBuilderInitialStep {
    return HttpClientBuilder.newBuilder(requester);
  }
}

export interface HttpTestOptions<T> {
  toTest: () => Promise<T>;
  request: string;
  method: string;
  status: number;
  testBody?: any;
  api?: number;
  expected?: T;
  payload?: string;
  requestHeaders?: Map<string, string>;
  responseHeaders?: Map<string, string>;
}
