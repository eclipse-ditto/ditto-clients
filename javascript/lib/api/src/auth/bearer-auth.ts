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

import { AuthProvider, DittoHeaders, DittoURL } from './auth-provider';

/**
 * Provides OAuth-Style authentication via bearer tokens
 */
export abstract class BearerAuth implements AuthProvider {

  /**
   * The bearer token used for authentication
   */
  protected token!: string;

  /**
   * Build a new instance of bearer auth
   * @param token The bearer token to be used
   */
  constructor(token: string) {
    this.token = token;
  }

  abstract authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders;

  abstract authenticateWithUrl(originalUrl: DittoURL): DittoURL;
}


/**
 * Http implementation of bearer auth using the Authorization HTTP header.
 */
export class HttpBearerAuth extends BearerAuth {

  /**
   * Creates a new instance for HTTP connections
   * @param token
   */
  static newInstance(token: string): HttpBearerAuth {
    return new HttpBearerAuth(token);
  }

  authenticateWithUrl(originalUrl: DittoURL): DittoURL {
    return originalUrl;
  }

  authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders {
    return originalHeaders.set('Authorization', `Bearer ${this.token}`);
  }

}
