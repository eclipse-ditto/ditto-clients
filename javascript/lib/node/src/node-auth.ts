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

import {
  Base64Encoder,
  BasicAuth,
  HttpBasicAuth,
  HttpBearerAuth
} from '@eclipse-ditto/ditto-javascript-client-api_1.0';

/**
 * Node implementation of base64 encoding.
 */
export class NodeBase64Encoder implements Base64Encoder {
  encodeBase64(toEncode: string): string {
    return Buffer.from(toEncode).toString('base64');
  }
}

/**
 * Node implementation of basic auth for HTTP connections.
 */
export class NodeHttpBasicAuth extends HttpBasicAuth {
  private constructor(username: string, password: string, encoder: Base64Encoder) {
    super(username, password, encoder);
  }

  /**
   * Create basic authentication for HTTP connections.
   * @param username - The username.
   * @param password - the password.
   */
  static newInstance(username: string, password: string): BasicAuth {
    return new NodeHttpBasicAuth(username, password, new NodeBase64Encoder());
  }
}

/**
 * Node implementation of basic auth for WebSocket connections.
 */
export class NodeWebSocketBasicAuth extends HttpBasicAuth {
  private constructor(username: string, password: string, encoder: Base64Encoder) {
    super(username, password, encoder);
  }

  /**
   * Create basic authentication for WebSocket connections.
   * @param username - The username.
   * @param password - the password.
   */
  static newInstance(username: string, password: string): BasicAuth {
    return new NodeWebSocketBasicAuth(username, password, new NodeBase64Encoder());
  }
}

/**
 * Node implementation of basic auth for HTTP connections
 */
export class NodeHttpBearerAuth extends HttpBearerAuth {

  private constructor(token: string) {
    super(token);
  }

  /**
   * Create berer token AuthProvider for HTTP connections
   * @param token The bearer token
   */
  static newInstance(token: string) {
    return new NodeHttpBearerAuth(token);
  }

}
