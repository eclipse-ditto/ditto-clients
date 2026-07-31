/*!
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

/**
 * Options to configure TLS for the NodeJS transports (e.g. {@code wss://} connections).
 *
 * By default (i.e. when no {@link TlsOptions} are provided) the client relies on NodeJS'
 * secure defaults: the server certificate is validated against the well-known certificate
 * authorities and the requested hostname. Use these options to supply additional trust
 * material (e.g. a corporate root CA) or a client certificate for mutual TLS.
 */
export interface TlsOptions {
  /**
   * Optionally overrides the trusted CA certificates. Provide one or more PEM encoded
   * certificates to trust, e.g. a self-signed or corporate root certificate that is not
   * part of the system trust store.
   */
  ca?: string | Buffer | Array<string | Buffer>;
  /** Optional client certificate chain (PEM) to present for mutual TLS. */
  cert?: string | Buffer | Array<string | Buffer>;
  /** Optional private key (PEM) belonging to {@link cert} for mutual TLS. */
  key?: string | Buffer | Array<string | Buffer>;
  /** Optional passphrase for the private key. */
  passphrase?: string;
  /** Optional PKCS#12 encoded private key and certificate chain for mutual TLS. */
  pfx?: string | Buffer | Array<string | Buffer>;
  /**
   * Whether to reject connections whose server certificate cannot be validated (invalid
   * chain, expired, or hostname mismatch).
   *
   * Defaults to {@code true} (secure). Setting this to {@code false} disables certificate
   * validation and exposes the connection to man-in-the-middle attacks; only use it for an
   * explicit, audited opt-out (e.g. local development against a self-signed certificate).
   */
  rejectUnauthorized?: boolean;
}
