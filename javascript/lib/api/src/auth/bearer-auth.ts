/*!
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
  protected supplier!: TokenSupplier;

  /**
   * Build a new instance of bearer auth
   * @param tokenSupplier Implementation of the TokenSupplier class to provide tokens for authentication
   */
  constructor(tokenSupplier: TokenSupplier) {
    this.supplier = tokenSupplier;
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
   * @param supplier TokenSupplier used to get a token
   */
  static newInstance(supplier: TokenSupplier): HttpBearerAuth {
    return new HttpBearerAuth(supplier);
  }

  authenticateWithUrl(originalUrl: DittoURL): DittoURL {
    return originalUrl;
  }

  authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders {
    return originalHeaders.set('Authorization', `Bearer ${this.supplier.getToken()}`);
  }

}

/**
 * Provides a token for use in the bearer auth
 */
export interface TokenSupplier {

  /**
   * Called by the AuthProvider when a token is needed
   */
  getToken(): string;
}


/**
 * Static implementation of the TokenSupplier interface that always returns the same token
 */
export class DefaultTokenSupplier implements TokenSupplier {

  constructor(readonly token: string) {
  }

  getToken(): string {
    return this.token;
  }

}
