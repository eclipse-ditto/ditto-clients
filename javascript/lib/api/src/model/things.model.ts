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
export class Thing extends EntityWithId {

  public static readonly NAMESPACE_SEPARATION_REGEX = /([^:]*):(.*)/;

  public constructor(
    private readonly _thingId: string,
    private readonly _policyId?: string,
    private readonly _attributes?: Record<string, any>,
    private readonly _features?: Features,
    private readonly __revision?: number,
    private readonly __modified?: string,
    private readonly _acl?: Acl,
    private readonly _definition?: string,
    private readonly __metadata?: Metadata) {
    super();
  }

  /**
   * Parses a Thing.
   *
   * @param o - The object to parse.
   * @returns The Thing
   */
  public static fromObject(o: any): Thing {
    if (o === undefined) {
      return o;
    }
    // @ts-ignore
    return new Thing(o['thingId'],
      o['policyId'],
      o['attributes'],
      Features.fromObject(o['features']),
      o['_revision'],
      o['_modified'],
      Acl.fromObject(o['acl']),
      o['definition'],
      Metadata.fromObject(o['_metadata']));
  }

  public static empty(): Thing {
    return new Thing('', '', undefined, undefined, 0, '', undefined, undefined, undefined);
  }

  public toObject(): object {
    const featuresObj = Features.toObject(this.features);
    const aclObj = Acl.toObject(this._acl);
    return EntityModel.buildObject(new Map<string, any>([
      ['thingId', this.thingId],
      ['policyId', this.policyId],
      ['attributes', this.attributes],
      ['features', featuresObj],
      ['_revision', this._revision],
      ['_modified', this._modified],
      ['acl', aclObj],
      ['definition', this._definition]
    ]));
  }

  get thingId(): string {
    return this._thingId;
  }

  get id(): string {
    return this._thingId;
  }

  get policyId(): string | undefined {
    return this._policyId;
  }

  get attributes(): object | undefined {
    return this._attributes;
  }

  get features(): Features | undefined {
    return this._features;
  }

  get _modified(): string | undefined {
    return this.__modified;
  }

  get _revision(): number | undefined {
    return this.__revision;
  }

  get _metadata(): Metadata | undefined {
    return this.__metadata;
  }

  get namespace(): string {
    return this.separateNamespaceAndThingId().namespace;
  }

  get acl(): Acl | undefined {
    return this._acl;
  }

  get name(): string {
    return this.separateNamespaceAndThingId().name;
  }

  get definition(): string | undefined {
    return this._definition;
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


/**
 * Representation of Features
 */
export class Features extends IndexedEntityModel<Feature> {

  [featureId: string]: Feature

  /**
   * Parses Features.
   *
   * @param o - The object to parse.
   * @returns The Features
   */
  public static fromObject(o: any): Features {
    if (o === undefined) {
      return o;
    }
    return IndexedEntityModel.fromPlainObject<Feature>(o, Feature.fromObject);
  }

}


export class Metadata extends EntityModel {

  public constructor(
    private readonly _attributes?: Record<string, any>,
    private readonly _features?: Features) {
    super();
  }


  get attributes(): Record<string, any> | undefined {
    return this._attributes;
  }

  get features(): Features | undefined {
    return this._features;
  }

  public static fromObject(o: any): Metadata {
    if (o === undefined) {
      return o;
    }
    return new Metadata(o.attributes, Features.fromObject(o.features));
  }

  toObject(): Object | undefined {
    const features = this.features ? Features.toObject(this.features) : undefined;

    return EntityModel.buildObject(new Map<string, any>([
      ['features', features],
      ['attributes', this.attributes]
    ]));
  }

}


/**
 * Representation of a Feature
 */
export class Feature extends EntityWithId {

  public constructor(private readonly _id: string,
    private readonly _definition?: string[],
    private readonly _properties?: Record<string, any>) {
    super();
  }

  /**
   * Parses a Feature.
   *
   * @param o - The object to parse.
   * @param key - The key of the new Feature.
   * @returns The Feature
   */
  public static fromObject(o: any, key: string): Feature {
    if (o === undefined) {
      return o;
    }
    // @ts-ignore
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

  get definition(): string[] | undefined {
    return this._definition;
  }

  get properties(): Record<string, any> | undefined {
    return this._properties;
  }
}

export class Acl extends IndexedEntityModel<AclEntry> {

  [entryId: string]: AclEntry

  /**
   * Parses Acl.
   *
   * @param o - The object to parse.
   * @returns The Acl
   */
  public static fromObject(o: any): Acl {
    if (o === undefined) {
      return o;
    }
    return IndexedEntityModel.fromPlainObject<AclEntry>(o, AclEntry.fromObject);
  }
}

export class AclEntry extends EntityWithId {

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
  public static fromObject(o: any, id: string): AclEntry {
    if (o === undefined) {
      return o;
    }
    // @ts-ignore
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
