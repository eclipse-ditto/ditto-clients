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
/* tslint:disable:no-duplicate-string */
import { ProtocolResponseValue } from '../../../src/client/request-factory/websocket-request-handler';
import { GenericResponse } from '../../../src/model/response';
import { EventsHelper as H } from './events.helper';

const testWithoutResponse: (method: () => void, request: Request) => Promise<void> = (method: () => void, request: Request) => {
  return new Promise<void>(resolve => {
    H.requester.addListener(request, resolve);
    method();
  });
};

describe('WebSocket Messages Handle', () => {
  jest.setTimeout(5000); // we need to tell jest, that it should also wait on promises ... default is 0 ms
  const handle = H.thingsClient.getMessagesHandle();
  const type = 'text/plain';
  const message = 'A Message!';
  const subject = 'dosomething';
  const baseTopic = `${H.splitNamespace}/${H.splitThingId}/things/live/messages`;
  const standardTopic = `${baseTopic}/${subject}`;
  const standardHeaders = () => {
    return Object.assign(H.standardHeaders, { 'content-type': 'text/plain' });
  };
  const standardHeadersMap: () => Map<string, string> = () => {
    const headers = standardHeaders();
    const headersMap = new Map();
    Object.keys(headers).forEach(k => headersMap.set(k, headers[k]));
    return headersMap;
  };
  const standardResponseMessage = 'Got it!';
  const standardResponse: GenericResponse = {
    status: 200,
    headers: standardHeadersMap(),
    body: standardResponseMessage
  };


  it('sends a claim', () => {
    return testWithoutResponse(() => handle.claimWithoutResponse(H.thing.thingId, message), {
      topic: `${baseTopic}/claim`,
      path: `/inbox/messages/claim`,
      value: message,
      headers: standardHeaders()
    });
  });

  it('sends a claim and gets a TestResponse', () => {
    return H.test({
      toTest: () => handle.claim(H.thing.thingId, message),
      topic: `${baseTopic}/claim`,
      path: `/inbox/messages/claim`,
      status: 204,
      requestBody: message,
      requestHeaders: standardHeaders()
    });
  });

  it('sends a Message from a Thing', () => {
    return testWithoutResponse(() => handle.messageFromThingWithoutResponse(H.thing.thingId, subject, message, type), {
      topic: standardTopic,
      path: `/outbox/messages/${subject}`,
      value: message,
      headers: standardHeaders()
    });
  });

  it('sends a Message from a Thing and gets a TestResponse', () => {
    return H.test({
      toTest: () => handle.messageFromThing(H.thing.thingId, subject, message, type),
      topic: standardTopic,
      path: `/outbox/messages/${subject}`,
      status: 200,
      requestBody: message,
      responseBody: standardResponseMessage,
      expected: standardResponse,
      requestHeaders: standardHeaders(),
      responseHeaders: standardHeaders()
    });
  });

  it('sends a message to a Thing', () => {
    return testWithoutResponse(() => handle.messageToThingWithoutResponse(H.thing.thingId, subject, message, type), {
      topic: standardTopic,
      path: `/inbox/messages/${subject}`,
      value: message,
      headers: standardHeaders()
    });
  });

  it('sends a Message to a Thing and gets a TestResponse', () => {
    return H.test({
      toTest: () => handle.messageToThing(H.thing.thingId, subject, message, type),
      topic: standardTopic,
      path: `/inbox/messages/${subject}`,
      status: 200,
      requestBody: message,
      responseBody: standardResponseMessage,
      expected: standardResponse,
      requestHeaders: standardHeaders(),
      responseHeaders: standardHeaders()
    });
  });

  it('sends a Message from a Feature', () => {
    return testWithoutResponse(() => handle.messageFromFeatureWithoutResponse(H.thing.thingId, H.feature.id, subject, message, type), {
      topic: standardTopic,
      path: `/features/${H.feature.id}/outbox/messages/${subject}`,
      value: message,
      headers: standardHeaders()
    });
  });

  it('sends a Message from a Feature and gets a TestResponse', () => {
    return H.test({
      toTest: () => handle.messageFromFeature(H.thing.thingId, H.feature.id, subject, message, type),
      topic: standardTopic,
      path: `/features/${H.feature.id}/outbox/messages/${subject}`,
      status: 200,
      requestBody: message,
      responseBody: standardResponseMessage,
      expected: standardResponse,
      requestHeaders: standardHeaders(),
      responseHeaders: standardHeaders()
    });
  });

  it('sends a Message to a Feature', () => {
    return testWithoutResponse(() => handle.messageToFeatureWithoutResponse(H.thing.thingId, H.feature.id, subject, message, type), {
      topic: standardTopic,
      path: `/features/${H.feature.id}/inbox/messages/${subject}`,
      value: message,
      headers: standardHeaders()
    });
  });

  it('sends a Message to a Feature and gets a TestResponse', () => {
    return H.test({
      toTest: () => handle.messageToFeature(H.thing.thingId, H.feature.id, subject, message, type),
      topic: standardTopic,
      path: `/features/${H.feature.id}/inbox/messages/${subject}`,
      status: 200,
      requestBody: message,
      responseBody: standardResponseMessage,
      expected: standardResponse,
      requestHeaders: standardHeaders(),
      responseHeaders: standardHeaders()
    });
  });

  it('subscribes to a Thing', async () => {
    await handle.requestMessages();
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToThing(H.thing.thingId, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic: standardTopic,
        action: subject,
        path: '/',
        headers: {},
        value: H.thing.toObject()
      }
    });
  });

  it('subscribes to a Feature', async () => {
    await handle.requestMessages();
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToFeature(H.thing.thingId, H.feature.id, callback, undefined, 'outbox'),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic: standardTopic,
        action: subject,
        path: `/features/${H.feature.id}/outbox`,
        headers: {},
        value: H.thing.toObject()
      }
    });
  });

  it('subscribes to all Messages', async  () => {
    await handle.requestMessages();
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToAllMessages(callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic: standardTopic,
        action: subject,
        path: '/',
        headers: {},
        value: H.thing.toObject()
      }
    });
  });

  it('stops sending messages', async () => {
    await handle.requestMessages();
    await handle.stopMessages();
    await handle.stopMessages();
    expect(() => handle.subscribeToAllMessages(() => {})).toThrowError();
  });
});

interface Request {
  topic: string;
  path: string;
  headers: object;
  value?: any;
}
