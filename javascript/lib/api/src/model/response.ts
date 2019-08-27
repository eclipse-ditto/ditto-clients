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

import { EntityModel } from './model';
import { Thing } from './things.model';

/**
 * A generic server response to a request.
 */
export interface GenericResponse {
  /** The status code of the response. */
  status: number;
  /** The body of the response. */
  body: any;
  /** The headers of the response inside a map. */
  headers: Map<string, string>;
}

export class PutResponse<T> implements GenericResponse {
  public constructor(private readonly _value?: T,
                     private readonly _status?: number,
                     private readonly _headers?: Map<string, string>) {
  }

  public wasCreated(): boolean {
    return this.body !== undefined;
  }

  public wasUpdated(): boolean {
    return !this.wasCreated();
  }

  get body(): T {
    return this._value;
  }

  get status(): number {
    return this._status;
  }

  get headers(): Map<string, string> {
    return this._headers;
  }
}


/**
 * Representation of a response ot a search request
 */
export class SearchThingsResponse extends EntityModel<SearchThingsResponse> {

  public constructor(private readonly _items: Thing[],
                     private readonly _nextPageOffset?: number,
                     private readonly _cursor?: string) {
    super();
  }

  /**
   * Parses a SearchThingsResponse.
   *
   * @param o - The object to parse.
   * @returns The SearchThingsResponse
   */
  public static fromObject(o: object): SearchThingsResponse {
    if (o === undefined) {
      return undefined;
    }
    return new SearchThingsResponse(o['items'].map((t: object) => Thing.fromObject(t)), o['nextPageOffset']);
  }

  public toObject(): object {
    return EntityModel.buildObject(new Map<string, any>([
      ['items', this._items.map((t: Thing) => t.toObject())],
      ['nextPageOffset', this.nextPageOffset],
      ['cursor', this.cursor]
    ]));
  }

  get items(): Thing[] {
    return this._items;
  }

  get nextPageOffset(): number {
    return this._nextPageOffset;
  }

  get cursor(): string {
    return this._cursor;
  }
}
