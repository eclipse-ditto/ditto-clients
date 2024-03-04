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

import { ContentType } from '../../../src/client/constants/content-type';
import { Header } from '../../../src/client/constants/header';
import { HttpVerb } from '../../../src/client/constants/http-verb';
import { SearchHandle } from '../../../src/client/handles/search';
import { SearchThingsResponse } from '../../../src/model/response';
import { DefaultSearchOptions } from '../../../src/options/request.options';
import { HttpHelper as H } from './http.helper';

describe('Http Search Handle', () => {
  const baseRequest = 'search/things';
  const handle: SearchHandle = H.thingsClient.getSearchHandle();
  const errorHandle: SearchHandle = H.errorThingsClient.getSearchHandle();

  const searchOptions = DefaultSearchOptions.getInstance()
    .withLimit(0, 25)
    .withFilter(
      'and(like(definition,"*test*"),and(or(ilike(/attributes/test,"*test*"),eq(/definition,"test"))))'
    )
    .withFields("thingId", "definition", "attributes", "features");

  const expectedPayload =
    "option=limit(0%2C25)&filter=and(like(definition%2C%22*test*%22)%2Cand(or(ilike(%2Fattributes%2Ftest%2C%22*test*%22)%2Ceq(%2Fdefinition%2C%22test%22))))&fields=thingId%2Cdefinition%2Cattributes%2Cfeatures";


  it('gets the Thing by post with search options', () => {
    const response = new SearchThingsResponse([H.thing], -1);
    return H.test({
      toTest: () => handle.postSearch(searchOptions),
      testBody: response.toObject(),
      expected: response,
      payload: expectedPayload,
      request: baseRequest,
      method: HttpVerb.POST,
      status: 200,
      requestHeaders: new Map<string, string>([
        [Header.CONTENT_TYPE, ContentType.FORM_URLENCODED],
      ]),
    });
  });

  it("gets the Thing by post", () => {
    const response = new SearchThingsResponse([H.thing], -1);
    return H.test({
      toTest: () => handle.postSearch(),
      testBody: response.toObject(),
      expected: response,
      request: baseRequest,
      method: HttpVerb.POST,
      status: 200,
      requestHeaders: new Map<string, string>([
        [Header.CONTENT_TYPE, ContentType.FORM_URLENCODED],
      ]),
    });
  });

  it("gets the Thing", () => {
    const response = new SearchThingsResponse([H.thing], -1);
    return H.test({
      toTest: () => handle.search(),
      testBody: response.toObject(),
      expected: response,
      request: baseRequest,
      method: HttpVerb.GET,
      status: 200
    });
  });

  it('counts Things', () => {
    return H.test({
      toTest: () => handle.count(),
      testBody: 4,
      expected: 4,
      request: `${baseRequest}/count`,
      method: HttpVerb.GET,
      status: 200
    });
  });

  it("counts Things by post with search options", () => {
    return H.test({
      toTest: () => handle.postCount(searchOptions),
      testBody: 4,
      expected: 4,
      request: `${baseRequest}/count`,
      method: HttpVerb.POST,
      payload: expectedPayload,
      status: 200,
      requestHeaders: new Map<string, string>([
        [Header.CONTENT_TYPE, ContentType.FORM_URLENCODED],
      ]),
    });
  });

  it("counts Things by post", () => {
    return H.test({
      toTest: () => handle.postCount(),
      testBody: 4,
      expected: 4,
      request: `${baseRequest}/count`,
      method: HttpVerb.POST,
      status: 200,
      requestHeaders: new Map<string, string>([
        [Header.CONTENT_TYPE, ContentType.FORM_URLENCODED],
      ]),
    });
  });

  it("counts Things by post with text/plain Content-Type", () => {
    const contentTypeHeader = DefaultSearchOptions.getInstance().addHeader(
      Header.CONTENT_TYPE,
      ContentType.TEXT
    );
    return H.test({
      toTest: () => handle.postCount(contentTypeHeader),
      testBody: 4,
      expected: 4,
      request: `${baseRequest}/count`,
      method: HttpVerb.POST,
      status: 200,
      payload: '',
      requestHeaders: new Map<string, string>([
        [Header.CONTENT_TYPE, ContentType.TEXT],
      ]),
    });
  });

  it('returns a search error message', () => {
    return H.testError(() => errorHandle.search());
  });

  it('returns a count error message', () => {
    return H.testError(() => errorHandle.count());
  });
});
