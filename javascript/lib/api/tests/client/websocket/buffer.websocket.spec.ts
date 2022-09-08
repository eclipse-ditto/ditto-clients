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

import { jest } from '@jest/globals';
import { DittoWebSocketLiveClient } from '../../../src/client/ditto-client-websocket';
import { bufferFullError } from '../../../src/client/request-factory/resilience/websocket-resilience-interfaces';
import { MockWebSocketStates, MockWebSocketStateTracker, WebSocketHelper as H } from './websocket.helper';
import { DefaultMockWebSocket } from './websocket.mock';

jasmine.DEFAULT_TIMEOUT_INTERVAL = 5000;

describe('WebSocket Resilience Handler with buffer', () => {
  jest.setTimeout(5000); // we need to tell jest, that it should also wait on promises ... default is 0 ms
  const topic = `${H.splitNamespace}/${H.splitThingId}/things/live/commands/retrieve`;
  const pressureBody = {
    status: 429,
    error: 'things:thing.toomanymodifyingrequests',
    message: 'Too many modifying requests are already outstanding to the Thing with ID \'Testspace:Testthing\'.',
    description: 'Throttle your modifying requests to the Thing ' +
      'or re-structure your Thing in multiple Things if you really need so many concurrent modifications.'
  };
  const thingRequest = H.buildRequest({ topic });
  const messageSubject = 'A SUBJECT';
  const message = 'CONTENT';
  const contentType = 'text/plain';
  const messageRequest = {
    topic: `${H.splitNamespace}/${H.splitThingId}/things/live/messages/${messageSubject}`,
    path: `/inbox/messages/${messageSubject}`,
    headers: Object.assign(H.standardHeaders, { 'content-type': contentType })
  };
  const pressureResponse = H.buildResponse({
    topic,
    status: 429,
    responseBody: pressureBody
  });
  const thingResponse = H.buildResponse({
    topic,
    status: 200,
    responseBody: H.thing.toObject()
  });
  let requester: DefaultMockWebSocket;
  let thingsClient: DittoWebSocketLiveClient;
  let stateTracker: MockWebSocketStateTracker;


  beforeEach(() => {
    requester = new DefaultMockWebSocket();
    stateTracker = new MockWebSocketStateTracker();
    thingsClient = H.buildWebSocketClient(requester)
      .withTls()
      .withDomain(H.url)
      .withAuthProvider(H.basicAuthProvider(H.testName, H.password))
      .withBuffer(H.bufferSize)
      .liveChannel()
      .withStateHandler(stateTracker)
      .build();
  });

  it('buffers requests', async () => {
    const handle = thingsClient.getThingsHandle();
    requester.addResponse(thingRequest, pressureResponse);
    let resolved = false;
    handle.getThing(H.thing.thingId)
      .then(thing => {
        resolved = true;
        expect(thing).toEqual(H.thing);
      });
    // force buffering state with another request while already being in backpressure state
    handle.getThing(H.thing.thingId);
    return new Promise(resolve => {
      setTimeout(async () => {
        expect(resolved).toBe(false);
        requester.addResponse(thingRequest, thingResponse);
        await setTimeout(() => {
          expect(resolved).toBe(true);
          expect(stateTracker.events).toEqual([MockWebSocketStates.CONNECTED, MockWebSocketStates.BACK_PRESSURE,
            MockWebSocketStates.BUFFERING, MockWebSocketStates.CONNECTED]);
          resolve();
        }, 1100);
      }, 2000);
    });
  });

  it('fills the request buffer', async () => {
    // TODO: implement
  });

  it('buffers messages', async () => {
    const handle = thingsClient.getMessagesHandle();
    let resolved = false;
    await handle.requestMessages();
    requester.closeWebSocket(1000);
    await new Promise(resolve => setTimeout(resolve, 1));
    handle.messageToThingWithoutResponse(H.thing.thingId, messageSubject, message, contentType)
      .then(() => {
        resolved = true;
      });
    await new Promise(resolve => {
      setTimeout(async () => {
        expect(resolved).toBe(false);
        requester.addResponse(thingRequest, thingResponse);
        resolve();
      }, 200);
    });
    await new Promise(resolve => {
      setTimeout(() => {
        expect(stateTracker.events).toEqual([MockWebSocketStates.CONNECTED, MockWebSocketStates.RECONNECTING,
          MockWebSocketStates.CONNECTED]);
        expect(resolved).toBe(true);
        resolve();
      }, 1000);
    });
  });

  it('fills the messages buffer', async () => {
    const overflow = 3;
    const handle = thingsClient.getMessagesHandle();
    let resolved = 0;
    let rejected = 0;
    await handle.requestMessages();
    requester.closeWebSocket(1000);
    await new Promise(resolve => setTimeout(resolve, 1));
    let i: number;
    for (i = 0; i < H.bufferSize + overflow; i += 1) {
      handle.messageToThingWithoutResponse(H.thing.thingId, messageSubject, message, contentType)
        .then(() => {
          resolved += 1;
        }).catch(reason => {
          expect(reason).toEqual(bufferFullError);
          rejected += 1;
        });
    }
    await new Promise(resolve => {
      setTimeout(() => {
        requester.addListener(messageRequest, () => {
        });
        expect(rejected).toBe(overflow);
        expect(resolved).toBe(0);
        resolve();
      }, 200);
    });
    await new Promise(resolve => {
      setTimeout(() => {
        expect(rejected).toBe(overflow);
        expect(resolved).toBe(H.bufferSize);
        expect(stateTracker.events).toEqual([MockWebSocketStates.CONNECTED, MockWebSocketStates.RECONNECTING,
          MockWebSocketStates.BUFFER_FULL, MockWebSocketStates.CONNECTED]);
        resolve();
      }, 1000);
    });
  });

});
