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
import { HttpThingsHandleV1, HttpThingsHandleV2 } from '../../../src/client/handles/things.interfaces';
import { PutResponse } from '../../../src/model/response';
import { Acl, AclEntry } from '../../../src/model/things.model';
import {
  DefaultFieldsOptions,
  DefaultGetThingsOptions,
  DefaultMatchOptions
} from '../../../src/options/request.options';
import { HttpHelper as H } from './http.helper';

describe('Http Things Handle', () => {
  const baseRequest = `things/${H.thing.thingId}`;
  const handleV2: HttpThingsHandleV2 = H.thingsClientV2.getThingsHandle();
  const errorHandleV2: HttpThingsHandleV2 = H.errorThingsClientV2.getThingsHandle();
  const handleV1: HttpThingsHandleV1 = H.thingsClientV1.getThingsHandle();
  const errorHandleV1: HttpThingsHandleV1 = H.errorThingsClientV1.getThingsHandle();
  const authorizationSubject = 'Id';
  const anAclEntry = new AclEntry(authorizationSubject, true, true, true);
  const anotherAclEntry = new AclEntry('Test', false, false, false);
  const acl = { [anAclEntry.id]: anAclEntry, [anotherAclEntry.id]: anotherAclEntry };

  it('sends options and gets a Thing', () => {
    const options = DefaultFieldsOptions.getInstance().withFields('A', 'B').ifMatch('C').ifNoneMatch('D');
    const headers: Map<string, string> = new Map();
    headers.set('If-Match', 'C');
    headers.set('If-None-Match', 'D');
    return H.test({
      toTest: () => handleV2.getThing(H.thing.thingId, options),
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
      toTest: () => handleV2.getThings([H.thing.thingId]),
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
      toTest: () => handleV2.getThings([H.thing.thingId], options),
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
      toTest: () => handleV2.getPolicyId(H.thing.thingId, options),
      testBody: 'ID',
      expected: 'ID',
      request: `${baseRequest}/policyId`,
      method: 'get',
      status: 200
    });
  });

  it('gets an Attribute', () => {
    return H.test({
      toTest: () => handleV2.getAttribute(H.thing.thingId, 'anAttribute'),
      testBody: H.attribute,
      expected: H.attribute,
      request: `${baseRequest}/attributes/anAttribute`,
      method: 'get',
      status: 200
    });
  });

  it('gets Attributes', () => {
    return H.test({
      toTest: () => handleV2.getAttributes(H.thing.thingId),
      testBody: H.attributes,
      expected: H.attributes,
      request: `${baseRequest}/attributes`,
      method: 'get',
      status: 200
    });
  });

  it('gets an Acl', () => {
    return H.test({
      toTest: () => handleV1.getAcl(H.thing.thingId),
      testBody: Acl.toObject(acl),
      expected: acl,
      request: `${baseRequest}/acl`,
      method: 'get',
      status: 200,
      api: 1
    });
  });

  it('gets an AclEntry', () => {
    return H.test({
      toTest: () => handleV1.getAclEntry(H.thing.thingId, authorizationSubject),
      testBody: anAclEntry.toObject(),
      expected: anAclEntry,
      request: `${baseRequest}/acl/${authorizationSubject}`,
      method: 'get',
      status: 200,
      api: 1
    });
  });

  it('posts a Thing', () => {
    const thing = H.thing.toObject();
    // @ts-ignore
    delete thing.thingId;
    return H.test({
      toTest: () => handleV2.postThing(thing),
      testBody: H.thing.toObject(),
      expected: H.thing,
      request: 'things',
      method: 'post',
      status: 201,
      payload: JSON.stringify(thing)
    });
  });

  it('puts a Thing', () => {
    return H.test({
      toTest: () => handleV2.putThing(H.thing),
      testBody: H.thing.toObject(),
      expected: new PutResponse(H.thing, 201, undefined),
      request: `${baseRequest}`,
      method: 'put',
      status: 201,
      payload: H.thing.toJson()
    });
  });

  it('updates a policyId', () => {
    return H.test({
      toTest: () => handleV2.putPolicyId(H.thing.thingId, 'ID'),
      testBody: 'ID',
      expected: new PutResponse('ID', 201, undefined),
      request: `${baseRequest}/policyId`,
      method: 'put',
      status: 201,
      payload: JSON.stringify('ID')
    });
  });

  it('updates an Attribute', () => {
    return H.test({
      toTest: () => handleV2.putAttributes(H.thing.thingId, H.attributes),
      testBody: H.attributes,
      expected: new PutResponse(H.attributes, 201, undefined),
      request: `${baseRequest}/attributes`,
      method: 'put',
      status: 201,
      payload: JSON.stringify(H.attributes)
    });
  });

  it('updates an Attribute', () => {
    return H.test({
      toTest: () => handleV2.putAttribute(H.thing.thingId, H.attributePath, H.attribute),
      testBody: H.attribute,
      expected: { status: 201, headers: undefined, body: H.attribute },
      request: `${baseRequest}/attributes/${H.attributePath}`,
      method: 'put',
      status: 201,
      payload: JSON.stringify(H.attribute)
    });
  });

  it('updates an Acl', () => {
    return H.test({
      toTest: () => handleV1.putAcl(H.thing.thingId, acl),
      request: `${baseRequest}/acl`,
      method: 'put',
      status: 204,
      payload: Acl.toJson(acl),
      api: 1
    });
  });

  it('updates an AclEntry', () => {
    return H.test({
      toTest: () => handleV1.putAclEntry(H.thing.thingId, anAclEntry),
      testBody: anAclEntry.toObject(),
      expected: new PutResponse(anAclEntry, 201, undefined),
      request: `${baseRequest}/acl/${authorizationSubject}`,
      method: 'put',
      status: 201,
      payload: anAclEntry.toJson(),
      api: 1
    });
  });

  it('deletes a Thing', () => {
    return H.test({
      toTest: () => handleV2.deleteThing(H.thing.thingId),
      request: baseRequest,
      method: 'delete',
      status: 204
    });
  });

  it('deletes Attributes', () => {
    return H.test({
      toTest: () => handleV2.deleteAttributes(H.thing.thingId),
      request: `${baseRequest}/attributes`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes an AclEntry', () => {
    return H.test({
      toTest: () => handleV1.deleteAclEntry(H.thing.thingId, authorizationSubject),
      request: `${baseRequest}/acl/${authorizationSubject}`,
      method: 'delete',
      status: 204,
      api: 1
    });
  });

  it('deletes an Attribute', () => {
    return H.test({
      toTest: () => handleV2.deleteAttribute(H.thing.thingId, H.attributePath),
      request: `${baseRequest}/attributes/${H.attributePath}`,
      method: 'delete',
      status: 204
    });
  });

  it('returns a get things error message', () => {
    return H.testError(() => errorHandleV2.getThings(['A', 'B']));
  });

  it('returns a get thing error message', () => {
    return H.testError(() => errorHandleV2.getThing(H.thing.thingId));
  });

  it('returns a get policyid error message', () => {
    return H.testError(() => errorHandleV2.getPolicyId(H.thing.thingId));
  });

  it('returns a get attribute error message', () => {
    return H.testError(() => errorHandleV2.getAttribute(H.thing.thingId, H.attributePath));
  });

  it('returns a get attributes error message', () => {
    return H.testError(() => errorHandleV2.getAttributes(H.thing.thingId));
  });

  it('returns a get acl error message', () => {
    return H.testError(() => errorHandleV1.getAcl(H.thing.thingId));
  });

  it('returns a get aclentry error message', () => {
    return H.testError(() => errorHandleV1.getAclEntry(H.thing.thingId, authorizationSubject));
  });

  it('returns a post thing error message', () => {
    return H.testError(() => errorHandleV2.postThing({}));
  });

  it('returns a put thing error message', () => {
    return H.testError(() => errorHandleV2.putThing(H.thing));
  });

  it('returns an update policyid error message', () => {
    return H.testError(() => errorHandleV2.putPolicyId(H.thing.thingId, 'ID'));
  });

  it('returns an update attributes error message', () => {
    return H.testError(() => errorHandleV2.putAttributes(H.thing.thingId, H.attributes));
  });

  it('returns an update attribute error message', () => {
    return H.testError(() => errorHandleV2.putAttribute(H.thing.thingId, H.attributePath, H.attribute));
  });

  it('returns an update acl error message', () => {
    return H.testError(() => errorHandleV1.putAcl(H.thing.thingId, acl));
  });

  it('returns an update aclentry error message', () => {
    return H.testError(() => errorHandleV1.putAclEntry(H.thing.thingId, anAclEntry));
  });

  it('returns a delete thing error message', () => {
    return H.testError(() => errorHandleV2.deleteThing(H.thing.thingId));
  });

  it('returns a delete attributes error message', () => {
    return H.testError(() => errorHandleV2.deleteAttributes(H.thing.thingId));
  });

  it('returns a delete attribute error message', () => {
    return H.testError(() => errorHandleV2.deleteAttribute(H.thing.thingId, H.attributePath));
  });

  it('returns a delete aclentry error message', () => {
    return H.testError(() => errorHandleV1.deleteAclEntry(H.thing.thingId, authorizationSubject));
  });
});
