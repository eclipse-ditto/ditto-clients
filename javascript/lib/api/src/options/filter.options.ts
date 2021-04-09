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
/**
 * Class that can provide a filter string
 */
export interface Filter {
  /**
   * Returns the filter string.
   *
   * @return the filter in string form.
   */
  toString(): string;
}

class DefaultFilter implements Filter {
  constructor(private readonly _value: string) {
  }

  /**
   * Returns the filter string.
   *
   * @return the filter in string form.
   */
  toString(): string {
    return this._value;
  }
}

/**
 * Combines Filters to an and-Filter. All Filters provided must be fulfilled.
 *
 * @param filter - The filters to combine.
 * @return The and-Filter.
 */
export const And: (...filter: Filter[]) => Filter =
  (...filter: Filter[]) => {
    return new DefaultFilter(`and(${toList(filter)})`);
  };

/**
 * Combines Filters to an or-Filter. At least one Filter provided must be fulfilled.
 *
 * @param filter - The filters to combine.
 * @return The or-Filter.
 */
export const Or: (...filter: Filter[]) => Filter =
  (...filter: Filter[]) => {
    return new DefaultFilter(`or(${toList(filter)})`);
  };

/**
 * Negates a Filter to a not-Filter.
 *
 * @param query - The filter to negate.
 * @return The not-Filter.
 */
export const Not: (query: Filter) => Filter =
  (query: Filter) => {
    return new DefaultFilter(`not(${query.toString()})`);
  };


/**
 * Builds an eq-Filter. The body of the property must equal the body
 *
 * @param property - The property to check.
 * @param value - The body to check for.
 * @return The eq-Filter.
 */
export const Eq: (property: string, value: any) => Filter =
  (property: string, value: any) => {
    return standardFilter('eq', property, value);
  };

/**
 * Builds a ne-Filter. The body of the property must not equal the body
 *
 * @param property - The property to check.
 * @param value - The body to check for.
 * @return The ne-Filter.
 */
export const Ne: (property: string, value: any) => Filter =
  (property: string, value: any) => {
    return standardFilter('ne', property, value);
  };

/**
 * Builds a gt-Filter. The body of the property must be greater than the body
 *
 * @param property - The property to check.
 * @param value - The body to check for.
 * @return The gt-Filter.
 */
export const Gt: (property: string, value: any) => Filter =
  (property: string, value: any) => {
    return standardFilter('gt', property, value);
  };

/**
 * Builds a ge-Filter. The body of the property must be greater than or equal to the body
 *
 * @param property - The property to check.
 * @param value - The body to check for.
 * @return The ge-Filter.
 */
export const Ge: (property: string, value: any) => Filter =
  (property: string, value: any) => {
    return standardFilter('ge', property, value);
  };

/**
 * Builds a lt-Filter. The body of the property must be less than the body
 *
 * @param property - The property to check.
 * @param value - The body to check for.
 * @return The lt-Filter.
 */
export const Lt: (property: string, value: any) => Filter =
  (property: string, value: any) => {
    return standardFilter('lt', property, value);
  };

/**
 * Builds a le-Filter. The body of the property must be less than or equal to the body
 *
 * @param property - The property to check.
 * @param value - The body to check for.
 * @return The le-Filter.
 */
export const Le: (property: string, value: any) => Filter =
  (property: string, value: any) => {
    return standardFilter('le', property, value);
  };


/**
 * Builds an in-Filter. The body of the property must be among the values
 *
 * @param property - The property to check.
 * @param value - The values to check for.
 * @return The in-Filter.
 */
export const In: (property: string, ...value: any[]) => Filter =
  (property: string, ...value: any[]) => {
    return new DefaultFilter(`in(${property},${value.map(stringify).join()})`);
  };

/**
 * Builds a like-Filter. The body of the property must conform to the form provided.
 *
 * @param property - The property to check.
 * @param form - The values to check for.
 * @return The like-Filter.
 */
export const Like: (property: string, form: string) => Filter =
  (property: string, form: string) => {
    return standardFilter('like', property, form);
  };

/**
 * Builds a exists-Filter. The property must exist.
 *
 * @param property - The property to check.
 * @return The exists-Filter.
 */
export const Exists: (property: string) => Filter =
  (property: string) => {
    return new DefaultFilter(`exists(${property})`);
  };


/**
 * Combines Filters to one string.
 *
 * @param filter - The filters to combine.
 * @return The combined Filter.
 */
const toList: (filter: Filter[]) => string =
  (filter: Filter[]) => {
    return filter.map(f => f.toString()).join();
  };

/**
 * Returns a string representation that can be used as a filter value:
 * number & boolean -> string representation
 * object -> JSON string
 * string -> string in quotes
 *
 * @param value The value of arbitrary type to stringify
 */
const stringify: (value: any) => string =
  (value => {
    if (typeof value === 'boolean' || typeof value === 'number') {
      return value.toString();
    }
    if (typeof value === 'object') {
      return JSON.stringify(value);
    }
    return `"${value}"`;
  });

/**
 * Standard operation for building Filters.
 *
 * @param operation - The type of Filter to build.
 * @param property - The property to check.
 * @param value - The body to check for.
 * @return The Filter.
 */
const standardFilter: (operation: string, property: string, value: any) => Filter =
  (operation: string, property: string, value: any) => {
    return new DefaultFilter(`${operation}(${property},${stringify(value)})`);
  };
