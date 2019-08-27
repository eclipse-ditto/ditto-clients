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
import { Feature, Features } from '../../model/things.model';
import { FieldsOptions, MatchOptions } from '../../options/request.options';

export interface FeaturesHandle {
  /**
   * Gets all Features of this handle's Thing.
   *
   * @param options - Options to use for the request.
   * @returns A Promise for the Features
   */
  getFeatures(options?: FieldsOptions): Promise<Features>;

  /**
   * Gets the Feature of the specified ID.
   *
   * @param featureId - The ID of the Feature to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Feature
   */
  getFeature(featureId: string, options?: FieldsOptions): Promise<Feature>;

  /**
   * Gets the definition of the specified Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param options - Options to use for the request.
   * @returns A Promise for the definition
   */
  getDefinition(featureId: string, options?: FieldsOptions): Promise<string[]>;

  /**
   * Gets the properties of the specified Feature
   *
   * @param featureId - The ID of the Feature.
   * @param options - Options to use for the request.
   * @returns A Promise for the properties
   */
  getProperties(featureId: string, options?: FieldsOptions): Promise<Object>;

  /**
   * Gets a property of the specified Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param propertyPath - The path to the property.
   * @param options - Options to use for the request.
   * @returns A Promise for the property
   */
  getProperty(featureId: string, propertyPath: string, options?: FieldsOptions): Promise<any>;

  /**
   * Adds or updates all Features of this handle's Thing.
   *
   * @param features - The new Features.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Features if provided by the response
   */
  putFeatures(features: Features, options?: MatchOptions): Promise<PutResponse<Features>>;

  /**
   * Adds or updates a Feature of this handle's Thing.
   *
   * @param feature - The new Feature.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  putFeature(feature: Feature, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Adds or updates the definition of the specified Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param definition - The new definition.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Definition if provided by the response
   */
  putDefinition(featureId: string, definition: string[], options?: MatchOptions): Promise<PutResponse<string[]>>;

  /**
   * Adds or updates the properties of the specified Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param properties - The new properties.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  putProperties(featureId: string, properties: object, options?: MatchOptions): Promise<GenericResponse>;


  /**
   * Adds or updates a Property of the specified Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param propertyPath - The path to the Property.
   * @param property - The new Property.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Property if provided by the response
   */
  putProperty(featureId: string, propertyPath: string, property: any, options?: MatchOptions): Promise<PutResponse<any>>;

  /**
   * Deletes all Features of this handle's Thing.
   *
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteFeatures(options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes a Feature of this handle's Thing.
   *
   * @param featureId - The ID of the Feature to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteFeature(featureId: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes the definition of a Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteDefinition(featureId: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes the properties of a Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteProperties(featureId: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes a property of a Feature.
   *
   * @param featureId - The ID of the Feature.
   * @param propertyPath - The path to the property.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteProperty(featureId: string, propertyPath: string, options?: MatchOptions): Promise<GenericResponse>;
}
