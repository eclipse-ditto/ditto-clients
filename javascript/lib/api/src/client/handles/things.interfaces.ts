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

import { GenericResponse, PutResponse } from '../../model/response';
import { Acl, AclEntry, Thing } from '../../model/things.model';
import { FieldsOptions, GetThingsOptions, MatchOptions } from '../../options/request.options';

export interface ThingsHandle {

  /**
   * Gets a Thing.
   *
   * @param thingId - The ID of the Thing to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Thing
   */
  getThing(thingId: string, options?: FieldsOptions): Promise<Thing>;

  /**
   * Gets the Attributes of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param options - Options to use for the request.
   * @returns A Promise for the Attributes
   */
  getAttributes(thingId: string, options?: FieldsOptions): Promise<object>;

  /**
   * Gets an Attribute of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param attributePath - The path to the Attribute.
   * @param options - Options to use for the request.
   * @returns A Promise for the Attribute
   */
  getAttribute(thingId: string, attributePath: string, options?: FieldsOptions): Promise<any>;

  /**
   * Adds or updates the Attributes of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param attributes - The new Attributes.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  putAttributes(thingId: string, attributes: object, options?: MatchOptions): Promise<PutResponse<object>>;

  /**
   * Adds or updates an Attribute of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param attributePath - The new path to the new Attribute.
   * @param attributeValue - The new Attribute.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  putAttribute(thingId: string, attributePath: string, attributeValue: any, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes a Thing.
   *
   * @param thingId - The ID of the Thing to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteThing(thingId: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes the Attributes of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteAttributes(thingId: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes an Attribute of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param attributePath - The path to the Attribute.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteAttribute(thingId: string, attributePath: string, options?: MatchOptions): Promise<GenericResponse>;
}

export interface HttpThingsHandle extends ThingsHandle {

  /**
   * Gets all available Things.
   *
   * @param thingIds - The ID of the Things to get.
   * @param options - Options to use for the request. The setThingIds option will be overridden
   * @returns A Promise for the Things
   */
  getThings(thingIds: string[], options?: GetThingsOptions): Promise<Thing[]>;

  /**
   * Creates a Thing.
   *
   * @param thingWithoutId - The Thing to create.
   * @returns A Promise for the new Thing provided in the response
   */
  postThing(thingWithoutId: Object): Promise<Thing>;

  /**
   * Adds or updates a Thing.
   *
   * @param thing - The Thing to create.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Thing if provided by the response
   */
  putThing(thing: Thing, options?: MatchOptions): Promise<PutResponse<Thing>>;
}

export interface HttpThingsHandleV1 extends HttpThingsHandle {

  /**
   * Gets the Acl of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param options - Options to use for the request.
   * @returns A Promise for the Acl
   */
  getAcl(thingId: string, options?: MatchOptions): Promise<Acl>;

  /**
   * Gets an AclEntry of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param authorizationSubject - The subject of the AclEntry to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the AclEntry
   */
  getAclEntry(thingId: string, authorizationSubject: string, options?: MatchOptions): Promise<AclEntry>;

  /**
   * Deletes and AclEntry of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param authorizationSubject - The subject of the AclEntry to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteAclEntry(thingId: string, authorizationSubject: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Updates the Acl of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param acl - The new Acl.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  putAcl(thingId: string, acl: Acl, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Updates an AclEntry of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param aclEntry - The ID of the Thing.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new AclEntry if provided by the response
   */
  putAclEntry(thingId: string, aclEntry: AclEntry, options?: MatchOptions): Promise<PutResponse<AclEntry>>;
}

export interface HttpThingsHandleV2 extends HttpThingsHandle {

  /**
   * Gets the PolicyId of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param options - Options to use for the request.
   * @returns A Promise for the PolicyId
   */
  getPolicyId(thingId: string, options?: MatchOptions): Promise<string>;

  /**
   * Adds or updates the policyId of a Thing.
   *
   * @param thingId - The ID of the Thing.
   * @param policyId - The new PolicyId.
   * @param options - Options to use for the request.
   * @returns A Promise for the new PolicyId provided in the response
   */
  putPolicyId(thingId: string, policyId: string, options?: MatchOptions): Promise<PutResponse<string>>;
}

export interface WebSocketThingsHandle extends ThingsHandle {

  /**
   * Adds a Thing.
   *
   * @param thing - The Thing to create.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Thing if provided by the response
   */
  createThing(thing: Thing, options?: FieldsOptions): Promise<PutResponse<Thing>>;

  /**
   * Adds or updates a Thing.
   *
   * @param thing - The Thing to create.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  putThing(thing: Thing, options?: MatchOptions): Promise<GenericResponse>;

}

