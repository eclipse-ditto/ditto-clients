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

/**
 * Model to represent an Entity in Ditto.
 */
export abstract class EntityModel<T extends EntityModel<T>> {

  /**
   * Turns an object into an array of the type A. It contains one Entity for every key of the original object
   *
   * @typeParam A - The type of array to return
   * @param o - The object to parse.
   * @param parser - A method to parse instances of the desired Entity.
   * @returns The array of Entities.
   */
  static objectToArray<T extends EntityWithId<T>>(o: Object, parser: (obj: Object, key: string) => T): T[] {
    return Object.keys(o).map((key: string) => {
      // @ts-ignore
      return parser(o[key], key);
    });
  }

  /**
   * Builds an object representation out of the provided content.
   *
   * @param content - The content of the object representation.
   * @returns The object containing all elements of the content that aren't undefined.
   */
  protected static buildObject(content: Map<string, any>): object {
    const res: { [key: string]: any } = {};
    content.forEach((v, k) => {
      if (v !== undefined && v !== '') {
        res[k] = v;
      }
    });
    return res;
  }

  toJson(): string {
    return JSON.stringify(this.toObject());
  }

  equals(toCompare: T): boolean {
    // @ts-ignore
    return toCompare !== undefined && (this === toCompare || this.toJson() === toCompare.toJson());
  }

  /**
   * Turns the Entity into an object.
   *
   * @returns The object representation of the Entity.
   */
  abstract toObject(): Object | undefined;
}

/**
 * Entity with an 'id' field
 */
export abstract class EntityWithId<T extends EntityWithId<T>> extends EntityModel<T> {
  id!: string;

  equals(toCompare: T): boolean {
    return super.equals(toCompare) && this.id === toCompare.id;
  }
}

/**
 * Type that can be used for entities with unknown property keys but known property values.
 * E.g. an entity that has user-defined keys but the values are always of the same type.
 * In typescript such interfaces are called 'index signatures'.
 */
export interface IndexedEntityModelType<T> {
  [index: string]: T;
}

/**
 * Abstract entity model class that consists of entities of the same type but with unknown property keys.
 * @param <T> - Type of the implementing class.
 * @param <V> - Type of the entities that are stored by the model.
 */
export abstract class IndexedEntityModel<T extends IndexedEntityModel<T, V>, V>
  extends EntityModel<IndexedEntityModel<T, V>> {

  protected constructor(private readonly _elements: IndexedEntityModelType<V> | undefined) {
    super();
  }

  /**
   * Map the object to an indexed object with types. Iterates all keys and maps all values with {@code mapValue}.
   *
   * @param objectToMap - the object to map.
   * @param mapValue - how to get the value from an objects value.
   * @param mapKey - how to get the key from an objects key.
   */
  static fromPlainObject<T>(objectToMap: Object | undefined,
                            mapValue: (value: any, key: string) => T,
                            mapKey: (key: string, value: any) => string = key => key): IndexedEntityModelType<T> | undefined {
    if (objectToMap) {
      const entries = Object.keys(objectToMap)
        .map(k => {
          // @ts-ignore
          const theKey = mapKey(k, objectToMap[k]);
          // @ts-ignore
          const theVal = mapValue(objectToMap[k], k);
          return { [theKey]: theVal };
        });
      return Object.assign({}, ...entries);
    }

    return undefined;
  }

  /**
   * Removes all type information from the indexed type.
   * @param objectToMap - the object to map.
   * @param removeType - function that is called to remove the type of the objects values.
   */
  static toPlainObject<T>(objectToMap: IndexedEntityModelType<T>, removeType: (typedObject: T) => any) {
    const entries = Object.keys(objectToMap)
      .map(k => {
        return { [k]: removeType(objectToMap[k]) };
      });
    return Object.assign({}, ...entries);
  }

  /**
   * Turns the Entity into an object.
   *
   * @returns The object representation of the Entity.
   */
  toObject(): Object | undefined {
    if (this._elements) {
      // @ts-ignore
      return IndexedEntityModel.toPlainObject(this._elements, element => element.toObject());
    }
    return undefined;
  }
}
