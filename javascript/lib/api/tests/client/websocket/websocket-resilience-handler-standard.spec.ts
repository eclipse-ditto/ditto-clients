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

import { jest } from '@jest/globals';
import { StandardResilienceHandler } from '../../../src/client/request-factory/resilience/websocket-resilience-handler-standard';
import {
  NoopWebSocketStateHandler,
  RequestHandler,
  WebSocketImplementation,
  WebSocketImplementationBuilderHandler
} from '../../../src/client/request-factory/resilience/websocket-resilience-interfaces';
import { DefaultDittoProtocolEnvelope } from '../../../src/model/ditto-protocol';

/**
 * #158: addToOutstandingBuffer starts a 500ms poll before the connect Promise
 * resolves. poll() must not call sendNextOutstanding with an uninitialized socket.
 */
describe('StandardResilienceHandler poll before connect', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('does not poll/send while the WebSocket is undefined and flushes after it opens', async () => {
    const executeCommand = jest.fn();
    const socket: WebSocketImplementation = {
      executeCommand,
      close: jest.fn()
    };

    let resolveSocket: (ws: WebSocketImplementation) => void = () => undefined;
    const pending = new Promise<WebSocketImplementation>(resolve => {
      resolveSocket = resolve;
    });
    const builder: WebSocketImplementationBuilderHandler = {
      withHandler: () => pending
    };
    const requestHandler: RequestHandler = {
      handleInput: jest.fn(),
      handleMessage: jest.fn(),
      handleError: jest.fn()
    };

    const handler = new StandardResilienceHandler(
      builder,
      new NoopWebSocketStateHandler(),
      requestHandler,
      10
    );

    const request = new DefaultDittoProtocolEnvelope(
      'org.eclipse.ditto/thing/things/twin/commands/retrieve',
      { 'correlation-id': 'corr-1' },
      '/',
      {}
    );

    expect(() => handler.sendRequest('corr-1', request)).not.toThrow();
    expect(executeCommand).not.toHaveBeenCalled();

    expect(() => {
      jest.advanceTimersByTime(500);
    }).not.toThrow();
    expect(executeCommand).not.toHaveBeenCalled();

    expect(() => {
      jest.advanceTimersByTime(1000);
    }).not.toThrow();
    expect(executeCommand).not.toHaveBeenCalled();

    resolveSocket(socket);
    await jest.advanceTimersByTimeAsync(0);

    expect(executeCommand).toHaveBeenCalledTimes(1);
    expect(String(executeCommand.mock.calls[0][0])).toContain('corr-1');
  });

  function setup() {
    let resolveSocket!: (socket: WebSocketImplementation) => void;
    const pending = new Promise<WebSocketImplementation>(resolve => { resolveSocket = resolve; });
    const state = new NoopWebSocketStateHandler();
    const connected = jest.spyOn(state, 'connected');
    const handler = new StandardResilienceHandler(
      { withHandler: () => pending }, state,
      { handleInput: jest.fn(), handleMessage: jest.fn(), handleError: jest.fn() }, 10
    );
    const socket = { executeCommand: jest.fn(), close: jest.fn() };
    return { handler, socket, resolveSocket, connected };
  }

  it('flushes a message buffered before the initial connection opens', async () => {
    const { handler, socket, resolveSocket } = setup();
    const sent = handler.send('buffered message');
    expect(socket.executeCommand).not.toHaveBeenCalled();
    resolveSocket(socket);
    await sent;
    expect(socket.executeCommand).toHaveBeenCalledWith('buffered message');
  });

  it('closes a socket arriving after close without sending buffered messages', async () => {
    const { handler, socket, resolveSocket, connected } = setup();
    const sent = handler.send('do not send');
    const rejected = expect(sent).rejects.toMatchObject({ error: 'connection.lost' });
    handler.close(1000, 'shutdown');
    await rejected;
    resolveSocket(socket);
    await jest.advanceTimersByTimeAsync(0);
    expect(socket.close).toHaveBeenCalledWith(1000, 'shutdown');
    expect(socket.executeCommand).not.toHaveBeenCalled();
    expect(connected).not.toHaveBeenCalled();
    await expect(handler.send('after close')).rejects.toMatchObject({ error: 'connection.lost' });
  });

  it('cancels the underlying reconnect loop and closes a late replacement', async () => {
    const { handler, socket, resolveSocket, connected } = setup();
    resolveSocket(socket);
    await jest.advanceTimersByTimeAsync(0);
    connected.mockClear();
    let reconnect!: (socket: WebSocketImplementation) => void;
    handler.handleClose(new Promise(resolve => { reconnect = resolve; }));
    handler.close(1000, 'shutdown');
    expect(socket.close).toHaveBeenCalledWith(1000, 'shutdown');
    const replacement = { executeCommand: jest.fn(), close: jest.fn() };
    reconnect(replacement);
    await jest.advanceTimersByTimeAsync(0);
    expect(replacement.close).toHaveBeenCalledWith(1000, 'shutdown');
    expect(connected).not.toHaveBeenCalled();
    expect(replacement.executeCommand).not.toHaveBeenCalled();
  });

  it('keeps reconnect requests off the stale socket until the replacement opens', async () => {
    const { handler, socket, resolveSocket } = setup();
    resolveSocket(socket);
    await jest.advanceTimersByTimeAsync(0);
    let reconnect!: (socket: WebSocketImplementation) => void;
    handler.handleClose(new Promise(resolve => { reconnect = resolve; }));
    const request = new DefaultDittoProtocolEnvelope(
      'org.eclipse.ditto/thing/things/twin/commands/retrieve', { 'correlation-id': 'reconnect' }, '/', {}
    );
    handler.sendRequest('reconnect', request);
    await jest.advanceTimersByTimeAsync(1000);
    expect(socket.executeCommand).not.toHaveBeenCalled();
    const replacement = { executeCommand: jest.fn(), close: jest.fn() };
    reconnect(replacement);
    await jest.advanceTimersByTimeAsync(0);
    expect(replacement.executeCommand).toHaveBeenCalledTimes(1);
  });
});
