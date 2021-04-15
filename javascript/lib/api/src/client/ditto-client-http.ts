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

import { HttpThingsHandle } from './handles/things.interfaces';
import { FeaturesHandle } from './handles/features.interfaces';
import { DefaultHttpMessagesHandle, HttpMessagesHandle } from './handles/messages-http';
import { DefaultSearchHandle, SearchHandle } from './handles/search';
import { DefaultPoliciesHandle, PoliciesHandle } from './handles/policies';
import { HttpRequestSenderBuilder } from './request-factory/http-request-sender';
import { AbstractDittoClient, DittoClient, DittoClientHandles } from './ditto-client';
import { DefaultThingsHandle } from './handles/things';
import { DefaultFeaturesHandle } from './handles/features';
import { CustomBuilderContext } from './builder-steps';

export interface DittoHttpClient extends DittoClient<HttpThingsHandle, FeaturesHandle> {

  /**
   * Builds a handle to handle Messages requests.
   *
   * @return a HttpMessagesHandle.
   */
  getMessagesHandle(customBuildContext?: CustomBuilderContext): HttpMessagesHandle;

  /**
   * Builds a handle to handle Search requests.
   *
   * @return a Search.
   */
  getSearchHandle(customBuildContext?: CustomBuilderContext): SearchHandle;

  /**
   * Builds a handle to handle Policies requests.
   *
   * @return a PoliciesHandle.
   */
  getPoliciesHandle(customBuildContext?: CustomBuilderContext): PoliciesHandle;
}

export interface DittoHttpClientHandles extends DittoClientHandles<HttpRequestSenderBuilder> {
  thingsHandle?: (requestSenderBuilder: HttpRequestSenderBuilder, customBuildContext?: CustomBuilderContext) => HttpThingsHandle;
  messagesHandle?: (requestSenderBuilder: HttpRequestSenderBuilder, customBuildContext?: CustomBuilderContext) => HttpMessagesHandle;
  searchHandle?: (requestSenderBuilder: HttpRequestSenderBuilder, customBuildContext?: CustomBuilderContext) => SearchHandle;
  policiesHandle?: (requestSenderBuilder: HttpRequestSenderBuilder, customBuildContext?: CustomBuilderContext) => PoliciesHandle;
}

class DefaultHandles implements DittoHttpClientHandles {
  thingsHandle = DefaultThingsHandle.getInstance;
  featuresHandle = DefaultFeaturesHandle.getInstance;
  messagesHandle = DefaultHttpMessagesHandle.getInstance;
  policiesHandle = DefaultPoliciesHandle.getInstance;
  searchHandle = DefaultSearchHandle.getInstance;
}

export class DefaultDittoHttpClient extends AbstractDittoClient<HttpRequestSenderBuilder, DittoHttpClientHandles>
  implements DittoHttpClient {

  private constructor(builder: HttpRequestSenderBuilder, handles: DittoHttpClientHandles) {
    super(builder, handles);
  }

  /**
   * Returns an instance of DittoClient based on the context provided.
   *
   * @param builder - The request sender builder to build on.
   * @param customHandles - Custom handles to use instead of the default ones.
   * @return the DittoClient instance.
   */
  public static getInstance(builder: HttpRequestSenderBuilder, customHandles?: DittoHttpClientHandles): DefaultDittoHttpClient {
    const handles: DittoHttpClientHandles = Object.assign(new DefaultHandles(), customHandles);
    return new DefaultDittoHttpClient(builder, handles);
  }

  public getFeaturesHandle(thingId: string, customBuildContext?: CustomBuilderContext): FeaturesHandle {
    return this.handles.featuresHandle!(this.builder, thingId, customBuildContext);
  }

  public getThingsHandle(customBuildContext?: CustomBuilderContext): HttpThingsHandle {
    return this.handles.thingsHandle!(this.builder, customBuildContext);
  }

  public getMessagesHandle(customBuildContext?: CustomBuilderContext): HttpMessagesHandle {
    return this.handles.messagesHandle!(this.builder, customBuildContext);
  }

  public getPoliciesHandle(customBuildContext?: CustomBuilderContext): PoliciesHandle {
    return this.handles.policiesHandle!(this.builder, customBuildContext);
  }

  public getSearchHandle(customBuildContext?: CustomBuilderContext): SearchHandle {
    return this.handles.searchHandle!(this.builder, customBuildContext);
  }

}
