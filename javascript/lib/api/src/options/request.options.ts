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

/**
 * Option provider for options. Options will be replaced if methods are called multiple times.
 */
abstract class AbstractRequestOptions<T extends AbstractRequestOptions<T>> implements RequestOptions {

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

  /**
   * Adds a URl option.
   *
   * @param id - The ID of the option to add.
   * @param value - The body of the option to add.
   */
  addRequestParameter(id: string, value: string): T {
    this.options.set(id, value);
    return this as unknown as T;
  }

  /**
   * Adds a header option.
   *
   * @param name - The name of the header to add.
   * @param value - The body of the header to add.
   */
  addHeader(name: string, value: string): T {
    this.headers.set(name, value);
    return this as unknown as T;
  }
}

interface HasMatch<T extends HasMatch<T>> {
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

interface HasFields<T extends HasFields<T>> {
  /**
   * Sets a fields option.
   *
   * @param fields - The fields to return.
   * @returns This Options instance with the added option
   */
  withFields(...fields: string[]): T;
}

interface HasFilterAndNamespace<T extends HasFilterAndNamespace<T>> {
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

abstract class AbstractRequestOptionsWithMatchOptions<T extends AbstractRequestOptionsWithMatchOptions<T>>
  extends AbstractRequestOptions<AbstractRequestOptionsWithMatchOptions<T>>
  implements HasMatch<AbstractRequestOptionsWithMatchOptions<T>> {
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
export class MatchOptions extends AbstractRequestOptionsWithMatchOptions<MatchOptions> {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of MatchOptions.
   *
   * @returns The MatchOptions
   */
  public static getInstance(): MatchOptions {
    return new MatchOptions();
  }
}

/**
 * Option provider for some get requests
 */
export class FieldsOptions extends AbstractRequestOptionsWithMatchOptions<FieldsOptions> implements HasFields<FieldsOptions> {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of FieldsOptions.
   *
   * @returns The FieldsOptions
   */
  public static getInstance(): FieldsOptions {
    return new FieldsOptions();
  }

  public withFields(...fields: string[]): FieldsOptions {
    super.addRequestParameter('fields', encodeURIComponent(fields.join()));
    return this;
  }

}

/**
 * Option provider for count requests
 */
export class CountOptions extends AbstractRequestOptions<CountOptions> implements HasFilterAndNamespace<CountOptions> {

  protected constructor() {
    super();
  }

  /**
   * Provides an instance of CountOptions.
   *
   * @returns The CountOptions
   */
  public static getInstance(): CountOptions {
    return new CountOptions();
  }

  public withRawFilter(rawFilterString: string): CountOptions {
    this.addRequestParameter('filter', encodeURIComponent(rawFilterString));
    return this;
  }

  public withFilter(filter: Filter): CountOptions {
    this.addRequestParameter('filter', encodeURIComponent(filter.toString()));
    return this;
  }

  public withNamespaces(...namespaces: string[]): CountOptions {
    this.addRequestParameter('namespaces', encodeURIComponent(namespaces.join()));
    return this;
  }

}

/**
 * Option provider for search requests
 */
export class SearchOptions extends AbstractRequestOptions<SearchOptions>
  implements HasFilterAndNamespace<SearchOptions>, HasFields<SearchOptions> {
  private sort: string;
  private limit: string;

  private constructor() {
    super();
    this.sort = '';
    this.limit = '';
  }

  /**
   * Provides an instance of SearchOptions.
   *
   * @returns The SearchOptions
   */
  public static getInstance(): SearchOptions {
    return new SearchOptions();
  }

  public withNamespaces(...namespaces: string[]): SearchOptions {
    this.addRequestParameter('namespaces', encodeURIComponent(namespaces.join()));
    return this;
  }

  public withFields(...fields: string[]): SearchOptions {
    this.addRequestParameter('fields', encodeURIComponent(fields.join()));
    return this;
  }

  public withRawFilter(rawFilterString: string): SearchOptions {
    this.addRequestParameter('filter', encodeURIComponent(rawFilterString));
    return this;
  }

  public withFilter(filter: Filter): SearchOptions {
    this.addRequestParameter('filter', encodeURIComponent(filter.toString()));
    return this;
  }

  /**
   * Sets a limit option.
   *
   * @param offset - The index to start at.
   * @param count - The number of things to return.
   * @returns The instance of SearchOptions with the added option
   */
  public withLimit(offset: number, count: number): SearchOptions {
    this.limit = `limit(${offset},${count})`;
    return this.setOption();
  }

  /**
   * Sets a sort option.
   *
   * @param sortOperation - The string to sort by.
   * @returns The instance of SearchOptions with the added option
   */
  public withSort(sortOperation: string): SearchOptions {
    this.sort = `sort(${encodeURIComponent(sortOperation)})`;
    return this.setOption();
  }

  /**
   * Constructs the 'option' option out of the values of limit and sort.
   *
   * @returns The instance of SearchOptions with the constructed option
   */
  private setOption(): SearchOptions {
    let parameter: string;
    if (this.sort === '') {
      parameter = this.limit;
    } else if (this.limit === '') {
      parameter = this.sort;
    } else {
      parameter = `${this.limit},${this.sort}`;
    }
    this.addRequestParameter('option', encodeURIComponent(parameter));
    return this;
  }
}

/**
 * Option provider for get Things requests
 */
export class GetThingsOptions extends AbstractRequestOptions<GetThingsOptions> implements HasFields<GetThingsOptions> {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of GetThingsOptions.
   *
   * @returns The GetThingsOptions
   */
  public static getInstance(): GetThingsOptions {
    return new GetThingsOptions();
  }

  public withFields(...fields: string[]): GetThingsOptions {
    super.addRequestParameter('fields', encodeURIComponent(fields.join()));
    return this;
  }

  /**
   * Sets the ids for a get Things request.
   *
   * @param ids - The ids to get.
   * @returns The instance of GetThingsOptions with the ids set
   */
  public setThingIds(ids: string[]): GetThingsOptions {
    super.addRequestParameter('ids', encodeURIComponent(ids.join()));
    return this;
  }
}

/**
 * Option provider for Messages requests
 */
export class MessagesOptions extends AbstractRequestOptions<MessagesOptions> {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of MessagesOptions.
   *
   * @returns The MessagesOptions
   */
  public static getInstance(): MessagesOptions {
    return new MessagesOptions();
  }

  /**
   * Sets a timeout option.
   *
   * @param timeout - The timeout to use.
   * @returns The instance of MessagesOptions with the added option
   */
  public withTimeout(timeout: number): MessagesOptions {
    this.addRequestParameter('timeout', timeout.toString());
    return this;
  }
}

/**
 * Option provider for post Connection requests
 */
export class PostConnectionOptions extends AbstractRequestOptions<PostConnectionOptions> {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of PostConnectionOptions.
   *
   * @returns The PostConnectionOptions
   */
  public static getInstance(): PostConnectionOptions {
    return new PostConnectionOptions();
  }

  /**
   * Gets an instance of PostConnectionOptions with the dry-run parameter set to true to test Connections.
   *
   * @returns The instance of MessagesOptions
   */
  public static getDryRunInstance(): PostConnectionOptions {
    return new PostConnectionOptions().asDryRun(true);
  }

  /**
   * Sets a dry-run option to test a Connection.
   *
   * @param dryRun - If the connection should only be tested, but not already created.
   * @returns The instance of PostConnectionOptions with the added option
   */
  public asDryRun(dryRun: boolean): PostConnectionOptions {
    this.addRequestParameter('dry-run', String(dryRun));
    return this;
  }
}
