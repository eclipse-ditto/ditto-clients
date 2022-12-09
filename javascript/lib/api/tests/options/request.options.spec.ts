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

import { And, Eq, Ne, Not } from '../../src/options/filter.options';
import {
  CountOptions,
  DefaultCountOptions,
  DefaultFieldsOptions,
  DefaultGetThingsOptions,
  DefaultMatchOptions,
  DefaultMessagesOptions,
  DefaultPostConnectionOptions,
  DefaultSearchOptions,
  FieldsOptions,
  MatchOptions,
  SearchOptions
} from '../../src/options/request.options';

describe('Match Options', () => {
  const match = 'If-Match';
  const noneMatch = 'If-None-Match';
  let matchOptions: MatchOptions;
  beforeEach(() => {
    matchOptions = DefaultMatchOptions.getInstance();
  });
  it('sets If-Match header', () => {
    matchOptions.ifMatch('A');
    expect(matchOptions.getHeaders().get(match)).toEqual('A');
  });
  it('overrides If-Match header', () => {
    matchOptions.ifMatch('A');
    matchOptions.ifMatch('B');
    expect(matchOptions.getHeaders().get(match)).toEqual('B');
  });
  it('sets If-None-Match header', () => {
    matchOptions.ifNoneMatch('A');
    expect(matchOptions.getHeaders().get(noneMatch)).toEqual('A');
  });
  it('overrides If-None-Match header', () => {
    matchOptions.ifNoneMatch('A');
    matchOptions.ifNoneMatch('B');
    expect(matchOptions.getHeaders().get(noneMatch)).toEqual('B');
  });
  it('combines headers', () => {
    matchOptions.ifMatch('A').ifNoneMatch('B');
    expect(matchOptions.getHeaders().get(match)).toEqual('A');
    expect(matchOptions.getHeaders().get(noneMatch)).toEqual('B');
  });
  it('overrides combined headers', () => {
    matchOptions.ifMatch('A').ifNoneMatch('B');
    matchOptions.ifMatch('C').ifNoneMatch('D');
    expect(matchOptions.getHeaders().get(match)).toEqual('C');
    expect(matchOptions.getHeaders().get(noneMatch)).toEqual('D');
  });
  it('matches multiple', () => {
    matchOptions.ifMatch('A', 'B').ifNoneMatch('C', 'D');
    expect(matchOptions.getHeaders().get(match)).toEqual('A, B');
    expect(matchOptions.getHeaders().get(noneMatch)).toEqual('C, D');
  });
  it('matches any', () => {
    matchOptions.ifMatchAny().ifNoneMatchAny();
    expect(matchOptions.getHeaders().get(match)).toEqual('*');
    expect(matchOptions.getHeaders().get(noneMatch)).toEqual('*');
  });
});

describe('Get Options', () => {
  let getOptions: FieldsOptions;
  beforeEach(() => {
    getOptions = DefaultFieldsOptions.getInstance();
  });
  it('returns empty options', () => {
    expect(getOptions.getOptions().size).toEqual(0);
  });
  it('sets fields', () => {
    getOptions.withFields('A', 'B');
    expect(getOptions.getOptions().get('fields')).toEqual(encodeURIComponent('A,B'));
  });
  it('overrides fields', () => {
    getOptions.withFields('A', 'B');
    getOptions.withFields('C', 'D');
    expect(getOptions.getOptions().get('fields')).toEqual(encodeURIComponent('C,D'));
  });
});

describe('Count Options', () => {
  let countOptions: CountOptions;
  beforeEach(() => {
    countOptions = DefaultCountOptions.getInstance();
  });
  it('sets raw filter', () => {
    countOptions.withRawFilter('test');
    expect(countOptions.getOptions().get('filter')).toEqual('test');
  });
  it('overrides raw filter', () => {
    countOptions.withRawFilter('test');
    countOptions.withRawFilter('anotherTest');
    expect(countOptions.getOptions().get('filter')).toEqual('anotherTest');
  });
  it('sets filter', () => {
    countOptions.withFilter(And(Ne('Prop', 7)));
    expect(countOptions.getOptions().get('filter')).toEqual(encodeURIComponent('and(ne(Prop,7))'));
  });
  it('overrides filter', () => {
    countOptions.withFilter(And(Ne('Prop', 7)));
    countOptions.withFilter(Not(Eq('Prop2', 'Value')));
    expect(countOptions.getOptions().get('filter')).toEqual(encodeURIComponent('not(eq(Prop2,"Value"))'));
  });
  it('sets namespaces', () => {
    countOptions.withNamespaces('A', 'B');
    expect(countOptions.getOptions().get('namespaces')).toEqual(encodeURIComponent('A,B'));
  });
  it('overrides namespaces', () => {
    countOptions.withNamespaces('A', 'B');
    countOptions.withNamespaces('C', 'D');
    expect(countOptions.getOptions().get('namespaces')).toEqual(encodeURIComponent('C,D'));
  });
  it('combines options', () => {
    countOptions.withNamespaces('A', 'B').withRawFilter('C');
    const toTest = countOptions.getOptions();
    const expectedNamespaces = encodeURIComponent('A,B');
    const expectedFilter = 'C';
    expect(toTest.get('namespaces')).toEqual(expectedNamespaces);
    expect(toTest.get('filter')).toEqual(expectedFilter);
  });
  it('overrides combined options', () => {
    countOptions.withNamespaces('A', 'B').withRawFilter('C');
    countOptions.withNamespaces('D', 'E').withRawFilter('F');
    const toTest = countOptions.getOptions();
    expect(toTest.get('namespaces')).toEqual(encodeURIComponent('D,E'));
    expect(toTest.get('filter')).toEqual('F');
  });
});

describe('Search Options', () => {
  let searchOptions: SearchOptions;
  beforeEach(() => {
    searchOptions = DefaultSearchOptions.getInstance();
  });
  it('sets fields', () => {
    searchOptions.withFields('A', 'B');
    expect(searchOptions.getOptions().get('fields')).toEqual(encodeURIComponent('A,B'));
  });
  it('overrides fields', () => {
    searchOptions.withFields('A', 'B');
    searchOptions.withFields('C', 'D');
    expect(searchOptions.getOptions().get('fields')).toEqual(encodeURIComponent('C,D'));
  });
  it('sets raw filter', () => {
    searchOptions.withRawFilter('A');
    expect(searchOptions.getOptions().get('filter')).toEqual('A');
  });
  it('overrides raw filter', () => {
    searchOptions.withRawFilter('A');
    searchOptions.withRawFilter('B');
    expect(searchOptions.getOptions().get('filter')).toEqual('B');
  });
  it('sets filter', () => {
    searchOptions.withFilter(And(Ne('Prop', 7)));
    expect(searchOptions.getOptions().get('filter')).toEqual(encodeURIComponent('and(ne(Prop,7))'));
  });
  it('overrides filter', () => {
    searchOptions.withFilter(And(Ne('Prop', 7)));
    searchOptions.withFilter(Not(Eq('Prop2', 'Value')));
    expect(searchOptions.getOptions().get('filter')).toEqual(encodeURIComponent('not(eq(Prop2,"Value"))'));
  });
  it('sets limit', () => {
    searchOptions.withLimit(1, 2);
    expect(searchOptions.getOptions().get('option')).toEqual(encodeURIComponent('limit(1,2)'));
  });
  it('overrides limit', () => {
    searchOptions.withLimit(1, 2);
    searchOptions.withLimit(3, 4);
    expect(searchOptions.getOptions().get('option')).toEqual(encodeURIComponent('limit(3,4)'));
  });
  it('sets namespaces', () => {
    searchOptions.withNamespaces('A', 'B');
    expect(searchOptions.getOptions().get('namespaces')).toEqual(encodeURIComponent('A,B'));
  });
  it('overrides splitNamespace', () => {
    searchOptions.withNamespaces('A', 'B');
    searchOptions.withNamespaces('C', 'D');
    expect(searchOptions.getOptions().get('namespaces')).toEqual(encodeURIComponent('C,D'));
  });
  it('sets sort', () => {
    searchOptions.withSort('+A');
    expect(searchOptions.getOptions().get('option')).toEqual(encodeURIComponent('sort(+A)'));
  });
  it('overrides sort', () => {
    searchOptions.withSort('+A');
    searchOptions.withSort('B');
    expect(searchOptions.getOptions().get('option')).toEqual(encodeURIComponent('sort(B)'));
  });
  it('combines option', () => {
    searchOptions.withLimit(1, 2).withSort('+A');
    expect(searchOptions.getOptions().get('option')).toEqual(encodeURIComponent('limit(1,2),sort(+A)'));
  });
  it('overrides combined option', () => {
    searchOptions.withLimit(1, 2).withSort('+A');
    searchOptions.withLimit(3, 4).withSort('-B');
    expect(searchOptions.getOptions().get('option')).toEqual(encodeURIComponent('limit(3,4),sort(-B)'));
  });
  it('sets cursor', () => {
    searchOptions.withCursor('eJylkD1PwzAQhv-LpxQc5YM0SbNBBBJDp0oslMGxz82JYJfLBQbEf8cpQupGKfZ29zz3Su');
    expect(searchOptions.getOptions().get('option')).toEqual('cursor(eJylkD1PwzAQhv-LpxQc5YM0SbNBBBJDp0oslMGxz82JYJfLBQbEf8cpQupGKfZ29zz3Su)');
  });
  it('overrides cursor', () => {
    searchOptions.withCursor('foo');
    searchOptions.withCursor('bar');
    expect(searchOptions.getOptions().get('option')).toEqual('cursor(bar)');
  });
  it('sets page size', () => {
    searchOptions.withPageSize(42);
    expect(searchOptions.getOptions().get('option')).toEqual('size(42)');
  });
  it('overrides page size', () => {
    searchOptions.withPageSize(42);
    searchOptions.withPageSize(10);
    expect(searchOptions.getOptions().get('option')).toEqual('size(10)');
  });
  it('sets cursor and size', () => {
    searchOptions.withCursor('eJylkD1PwzAQhv-LpxQc5YM0SbNBBBJDp0oslMGxz82JYJfLBQbEf8cpQupGKfZ29zz3Su').withPageSize(42);
    expect(searchOptions.getOptions().get('option')).toEqual(encodeURIComponent('cursor(eJylkD1PwzAQhv-LpxQc5YM0SbNBBBJDp0oslMGxz82JYJfLBQbEf8cpQupGKfZ29zz3Su),size(42)'));
  });
  it('throws an error when setting limit and cursor is already set', () => {
    searchOptions.withCursor('foo');
    expect(() => searchOptions.withLimit(10, 10)).toThrowError();
  });
  it('throws an error when setting pageSize and limit is already set', () => {
    searchOptions.withLimit(10, 10);
    expect(() => searchOptions.withPageSize(10)).toThrowError();
  });
  it('throws an error when setting cursor and limit is already set', () => {
    searchOptions.withLimit(10, 10);
    expect(() => searchOptions.withCursor('foo')).toThrowError();
  });

});

describe('Get Things Options', () => {
  const getThingsOptions = DefaultGetThingsOptions.getInstance();
  it('sets fields', () => {
    getThingsOptions.withFields('A', 'B');
    expect(getThingsOptions.getOptions().get('fields')).toEqual(encodeURIComponent('A,B'));
  });
  it('overrides fields', () => {
    getThingsOptions.withFields('A', 'B');
    getThingsOptions.withFields('C', 'D');
    expect(getThingsOptions.getOptions().get('fields')).toEqual(encodeURIComponent('C,D'));
  });
});

describe('Messages Options', () => {
  const messagesOptions = DefaultMessagesOptions.getInstance();
  it('sets timeout', () => {
    messagesOptions.withTimeout(1);
    expect(messagesOptions.getOptions().get('timeout')).toEqual('1');
  });
  it('overrides timeout', () => {
    messagesOptions.withTimeout(2);
    expect(messagesOptions.getOptions().get('timeout')).toEqual('2');
  });
});

describe('Post Connection Options', () => {
  const postConnectionOptions = DefaultPostConnectionOptions.getInstance();
  it('sets dry-run', () => {
    postConnectionOptions.asDryRun(false);
    expect(postConnectionOptions.getOptions().get('dry-run')).toEqual('false');
  });
  it('overrides dry-run', () => {
    postConnectionOptions.asDryRun(true);
    expect(postConnectionOptions.getOptions().get('dry-run')).toEqual('true');
  });
});
