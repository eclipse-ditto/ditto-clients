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

/* tslint:disable:no-empty-interface */
import { FeaturesHandle } from './handles/features.interfaces';
import { ThingsHandle } from './handles/things.interfaces';
import { RequestSenderFactory } from './request-factory/request-sender';
import { CustomBuilderContext } from './builder-steps';

export interface DittoClient<T extends ThingsHandle, U extends FeaturesHandle> {

  /**
   * Builds a handle to handle Things requests.
   *
   * @param customBuildContext - The custom builder context.
   * @return a ThingsHandle.
   */
  getThingsHandle(customBuildContext?: CustomBuilderContext): T;

  /**
   * Builds a handle for the specified ThingId to handle Feature requests.
   *
   * @param customBuildContext - The custom builder context.
   * @param thingId - The ThingId to use.
   * @return a FeaturesHandle.
   */
  getFeaturesHandle(thingId: string, customBuildContext?: CustomBuilderContext): U;
}

export interface DittoClientHandles<T extends RequestSenderFactory> {
  thingsHandle?: (requestSenderBuilder: T, customBuildContext?: CustomBuilderContext) => ThingsHandle;
  featuresHandle?: (requestSenderBuilder: T, thingId: string, customBuildContext?: CustomBuilderContext) => FeaturesHandle;
}

export class AbstractDittoClient<T extends RequestSenderFactory, H extends DittoClientHandles<T>> {

  protected constructor(protected readonly builder: T, protected readonly handles: H) {
  }

  public getFeaturesHandle(thingId: string, customBuildContext?: CustomBuilderContext): FeaturesHandle {
    return this.handles.featuresHandle!(this.builder, thingId, customBuildContext);
  }

  public getThingsHandle(customBuildContext?: CustomBuilderContext): ThingsHandle {
    return this.handles.thingsHandle!(this.builder, customBuildContext);
  }
}
