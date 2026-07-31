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

import * as https from 'https';
import * as crypto from 'crypto';
import { execFileSync } from 'child_process';
import { AddressInfo, Socket } from 'net';
import { ImmutableURL } from '../../api/src/auth/auth-provider';
import { NodeWebSocketBasicAuth } from '../src/node-auth';
import { NodeWebSocket } from '../src/node-websocket';
import { ProxyAgent } from '../src/proxy-settings';

/**
 * Regression tests for the TLS certificate validation of the NodeJS WebSocket transport.
 *
 * Historically {@code NodeWebSocket.buildInstance} hard-coded {@code rejectUnauthorized: false},
 * which silently disabled certificate validation for every {@code wss://} connection and allowed
 * man-in-the-middle attackers to intercept credentials (CWE-295). These tests verify that the
 * secure NodeJS default is used and that an explicit, per-client opt-out remains possible.
 */
describe('NodeWebSocket TLS certificate validation', () => {

  let server: https.Server;
  let port: number;
  let capturedAuthorization: string | undefined;
  const upgradedSockets: Socket[] = [];

  const noopHandler: any = {
    handleInput: () => { /* noop */ },
    handleResponse: () => { /* noop */ },
    handleMessage: () => { /* noop */ },
    handleClose: (promise: Promise<unknown>) => { promise.catch(() => { /* reconnect disabled in test */ }); },
    handleFailure: () => { /* noop */ },
    handleError: () => { /* noop */ }
  };

  const buildWss = (rejectUnauthorized?: boolean) => {
    const url = ImmutableURL.newInstance('wss', `127.0.0.1:${port}`, '/ws/2');
    const authProvider = NodeWebSocketBasicAuth.newInstance('victim-user', 'victim-pass');
    const tlsOptions = rejectUnauthorized === undefined ? undefined : { rejectUnauthorized };
    return NodeWebSocket.buildInstance(url, noopHandler, [authProvider], new ProxyAgent(), false, tlsOptions);
  };

  beforeAll(done => {
    // Self-signed certificate for a non-matching hostname (CN=evil.example) so both the chain
    // and the hostname check fail. Generated with openssl to avoid an extra runtime dependency.
    const pems = execFileSync('openssl', [
      'req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-keyout', '-', '-days', '1',
      '-subj', '/CN=evil.example'
    ]).toString();
    const key = pems.substring(pems.indexOf('-----BEGIN PRIVATE KEY-----'), pems.indexOf('-----END PRIVATE KEY-----') + '-----END PRIVATE KEY-----'.length);
    const cert = pems.substring(pems.indexOf('-----BEGIN CERTIFICATE-----'));

    server = https.createServer({ key, cert });
    server.on('upgrade', (req, socket) => {
      upgradedSockets.push(socket);
      capturedAuthorization = req.headers['authorization'];
      const accept = crypto.createHash('sha1')
        .update(req.headers['sec-websocket-key'] + '258EAFA5-E914-47DA-95CA-C5AB0DC85B11')
        .digest('base64');
      socket.write(
        'HTTP/1.1 101 Switching Protocols\r\n' +
        'Upgrade: websocket\r\n' +
        'Connection: Upgrade\r\n' +
        `Sec-WebSocket-Accept: ${accept}\r\n\r\n`
      );
    });
    server.listen(0, '127.0.0.1', () => {
      port = (server.address() as AddressInfo).port;
      done();
    });
  });

  afterAll(done => {
    upgradedSockets.forEach(socket => socket.destroy());
    server.close(() => done());
  });

  beforeEach(() => {
    capturedAuthorization = undefined;
  });

  it('rejects an untrusted (self-signed, hostname-mismatched) certificate by default', async () => {
    await expect(buildWss()).rejects.toBeDefined();
    expect(capturedAuthorization).toBeUndefined();
  });

  it('allows an explicit, per-client opt-out via { rejectUnauthorized: false }', async () => {
    const webSocket = await buildWss(false);
    expect(webSocket).toBeTruthy();
    expect(capturedAuthorization).toBeDefined();
    webSocket.close();
  });
});
