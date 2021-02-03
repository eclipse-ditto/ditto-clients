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
import {
  AccessRight, DittoSubjectIssuer,
  Entries,
  Entry,
  Policy,
  Resource,
  Resources,
  Subject,
  SubjectId,
  Subjects,
  SubjectType
} from '../../src/model/policies.model';

const aResourceObj = { grant: ['READ'], revoke: ['READ'] };
const someResourcesObj = { 'thing:/': aResourceObj, 'thing:/attributes/some/path': aResourceObj };
const someMoreResourcesObj = { 'thing:/': aResourceObj };

const aResource = new Resource('thing:/', [AccessRight.Read], [AccessRight.Read]);
const anotherResource = new Resource('thing:/attributes/some/path', [AccessRight.Read], [AccessRight.Read]);
const typedResources = { 'thing:/': aResource, 'thing:/attributes/some/path': anotherResource };
const someResources = typedResources;
const someMoreResources = { 'thing:/': aResource };
const aSubject = new Subject(SubjectId.fromString('nginx:user'), 'my default nginx user');
const anotherSubject = new Subject(SubjectId.fromIssuerAndId(DittoSubjectIssuer.GOOGLE, 'my-google-user@gmail.com'), 'my google user');
const aLastSubject = new Subject(SubjectId.fromIssuerAndId(DittoSubjectIssuer.NGINX, 'admin'), 'my nginx admin user');
const typedSubjects = { [aSubject.id]: aSubject, [anotherSubject.id]: anotherSubject };
const someSubjects = typedSubjects;
const someMoreSubjects = { [aLastSubject.id]: aLastSubject };
const anEntry = new Entry('label1', someSubjects, someResources);
const anotherEntry = new Entry('label2', someMoreSubjects, someMoreResources);
const typedEntries = { label1: anEntry, label2: anotherEntry };
const entries = typedEntries;
const policy = new Policy('PolicyId', entries);

const aSubjectObj = { type: aSubject.type };
const anotherSubjectObj = { type: anotherSubject.type };
const someSubjectsObj = {
  [aSubject.id]: aSubjectObj,
  [anotherSubject.id]: anotherSubjectObj
};
const someMoreSubjectsObj = { [aLastSubject.id]: { type: aLastSubject.type } };
const anEntryObj = {
  subjects: someSubjectsObj,
  resources: someResourcesObj
};
const anotherEntryObj = {
  subjects: someMoreSubjectsObj,
  resources: someMoreResourcesObj
};
const entriesObj = { label1: anEntryObj, label2: anotherEntryObj };
const policyObj = { entries: entriesObj };

describe('Resource', () => {
  it('parses an object', () => {
    expect(Resource.fromObject(aResourceObj, 'thing:/')).toEqual(aResource);
    expect(Resource.fromObject(aResourceObj, 'thing:/').equals(aResource)).toBe(true);
  });
  it('builds an object', () => {
    expect(aResource.toObject()).toEqual(aResourceObj);
  });
  it('returns content', () => {
    expect(aResource.id).toEqual('thing:/');
    expect(aResource.grant).toEqual([AccessRight.Read]);
    expect(aResource.revoke).toEqual([AccessRight.Read]);
  });
  it('handles an undefined object', () => {
    expect(Resource.fromObject(undefined, '')).toEqual(undefined);
  });
});
describe('Resources', () => {
  it('parses an object', () => {
    expect(Resources.fromObject(someResourcesObj)).toEqual(someResources);
    expect(Resources.equals(Resources.fromObject(someResourcesObj), someResources)).toBe(true);
  });
  it('builds an object', () => {
    expect(Resources.toObject(someResources)).toEqual(someResourcesObj);
  });
  it('returns its content', () => {
    expect(someResources).toEqual({ 'thing:/': aResource, 'thing:/attributes/some/path': anotherResource });
  });
  it('handles an undefined object', () => {
    expect(Resources.fromObject(undefined)).toEqual(undefined);
  });
});

describe('Subject', () => {
  it('parses an object', () => {
    expect(Subject.fromObject(aSubjectObj, aSubject.id)).toEqual(aSubject);
    expect(Subject.fromObject(aSubjectObj, aSubject.id).equals(aSubject)).toBe(true);
  });
  it('builds an object', () => {
    expect(aSubject.toObject()).toEqual(aSubjectObj);
  });
  it('returns its content', () => {
    expect(aSubject.id).toEqual('nginx:user');
    expect(aSubject.type).toEqual('my default nginx user');
  });
  it('handles an undefined object', () => {
    expect(Subject.fromObject(undefined, '')).toEqual(undefined);
  });
});

describe('Subjects', () => {
  it('parses an object', () => {
    expect(Subjects.fromObject(someSubjectsObj)).toEqual(someSubjects);
    expect(Subjects.equals(Subjects.fromObject(someSubjectsObj), someSubjects)).toBe(true);
  });
  it('builds an object', () => {
    expect(Subjects.toObject(someSubjects)).toEqual(someSubjectsObj);
  });
  it('returns its content', () => {
    expect(someSubjects).toEqual({ [aSubject.id]: aSubject, [anotherSubject.id]: anotherSubject });
  });
  it('handles an undefined object', () => {
    expect(Subjects.fromObject(undefined)).toEqual(undefined);
  });
});

describe('Entry', () => {
  it('parses an object', () => {
    expect(Entry.fromObject(anEntryObj, 'label1')).toEqual(anEntry);
    expect(Entry.fromObject(anEntryObj, 'label1').equals(anEntry)).toBe(true);
  });
  it('builds an object', () => {
    expect(anEntry.toObject()).toEqual({ subjects: Subjects.toObject(someSubjects), resources: Resources.toObject(someResources) });
  });
  it('returns its content', () => {
    expect(anEntry.id).toEqual('label1');
    expect(anEntry.subjects).toEqual(someSubjects);
    expect(anEntry.resources).toEqual(someResources);
  });
  it('handles an undefined object', () => {
    expect(Entry.fromObject(undefined, '')).toEqual(undefined);
  });
});
describe('Entries', () => {
  it('parses an object', () => {
    expect(Entries.fromObject(entriesObj)).toEqual(entries);
    expect(Entries.equals(Entries.fromObject(entriesObj), entries)).toBe(true);
  });
  it('builds an object', () => {
    expect(Entries.toObject(entries)).toEqual(entriesObj);
  });
  it('returns its content', () => {
    expect(entries).toEqual({ label1: anEntry, label2: anotherEntry });
  });
  it('handles an undefined object', () => {
    expect(Entries.fromObject(undefined)).toEqual(undefined);
  });
  it('handles an incomplete Entry', () => {
    expect(new Entry(undefined, undefined, undefined).toObject()).toEqual({});
  });
});

describe('Policy', () => {
  it('parses an object', () => {
    expect(Policy.fromObject(policyObj, 'PolicyId')).toEqual(policy);
    expect(Policy.fromObject(policyObj, 'PolicyId').equals(policy)).toBe(true);
  });
  it('builds an object', () => {
    expect(policy.toObject()).toEqual({ entries: Entries.toObject(entries) });
  });
  it('returns its content', () => {
    expect(policy.id).toEqual('PolicyId');
    expect(policy.entries).toEqual(entries);
  });
  it('handles an undefined object', () => {
    expect(Policy.fromObject(undefined, '')).toEqual(undefined);
  });
  it('handles an incomplete Policy', () => {
    expect(new Policy(undefined, undefined).toObject()).toEqual({});
  });
});
