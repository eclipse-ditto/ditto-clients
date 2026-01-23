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
import { CountOptions, DefaultSearchOptions, RequestOptions, SearchOptions } from '../../options/request.options';
import { ContentType } from '../constants/content-type';
import { Header } from '../constants/header';
import { HttpVerb } from '../constants/http-verb';
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
   * Searches for Things that match the restrictions set in options using a http post request.
   *
   * @param options - Options to use for the search.
   * @returns The search result
   */
    postSearch(options?: SearchOptions): Promise<SearchThingsResponse>;

    /**
   * Counts the Things that match the restrictions set in options.
   *
   * @param options - Options to use for the search.
   * @returns The count
   */
    count(options?: CountOptions): Promise<number>;

    /**
   * Counts the Things that match the restrictions set in options using a http post request.
   *
   * @param options - Options to use for the search.
   * @returns The count
   */
    postCount(options?: CountOptions): Promise<number>;
}

/**
 * Handle to send Search requests.
 */
export class DefaultSearchHandle implements SearchHandle {
    private constructor(readonly requestFactory: RequestSender) {}

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
            verb: HttpVerb.GET,
            parser: SearchThingsResponse.fromObject,
            requestOptions: options
        });
    }

    /**
   * Searches for Things that match the restrictions set in options using a http post request.
   *
   * @param options - Options to use for the search.
   * @returns The search result
   */
    postSearch(options?: SearchOptions): Promise<SearchThingsResponse> {
        return this.requestFactory.fetchFormRequest({
            verb: HttpVerb.POST,
            parser: SearchThingsResponse.fromObject,
            payload: options?.getOptions(),
            requestOptions: this.getFormRequestOptions(options)
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
            verb: HttpVerb.GET,
            parser: Number,
            path: 'count',
            requestOptions: options
        });
    }

    /**
   * Counts the Things that match the restrictions set in options using a http post request.
   *
   * @param options - Options to use for the search.
   * @returns The count
   */
    postCount(options?: CountOptions): Promise<number> {
        return this.requestFactory.fetchFormRequest({
            verb: HttpVerb.POST,
            parser: Number,
            path: 'count',
            payload: options?.getOptions(),
            requestOptions: this.getFormRequestOptions(options)
        });
    }

    private getFormRequestOptions(options?: SearchOptions | CountOptions): RequestOptions | undefined {
        const requestOptions = DefaultSearchOptions.getInstance();
        options?.getHeaders().forEach((value, key) => {
            requestOptions.addHeader(key, value);
        });
        if (!requestOptions?.getHeaders().has(Header.CONTENT_TYPE)) {
            requestOptions?.addHeader(Header.CONTENT_TYPE, ContentType.FORM_URLENCODED);
        }
        return requestOptions;
    }
}
