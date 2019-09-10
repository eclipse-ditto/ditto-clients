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

import { ProtocolResponseValue } from '../../../src/client/request-factory/websocket-request-handler';
import { EventsHelper as H } from './events.helper';


describe('WebSocket Events Handle', () => {
  jest.setTimeout(5000); // we need to tell jest, that it should also wait on promises ... default is 0 ms
  const action = 'created';
  const topic = `${H.splitNamespace}/${H.splitThingId}/things/live/events/${action}`;
  const handle = H.thingsClient.getEventsHandle();

  beforeEach(async () => {
    await handle.requestEvents();
  });

  it('subscribes to all events', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToAllEvents(callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: '/',
        value: H.thing.toObject()
      }
    });
  });

  it('subscribes to a Thing', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToThing(H.thing.thingId, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: '/',
        value: H.thing.toObject()
      }
    });
  });

  it('subscribes to Attributes', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToAttributes(H.thing.thingId, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: `/attributes`,
        value: H.attributes
      }
    });
  });

  it('subscribes to an Attribute', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToAttribute(H.thing.thingId, H.attributePath, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: `/attributes/${H.attributePath}`,
        value: H.attribute
      }
    });
  });

  it('subscribes to Features', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToFeatures(H.thing.thingId, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: '/features',
        value: H.features.toObject()
      }
    });
  });

  it('subscribes to a Feature', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToFeature(H.thing.thingId, H.feature.id, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: `/features/${H.feature.id}`,
        value: H.feature.toObject()
      }
    });
  });

  it('subscribes to a Definition', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToDefinition(H.thing.thingId, H.feature.id, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: `/features/${H.feature.id}/definition`,
        value: H.definition
      }
    });
  });

  it('subscribes to Properties', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToProperties(H.thing.thingId, H.feature.id, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: `/features/${H.feature.id}/properties`,
        value: H.properties
      }
    });
  });

  it('subscribes to a Property', () => {
    return H.testEvent({
      method: (callback: (message: ProtocolResponseValue) => any) =>
        handle.subscribeToProperty(H.thing.thingId, H.feature.id, H.propertyPath, callback),
      deleteSubscription: (id: string) => handle.deleteSubscription(id),
      message: {
        topic,
        action,
        path: `/features/${H.feature.id}/properties/${H.propertyPath}`,
        value: H.property
      }
    });
  });

  it('turns off events', async () => {
    await handle.requestEvents();
    await handle.stopEvents();
    await handle.stopEvents();
    expect(() => handle.subscribeToAllEvents(() => {})).toThrowError();
  });
});
