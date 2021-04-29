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

import { DittoHeaders } from '../model/ditto-protocol';

export {
  AuthProvider,
  DittoHeaders,
  DittoURL,
  DittoURLParams,
  authenticateWithUrl,
  authenticateWithHeaders,
  authenticateWithUrlAndHeaders,
  ImmutableURL
};

/**
 * Query Parameters that can be used in a Ditto URL.
 */
type DittoURLParams = string[];

/**
 * URL splitted into its parts.
 */
interface DittoURL {
  /**
   * Protocol of the URL, e.g. http, wss, ...
   */
  readonly protocol: string;
  /**
   * Domain of the URL, e.g. ditto.eclipse.org, localhost, ...
   */
  readonly domain: string;
  /**
   * Path for the URL, e.g. /api/2/things
   */
  readonly path: string;
  /**
   * Params for the url, e.g. ['x-correlation-id=myCorrelationid', 'x-ditto-pre-authenticated=nginx:ditto']
   */
  readonly queryParams: DittoURLParams;

  /**
   * Use the given protocol for the url.
   * @param protocol - e.g. http, wss, ...
   * @return the url object for method chaining.
   */
  withProtocol(protocol: string): DittoURL;

  /**
   * Use the given domain for the url.
   * @param domain -  e.g. ditto.eclipse.org, localhost, ...
   * @return the url object for method chaining.
   */
  withDomain(domain: string): DittoURL;

  /**
   * Use the given path for the url.
   * @param path - e.g. /api/2/things
   * @return the url object for method chaining.
   */
  withPath(path: string): DittoURL;

  /**
   * Use the given queryParams for the url.
   * @param params - e.g. ['x-correlation-id=myCorrelationId', 'x-ditto-pre-authenticated=nginx:ditto']
   * @return the url object for method chaining.
   */
  withParams(params: DittoURLParams): DittoURL;

  /**
   * Convert to string.
   * @return string representation of the url, e.g.
   * https://ditto.eclipse.org/api/2/things?x-correlation-id=myCorrelationId&x-ditto-pre-authenticated=nginx:ditto
   */
  toString(): string;
}

/**
 * Immutable implementation of DittoURL.
 */
class ImmutableURL implements DittoURL {

  private constructor(readonly protocol: string, readonly domain: string, readonly path: string, readonly queryParams: DittoURLParams) {
  }

  static newInstance(protocol: string, domain: string, path: string, params: DittoURLParams = []): DittoURL {
    return new ImmutableURL(protocol, domain, path, params);
  }

  withProtocol(protocol: string): ImmutableURL {
    return new ImmutableURL(protocol, this.domain, this.path, this.queryParams);
  }

  withDomain(domain: string): ImmutableURL {
    return new ImmutableURL(this.protocol, domain, this.path, this.queryParams);
  }

  withPath(path: string): ImmutableURL {
    return new ImmutableURL(this.protocol, this.domain, path, this.queryParams);
  }

  withParams(params: string[]): ImmutableURL {
    return new ImmutableURL(this.protocol, this.domain, this.path, params);
  }

  toString(): string {
    const theParams = this.queryParams.length > 0 ? `?${this.queryParams.join('&')}` : '';
    return `${this.protocol}://${this.domain}${this.path}${theParams}`;
  }

}

/**
 * Provides authentication or authorization.
 */
interface AuthProvider {
  /**
   * Enhance the url with authentication or authorization.
   * @param originalUrl - the URL to enhance.
   * @return the enchanced URL.
   */
  authenticateWithUrl(originalUrl: DittoURL): DittoURL;

  /**
   * Enhance the headers with authentication or authorization.
   * @param originalHeaders - the headers to enhance.
   * @return the enhanced Headers.
   */
  authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders;

}

/**
 * Enhance the url with all auth providers.
 * @param originalUrl - the url to enhance.
 * @param authProviders - the auth providers to apply.
 * @return the enhanced url.
 */
const authenticateWithUrl = (originalUrl: DittoURL, authProviders: AuthProvider[]): DittoURL => {
  let enhancedUrl = originalUrl;
  for (const authProvider of authProviders) {
    enhancedUrl = authProvider.authenticateWithUrl(enhancedUrl);
  }
  return enhancedUrl;
};

/**
 * Enhance the headers with all auth providers.
 * @param originalHeaders - the headers to enhance.
 * @param authProviders - the auth providers to apply.
 * @return the enhanced headers.
 */
const authenticateWithHeaders = (originalHeaders: DittoHeaders, authProviders: AuthProvider[]): DittoHeaders => {
  let enhancedHeaders = originalHeaders;
  for (const authProvider of authProviders) {
    enhancedHeaders = authProvider.authenticateWithHeaders(enhancedHeaders);
  }
  return enhancedHeaders;
};

/**
 * Enhance the url and headers with all auth providers.
 * @param originalUrl - the url to enhance.
 * @param originalHeaders - the headers to enhance.
 * @param authProviders - the auth providers to apply.
 * @return the enhanced url and headers.
 */
const authenticateWithUrlAndHeaders = (originalUrl: DittoURL, originalHeaders: DittoHeaders, authProviders: AuthProvider[]):
  [DittoURL, DittoHeaders] => {
  let enhancedUrl = originalUrl;
  let enhancedHeaders = originalHeaders;
  for (const authProvider of authProviders) {
    enhancedUrl = authProvider.authenticateWithUrl(enhancedUrl);
    enhancedHeaders = authProvider.authenticateWithHeaders(enhancedHeaders);
  }
  return [enhancedUrl, enhancedHeaders];
};

