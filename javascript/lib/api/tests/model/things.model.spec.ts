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

import { PutResponse, SearchThingsResponse } from '../../src/model/response';
import { Feature, Features, Metadata, Thing } from '../../src/model/things.model';

const aDefinition = ['aDefinition', 'aSecondOne'];
const anotherDefinition = ['anotherDefinition'];
const someProperties = { key: 'value' };
const moreProperties = { anotherKey: 'another body' };
const aFeatureObj = { definition: aDefinition, properties: someProperties };
const anotherFeatureObj = { definition: anotherDefinition, properties: moreProperties };
const featuresObj = { additionalProp1: aFeatureObj, additionalProp2: anotherFeatureObj };
const anAttribute = 7;
const anotherAttribute = 'ABS';
const attributes = { anAttribute, anotherAttribute };
const metadataObject = {
  features: { lamp: { properties: { color: {} } } }, attributes: { foo: 'bar', bar: 'foo' }
};
const thingObj = {
  attributes,
  thingId: 'Testspace:Testthing',
  policyId: 'PolicyId',
  features: featuresObj,
  _revision: 0,
  _modified: '08042019',
  _created: '2017-06-01T00:01:57Z',
  definition: 'example:test:definition',
  _metadata: metadataObject
};

const thingObjWithoutMetadata = {
  attributes,
  thingId: 'Testspace:Testthing',
  definition: 'example:test:definition',
  policyId: 'PolicyId',
  features: featuresObj,
  _revision: 0,
  _modified: '08042019',
  _created: '2017-06-01T00:01:57Z'
};

const responseObj = { items: [thingObj], nextPageOffset: 0 };
const responseObjWithoutMetadata = { items: [thingObjWithoutMetadata], nextPageOffset: 0 };

const aFeature = new Feature('additionalProp1', aDefinition, someProperties);
const anotherFeature = new Feature('additionalProp2', anotherDefinition, moreProperties);
const typedFeatureObject = { additionalProp1: aFeature, additionalProp2: anotherFeature };
const features = typedFeatureObject;
const metadata = new Metadata({ foo: 'bar', bar: 'foo' }, { lamp: new Feature('lamp', undefined, { color: {} }) });
const thing = new Thing('Testspace:Testthing',
  'PolicyId',
  attributes,
  features,
  0,
  '08042019',
  'example:test:definition',
  metadata,
  '2017-06-01T00:01:57Z');
const response = new SearchThingsResponse([thing], 0);

describe('Feature', () => {

  it('parses an object', () => {
    expect(Feature.fromObject(aFeatureObj, 'additionalProp1')).toEqual(aFeature);
    expect(Feature.fromObject(aFeatureObj, 'additionalProp1').equals(aFeature)).toBe(true);
  });
  it('builds an object', () => {
    expect(aFeature.toObject()).toEqual(aFeatureObj);
  });
  it('returns its content', () => {
    expect(aFeature.id).toEqual('additionalProp1');
    expect(aFeature.properties).toEqual(someProperties);
    expect(aFeature.definition).toEqual(aDefinition);
  });
  it('handles an undefined object', () => {
    expect(Feature.fromObject(undefined, '')).toEqual(undefined);
  });
});

describe('Features', () => {
  it('parses an object', () => {
    expect(Features.fromObject(featuresObj)).toEqual(features);
    expect(Features.equals(Features.fromObject(featuresObj), features)).toBe(true);
  });
  it('builds an object', () => {
    expect(Features.toObject(features)).toEqual(featuresObj);
  });
  it('returns its content', () => {
    expect(features).toEqual(typedFeatureObject);
  });
  it('handles an undefined object', () => {
    expect(Features.fromObject(undefined)).toEqual(undefined);
  });
});

describe('Metadata', () => {
  it('parses an object', () => {
    expect(Metadata.fromObject(metadataObject)).toEqual(metadata);
    expect(metadata.equals(Metadata.fromObject(metadataObject))).toBe(true);
  });

  it('parses an object', () => {
    expect(metadata.toObject()).toEqual(metadataObject);
  });

  it('handles undefined objects', () => {
    expect(Metadata.fromObject(undefined)).toEqual(undefined);
  });

});

describe('Thing', () => {
  it('parses an object', () => {
    expect(Thing.fromObject(thingObj)).toEqual(thing);
    expect(Thing.fromObject(thingObj).equals(thing)).toBe(true);
  });
  it('builds an object', () => {
    expect(thing.toObject()).toEqual(thingObjWithoutMetadata);
  });
  it('returns its content', () => {
    expect(thing.thingId).toEqual('Testspace:Testthing');
    expect(thing.attributes).toEqual(attributes);
    expect(thing.features).toEqual(features);
    expect(thing._modified).toEqual('08042019');
    expect(thing.namespace).toEqual('Testspace');
    expect(thing.policyId).toEqual('PolicyId');
    expect(thing._revision).toEqual(0);
    expect(thing.name).toEqual('Testthing');
    expect(thing.definition).toEqual('example:test:definition');
    expect(thing._created).toEqual('2017-06-01T00:01:57Z');
  });
  it('handles a minimal thing', () => {
    const minimalThing = new Thing('Tespspace:Minimal');
    expect(minimalThing.toObject()).toEqual({ thingId: 'Tespspace:Minimal' });
  });
  it('handles an empty thing', () => {
    const emptyThing = Thing.empty();
    expect(emptyThing.toObject()).toEqual({ _revision: 0 });
  });
  it('handles a minimal splitThingId', () => {
    const testThing = new Thing('MinimalId');
    expect(testThing.name).toEqual('MinimalId');
  });
  it('handles an undefined object', () => {
    expect(Thing.fromObject(undefined)).toEqual(undefined);
  });
});

describe('SearchThingsResponse', () => {
  it('parses an object', () => {
    expect(SearchThingsResponse.fromObject(responseObj)).toEqual(response);
    expect(SearchThingsResponse.fromObject(responseObj).equals(response)).toBe(true);
  });
  it('builds an object', () => {
    expect(response.toObject()).toEqual(responseObjWithoutMetadata);
  });
  it('returns its content', () => {
    expect(response.items).toEqual([thing]);
    expect(response.nextPageOffset).toEqual(0);
  });
  it('handles an undefined object', () => {
    expect(SearchThingsResponse.fromObject(undefined)).toEqual(undefined);
  });
});

describe('PutResponse', () => {
  it('stores a body', () => {
    const toTest = new PutResponse(thing, 201, new Map());
    expect(toTest.wasCreated()).toBe(true);
    expect(toTest.wasUpdated()).toBe(false);
    expect(toTest.body).toEqual(thing);
  });
  it('handles an empty response', () => {
    const toTest = new PutResponse(undefined, 201, new Map());
    expect(toTest.wasCreated()).toBe(false);
    expect(toTest.wasUpdated()).toBe(true);
    expect(toTest.body).toBe(undefined);
  });
});
