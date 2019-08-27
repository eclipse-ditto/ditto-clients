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
 * Representation of a Thing
 */
export class Thing extends EntityWithId<Thing> {

  public static readonly NAMESPACE_SEPARATION_REGEX = /([^:]*):(.*)/;

  public constructor(private readonly _thingId: string,
                     private readonly _policyId?: string,
                     private readonly _attributes?: object,
                     private readonly _features?: Features,
                     private readonly __revision?: number,
                     private readonly __modified?: string,
                     private readonly _acl?: Acl) {
    super();
  }

  /**
   * Parses a Thing.
   *
   * @param o - The object to parse.
   * @returns The Thing
   */
  public static fromObject(o: object): Thing {
    if (o === undefined) {
      return undefined;
    }
    return new Thing(o['thingId'], o['policyId'], o['attributes'],
      Features.fromObject(o['features']), o['_revision'], o['_modified'], Acl.fromObject(o['acl']));
  }

  public static empty(): Thing {
    return new Thing('', '', undefined, undefined, 0, '', undefined);
  }

  public toObject(): object {
    const featuresObj = this.features ? this.features.toObject() : undefined;
    const aclObj = this._acl ? this.acl.toObject() : undefined;
    return EntityModel.buildObject(new Map<string, any>([
      ['thingId', this.thingId],
      ['policyId', this.policyId],
      ['attributes', this.attributes],
      ['features', featuresObj],
      ['_revision', this._revision],
      ['_modified', this._modified],
      ['acl', aclObj]
    ]));
  }

  get thingId(): string {
    return this._thingId;
  }

  get id(): string {
    return this._thingId;
  }

  get policyId(): string {
    return this._policyId;
  }

  get attributes(): object {
    return this._attributes;
  }

  get features(): Features {
    return this._features;
  }

  get _modified(): string {
    return this.__modified;
  }

  get _revision(): number {
    return this.__revision;
  }

  get namespace(): string {
    return this.separateNamespaceAndThingId().namespace;
  }

  get acl(): Acl {
    return this._acl;
  }

  get name(): string {
    return this.separateNamespaceAndThingId().name;
  }

  private separateNamespaceAndThingId(): { namespace: string, name: string } {
    const indexOfFirstColon = this.thingId.indexOf(':');
    if (indexOfFirstColon >= 0) {
      const namespace = this.thingId.substring(0, indexOfFirstColon);
      const name = this.thingId.length === indexOfFirstColon ? '' : this.thingId.substring(indexOfFirstColon + 1);
      return { namespace, name };
    }
    return { namespace: '', name: this.thingId };
  }
}

interface FeaturesType {
  [featureId: string]: Feature;
}

/**
 * Representation of Features
 */
export class Features extends IndexedEntityModel<Features, Feature> {

  public constructor(readonly features?: FeaturesType) {
    super(features);
  }

  /**
   * Parses Features.
   *
   * @param o - The object to parse.
   * @returns The Features
   */
  public static fromObject(o: object): Features {
    if (o === undefined) {
      return undefined;
    }
    return new Features(IndexedEntityModel.fromPlainObject(o, Feature.fromObject));
  }
}

/**
 * Representation of a Feature
 */
export class Feature extends EntityWithId<Feature> {

  public constructor(private readonly _id: string,
                     private readonly _definition?: string[],
                     private readonly _properties?: object) {
    super();
  }

  /**
   * Parses a Feature.
   *
   * @param o - The object to parse.
   * @param key - The key of the new Feature.
   * @returns The Feature
   */
  public static fromObject(o: object, key: string): Feature {
    if (o === undefined) {
      return undefined;
    }
    return new Feature(key, o['definition'], o['properties']);
  }

  public toObject(): object {
    return EntityModel.buildObject(new Map<string, any>([
      ['definition', this.definition],
      ['properties', this.properties]
    ]));
  }

  get id(): string {
    return this._id;
  }

  get definition(): string[] {
    return this._definition;
  }

  get properties(): object {
    return this._properties;
  }
}

interface AclType {
  [aclEntryId: string]: AclEntry;
}

export class Acl extends IndexedEntityModel<Acl, AclEntry> {

  public constructor(readonly aclEntries?: AclType) {
    super(aclEntries);
  }

  /**
   * Parses Acl.
   *
   * @param o - The object to parse.
   * @returns The Acl
   */
  public static fromObject(o: object): Acl {
    if (o === undefined) {
      return undefined;
    }
    return new Acl(IndexedEntityModel.fromPlainObject(o, AclEntry.fromObject));
  }
}

export class AclEntry extends EntityWithId<AclEntry> {

  public constructor(private readonly _id: string,
                     private readonly _read: boolean,
                     private readonly _write: boolean,
                     private readonly _administrate: boolean) {
    super();
  }

  /**
   * Parses an AclEntry.
   *
   * @param o - The object to parse.
   * @param id - The id of the new AclEntry.
   * @returns The Feature
   */
  public static fromObject(o: object, id: string): AclEntry {
    if (o === undefined) {
      return undefined;
    }
    return new AclEntry(id, o['READ'], o['WRITE'], o['ADMINISTRATE']);
  }

  public toObject(): object {
    return EntityModel.buildObject(new Map<string, any>([
      ['READ', this.read],
      ['WRITE', this.write],
      ['ADMINISTRATE', this.administrate]
    ]));
  }

  get id(): string {
    return this._id;
  }

  get read(): boolean {
    return this._read;
  }

  get write(): boolean {
    return this._write;
  }

  get administrate(): boolean {
    return this._administrate;
  }
}
