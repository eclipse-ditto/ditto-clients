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
import { AddressInfo } from 'net';
import { Duplex } from 'stream';
import { IncomingMessage } from 'http';
import { ImmutableURL } from '../../api/src/auth/auth-provider';
import { NodeWebSocketBasicAuth } from '../src/node-auth';
import { NodeWebSocket } from '../src/node-websocket';
import { ProxyAgent } from '../src/proxy-settings';

/**
 * Self-signed certificate for a non-matching hostname (CN=evil.example) so that both the chain and
 * the hostname check fail. Generated with openssl to avoid an extra runtime dependency.
 */
const selfSigned = (() => {
  const pems = execFileSync('openssl', [
    'req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-keyout', '-', '-days', '1',
    '-subj', '/CN=evil.example'
  ]).toString();
  const key = pems.substring(pems.indexOf('-----BEGIN PRIVATE KEY-----'), pems.indexOf('-----END PRIVATE KEY-----') + '-----END PRIVATE KEY-----'.length);
  const cert = pems.substring(pems.indexOf('-----BEGIN CERTIFICATE-----'));
  return { key, cert };
})();

/**
 * Completes a WebSocket upgrade with a bare {@code 101} handshake response, which is all the
 * {@code ws} client needs to consider itself connected.
 */
const acceptUpgrade = (req: IncomingMessage, socket: Duplex): void => {
  const accept = crypto.createHash('sha1')
    .update(String(req.headers['sec-websocket-key']) + '258EAFA5-E914-47DA-95CA-C5AB0DC85B11')
    .digest('base64');
  socket.write(
    'HTTP/1.1 101 Switching Protocols\r\n' +
    'Upgrade: websocket\r\n' +
    'Connection: Upgrade\r\n' +
    `Sec-WebSocket-Accept: ${accept}\r\n\r\n`
  );
};

const noopHandler: any = {
  handleInput: () => { /* noop */ },
  handleResponse: () => { /* noop */ },
  handleMessage: () => { /* noop */ },
  handleClose: (promise: Promise<unknown>) => { promise.catch(() => { /* swallow reconnect failures */ }); },
  handleFailure: () => { /* noop */ },
  handleError: () => { /* noop */ }
};

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
  const upgradedSockets: Duplex[] = [];

  const buildWss = (rejectUnauthorized?: boolean) => {
    const url = ImmutableURL.newInstance('wss', `127.0.0.1:${port}`, '/ws/2');
    const authProvider = NodeWebSocketBasicAuth.newInstance('victim-user', 'victim-pass');
    const tlsOptions = rejectUnauthorized === undefined ? undefined : { rejectUnauthorized };
    return NodeWebSocket.buildInstance(url, noopHandler, [authProvider], new ProxyAgent(), tlsOptions);
  };

  beforeAll(done => {
    server = https.createServer(selfSigned);
    server.on('upgrade', (req, socket) => {
      upgradedSockets.push(socket);
      capturedAuthorization = req.headers['authorization'];
      acceptUpgrade(req, socket);
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
    // 3.8.x has neither NodeWebSocket#close() nor a way to turn the automatic reconnect off -
    // both arrived in 3.9.0. Detach the 'close' handler before closing the underlying socket so
    // closing does not kick off the retry ladder and leak timers into the test run.
    const underlying = (webSocket as any).webSocket;
    underlying.removeAllListeners('close');
    underlying.close();
  });
});

/**
 * Regression test for the error handling of {@code NodeWebSocket.reconnect}.
 *
 * The reconnect attempt installs its message/close/error handlers only once the fresh connection is
 * open. A handshake failure before that point - which certificate validation makes a realistic
 * scenario, e.g. after a certificate rotation - used to be emitted on a {@code WebSocket} without
 * any 'error' listener, which NodeJS escalates to an uncaught exception that terminates the host
 * process.
 */
describe('NodeWebSocket reconnect', () => {

  let server: https.Server;
  let port: number;
  let upgradedSocket: Duplex;

  beforeEach(done => {
    server = https.createServer(selfSigned);
    server.on('upgrade', (req, socket) => {
      upgradedSocket = socket;
      acceptUpgrade(req, socket);
    });
    server.listen(0, '127.0.0.1', () => {
      port = (server.address() as AddressInfo).port;
      done();
    });
  });

  it('reports a failed reconnect attempt instead of escalating it to an uncaught exception', async () => {
    let closed = false;
    let reconnectError: string | undefined;
    let reportReconnectError: () => void;
    const reconnectErrorReported = new Promise<void>(resolve => {
      reportReconnectError = resolve;
    });
    const handler: any = {
      ...noopHandler,
      handleClose: (promise: Promise<unknown>) => {
        closed = true;
        // the retry ladder eventually gives up; keep the rejection from becoming an unhandled one
        promise.catch(() => { /* expected: the server is gone */ });
      },
      handleError: (error: string) => {
        // errors reported after the close event originate from the reconnect attempt
        if (closed && reconnectError === undefined) {
          reconnectError = error;
          reportReconnectError();
        }
      }
    };

    const url = ImmutableURL.newInstance('wss', `127.0.0.1:${port}`, '/ws/2');
    const authProvider = NodeWebSocketBasicAuth.newInstance('user', 'pass');
    const webSocket = await NodeWebSocket.buildInstance(url, handler, [authProvider], new ProxyAgent(),
      { rejectUnauthorized: false });

    // stop listening first, so that the reconnect attempt triggered by the close below cannot succeed
    await new Promise<void>(resolve => {
      server.close(() => resolve());
      upgradedSocket.end();
    });

    await reconnectErrorReported;
    expect(reconnectError).toContain('ECONNREFUSED');

    // 3.8.x has neither NodeWebSocket#close() nor a way to disable the reconnect - both arrived
    // in 3.9.0 - so the retry ladder cannot be cancelled from the outside. Flipping `connected`
    // makes the pending retry timer take its early-return branch, which stops the ladder instead
    // of letting it double its way up to 120s well past the end of this test.
    (webSocket as any).connected = true;
    await new Promise<void>(resolve => setTimeout(resolve, 1500));
  });
});
