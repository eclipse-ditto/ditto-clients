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

import { SearchThingsResponse } from '../../model/response';
import { CountOptions, SearchOptions } from '../../options/request.options';
import { RequestSender, RequestSenderFactory } from '../request-factory/request-sender';

export interface SearchHandle {
  /**
   * Searches for Things that match the restrictions set in options.
   *
   * @param options - Options to use for the search.
   * @returns The search result
   */
  search(options?: SearchOptions): Promise<SearchThingsResponse>;

  /**
   * Counts the Things that match the restrictions set in options.
   *
   * @param options - Options to use for the search.
   * @returns The count
   */
  count(options?: CountOptions): Promise<number>;
}

/**
 * Handle to send Search requests.
 */
export class DefaultSearchHandle implements SearchHandle {

  private constructor(readonly requestFactory: RequestSender) {
  }

  /**
   * returns an instance of SearchHandle using the provided RequestSender.
   *
   * @param builder - The builder for the RequestSender to work with.
   * @returns The SearchHandle
   */
  public static getInstance(builder: RequestSenderFactory): DefaultSearchHandle {
    return new DefaultSearchHandle(builder.buildInstance('search/things'));
  }

  /**
   * Searches for Things that match the restrictions set in options.
   *
   * @param options - Options to use for the search.
   * @returns The search result
   */
  search(options?: SearchOptions): Promise<SearchThingsResponse> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: SearchThingsResponse.fromObject,
      requestOptions: options
    });
  }

  /**
   * Counts the Things that match the restrictions set in options.
   *
   * @param options - Options to use for the search.
   * @returns The count
   */
  count(options?: CountOptions): Promise<number> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: Number,
      path: 'count',
      requestOptions: options
    });
  }

}
