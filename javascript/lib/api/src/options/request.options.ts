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

import { Filter } from './filter.options';

export interface RequestOptions {
  /**
   * Provides the specified URL options.
   *
   * @returns The string to attach to the URL
   */
  getOptions(): Map<string, string>;

  /**
   * Provides the specified header options.
   *
   * @returns The headers
   */
  getHeaders(): Map<string, string>;
}

export interface AddRequestOptions<T extends AddRequestOptions<T>> extends RequestOptions {

  /**
   * Adds a URl option.
   *
   * @param id - The ID of the option to add.
   * @param value - The body of the option to add.
   */
  addRequestParameter(id: string, value: string): T;

  /**
   * Adds a header option.
   *
   * @param name - The name of the header to add.
   * @param value - The body of the header to add.
   */
  addHeader(name: string, value: string): T;
}

/**
 * Option provider for options. Options will be replaced if methods are called multiple times.
 */
export abstract class AbstractRequestOptions<T extends AbstractRequestOptions<T>> implements AddRequestOptions<T> {

  private readonly options: Map<string, string>;
  private readonly headers: Map<string, string>;

  protected constructor() {
    this.options = new Map<string, string>();
    this.headers = new Map<string, string>();
  }

  getOptions(): Map<string, string> {
    return new Map(this.options);
  }

  getHeaders(): Map<string, string> {
    return new Map(this.headers);
  }

  addRequestParameter(id: string, value: string): T {
    this.options.set(id, value);
    return this as unknown as T;
  }

  addHeader(name: string, value: string): T {
    this.headers.set(name, value);
    return this as unknown as T;
  }
}

export interface HasMatch<T extends HasMatch<T>> {
  /**
   * Sets an If-Match option.
   *
   * @param tag - The tags to match.
   * @returns This Options instance with the added option
   */
  ifMatch(...tag: string[]): T;

  /**
   * Shortcut for If-Match: *
   * @returns This Options instance with the added option
   */
  ifMatchAny(): T;

  /**
   * Sets an If-None-Match option.
   *
   * @param tag - The tags to match.
   * @returns This Options instance with the added option
   */
  ifNoneMatch(...tag: string[]): T;

  /**
   * Shortcut for If-None-Match: *
   * @returns This Options instance with the added option
   */
  ifNoneMatchAny(): T;
}

export interface HasFields<T extends HasFields<T>> {
  /**
   * Sets a fields option.
   *
   * @param fields - The fields to return.
   * @returns This Options instance with the added option
   */
  withFields(...fields: string[]): T;
}

export interface HasFilterAndNamespace<T extends HasFilterAndNamespace<T>> {
  /**
   * Sets a filter option.
   *
   * @param rawFilterString - The string to filter by.
   * @returns This Options instance with the added option
   */
  withRawFilter(rawFilterString: string): T;

  /**
   * Sets a filter option.
   *
   * @param filter - The instance of Filter to use.
   * @returns This Options instance with the added option
   */
  withFilter(filter: Filter): T;

  /**
   * Sets a namespaces option.
   *
   * @param namespaces - The namespaces to set.
   * @returns This Options instance with the added option
   */
  withNamespaces(...namespaces: string[]): T;
}

export interface RequestOptionsWithMatchOptions<T extends RequestOptionsWithMatchOptions<T>>
  extends AddRequestOptions<RequestOptionsWithMatchOptions<T>>,
    HasMatch<RequestOptionsWithMatchOptions<T>> {
}


export abstract class AbstractRequestOptionsWithMatchOptions<T extends AbstractRequestOptionsWithMatchOptions<T>>
  extends AbstractRequestOptions<AbstractRequestOptionsWithMatchOptions<T>>
  implements RequestOptionsWithMatchOptions<AbstractRequestOptionsWithMatchOptions<T>> {
  public ifMatch(...tags: string[]): T {
    this.addHeader('If-Match', tags.join(', '));
    return this as unknown as T;
  }

  public ifNoneMatch(...tags: string[]): T {
    this.addHeader('If-None-Match', tags.join(', '));
    return this as unknown as T;
  }

  ifMatchAny(): T {
    this.addHeader('If-Match', '*');
    return this as unknown as T;
  }

  ifNoneMatchAny(): T {
    this.addHeader('If-None-Match', '*');
    return this as unknown as T;
  }
}

/**
 * Option provider for If-Match / If-None-Match headers
 */
export interface MatchOptions extends RequestOptionsWithMatchOptions<MatchOptions> {
}

export class DefaultMatchOptions extends AbstractRequestOptionsWithMatchOptions<DefaultMatchOptions> implements MatchOptions {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of MatchOptions.
   *
   * @returns The MatchOptions
   */
  public static getInstance(): DefaultMatchOptions {
    return new DefaultMatchOptions();
  }
}

/**
 * Option provider for some get requests
 */
export interface FieldsOptions extends RequestOptionsWithMatchOptions<FieldsOptions>, HasFields<FieldsOptions> {
  withFields(...fields: string[]): FieldsOptions;
}

export class DefaultFieldsOptions extends AbstractRequestOptionsWithMatchOptions<DefaultFieldsOptions> implements FieldsOptions {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of FieldsOptions.
   *
   * @returns The FieldsOptions
   */
  public static getInstance(): DefaultFieldsOptions {
    return new DefaultFieldsOptions();
  }

  public withFields(...fields: string[]): DefaultFieldsOptions {
    super.addRequestParameter('fields', encodeURIComponent(fields.join()));
    return this;
  }
}

/**
 * Option provider for count requests
 */
export interface CountOptions extends AddRequestOptions<CountOptions>, HasFilterAndNamespace<CountOptions> {
}

export class DefaultCountOptions extends AbstractRequestOptions<DefaultCountOptions> implements CountOptions {

  protected constructor() {
    super();
  }

  /**
   * Provides an instance of DefaultCountOptions.
   *
   * @returns The DefaultCountOptions
   */
  public static getInstance(): DefaultCountOptions {
    return new DefaultCountOptions();
  }

  public withRawFilter(rawFilterString: string): DefaultCountOptions {
    this.addRequestParameter('filter', encodeURIComponent(rawFilterString));
    return this;
  }

  public withFilter(filter: Filter): DefaultCountOptions {
    this.addRequestParameter('filter', encodeURIComponent(filter.toString()));
    return this;
  }

  public withNamespaces(...namespaces: string[]): DefaultCountOptions {
    this.addRequestParameter('namespaces', encodeURIComponent(namespaces.join()));
    return this;
  }

}

/**
 * Option provider for search requests
 */
export interface SearchOptions extends AddRequestOptions<SearchOptions>, HasFilterAndNamespace<SearchOptions>, HasFields<SearchOptions> {
  /**
   * @param offset - The index to start at.
   * @param count - The number of things to return.
   * @returns The instance of SearchOptions with the added option
   *
   * @Deprecated See {@link https://www.eclipse.org/ditto/basic-search.html#rql-paging-deprecated} Use cursor pagination instead
   * Sets a limit option.
   */
  withLimit(offset: number, count: number): SearchOptions;

  /**
   * Sets a sort option.
   *
   * @param sortOperation - The string to sort by.
   * @returns The instance of SearchOptions with the added option
   */
  withSort(sortOperation: string): SearchOptions;

  /**
   * Sets a cursor option
   *
   * @param cursor - The cursor to use for pagination, returned by a previous search request
   */
  withCursor(cursor: string): SearchOptions;

  /**
   * Sets the page size for pagination.
   * Only works with cursor pagination.
   * Maximum page size supported by ditto is 200.
   * @param pageSize Number of items to return per page
   */
  withPageSize(pageSize: number): SearchOptions;
}


export class DefaultSearchOptions extends AbstractRequestOptions<DefaultSearchOptions>
  implements SearchOptions {

  /**
   * Map of name-value pairs to add to the request parameters as "option"
   */
  private optionParameters: Map<string, string> = new Map();

  private constructor() {
    super();

  }

  /**
   * Provides an instance of DefaultSearchOptions.
   *
   * @returns The DefaultSearchOptions
   */
  public static getInstance(): DefaultSearchOptions {
    return new DefaultSearchOptions();
  }

  public withNamespaces(...namespaces: string[]): DefaultSearchOptions {
    this.addRequestParameter('namespaces', encodeURIComponent(namespaces.join()));
    return this;
  }

  public withFields(...fields: string[]): DefaultSearchOptions {
    this.addRequestParameter('fields', encodeURIComponent(fields.join()));
    return this;
  }

  public withRawFilter(rawFilterString: string): DefaultSearchOptions {
    this.addRequestParameter('filter', encodeURIComponent(rawFilterString));
    return this;
  }

  public withFilter(filter: Filter): DefaultSearchOptions {
    this.addRequestParameter('filter', encodeURIComponent(filter.toString()));
    return this;
  }

  public withLimit(offset: number, count: number): DefaultSearchOptions {
    if (this.optionParameters.has('cursor') || this.optionParameters.has('size')) {
      throw new Error('Cursor/size and limit options cannot be set at the same time');
    }
    this.optionParameters.set('limit', `${offset},${count}`);
    return this.setOption();
  }

  public withSort(sortOperation: string): DefaultSearchOptions {
    this.optionParameters.set(`sort`, sortOperation);
    return this.setOption();
  }

  public withCursor(cursor: string): DefaultSearchOptions {
    if (this.optionParameters.has('limit')) {
      throw new Error('Limit and cursor options cannot be set at the same time');
    }
    this.optionParameters.set(`cursor`, cursor);
    return this.setOption();
  }

  public withPageSize(pageSize: number): DefaultSearchOptions {
    if (this.optionParameters.has('limit')) {
      throw new Error('Limit and cursor options cannot be set at the same time');
    }
    this.optionParameters.set('size', pageSize.toFixed(0));
    return this.setOption();
  }

  /**
   * Constructs the 'option' option out of the values of limit and sort.
   *
   * @returns The instance of DefaultSearchOptions with the constructed option
   */
  private setOption(): DefaultSearchOptions {

    const parameter = Array.from(this.optionParameters.entries())
      .map(([key, value]) => `${key}(${value})`)
      .join(',');

    this.addRequestParameter('option', encodeURIComponent(parameter));
    return this;
  }
}

/**
 * Option provider for get Things requests
 */
export interface GetThingsOptions extends AddRequestOptions<GetThingsOptions>, HasFields<GetThingsOptions> {
  setThingIds(ids: string[]): GetThingsOptions;
}

export class DefaultGetThingsOptions extends AbstractRequestOptions<DefaultGetThingsOptions> implements GetThingsOptions {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of GetThingsOptions.
   *
   * @returns The GetThingsOptions
   */
  public static getInstance(): DefaultGetThingsOptions {
    return new DefaultGetThingsOptions();
  }

  public withFields(...fields: string[]): DefaultGetThingsOptions {
    super.addRequestParameter('fields', encodeURIComponent(fields.join()));
    return this;
  }

  public setThingIds(ids: string[]): DefaultGetThingsOptions {
    super.addRequestParameter('ids', encodeURIComponent(ids.join()));
    return this;
  }
}

/**
 * Option provider for Messages requests
 */
export interface MessagesOptions extends AddRequestOptions<MessagesOptions> {
  /**
   * Sets a timeout option.
   *
   * @param timeout - The timeout to use.
   * @returns The instance of MessagesOptions with the added option
   */
  withTimeout(timeout: number): MessagesOptions;
}

export class DefaultMessagesOptions extends AbstractRequestOptions<DefaultMessagesOptions> implements MessagesOptions {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of MessagesOptions.
   *
   * @returns The MessagesOptions
   */
  public static getInstance(): DefaultMessagesOptions {
    return new DefaultMessagesOptions();
  }


  public withTimeout(timeout: number): DefaultMessagesOptions {
    this.addRequestParameter('timeout', timeout.toString());
    return this;
  }
}

/**
 * Option provider for post Connection requests
 */
export interface PostConnectionOptions extends AddRequestOptions<PostConnectionOptions> {

  /**
   * Sets a dry-run option to test a Connection.
   *
   * @param dryRun - If the connection should only be tested, but not already created.
   * @returns The instance of DefaultPostConnectionOptions with the added option
   */
  asDryRun(dryRun: boolean): DefaultPostConnectionOptions;
}


export class DefaultPostConnectionOptions extends AbstractRequestOptions<DefaultPostConnectionOptions> implements PostConnectionOptions {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of DefaultPostConnectionOptions.
   *
   * @returns The DefaultPostConnectionOptions
   */
  public static getInstance(): DefaultPostConnectionOptions {
    return new DefaultPostConnectionOptions();
  }

  /**
   * Gets an instance of DefaultPostConnectionOptions with the dry-run parameter set to true to test Connections.
   *
   * @returns The instance of MessagesOptions
   */
  public static getDryRunInstance(): DefaultPostConnectionOptions {
    return new DefaultPostConnectionOptions().asDryRun(true);
  }

  public asDryRun(dryRun: boolean): DefaultPostConnectionOptions {
    this.addRequestParameter('dry-run', String(dryRun));
    return this;
  }
}
