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
import { GenericResponse, PutResponse } from '../../model/response';
import { Acl, AclEntry, Thing } from '../../model/things.model';
import { FieldsOptions, GetThingsOptions, MatchOptions } from '../../options/request.options';
import { RequestSender, RequestSenderFactory } from '../request-factory/request-sender';
import { HttpThingsHandleV1, HttpThingsHandleV2, WebSocketThingsHandle } from './things.interfaces';

/**
 * Handle to send Things requests.
 */
export class DefaultThingsHandle implements WebSocketThingsHandle, HttpThingsHandleV1, HttpThingsHandleV2 {

  protected constructor(protected readonly requestFactory: RequestSender) {
  }

  /**
   * returns an instance of DefaultThingsHandle using the provided RequestSender.
   *
   * @param builder - The builder for the RequestSender to work with.
   * @returns The DefaultThingsHandle
   */
  public static getInstance(builder: RequestSenderFactory): DefaultThingsHandle {
    return new DefaultThingsHandle(builder.buildInstance('things'));
  }

  public getThing(thingId: string, options?: FieldsOptions): Promise<Thing> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: Thing.fromObject,
      id: thingId,
      requestOptions: options
    });
  }

  public getAttributes(thingId: string, options?: FieldsOptions): Promise<object> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => o,
      id: thingId,
      path: 'attributes',
      requestOptions: options
    });
  }

  public getAttribute(thingId: string, attributePath: string, options?: MatchOptions): Promise<any> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => o,
      id: thingId,
      path: `attributes/${attributePath}`,
      requestOptions: options
    });
  }

  public getPolicyId(thingId: string, options?: MatchOptions): Promise<string> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: String,
      id: thingId,
      path: 'policyId',
      requestOptions: options
    });
  }

  public getAcl(thingId: string, options?: MatchOptions): Promise<Acl> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: Acl.fromObject,
      id: thingId,
      path: 'acl',
      requestOptions: options
    });
  }

  public getAclEntry(thingId: string, authorizationSubject: string, options?: MatchOptions): Promise<AclEntry> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => AclEntry.fromObject(o, authorizationSubject),
      id: thingId,
      path: `acl/${authorizationSubject}`,
      requestOptions: options
    });
  }

  public deleteThing(thingId: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: thingId,
      requestOptions: options
    });
  }

  public deleteAttributes(thingId: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: thingId,
      path: 'attributes',
      requestOptions: options
    });
  }

  public deleteAttribute(thingId: string, attributePath: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: thingId,
      path: `attributes/${attributePath}`,
      requestOptions: options
    });
  }

  public deleteAclEntry(thingId: string, authorizationSubject: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: thingId,
      path: `acl/${authorizationSubject}`,
      requestOptions: options
    });
  }


  public getThings(thingIds: string[], options?: GetThingsOptions): Promise<Thing[]> {
    let actualOptions: GetThingsOptions;
    if (options === undefined) {
      actualOptions = GetThingsOptions.getInstance().setThingIds(thingIds);
    } else {
      actualOptions = options.setThingIds(thingIds);
    }
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => Object(o).map((obj: any) => Thing.fromObject(obj)),
      requestOptions: actualOptions
    });
  }

  public postThing(thingWithoutId: Object): Promise<Thing> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'POST',
      parser: Thing.fromObject,
      payload: thingWithoutId
    })
      ;
  }

  public putThing(thing: Thing, options?: MatchOptions): Promise<PutResponse<Thing>> {
    return this.changeThing('PUT', thing, options);
  }

  public createThing(thing: Thing, options?: MatchOptions): Promise<PutResponse<Thing>> {
    return this.changeThing('create', thing, options);
  }

  public putAttributes(thingId: string, attributes: object, options?: MatchOptions): Promise<PutResponse<object>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: o => o,
      id: thingId,
      path: 'attributes',
      requestOptions: options,
      payload: attributes
    });
  }

  public putAttribute(thingId: string, attributePath: string,
                      attributeValue: any, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'PUT',
      id: thingId,
      path: `attributes/${attributePath}`,
      requestOptions: options,
      payload: attributeValue
    });
  }

  public putPolicyId(thingId: string, policyId: string, options?: MatchOptions): Promise<PutResponse<string>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: String,
      id: thingId,
      path: 'policyId',
      requestOptions: options,
      payload: policyId
    });
  }

  public putAcl(thingId: string, acl: Acl, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'PUT',
      id: thingId,
      path: 'acl',
      requestOptions: options,
      payload: acl.toObject()
    });
  }

  public putAclEntry(thingId: string, aclEntry: AclEntry, options?: MatchOptions): Promise<PutResponse<AclEntry>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: o => AclEntry.fromObject(o, aclEntry.id),
      id: thingId,
      path: `acl/${aclEntry.id}`,
      requestOptions: options,
      payload: aclEntry.toObject()
    });
  }

  private changeThing(verb: string, thing: Thing, options?: MatchOptions): Promise<PutResponse<Thing>> {
    return this.requestFactory.fetchPutRequest({
      verb,
      parser: Thing.fromObject,
      id: thing.thingId,
      requestOptions: options,
      payload: thing.toObject()
    });
  }
}
