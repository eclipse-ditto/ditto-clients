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

/* tslint:disable:no-big-function */
import { FeaturesHandle } from '../../../src/client/handles/features.interfaces';
import { PutResponse } from '../../../src/model/response';
import { HttpHelper as H } from './http.helper';
import { Features } from '../../../src/model/things.model';

describe('Http Features Handle', () => {
  const baseRequest = `things/${H.thing.thingId}/features`;
  const handle: FeaturesHandle = H.thingsClient.getFeaturesHandle(H.thing.thingId);
  const errorHandle: FeaturesHandle = H.errorThingsClient.getFeaturesHandle(H.thing.thingId);

  it('gets Features', () => {
    return H.test({
      toTest: () => handle.getFeatures(),
      testBody: Features.toObject(H.features),
      expected: H.features,
      request: baseRequest,
      method: 'get',
      status: 200
    });
  });

  it('gets a Feature', () => {
    return H.test({
      toTest: () => handle.getFeature(H.feature.id),
      testBody: H.feature.toObject(),
      expected: H.feature,
      request: `${baseRequest}/${H.feature.id}`,
      method: 'get',
      status: 200
    });
  });

  it('gets a Definition', () => {
    return H.test({
      toTest: () => handle.getDefinition(H.feature.id),
      testBody: H.definition,
      expected: H.definition,
      request: `${baseRequest}/${H.feature.id}/definition`,
      method: 'get',
      status: 200
    });
  });

  it('gets Properties', () => {
    return H.test({
      toTest: () => handle.getProperties(H.feature.id),
      testBody: H.properties,
      expected: H.properties,
      request: `${baseRequest}/${H.feature.id}/properties`,
      method: 'get',
      status: 200
    });
  });

  it('gets a Property', () => {
    return H.test({
      toTest: () => handle.getProperty(H.feature.id, H.propertyPath),
      testBody: H.property,
      expected: H.property,
      request: `${baseRequest}/${H.feature.id}/properties/${H.propertyPath}`,
      method: 'get',
      status: 200
    });
  });

  it('creates Features', () => {
    return H.test({
      toTest: () => handle.putFeatures(H.features),
      testBody: Features.toObject(H.features),
      expected: new PutResponse(H.features, 201, undefined),
      request: baseRequest,
      method: 'put',
      status: 201,
      payload: Features.toJson(H.features)
    });
  });

  it('updates Features', () => {
    return H.test({
      toTest: () => handle.putFeatures(H.features),
      testBody: Features.toObject(H.features),
      expected: new PutResponse(null, 204, undefined),
      request: baseRequest,
      method: 'put',
      status: 204,
      payload: Features.toJson(H.features)
    });
  });

  it('creates a Feature', () => {
    return H.test({
      toTest: () => handle.putFeature(H.feature),
      testBody: H.feature.toObject(),
      expected: new PutResponse(H.feature, 201, undefined),
      request: `${baseRequest}/${H.feature.id}`,
      method: 'put',
      status: 201,
      payload: H.feature.toJson()
    });
  });

  it('updates a Feature', () => {
    return H.test({
      toTest: () => handle.putFeature(H.feature),
      testBody: H.feature.toObject(),
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}/${H.feature.id}`,
      method: 'put',
      status: 204,
      payload: H.feature.toJson()
    });
  });

  it('creates a Definition', () => {
    return H.test({
      toTest: () => handle.putDefinition(H.feature.id, H.definition),
      testBody: H.definition,
      expected: new PutResponse(H.definition, 201, undefined),
      request: `${baseRequest}/${H.feature.id}/definition`,
      method: 'put',
      status: 201,
      payload: JSON.stringify(H.definition)
    });
  });

  it('updates a Definition', () => {
    return H.test({
      toTest: () => handle.putDefinition(H.feature.id, H.definition),
      testBody: H.definition,
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}/${H.feature.id}/definition`,
      method: 'put',
      status: 204,
      payload: JSON.stringify(H.definition)
    });
  });

  it('creates Properties', () => {
    return H.test({
      toTest: () => handle.putProperties(H.feature.id, H.properties),
      testBody: H.properties,
      expected: new PutResponse(H.properties, 201, undefined),
      request: `${baseRequest}/${H.feature.id}/properties`,
      method: 'put',
      status: 201,
      payload: JSON.stringify(H.properties)
    });
  });

  it('updates Properties', () => {
    return H.test({
      toTest: () => handle.putProperties(H.feature.id, H.properties),
      testBody: H.properties,
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}/${H.feature.id}/properties`,
      method: 'put',
      status: 204,
      payload: JSON.stringify(H.properties)
    });
  });

  it('creates a Property', () => {
    return H.test({
      toTest: () => handle.putProperty(H.feature.id, H.propertyPath, H.property),
      testBody: H.property,
      expected: new PutResponse(H.property, 201, undefined),
      request: `${baseRequest}/${H.feature.id}/properties/${H.propertyPath}`,
      method: 'put',
      status: 201,
      payload: JSON.stringify(H.property)
    });
  });

  it('updates a Property', () => {
    return H.test({
      toTest: () => handle.putProperty(H.feature.id, H.propertyPath, H.property),
      testBody: H.property,
      expected: new PutResponse(null, 204, undefined),
      request: `${baseRequest}/${H.feature.id}/properties/${H.propertyPath}`,
      method: 'put',
      status: 204,
      payload: JSON.stringify(H.property)
    });
  });

  it('deletes Features', () => {
    return H.test({
      toTest: () => handle.deleteFeatures(),
      request: baseRequest,
      method: 'delete',
      status: 204
    });
  });

  it('deletes a Feature', () => {
    return H.test({
      toTest: () => handle.deleteFeature(H.feature.id),
      request: `${baseRequest}/${H.feature.id}`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes a Definition', () => {
    return H.test({
      toTest: () => handle.deleteDefinition(H.feature.id),
      request: `${baseRequest}/${H.feature.id}/definition`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes Properties', () => {
    return H.test({
      toTest: () => handle.deleteProperties(H.feature.id),
      request: `${baseRequest}/${H.feature.id}/properties`,
      method: 'delete',
      status: 204
    });
  });

  it('deletes a Property', () => {
    return H.test({
      toTest: () => handle.deleteProperty(H.feature.id, H.propertyPath),
      request: `${baseRequest}/${H.feature.id}/properties/${H.propertyPath}`,
      method: 'delete',
      status: 204
    });
  });

  it('returns a get features error message', () => {
    return H.testError(() => errorHandle.getFeatures());
  });

  it('returns a get feature error message', () => {
    return H.testError(() => errorHandle.getFeature(H.feature.id));
  });

  it('returns a get definition error message', () => {
    return H.testError(() => errorHandle.getDefinition(H.feature.id));
  });

  it('returns an update properties error message', () => {
    return H.testError(() => errorHandle.getProperties(H.feature.id));
  });

  it('returns an update property error message', () => {
    return H.testError(() => errorHandle.getProperty(H.feature.id, ''));
  });

  it('returns an update features error message', () => {
    return H.testError(() => errorHandle.putFeatures(H.features));
  });

  it('returns an update feature error message', () => {
    return H.testError(() => errorHandle.putFeature(H.feature));
  });

  it('returns an update definition error message', () => {
    return H.testError(() => errorHandle.putDefinition(H.feature.id, H.definition));
  });

  it('returns an update properties error message', () => {
    return H.testError(() => errorHandle.putProperties(H.feature.id, H.properties));
  });

  it('returns an update property error message', () => {
    return H.testError(() => errorHandle.putProperty(H.feature.id, H.propertyPath, H.property));
  });

  it('returns a delete features error message', () => {
    return H.testError(() => errorHandle.deleteFeatures());
  });

  it('returns a delete feature error message', () => {
    return H.testError(() => errorHandle.deleteFeature(H.feature.id));
  });

  it('returns a delete definition error message', () => {
    return H.testError(() => errorHandle.deleteDefinition(H.feature.id));
  });

  it('returns a delete properties error message', () => {
    return H.testError(() => errorHandle.deleteProperties(H.feature.id));
  });

  it('returns a delete property error message', () => {
    return H.testError(() => errorHandle.deleteProperty(H.feature.id, H.propertyPath));
  });
});
