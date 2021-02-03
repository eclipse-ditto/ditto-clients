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

import { WebSocketHelper as H } from './websocket.helper';
import { Features } from '../../../src/model/things.model';

// tslint:disable-next-line:no-big-function
describe('WebSocket Features Handle', () => {
  const handle = H.thingsClient.getFeaturesHandle(H.thing.thingId);
  const errorHandle = H.errorClient.getFeaturesHandle(H.thing.thingId);
  const baseTopic = `${H.splitNamespace}/${H.splitThingId}/things/live/commands`;

  it('retrieves Features', () => {
    return H.test({
      toTest: () => handle.getFeatures(),
      topic: `${baseTopic}/retrieve`,
      path: '/features',
      status: 200,
      responseBody: Features.toObject(H.features),
      expected: H.features
    });
  });

  it('retrieves a Feature', () => {
    return H.test({
      toTest: () => handle.getFeature(H.feature.id),
      topic: `${baseTopic}/retrieve`,
      path: `/features/${H.feature.id}`,
      status: 200,
      responseBody: H.feature.toObject(),
      expected: H.feature
    });
  });

  it('retrieves a Definition', () => {
    return H.test({
      toTest: () => handle.getDefinition(H.feature.id),
      topic: `${baseTopic}/retrieve`,
      path: `/features/${H.feature.id}/definition`,
      status: 200,
      responseBody: H.definition,
      expected: H.definition
    });
  });

  it('retrieves Properties', () => {
    return H.test({
      toTest: () => handle.getProperties(H.feature.id),
      topic: `${baseTopic}/retrieve`,
      path: `/features/${H.feature.id}/properties`,
      status: 200,
      responseBody: H.properties,
      expected: H.properties
    });
  });

  it('retrieves a Property', () => {
    return H.test({
      toTest: () => handle.getProperty(H.feature.id, H.propertyPath),
      topic: `${baseTopic}/retrieve`,
      path: `/features/${H.feature.id}/properties/${H.propertyPath}`,
      status: 200,
      responseBody: H.property,
      expected: H.property
    });
  });

  it('modifies Features', () => {
    return H.test({
      toTest: () => handle.putFeatures(H.features),
      topic: `${baseTopic}/modify`,
      path: '/features',
      status: 204,
      requestBody: Features.toObject(H.features)
    });
  });

  it('modifies a Feature', () => {
    return H.test({
      toTest: () => handle.putFeature(H.feature),
      topic: `${baseTopic}/modify`,
      path: `/features/${H.feature.id}`,
      status: 204,
      requestBody: H.feature.toObject()
    });
  });

  it('modifies a Definition', () => {
    return H.test({
      toTest: () => handle.putDefinition(H.feature.id, H.definition),
      topic: `${baseTopic}/modify`,
      path: `/features/${H.feature.id}/definition`,
      status: 204,
      requestBody: H.definition
    });
  });

  it('modifies Properties', () => {
    return H.test({
      toTest: () => handle.putProperties(H.feature.id, H.properties),
      topic: `${baseTopic}/modify`,
      path: `/features/${H.feature.id}/properties`,
      status: 204,
      requestBody: H.properties
    });
  });

  it('modifies a Property', () => {
    return H.test({
      toTest: () => handle.putProperty(H.feature.id, H.propertyPath, H.property),
      topic: `${baseTopic}/modify`,
      path: `/features/${H.feature.id}/properties/${H.propertyPath}`,
      status: 204,
      requestBody: H.property
    });
  });

  it('deletes Features', () => {
    return H.test({
      toTest: () => handle.deleteFeatures(),
      topic: `${baseTopic}/delete`,
      path: '/features',
      status: 204
    });
  });

  it('deletes a Feature', () => {
    return H.test({
      toTest: () => handle.deleteFeature(H.feature.id),
      topic: `${baseTopic}/delete`,
      path: `/features/${H.feature.id}`,
      status: 204
    });
  });

  it('deletes a Definition', () => {
    return H.test({
      toTest: () => handle.deleteDefinition(H.feature.id),
      topic: `${baseTopic}/delete`,
      path: `/features/${H.feature.id}/definition`,
      status: 204
    });
  });

  it('deletes Properties', () => {
    return H.test({
      toTest: () => handle.deleteProperties(H.feature.id),
      topic: `${baseTopic}/delete`,
      path: `/features/${H.feature.id}/properties`,
      status: 204
    });
  });

  it('deletes a Property', () => {
    return H.test({
      toTest: () => handle.deleteProperty(H.feature.id, H.propertyPath),
      topic: `${baseTopic}/delete`,
      path: `/features/${H.feature.id}/properties/${H.propertyPath}`,
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
