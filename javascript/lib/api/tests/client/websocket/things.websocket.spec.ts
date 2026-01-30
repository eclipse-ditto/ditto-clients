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

/* tslint:disable:no-duplicate-string */
import { ContentType } from '../../../src/client/constants/content-type';
import { DittoAction } from '../../../src/client/constants/ditto-actions';
import { PutResponse } from '../../../src/model/response';
import {  DefaultFieldsOptions } from '../../../src/options/request.options';
import { WebSocketHelper as H } from './websocket.helper';

describe('WebSocket Things Handle', () => {
  const handle = H.thingsClient.getThingsHandle();
  const errorHandle = H.errorClient.getThingsHandle();
  const baseTopic = `${H.splitNamespace}/${H.splitThingId}/things/live/commands`;

  it('retrieves a Thing', () => {
    return H.test({
      toTest: () => handle.getThing(H.thing.thingId, DefaultFieldsOptions.getInstance().ifMatch('a')),
      topic: `${baseTopic}/${DittoAction.RETRIEVE}`,
      status: 200,
      requestHeaders: Object.assign(H.standardHeaders, { 'If-Match': 'a' }),
      responseBody: H.thing.toObject(),
      expected: H.thing
    });
  });

  it('retrieves an Attribute', () => {
    return H.test({
      toTest: () => handle.getAttribute(H.thing.thingId, H.attributePath),
      topic: `${baseTopic}/${DittoAction.RETRIEVE}`,
      path: `/attributes/${H.attributePath}`,
      status: 200,
      responseBody: H.attribute,
      expected: H.attribute
    });
  });

  it('retrieves Attributes', () => {
    return H.test({
      toTest: () => handle.getAttributes(H.thing.thingId),
      topic: `${baseTopic}/${DittoAction.RETRIEVE}`,
      path: '/attributes',
      status: 200,
      responseBody: H.attributes,
      expected: H.attributes
    });
  });

  it('creates a Thing', () => {
    return H.test({
      toTest: () => handle.createThing(H.thing),
      topic: `${baseTopic}/${DittoAction.CREATE}`,
      status: 201,
      requestBody: H.thing.toObject(),
      responseBody: H.thing.toObject(),
      responseHeaders: {},
      expected: new PutResponse(H.thing, 201, new Map())
    });
  });

  it('modifies a Thing', () => {
    return H.test({
      toTest: () => handle.putThing(H.thing),
      topic: `${baseTopic}/${DittoAction.MODIFY}`,
      status: 204,
      requestBody: H.thing.toObject()
    });
  });

  it('merges a Thing', () => {
    return H.test({
      toTest: () => handle.patchThing(H.thing),
      topic: `${baseTopic}/${DittoAction.MERGE}`,
      status: 204,
      requestBody: H.thing.toObject(),
      requestHeaders: { "Content-Type": ContentType.MERGE_PATCH_JSON, "version": 2 },
    });
  });

  it('modifies an Attribute', () => {
    return H.test({
      toTest: () => handle.putAttribute(H.thing.thingId, H.attributePath, H.attribute),
      topic: `${baseTopic}/${DittoAction.MODIFY}`,
      path: `/attributes/${H.attributePath}`,
      status: 204,
      requestBody: H.attribute
    });
  });

  it('merges an Attribute', () => {
    return H.test({
      toTest: () => handle.patchAttribute(H.thing.thingId, H.attributePath, H.attribute),
      topic: `${baseTopic}/${DittoAction.MERGE}`,
      path: `/attributes/${H.attributePath}`,
      status: 204,
      requestBody: H.attribute,
      requestHeaders: { "Content-Type": ContentType.MERGE_PATCH_JSON, "version": 2 },
    });
  });

  it('modifies Attributes', () => {
    return H.test({
      toTest: () => handle.putAttributes(H.thing.thingId, H.attributes),
      topic: `${baseTopic}/${DittoAction.MODIFY}`,
      path: '/attributes',
      status: 204,
      requestBody: H.attributes
    });
  });

  it('merges Attributes', () => {
    return H.test({
      toTest: () => handle.patchAttributes(H.thing.thingId, H.attributes),
      topic: `${baseTopic}/${DittoAction.MERGE}`,
      path: '/attributes',
      status: 204,
      requestBody: H.attributes,
      requestHeaders: { "Content-Type": ContentType.MERGE_PATCH_JSON, "version": 2 },
    });
  });

  it('deletes a Thing', () => {
    return H.test({
      toTest: () => handle.deleteThing(H.thing.thingId),
      topic: `${baseTopic}/delete`,
      status: 204
    });
  });

  it('deletes Attributes', () => {
    return H.test({
      toTest: () => handle.deleteAttributes(H.thing.thingId),
      topic: `${baseTopic}/delete`,
      path: '/attributes',
      status: 204
    });
  });

  it('deletes an Attribute', () => {
    return H.test({
      toTest: () => handle.deleteAttribute(H.thing.thingId, H.attributePath),
      topic: `${baseTopic}/delete`,
      path: `/attributes/${H.attributePath}`,
      status: 204
    });
  });

  it('returns a retrieve thing error message', () => {
    return H.testError(() => errorHandle.getThing(H.thing.thingId));
  });

  it('returns a retrieve attribute error message', () => {
    return H.testError(() => errorHandle.getAttribute(H.thing.thingId, H.attributePath));
  });

  it('returns a retrieve attributes error message', () => {
    return H.testError(() => errorHandle.getAttributes(H.thing.thingId));
  });

  it('returns a create thing error message', () => {
    return H.testError(() => errorHandle.createThing(H.thing));
  });

  it('returns a modify thing error message', () => {
    return H.testError(() => errorHandle.putThing(H.thing));
  });

  it('returns a merge thing error message', () => {
    return H.testError(() => errorHandle.patchThing(H.thing));
  });

  it('returns a modify attributes error message', () => {
    return H.testError(() => errorHandle.putAttributes(H.thing.thingId, H.attributes));
  });

  it('returns a merge attributes error message', () => {
    return H.testError(() => errorHandle.patchAttributes(H.thing.thingId, H.attributes));
  });

  it('returns a modify attribute error message', () => {
    return H.testError(() => errorHandle.putAttribute(H.thing.thingId, H.attributePath, H.attribute));
  });

  it('returns a merge attribute error message', () => {
    return H.testError(() => errorHandle.patchAttribute(H.thing.thingId, H.attributePath, H.attribute));
  });

  it('returns a delete thing error message', () => {
    return H.testError(() => errorHandle.deleteThing(H.thing.thingId));
  });

  it('returns a delete attributes error message', () => {
    return H.testError(() => errorHandle.deleteAttributes(H.thing.thingId));
  });

  it('returns a delete attribute error message', () => {
    return H.testError(() => errorHandle.deleteAttribute(H.thing.thingId, H.attributePath));
  });
});
