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

/* tslint:disable:no-nested-template-literals */
import { GenericResponse, PutResponse } from '../../model/response';
import { Feature, Features } from '../../model/things.model';
import { FieldsOptions, MatchOptions, MatchOptionsHelper } from '../../options/request.options';
import { HttpVerb } from '../constants/http-verb';
import { RequestSender, RequestSenderFactory } from '../request-factory/request-sender';
import { FeaturesHandle } from './features.interfaces';

/**
 * Handle to send Feature requests.
 */
export class DefaultFeaturesHandle implements FeaturesHandle {

  private constructor(private readonly requestFactory: RequestSender,
                      private readonly thingId: string) {
  }

  /**
   * returns an instance of FeaturesHandle using the provided RequestSender.
   *
   * @param builder - The for the RequestSender to work with.
   * @param thingId - The ThingId to use.
   * @returns The FeaturesHandle
   */
  public static getInstance(builder: RequestSenderFactory, thingId: string): DefaultFeaturesHandle {
    return new DefaultFeaturesHandle(builder.buildInstance('things'), thingId);
  }

  public getFeatures(options?: FieldsOptions): Promise<Features> {
    return this.requestFactory.fetchJsonRequest({
      verb: HttpVerb.GET,
      parser: Features.fromObject,
      id: this.thingId,
      path: 'features',
      requestOptions: options
    });
  }

  public getFeature(featureId: string, options?: FieldsOptions): Promise<Feature> {
    return this.requestFactory.fetchJsonRequest({
      verb: HttpVerb.GET,
      parser: o => Feature.fromObject(o, featureId),
      id: this.thingId,
      path: `features/${featureId}`,
      requestOptions: options
    });
  }

  public getDefinition(featureId: string, options?: MatchOptions): Promise<string[]> {
    return this.requestFactory.fetchJsonRequest({
      verb: HttpVerb.GET,
      parser: o => Object(o).map((obj: any) => String(obj)),
      id: this.thingId,
      path: `features/${featureId}/definition`,
      requestOptions: options
    });
  }

  public getProperties(featureId: string, options?: FieldsOptions): Promise<Object> {
    return this.requestFactory.fetchJsonRequest({
      verb: HttpVerb.GET,
      parser: o => o,
      id: this.thingId,
      path: `features/${featureId}/properties`,
      requestOptions: options
    });
  }

  public getProperty(featureId: string, propertyPath: string, options?: MatchOptions): Promise<any> {
    return this.requestFactory.fetchJsonRequest({
      verb: HttpVerb.GET,
      parser: o => o,
      id: this.thingId,
      path: `features/${featureId}/properties/${propertyPath}`,
      requestOptions: options
    });
  }

  public deleteFeatures(options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: HttpVerb.DELETE,
      id: this.thingId,
      path: 'features',
      requestOptions: options
    });
  }

  public deleteFeature(featureId: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: HttpVerb.DELETE,
      id: this.thingId,
      path: `features/${featureId}`,
      requestOptions: options
    });
  }

  public deleteDefinition(featureId: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: HttpVerb.DELETE,
      id: this.thingId,
      path: `features/${featureId}/definition`,
      requestOptions: options
    });
  }

  public deleteProperties(featureId: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: HttpVerb.DELETE,
      id: this.thingId,
      path: `features/${featureId}/properties`,
      requestOptions: options
    });
  }

  public deleteProperty(featureId: string, propertyPath: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: HttpVerb.DELETE,
      id: this.thingId,
      path: `features/${featureId}/properties/${propertyPath}`,
      requestOptions: options
    });
  }

  public putFeatures(features: Features, options?: MatchOptions): Promise<PutResponse<Features>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PUT,
      parser: Features.fromObject,
      id: this.thingId,
      path: 'features',
      requestOptions: options,
      payload: Features.toObject(features)
    });
  }

  public putFeature(feature: Feature, options?: MatchOptions): Promise<PutResponse<Feature>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PUT,
      parser: o => Feature.fromObject(o, feature.id),
      id: this.thingId,
      path: `features/${feature.id}`,
      requestOptions: options,
      payload: feature.toObject()
    });
  }

  public patchFeatures(features: Features, options?: MatchOptions): Promise<PutResponse<Features>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PATCH,
      parser: Features.fromObject,
      id: this.thingId,
      path: 'features',
      requestOptions: MatchOptionsHelper.getWithMergeHeader(options),
      payload: Features.toObject(features)
    });
  }

  public patchFeature(feature: Feature, options?: MatchOptions): Promise<PutResponse<Feature>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PATCH,
      parser: o => Feature.fromObject(o, feature.id),
      id: this.thingId,
      path: `features/${feature.id}`,
      requestOptions: MatchOptionsHelper.getWithMergeHeader(options),
      payload: feature.toObject()
    });
  }

  public putDefinition(featureId: string, definition: string[], options?: MatchOptions): Promise<PutResponse<string[]>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PUT,
      parser: o => o !== undefined ? Object.values(o).map((obj: any) => String(obj)) : [],
      id: this.thingId,
      path: `features/${featureId}/definition`,
      requestOptions: options,
      payload: definition
    }); // `[${String(definition.map(s => `"${s}"`))}]`
  }

  public patchDefinition(featureId: string, definition: string[], options?: MatchOptions): Promise<PutResponse<string[]>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PATCH,
      parser: o => o !== undefined ? Object.values(o).map((obj: any) => String(obj)) : [],
      id: this.thingId,
      path: `features/${featureId}/definition`,
      requestOptions: MatchOptionsHelper.getWithMergeHeader(options),
      payload: definition
    });
  }

  public putProperties(featureId: string, properties: object, options?: MatchOptions): Promise<PutResponse<Object>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PUT,
      parser: o => o,
      id: this.thingId,
      path: `features/${featureId}/properties`,
      requestOptions: options,
      payload: properties
    });
  }

  public putProperty(featureId: string, propertyPath: string,
                      property: any, options?: MatchOptions): Promise<PutResponse<any>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PUT,
      parser: o => o,
      id: this.thingId,
      path: `features/${featureId}/properties/${propertyPath}`,
      requestOptions: options,
      payload: property
    });
  }

  public patchProperties(featureId: string, properties: object, options?: MatchOptions): Promise<PutResponse<Object>> {
    return this.requestFactory.fetchPutRequest({
      verb: HttpVerb.PATCH,
      parser: o => o,
      id: this.thingId,
      path: `features/${featureId}/properties`,
      requestOptions: MatchOptionsHelper.getWithMergeHeader(options),
      payload: properties
    });
  }

  public patchProperty(featureId: string, propertyPath: string, property: any, options?: MatchOptions): Promise<PutResponse<any>> {
    return this.requestFactory.fetchPutRequest({
      verb:  HttpVerb.PATCH,
      parser: o => o,
      id: this.thingId,
      path: `features/${featureId}/properties/${propertyPath}`,
      requestOptions: MatchOptionsHelper.getWithMergeHeader(options),
      payload: property
    });
  }
}

