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
import {
  connectionUnavailableError
} from '../../../src/client/request-factory/resilience/websocket-resilience-interfaces';
import { MockWebSocketStates, MockWebSocketStateTracker, WebSocketHelper as H } from './websocket.helper';
import { DefaultMockWebSocket } from './websocket.mock';

describe('WebSocket Resilience Handler without buffer', () => {
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
  const pressureResponse = H.buildResponse({
    topic,
    status: 429,
    responseBody: pressureBody
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
      .withoutBuffer()
      .liveChannel()
      .withStateHandler(stateTracker)
      .build();
  });

  it('rejects messages for backpressure', async () => {
    const handle = thingsClient.getThingsHandle();
    requester.addResponse(thingRequest, pressureResponse);
    return handle.getThing(H.thing.thingId)
      .then(() => {
        fail('Request with backpressure was successful');
      })
      .catch(error => {
        expect(error).toEqual(pressureBody);
        expect(stateTracker.events).toEqual([MockWebSocketStates.CONNECTED]);
      });
  });

  it('rejects messages while reconnecting', async (done) => {
    const handle = thingsClient.getMessagesHandle();
    requester.closeWebSocket(1000);
    handle.messageToThingWithoutResponse(H.thing.thingId, messageSubject, message, contentType)
      .then(() => {
        fail('Request while reconnecting was successful');
      })
      .catch(error => {
        expect(error).toEqual(connectionUnavailableError);
      });

    await new Promise(resolve => setTimeout(resolve, 1100));
    handle.messageToThingWithoutResponse(H.thing.thingId, messageSubject, message, contentType)
      .finally(() => {
        expect(stateTracker.events).toEqual([MockWebSocketStates.CONNECTED, MockWebSocketStates.RECONNECTING, MockWebSocketStates.CONNECTED]);
        done();
      });

  });

})
;
