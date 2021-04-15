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

/* tslint:disable:no-big-function */
import { HttpThingsHandle } from '../../../src/client/handles/things.interfaces';
import { PutResponse } from '../../../src/model/response';
import {
  DefaultFieldsOptions,
  DefaultGetThingsOptions,
  DefaultMatchOptions
} from '../../../src/options/request.options';
import { HttpHelper as H } from './http.helper';

describe('Http Things Handle', () => {
  const baseRequest = `things/${H.thing.thingId}`;
  const handle: HttpThingsHandle = H.thingsClient.getThingsHandle();
  const errorHandle: HttpThingsHandle = H.errorThingsClient.getThingsHandle();

  it('sends options and gets a Thing', () => {
    const options = DefaultFieldsOptions.getInstance().withFields('A', 'B').ifMatch('C').ifNoneMatch('D');
    const headers: Map<string, string> = new Map();
    headers.set('If-Match', 'C');
    headers.set('If-None-Match', 'D');
    return H.test({
      toTest: () => handle.getThing(H.thing.thingId, options),
      testBody: H.thing.toObject(),
      expected: H.thing,
      request: `${baseRequest}?fields=A%2CB`,
      method: 'get',
      status: 200,
      requestHeaders: headers
    });
  });

  it('gets Things', () => {
    return H.test({
      toTest: () => handle.getThings([H.thing.thingId]),
      testBody: [H.thing.toObject()],
      expected: [H.thing],
      request: `things?ids=${encodeURIComponent(H.thing.thingId)}`,
      method: 'get',
      status: 200
    });
  });

  it('gets Things with options', () => {
    const options = DefaultGetThingsOptions.getInstance().withFields('A,B');
    return H.test({
      toTest: () => handle.getThings([H.thing.thingId], options),
      testBody: [H.thing.toObject()],
      expected: [H.thing],
      request: `things?fields=A%2CB&ids=${encodeURIComponent(H.thing.thingId)}`,
      method: 'get',
      status: 200
    });
  });

  it('sends empty options and gets a PolicyId', () => {
    const options = DefaultMatchOptions.getInstance();
    return H.test({
      toTest: () => handle.getPolicyId(H.thing.thingId, options),
      testBody: 'ID',
      expected: 'ID',
      request: `${baseRequest}/policyId`,
      method: 'get',
      status: 200
    });
  });

  it('gets an Attribute', () => {
    return H.test({
      toTest: () => handle.getAttribute(H.thing.thingId, 'anAttribute'),
      testBody: H.attribute,
      expected: H.attribute,
      request: `${baseRequest}/attributes/anAttribute`,
      method: 'get',
      status: 200
    });
  });

  it('gets Attributes', () => {
    return H.test({
      toTest: () => handle.getAttributes(H.thing.thingId),
      testBody: H.attributes,
      expected: H.attributes,
      request: `${baseRequest}/attributes`,
      method: 'get',
      status: 200
    });
  });

  it('gets the definition', () => {
    return H.test({
      toTest: () => handle.getDefinition(H.thing.thingId),
      testBody: 'example:test:definition',
      expected: 'example:test:definition',
      request: `${baseRequest}/definition`,
      method: 'get',
      status: 200
    });
  });

  it('posts a Thing', () => {
    const thing = H.thing.toObject();
    // @ts-ignore
    delete thing.thingId;
    return H.test({
      toTest: () => handle.postThing(thing),
      testBody: H.thing.toObject(),
      expected: H.thing,
      request: 'things',
      method: 'post',
      status: 201,
      payload: JSON.stringify(thing)
    });
  });

  it('puts a new Thing', () => {
    return H.test({
      toTest: () => handle.putThing(H.thing),
      testBody: H.thing.toObject(),
      expected: new PutResponse(H.thing, 201, undefined),
      request: `${baseRequest}`,
      method: 'put',
      status: 201,
      payload: H.thing.toJson()
    });
  });

  it('puts a Thing that already exists', () => {
    return H.test({
      toTest: () => handle.putThing(H.thing),
      testBody: H.thing.toObject(),
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}`,
      method: 'put',
      status: 204,
      payload: H.thing.toJson()
    });
  });

  it('updates a policyId', () => {
    return H.test({
      toTest: () => handle.putPolicyId(H.thing.thingId, 'ID'),
      testBody: 'ID',
      expected: new PutResponse('ID', 201, undefined),
      request: `${baseRequest}/policyId`,
      method: 'put',
      status: 201,
      payload: JSON.stringify('ID')
    });
  });

  it('creates Attributes', () => {
    return H.test({
      toTest: () => handle.putAttributes(H.thing.thingId, H.attributes),
      testBody: H.attributes,
      expected: new PutResponse(H.attributes, 201, undefined),
      request: `${baseRequest}/attributes`,
      method: 'put',
      status: 201,
      payload: JSON.stringify(H.attributes)
    });
  });

  it('updates Attributes', () => {
    return H.test({
      toTest: () => handle.putAttributes(H.thing.thingId, H.attributes),
      testBody: H.attributes,
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}/attributes`,
      method: 'put',
      status: 204,
      payload: JSON.stringify(H.attributes)
    });
  });

  it('creates an Attribute', () => {
    return H.test({
      toTest: () => handle.putAttribute(H.thing.thingId, H.attributePath, H.attribute),
      testBody: H.attribute,
      expected: new PutResponse(H.attribute, 201, undefined),
      request: `${baseRequest}/attributes/${H.attributePath}`,
      method: 'put',
      status: 201,
      payload: JSON.stringify(H.attribute)
    });
  });

  it('updates an Attribute', () => {
    return H.test({
      toTest: () => handle.putAttribute(H.thing.thingId, H.attributePath, H.attribute),
      testBody: H.attribute,
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}/attributes/${H.attributePath}`,
      method: 'put',
      status: 204,
      payload: JSON.stringify(H.attribute)
    });
  });

  it('creates the definition', () => {
    return H.test({
      toTest: () => handle.putDefinition(H.thing.thingId, 'example:test:definition'),
      testBody: 'example:test:definition',
      expected: new PutResponse('example:test:definition', 201, undefined),
      request: `${baseRequest}/definition`,
      method: 'put',
      status: 201,
      payload: JSON.stringify('example:test:definition')
    });
  });

  it('updates the definition', () => {
    return H.test({
      toTest: () => handle.putDefinition(H.thing.thingId, 'example:test:definition'),
      testBody: 'example:test:definition',
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}/definition`,
      method: 'put',
      status: 204,
      payload: JSON.stringify('example:test:definition')
    });
  });

  it('deletes a Thing', () => {
    return H.test({
      toTest: () => handle.deleteThing(H.thing.thingId),
      request: baseRequest,
      method: 'delete',
      status: 204
    });
  });

  it('deletes Attributes', () => {
    return H.test({
      toTest: () => handle.deleteAttributes(H.thing.thingId),
      request: `${baseRequest}/attributes`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes an Attribute', () => {
    return H.test({
      toTest: () => handle.deleteAttribute(H.thing.thingId, H.attributePath),
      request: `${baseRequest}/attributes/${H.attributePath}`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes the definition', () => {
    return H.test({
      toTest: () => handle.deleteDefinition(H.thing.thingId),
      request: `${baseRequest}/definition`,
      method: 'delete',
      status: 204
    });
  });

  it('returns a get things error message', () => {
    return H.testError(() => errorHandle.getThings(['A', 'B']));
  });

  it('returns a get thing error message', () => {
    return H.testError(() => errorHandle.getThing(H.thing.thingId));
  });

  it('returns a get policyid error message', () => {
    return H.testError(() => errorHandle.getPolicyId(H.thing.thingId));
  });

  it('returns a get attribute error message', () => {
    return H.testError(() => errorHandle.getAttribute(H.thing.thingId, H.attributePath));
  });

  it('returns a get attributes error message', () => {
    return H.testError(() => errorHandle.getAttributes(H.thing.thingId));
  });

  it('returns a post thing error message', () => {
    return H.testError(() => errorHandle.postThing({}));
  });

  it('returns a put thing error message', () => {
    return H.testError(() => errorHandle.putThing(H.thing));
  });

  it('returns an update policyid error message', () => {
    return H.testError(() => errorHandle.putPolicyId(H.thing.thingId, 'ID'));
  });

  it('returns an update attributes error message', () => {
    return H.testError(() => errorHandle.putAttributes(H.thing.thingId, H.attributes));
  });

  it('returns an update attribute error message', () => {
    return H.testError(() => errorHandle.putAttribute(H.thing.thingId, H.attributePath, H.attribute));
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
