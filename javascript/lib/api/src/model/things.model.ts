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
    private readonly _definition?: string,
    private readonly __metadata?: Metadata,
    private readonly __created?: string) {
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
      o['definition'],
      Metadata.fromObject(o['_metadata']),
      o['_created']
    );
  }

  public static empty(): Thing {
    return new Thing('', '', undefined, undefined, 0, '', undefined, undefined, undefined);
  }

  public toObject(): object {
    const featuresObj = Features.toObject(this.features);
    return EntityModel.buildObject(new Map<string, any>([
      ['thingId', this.thingId],
      ['policyId', this.policyId],
      ['attributes', this.attributes],
      ['features', featuresObj],
      ['_revision', this._revision],
      ['_modified', this._modified],
      ['definition', this._definition],
      ['_created', this.__created]
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

  get name(): string {
    return this.separateNamespaceAndThingId().name;
  }

  get definition(): string | undefined {
    return this._definition;
  }

  get _created(): string | undefined {
    return this.__created;
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

