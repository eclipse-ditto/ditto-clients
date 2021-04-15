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

import { HttpHelper as H } from './http.helper';

describe('Http Messages Handle', () => {
  const handle = H.thingsClient.getMessagesHandle();
  const errorHandle = H.errorThingsClient.getMessagesHandle();
  const baseRequest = `things/${H.thing.thingId}`;
  const messageSubject = 'Subject!';
  const message = 'Hello :)';

  it('claims a Thing', () => {
    return H.test({
      toTest: () => handle.claim(H.thing.thingId, message),
      testBody: { content: 'Information' },
      expected: { status: 200, headers: undefined as Map<string, string>, body: { content: 'Information' } },
      request: `${baseRequest}/inbox/claim`,
      method: 'post',
      status: 200,
      payload: JSON.stringify(message)
    });
  });

  it('sends a Message to a Thing', () => {
    return H.test({
      toTest: () => handle.messageToThing(H.thing.thingId, messageSubject, message),
      request: `${baseRequest}/inbox/messages/${messageSubject}`,
      method: 'post',
      status: 202,
      payload: JSON.stringify(message)
    });
  });

  it('sends a Message from a Thing', () => {
    return H.test({
      toTest: () => handle.messageFromThing(H.thing.thingId, messageSubject, message),
      request: `${baseRequest}/outbox/messages/${messageSubject}`,
      method: 'post',
      status: 202,
      payload: JSON.stringify(message)
    });
  });

  it('sends a Message to a Feature', () => {
    return H.test({
      toTest: () => handle.messageToFeature(H.thing.thingId, H.feature.id, messageSubject, message),
      request: `${baseRequest}/features/${H.feature.id}/inbox/messages/${messageSubject}`,
      method: 'post',
      status: 202,
      payload: JSON.stringify(message)
    });
  });

  it('sends a Message from a Feature', () => {
    return H.test({
      toTest: () => handle.messageFromFeature(H.thing.thingId, H.feature.id, messageSubject, message),
      request: `${baseRequest}/features/${H.feature.id}/outbox/messages/${messageSubject}`,
      method: 'post',
      status: 202,
      payload: JSON.stringify(message)
    });
  });

  it('returns a claim error message', () => {
    return H.testError(() => errorHandle.claim(H.thing.thingId, message));
  });

  it('returns a send message to thing error message', () => {
    return H.testError(() => errorHandle.messageToThing(H.thing.thingId, messageSubject, message));
  });

  it('returns a send message from thing error message', () => {
    return H.testError(() => errorHandle.messageFromThing(H.thing.thingId, messageSubject, message));
  });

  it('returns a send message to feature error message', () => {
    return H.testError(() => errorHandle.messageToFeature(H.thing.thingId, H.feature.id, messageSubject, message));
  });

  it('returns a send message from feature error message', () => {
    return H.testError(() => errorHandle.messageFromFeature(H.thing.thingId, H.feature.id, messageSubject, message));
  });
});
