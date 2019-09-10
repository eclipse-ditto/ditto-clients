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
 * Basic authentication provider.
 */
export abstract class BasicAuth implements AuthProvider {

  /**
   * Build a new instance for basic auth.
   * @param username - the username.
   * @param password - the password.
   * @param encoder - the encoder.
   */
  constructor(readonly username: string, readonly password: string, readonly encoder: Base64Encoder) {
  }

  /**
   * The string to encode.
   */
  getStringToEncode(): string {
    return `${this.username}:${this.password}`;
  }

  /**
   * Encodes 'getStringToEncode()'.
   */
  getEncodedAuthentication(): string {
    return this.encoder.encodeBase64(this.getStringToEncode());
  }

  abstract authenticateWithUrl(originalUrl: DittoURL): DittoURL;

  abstract authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders;
}

/**
 * Encoder for Base64.
 */
export interface Base64Encoder {
  /**
   * Encode the string into base64.
   * @param toEncode - the string to encode.
   * @return the encoded string.
   */
  encodeBase64(toEncode: string): string;
}

/**
 * HTTP implementation of basic auth using the Authorization HTTP header.
 */
export class HttpBasicAuth extends BasicAuth {

  protected constructor(username: string, password: string, encoder: Base64Encoder) {
    super(username, password, encoder);
  }
  /**
   * Create basic authentication for Http connections.
   * @param username - The username.
   * @param password - the password.
   * @param encoder - the encoder to use when encoding the authentication in Base64.
   */
  static newInstance(username: string, password: string, encoder: Base64Encoder): BasicAuth {
    return new HttpBasicAuth(username, password, encoder);
  }

  authenticateWithUrl(originalUrl: DittoURL): DittoURL {
    return originalUrl;
  }

  authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders {
    const encoded = this.getEncodedAuthentication();
    return originalHeaders.set('Authorization', `Basic ${encoded}`);
  }
}
