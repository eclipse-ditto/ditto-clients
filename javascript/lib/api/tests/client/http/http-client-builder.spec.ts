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

import { HttpClientBuilder } from '../../../src/client/http-client-builder';
import { TestRequester } from './http-requester.mock';
import { AuthProvider, DittoHeaders, DittoURL, ImmutableURL } from '../../../src/auth/auth-provider';
import { DefaultSearchHandle, SearchHandle } from '../../../src/client/handles/search';
import { CustomBuilderContext } from '../../../src/client/builder-steps';
import { HttpRequestSenderBuilder } from '../../../src/client/request-factory/http-request-sender';
import { DefaultDittoHttpClient } from '../../../src/client/ditto-client-http';
import { GenericResponse } from '../../../src/model/response';
import { jest } from '@jest/globals';

class DummyAuthProvider implements AuthProvider {

  constructor(private readonly username: string, private readonly password: string) {

  }

  authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders {
    return originalHeaders;
  }

  authenticateWithUrl(originalUrl: DittoURL): DittoURL {
    return originalUrl;
  }

}

const dummyAuthProvider1: AuthProvider = new DummyAuthProvider('a', 'b');
const dummyAuthProvider2: AuthProvider = new DummyAuthProvider('a', 'b');
const dummyAuthProviders: AuthProvider[] = [dummyAuthProvider1, dummyAuthProvider2];
const expectedUrl = ImmutableURL.newInstance('https', 'eclipse.ditto.org', '/api/2');
const expectedCustomUrl = ImmutableURL.newInstance('https', 'eclipse.ditto.org', '/secure-api/2');
const requester = new TestRequester('a', {} as GenericResponse);
const requestSenderFactory = new HttpRequestSenderBuilder(requester, expectedUrl, dummyAuthProviders);
const customUrlRequestSenderFactory = new HttpRequestSenderBuilder(requester, expectedCustomUrl, dummyAuthProviders);


describe('HttpClientBuilder', () => {
  it('builds a new http client', () => {
    const expectedClient = DefaultDittoHttpClient.getInstance(requestSenderFactory);
    const dittoHttpClient = HttpClientBuilder.newBuilder(requester)
      .withTls()
      .withDomain('eclipse.ditto.org')
      .withAuthProvider(dummyAuthProvider1, dummyAuthProvider2)
      .build();

    expect(dittoHttpClient).toBeTruthy();
    expect(dittoHttpClient).toEqual(expectedClient);
  });

  it('builds a new http client with custom path', () => {
    const expectedClient = DefaultDittoHttpClient.getInstance(customUrlRequestSenderFactory);
    const dittoHttpClient = HttpClientBuilder.newBuilder(requester)
      .withTls()
      .withCustomPath('/secure-api')
      .withDomain('eclipse.ditto.org')
      .withAuthProvider(dummyAuthProvider1, dummyAuthProvider2)
      .build();

    expect(dittoHttpClient).toBeTruthy();
    expect(dittoHttpClient).toEqual(expectedClient);
  });

  it('passes custom handle factories through to the client', () => {
    const dummyCustomContext = { foo: 'bar' };
    const dummySearchHandle: SearchHandle = DefaultSearchHandle.getInstance(requestSenderFactory);
    const called = jest.fn();
    const createCustomSearchHandle: ((requestSenderBuilder: HttpRequestSenderBuilder,
                                      customBuilderContext?: CustomBuilderContext) => SearchHandle) =
      (requestSenderBuilder, customBuilderContext) => {
        called(customBuilderContext);
        return dummySearchHandle;
      };

    const dittoHttpClient = HttpClientBuilder.newBuilder(requester)
      .withTls()
      .withDomain('eclipse.ditto.org')
      .withAuthProvider(dummyAuthProvider1, dummyAuthProvider2)
      .withCustomSearchHandle(createCustomSearchHandle)
      .build();

    expect(dittoHttpClient.getSearchHandle(dummyCustomContext)).toEqual(dummySearchHandle);
    expect(called).toHaveBeenCalledWith(dummyCustomContext);
  });

});
