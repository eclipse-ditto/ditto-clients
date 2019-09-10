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
import {
  AccessRight, DittoSubjectIssuer,
  Entries,
  Entry,
  Policy,
  Resource,
  Resources,
  Subject, SubjectId,
  Subjects,
  SubjectType
} from '../../../src/model/policies.model';
import { PutResponse } from '../../../src/model/response';
import { HttpHelper as H } from './http.helper';

describe('Http Policies Handle', () => {
  const handle = H.thingsClientV2.getPoliciesHandle();
  const errorHandle = H.errorThingsClientV2.getPoliciesHandle();
  const resourcePath = 'aResource';
  const aResource = new Resource(resourcePath, [AccessRight.Read], [AccessRight.Write]);
  const anotherResource = new Resource('anotherResource', [AccessRight.Write], [AccessRight.Write]);
  const resources = new Resources({ aResource, anotherResource });
  const subjectId = 'nginx:aSubject';
  const aSubject = new Subject(SubjectId.fromString(subjectId), 'my default nginx user');
  const anotherSubject = new Subject(SubjectId.fromString('anotherSubject'), 'my other nginx user');
  const subjects = new Subjects({ [aSubject.id]: aSubject, [anotherSubject.id]: anotherSubject });
  const label = 'anEntry';
  const anEntry = new Entry(label, subjects, resources);
  const anotherEntry = new Entry('anotherEntry', subjects, resources);
  const entries = new Entries({ anEntry, anotherEntry });
  const policyId = 'Testspace:Testpolicy';
  const policy = new Policy(policyId, entries);
  const baseRequest = `policies/${policy.id}`;


  it('gets a Policy', () => {
    return H.test({
      toTest: () => handle.getPolicy(policy.id),
      testBody: policy.toObject(),
      expected: policy,
      request: baseRequest,
      method: 'get',
      status: 200
    });
  });

  it('gets Entries', () => {
    return H.test({
      toTest: () => handle.getEntries(policy.id),
      testBody: entries.toObject(),
      expected: entries,
      request: `${baseRequest}/entries`,
      method: 'get',
      status: 200
    });
  });

  it('gets an Entry', () => {
    return H.test({
      toTest: () => handle.getEntry(policy.id, label),
      testBody: anEntry.toObject(),
      expected: anEntry,
      request: `${baseRequest}/entries/${label}`,
      method: 'get',
      status: 200
    });
  });

  it('gets Subjects', () => {
    return H.test({
      toTest: () => handle.getSubjects(policy.id, label),
      testBody: subjects.toObject(),
      expected: subjects,
      request: `${baseRequest}/entries/${label}/subjects`,
      method: 'get',
      status: 200
    });
  });

  it('gets a Subject', () => {
    return H.test({
      toTest: () => handle.getSubject(policy.id, label, subjectId),
      testBody: aSubject.toObject(),
      expected: aSubject,
      request: `${baseRequest}/entries/${label}/subjects/${subjectId}`,
      method: 'get',
      status: 200
    });
  });

  it('gets Resources', () => {
    return H.test({
      toTest: () => handle.getResources(policy.id, label),
      testBody: resources.toObject(),
      expected: resources,
      request: `${baseRequest}/entries/${label}/resources`,
      method: 'get',
      status: 200
    });
  });

  it('gets a Resource', () => {
    return H.test({
      toTest: () => handle.getResource(policy.id, label, resourcePath),
      testBody: aResource.toObject(),
      expected: aResource,
      request: `${baseRequest}/entries/${label}/resources/${resourcePath}`,
      method: 'get',
      status: 200
    });
  });

  it('puts a Policy', () => {
    return H.test({
      toTest: () => handle.putPolicy(policy),
      testBody: policy.toObject(),
      expected: new PutResponse(policy, 201, undefined),
      request: baseRequest,
      method: 'put',
      status: 201,
      payload: policy.toJson()
    });
  });

  it('updates Entries', () => {
    return H.test({
      toTest: () => handle.putEntries(policy.id, entries),
      testBody: entries.toObject(),
      expected: new PutResponse(entries, 201, undefined),
      request: `${baseRequest}/entries`,
      method: 'put',
      status: 201,
      payload: entries.toJson()
    });
  });

  it('updates an Entry', () => {
    return H.test({
      toTest: () => handle.putEntry(policy.id, anEntry),
      testBody: anEntry.toObject(),
      expected: new PutResponse(anEntry, 201, undefined),
      request: `${baseRequest}/entries/${label}`,
      method: 'put',
      status: 201,
      payload: anEntry.toJson()
    });
  });

  it('updates Subjects', () => {
    return H.test({
      toTest: () => handle.putSubjects(policy.id, label, subjects),
      testBody: subjects.toObject(),
      expected: new PutResponse(subjects, 201, undefined),
      request: `${baseRequest}/entries/${label}/subjects`,
      method: 'put',
      status: 201,
      payload: subjects.toJson()
    });
  });

  it('updates a Subject', () => {
    return H.test({
      toTest: () => handle.putSubject(policy.id, label, aSubject),
      testBody: aSubject.toObject(),
      expected: new PutResponse(aSubject, 201, undefined),
      request: `${baseRequest}/entries/${label}/subjects/${subjectId}`,
      method: 'put',
      status: 201,
      payload: aSubject.toJson()
    });
  });

  it('updates Resources', () => {
    return H.test({
      toTest: () => handle.putResources(policy.id, label, resources),
      testBody: resources.toObject(),
      expected: new PutResponse(resources, 201, undefined),
      request: `${baseRequest}/entries/${label}/resources`,
      method: 'put',
      status: 201,
      payload: resources.toJson()
    });
  });

  it('updates a Resource', () => {
    return H.test({
      toTest: () => handle.putResource(policy.id, label, aResource),
      testBody: aResource.toObject(),
      expected: new PutResponse(aResource, 201, undefined),
      request: `${baseRequest}/entries/${label}/resources/${resourcePath}`,
      method: 'put',
      status: 201,
      payload: aResource.toJson()
    });
  });

  it('deletes a Policy', () => {
    return H.test({
      toTest: () => handle.deletePolicy(policy.id),
      request: baseRequest,
      method: 'delete',
      status: 204
    });
  });

  it('deletes an Entry', () => {
    return H.test({
      toTest: () => handle.deleteEntry(policy.id, label),
      request: `${baseRequest}/entries/${label}`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes a Subject', () => {
    return H.test({
      toTest: () => handle.deleteSubject(policy.id, label, subjectId),
      request: `${baseRequest}/entries/${label}/subjects/${subjectId}`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes a Resource', () => {
    return H.test({
      toTest: () => handle.deleteResource(policy.id, label, resourcePath),
      request: `${baseRequest}/entries/${label}/resources/${resourcePath}`,
      method: 'delete',
      status: 204
    });
  });

  it('returns a get policy error message', () => {
    return H.testError(() => errorHandle.getPolicy(policy.id));
  });

  it('returns a get enries error message', () => {
    return H.testError(() => errorHandle.getEntries(policy.id));
  });

  it('returns a get entry error message', () => {
    return H.testError(() => errorHandle.getEntry(policy.id, label));
  });

  it('returns a get subjects error message', () => {
    return H.testError(() => errorHandle.getSubjects(policy.id, label));
  });

  it('returns a get subject error message', () => {
    return H.testError(() => errorHandle.getSubject(policy.id, label, subjectId));
  });

  it('returns a get resources error message', () => {
    return H.testError(() => errorHandle.getResources(policy.id, label));
  });

  it('returns a get resource error message', () => {
    return H.testError(() => errorHandle.getResource(policy.id, label, resourcePath));
  });

  it('returns a put policy error message', () => {
    return H.testError(() => errorHandle.putPolicy(policy));
  });

  it('returns an update entries error message', () => {
    return H.testError(() => errorHandle.putEntries(policy.id, entries));
  });

  it('returns an update entry error message', () => {
    return H.testError(() => errorHandle.putEntry(policy.id, anEntry));
  });

  it('returns an update subjects error message', () => {
    return H.testError(() => errorHandle.putSubjects(policy.id, label, subjects));
  });

  it('returns an update subject error message', () => {
    return H.testError(() => errorHandle.putSubject(policy.id, label, aSubject));
  });

  it('returns an update resources error message', () => {
    return H.testError(() => errorHandle.putResources(policy.id, label, resources));
  });

  it('returns an update resource error message', () => {
    return H.testError(() => errorHandle.putResource(policy.id, label, aResource));
  });

  it('returns a delete policy error message', () => {
    return H.testError(() => errorHandle.deletePolicy(policy.id));
  });

  it('returns a delete entry error message', () => {
    return H.testError(() => errorHandle.deleteEntry(policy.id, label));
  });

  it('returns a delete subject error message', () => {
    return H.testError(() => errorHandle.deleteSubject(policy.id, label, subjectId));
  });

  it('returns a delete resource error message', () => {
    return H.testError(() => errorHandle.deleteResource(policy.id, label, resourcePath));
  });
});
