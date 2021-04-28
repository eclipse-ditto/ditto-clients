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

import { Base64Encoder, BasicAuth, HttpBasicAuth } from '../../api/src/auth/basic-auth';
import { HttpBearerAuth, TokenSupplier } from '../../api/src/auth/bearer-auth';
import { DittoHeaders, DittoURL } from '../../api/src/auth/auth-provider';

/**
 * Dom implementation of a Base64 encoder.
 */
export class DomBase64Encoder implements Base64Encoder {
  encodeBase64(toEncode: string): string {
    return btoa(toEncode);
  }
}

/**
 * Dom implementation of basic auth for HTTP connections.
 */
export class DomHttpBasicAuth extends HttpBasicAuth {
  private constructor(username: string, password: string, encoder: Base64Encoder) {
    super(username, password, encoder);
  }

  /**
   * Create basic authentication for HTTP connections.
   * @param username - The username.
   * @param password - the password.
   */
  static newInstance(username: string, password: string): BasicAuth {
    return new DomHttpBasicAuth(username, password, new DomBase64Encoder());
  }
}

/**
 * Dom implementation of basic auth for WebSocket connections.
 */
export class DomWebSocketBasicAuth extends BasicAuth {
  private constructor(username: string, password: string, encoder: Base64Encoder) {
    super(username, password, encoder);
  }

  /**
   * Create basic authentication for Http connections.
   * @param username - The username.
   * @param password - the password.
   */
  static newInstance(username: string, password: string): BasicAuth {
    return new DomWebSocketBasicAuth(username, password, new DomBase64Encoder());
  }

  authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders {
    return originalHeaders;
  }

  authenticateWithUrl(originalUrl: DittoURL): DittoURL {
    return originalUrl.withDomain(`${encodeURIComponent(this.username)}:${encodeURIComponent(this.password)}@${originalUrl.domain}`);
  }
}

/**
 * DOM implementation of bearer authentication for HTTP connections
 */
export class DomHttpBearerAuth extends HttpBearerAuth {

  constructor(tokenSupplier: TokenSupplier) {
    super(tokenSupplier);
  }

  /**
   * Create a new AuthProvider for bearer token authentication over http
   * @param tokenSupplier Provides auth tokens to this AuthProvider when needed
   */
  static newInstance(tokenSupplier: TokenSupplier) {
    return new DomHttpBearerAuth(tokenSupplier);
  }
}

/**
 * DOM implementation of bearer authentication for WebSocket connections
 */
export class DomWebSocketBearerAuth extends HttpBearerAuth {

  constructor(tokenSupplier: TokenSupplier) {
    super(tokenSupplier);
  }

  /**
   * Create a new AuthProvider for bearer token authentication over WebSocket
   * @param tokenSupplier Provides auth tokens to this AuthProvider when needed
   */
  static newInstance(tokenSupplier: TokenSupplier) {
    return new DomWebSocketBearerAuth(tokenSupplier);
  }

  authenticateWithUrl(originalUrl: DittoURL): DittoURL {
    const accessToken = [`access_token=${this.supplier.getToken()}`];
    const params = [...originalUrl.queryParams, ...accessToken];
    return originalUrl.withParams(params);
  }

  authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders {
    return originalHeaders;
  }
}
