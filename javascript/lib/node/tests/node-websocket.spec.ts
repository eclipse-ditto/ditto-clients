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
import { IncomingMessage } from 'http';
import * as http from 'http';
import { clearProxyEnvironment } from './proxy-environment';
import { ImmutableURL } from '../../api/src/auth/auth-provider';
import { NodeWebSocketBasicAuth } from '../src/node-auth';
import { NodeWebSocket } from '../src/node-websocket';
import { ProxyAgent } from '../src/proxy-settings';
import * as WebSocket from 'ws';

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
const acceptUpgrade = (req: IncomingMessage, socket: Socket): void => {
  const accept = crypto.createHash('sha1')
    .update(req.headers['sec-websocket-key'] + '258EAFA5-E914-47DA-95CA-C5AB0DC85B11')
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
  handleClose: (promise: Promise<unknown>) => { promise.catch(() => { /* reconnect disabled in test */ }); },
  handleFailure: () => { /* noop */ },
  handleError: () => { /* noop */ }
};

let restoreEnvironment: () => void;
beforeEach(() => { restoreEnvironment = clearProxyEnvironment(); });
afterEach(() => restoreEnvironment());

describe('NodeWebSocket environment proxy exclusions', () => {
  it.each([false, true])('uses the excluded authenticated ws destination (URL rewritten=%s)', async rewritten => {
    const server = new WebSocket.Server({ port: 0, host: '127.0.0.1' });
    let proxyAuthorization: unknown = 'unset';
    server.on('headers', (_headers, req) => { proxyAuthorization = req.headers['proxy-authorization']; });
    let proxyRequests = 0;
    const proxy = http.createServer((_req, res) => { proxyRequests++; res.writeHead(502); res.end(); });
    proxy.on('connect', (_req, socket) => { proxyRequests++; socket.end('HTTP/1.1 502 Bad Gateway\r\n\r\n'); });
    try {
      await Promise.all([
        new Promise<void>((resolve, reject) => {
          server.once('error', reject);
          server.once('listening', resolve);
        }),
        new Promise<void>((resolve, reject) => {
          proxy.once('error', reject);
          proxy.listen(0, '127.0.0.1', resolve);
        })
      ]);
      process.env.HTTP_PROXY = `http://127.0.0.1:${(proxy.address() as AddressInfo).port}`;
      delete process.env.http_proxy;
      process.env.NO_PROXY = '127.0.0.1';
      delete process.env.no_proxy;
      const port = (server.address() as AddressInfo).port;
      const url = ImmutableURL.newInstance('ws', `127.0.0.1:${port}`, '/ws/2');
      const original = rewritten ? url.withDomain('original.invalid') : url;
      const authProviders = rewritten ? [{
        authenticateWithUrl: () => url,
        authenticateWithHeaders: (headers: Map<string, string>) => headers
      }] : [];
      const options = { url: process.env.HTTP_PROXY, username: 'synthetic-user', password: 'synthetic-password' };
      const client = await NodeWebSocket.buildInstance(original, noopHandler, authProviders, new ProxyAgent(options), false);
      expect(client).toBeTruthy();
      expect(proxyRequests).toBe(0);
      expect(proxyAuthorization).toBeUndefined();
      client.close();
      delete process.env.NO_PROXY;
      await expect(NodeWebSocket.buildInstance(original, noopHandler, authProviders, new ProxyAgent(options), false)).rejects.toBeDefined();
      expect(proxyRequests).toBe(1);
    } finally {
      server.clients.forEach(client => client.terminate());
      await new Promise<void>(resolve => server.close(() => resolve()));
      await new Promise<void>(resolve => proxy.close(() => resolve()));
    }
  });

  it('rejects an invalid authenticated URL instead of throwing outside the promise', async () => {
    const url = ImmutableURL.newInstance('ws', 'invalid host:8080', '/ws/2');
    await expect(NodeWebSocket.buildInstance(url, noopHandler, [], new ProxyAgent(), false))
      .rejects.toMatchObject({ code: 'ERR_INVALID_URL' });
  });
});

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

  const buildWss = (rejectUnauthorized?: boolean) => {
    const url = ImmutableURL.newInstance('wss', `127.0.0.1:${port}`, '/ws/2');
    const authProvider = NodeWebSocketBasicAuth.newInstance('victim-user', 'victim-pass');
    const tlsOptions = rejectUnauthorized === undefined ? undefined : { rejectUnauthorized };
    return NodeWebSocket.buildInstance(url, noopHandler, [authProvider], new ProxyAgent(), false, tlsOptions);
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
    webSocket.close();
  });

  it('bypasses an excluded wss proxy without bypassing TLS certificate validation', async () => {
    try {
      process.env.HTTPS_PROXY = 'http://127.0.0.1:1';
      delete process.env.https_proxy;
      process.env.NO_PROXY = `127.0.0.1:${port}`;
      delete process.env.no_proxy;
      await expect(buildWss()).rejects.toMatchObject({ code: 'DEPTH_ZERO_SELF_SIGNED_CERT' });
      expect(capturedAuthorization).toBeUndefined();
      const client = await buildWss(false);
      expect(capturedAuthorization).toBeDefined();
      client.close();
    } finally {
      restoreEnvironment();
    }
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
  let upgradedSocket: Socket;

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
    let reconnectSettled: Promise<unknown> = Promise.resolve();
    const handler: any = {
      ...noopHandler,
      handleClose: (promise: Promise<unknown>) => {
        closed = true;
        // the retry ladder eventually gives up; keep the rejection from becoming an unhandled one
        reconnectSettled = promise.catch(() => { /* expected: the server is gone */ });
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
      true, { rejectUnauthorized: false });

    // stop listening first, so that the reconnect attempt triggered by the close below cannot succeed
    await new Promise<void>(resolve => {
      server.close(() => resolve());
      upgradedSocket.end();
    });

    await reconnectErrorReported;
    expect(reconnectError).toContain('ECONNREFUSED');

    // disable further attempts and let the pending retry ladder settle, so that no timer of it
    // outlives the test
    webSocket.close();
    await reconnectSettled;
  });
});
