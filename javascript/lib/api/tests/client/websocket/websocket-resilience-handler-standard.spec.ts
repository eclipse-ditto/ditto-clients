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
    await Promise.resolve();
    await Promise.resolve();

    expect(executeCommand).toHaveBeenCalledTimes(1);
    expect(String(executeCommand.mock.calls[0][0])).toContain('corr-1');
  });
});
