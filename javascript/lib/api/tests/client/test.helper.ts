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

import { Feature, Thing } from '../../src/model/things.model';
import { AuthProvider, DittoHeaders, DittoURL } from '../../src/auth/auth-provider';

jasmine.DEFAULT_TIMEOUT_INTERVAL = 0;

export class Helper {
  public static readonly apiToken = '60ab384eb8a7d7c26dd';
  public static readonly testName = 'User';
  public static readonly password = 'Word';

  public static get errorBody() {
    return {
      status: 403,
      error: 'gateway:authentication.failed',
      message: 'Unauthorized.',
      description: 'Check if your credentials were correct.'
    };
  }

  public static readonly attributePath = 'anAttribute';

  public static get attribute() {
    return { name: 'Dampf', firstName: 'Hans' };
  }

  public static get attributes() {
    return { anAttribute: Helper.attribute, anotherOne: 3 };
  }

  private static readonly thingId = 'Testspace:Testthing';
  public static readonly thing: Thing = new Thing(Helper.thingId, Helper.thingId, Helper.attributes);
  public static readonly successMessage = 'Success!';
  public static readonly definition = ['def1'];
  public static readonly propertyPath = 'A31';
  public static readonly property = 'A66';
  public static readonly properties = { A21: 'A3', A31: Helper.property };
  public static readonly feature = new Feature('F1', Helper.definition, Helper.properties);
  public static readonly anotherFeature = new Feature('F2', ['def2'], { A22: 'A3', A32: 'A4' });
  public static readonly features = {
    [Helper.feature.id]: Helper.feature,
    [Helper.anotherFeature.id]: Helper.anotherFeature
  };


  public static testError(method: () => Promise<any>): Promise<any> {
    return method()
      .then(() => {
        fail('response was accepted');
      })
      .catch(error => {
        expect(error).toEqual(Helper.errorBody);
      });
  }

  public static basicAuthProvider(username: string, password: string): AuthProvider {
    return {
      authenticateWithHeaders(originalHeaders: DittoHeaders): DittoHeaders {
        return originalHeaders.set('Authorization', `${username}:${password}`);
      }, authenticateWithUrl(originalUrl: DittoURL): DittoURL {
        return originalUrl;
      }
    };
  }
}
