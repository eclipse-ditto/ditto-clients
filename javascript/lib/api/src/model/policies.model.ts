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

import { EntityModel, EntityWithId, IndexedEntityModel } from './model';

/**
 * Representation of a Policy
 */
export class Policy extends EntityWithId {

  public constructor(private readonly _id: string,
    private readonly _entries: Entries) {
    super();
  }

  /**
   * Parses a Policy.
   *
   * @param o - The object to parse.
   * @param id - The id of the new Policy.
   * @returns The Policy
   */
  public static fromObject(o: any, id: string): Policy {
    if (o === undefined) {
      return o;
    }
    // @ts-ignore
    return new Policy(id, Entries.fromObject(o['entries']));
  }

  public toObject(): Object {
    const entriesObj = Entries.toObject(this.entries);
    return EntityModel.buildObject(new Map<string, any>([
      ['entries', entriesObj]
    ]));
  }

  get id(): string {
    return this._id;
  }

  get entries(): Entries {
    return this._entries;
  }
}


/**
 * Representation of Entries
 */
export class Entries extends IndexedEntityModel<Entry> {


  /**
   * Parses Entries.
   *
   * @param o - The object to parse.
   * @returns The Entries
   */
  public static fromObject(o: any): Entries {
    if (o === undefined) {
      return o;
    }
    return IndexedEntityModel.fromPlainObject(o, Entry.fromObject);
  }

}

/**
 * Representation of an Entry
 */
export class Entry extends EntityWithId {

  public constructor(private readonly _id: string,
    private readonly _subjects: Subjects,
    private readonly _resources: Resources) {
    super();
  }

  /**
   * Parses an Entry.
   *
   * @param o - The object to parse.
   * @param label - The label of the new Entry.
   * @returns The Entry
   */
  public static fromObject(o: any, label: string): Entry {
    if (o === undefined) {
      return o;
    }
    // @ts-ignore
    return new Entry(label, Subjects.fromObject(o['subjects']), Resources.fromObject(o['resources']));
  }

  public toObject(): Object {
    const subjectsObj = Subjects.toObject(this.subjects) ;
    const resourcesObj = Resources.toObject(this.resources);
    return EntityModel.buildObject(new Map<string, any>([
      ['subjects', subjectsObj],
      ['resources', resourcesObj]
    ]));
  }

  get id(): string {
    return this._id;
  }

  get subjects(): Subjects {
    return this._subjects;
  }

  get resources(): Resources {
    return this._resources;
  }
}


/**
 * Representation of Subjects
 */
export class Subjects extends IndexedEntityModel<Subject> {

  /**
   * Parses Subjects.
   *
   * @param o - The object to parse.
   * @returns The Subjects
   */
  public static fromObject(o: any): Subjects {
    if (o === undefined) {
      return o;
    }
    return IndexedEntityModel.fromPlainObject(o, Subject.fromObject, key => key);
  }
}

/**
 * Representation of Resources
 */
export class Resources extends IndexedEntityModel<Resource> {

  /**
   * Parses Resources.
   *
   * @param o - The object to parse.
   * @returns The Resources
   */
  public static fromObject(o: any): Resources {
    if (o === undefined) {
      return o;
    }
    return IndexedEntityModel.fromPlainObject(o, Resource.fromObject);
  }
}

export type SubjectIssuer = string;

export enum DittoSubjectIssuer {
  GOOGLE = 'google',
  NGINX = 'nginx'
}

export class SubjectId {

  private constructor(private readonly _id: string) {
  }

  static fromIssuerAndId(issuer: SubjectIssuer, subjectId: string) {
    return new SubjectId(`${issuer}:${subjectId}`);
  }

  static fromString(subjectId: string) {
    return new SubjectId(subjectId);
  }

  toString(): string {
    return this._id;
  }
}

export type SubjectType = string;

/**
 * Representation of a Subject
 */
export class Subject extends EntityWithId {

  public constructor(private readonly _id: SubjectId,
    private readonly _type: SubjectType) {
    super();
  }

  /**
   * Parses a Subject.
   *
   * @param o - The object to parse.
   * @param id - The id of the new Subject.
   * @returns The Subject
   */
  public static fromObject(o: any, id: string): Subject {
    if (o === undefined) {
      return o;
    }
    // @ts-ignore
    return new Subject(SubjectId.fromString(id), o['type']);
  }

  public toObject(): Object {
    return EntityModel.buildObject(new Map<string, any>([
      ['type', this.type]
    ]));
  }

  get id(): string {
    return this._id.toString();
  }

  get type(): SubjectType {
    return this._type;
  }
}

export enum AccessRight {
  Read = 'READ',
  Write = 'WRITE'
}

/**
 * Representation of a Resource
 */
export class Resource extends EntityWithId {

  public constructor(private readonly _id: string,
    private readonly _grant: AccessRight[],
    private readonly _revoke: AccessRight[]) {
    super();
  }

  /**
   * Parses a Resource.
   *
   * @param o - The object to parse.
   * @param id - The id of the new Resource.
   * @returns The Resource
   */
  public static fromObject(o: any, id: string): Resource {
    if (o === undefined) {
      return o;
    }
    // @ts-ignore
    return new Resource(id, o['grant'], o['revoke']);
  }

  public toObject(): Object {
    return EntityModel.buildObject(new Map<string, any>([
      ['revoke', this.revoke],
      ['grant', this.grant]
    ]));
  }

  get id(): string {
    return this._id;
  }

  get grant(): AccessRight[] {
    return this._grant;
  }

  get revoke(): AccessRight[] {
    return this._revoke;
  }
}
