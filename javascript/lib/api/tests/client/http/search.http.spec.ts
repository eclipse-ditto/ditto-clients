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

import { SearchHandle } from '../../../src/client/handles/search';
import { SearchThingsResponse } from '../../../src/model/response';
import { HttpHelper as H } from './http.helper';

describe('Http Search Handle', () => {
  const baseRequest = 'search/things';
  const handle: SearchHandle = H.thingsClient.getSearchHandle();
  const errorHandle: SearchHandle = H.errorThingsClient.getSearchHandle();

  it('gets the Thing', () => {
    const response = new SearchThingsResponse([H.thing], -1);
    return H.test({
      toTest: () => handle.search(),
      testBody: response.toObject(),
      expected: response,
      request: baseRequest,
      method: 'get',
      status: 200
    });
  });

  it('counts Things', () => {
    return H.test({
      toTest: () => handle.count(),
      testBody: 4,
      expected: 4,
      request: `${baseRequest}/count`,
      method: 'get',
      status: 200
    });
  });

  it('returns a search error message', () => {
    return H.testError(() => errorHandle.search());
  });

  it('returns a count error message', () => {
    return H.testError(() => errorHandle.count());
  });
});
