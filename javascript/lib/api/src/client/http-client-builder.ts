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
import { HttpRequester, HttpRequestSenderBuilder } from './request-factory/http-request-sender';
import {
  DefaultDittoHttpClient,
  DittoHttpClient,
  DittoHttpClientHandles
} from './ditto-client-http';
import { HttpMessagesHandle } from './handles/messages-http';
import { PoliciesHandle } from './handles/policies';
import { SearchHandle } from './handles/search';
import { FeaturesHandle } from './handles/features.interfaces';
import { AuthProvider, DittoURL, ImmutableURL } from '../auth/auth-provider';
import { ApiVersion } from '../model/ditto-protocol';
import {
  AbstractBuilder,
  AuthenticationStep,
  BuildStep,
  CustomBuilderContext,
  CustomFeaturesHandleStep,
  CustomMessagesHandleStep,
  CustomPoliciesHandleStep,
  CustomSearchHandleStep,
  CustomThingsHandleStep,
  EnvironmentStep,
  ProtocolStep
} from './builder-steps';

export interface HttpBuilderInitialStep extends ProtocolStep<HttpClientBuildStep> {
}

export interface HttpBuildStep<C extends DittoHttpClient> extends BuildStep {

  /**
   * Builds a DittoClient for the selected API.
   *
   * @returns The DittoClient
   */
  build(): C;
}

/**
 * Build step for adding custom handles.
 * @param <H> - Type of the things handle.
 * @param <C> - Type of the client.
 */
export interface HttpCustomHandlesBuildStep<H extends HttpThingsHandle, C extends DittoHttpClient>
  extends HttpBuildStep<C>,
    BuildStep,
    CustomThingsHandleStep<HttpRequestSenderBuilder, H>,
    CustomFeaturesHandleStep<HttpRequestSenderBuilder, FeaturesHandle>,
    CustomMessagesHandleStep<HttpRequestSenderBuilder, HttpMessagesHandle>,
    CustomPoliciesHandleStep<HttpRequestSenderBuilder, PoliciesHandle>,
    CustomSearchHandleStep<HttpRequestSenderBuilder, SearchHandle> {
}


/**
 * Interface to build the Context.
 */
export interface HttpClientBuildStep extends HttpCustomHandlesBuildStep<HttpThingsHandle, DittoHttpClient> {
}

/**
 * Implementation of all the methods to build a Context.
 */
export class HttpClientBuilder extends AbstractBuilder<HttpClientBuildStep> implements HttpBuilderInitialStep,
  EnvironmentStep<HttpClientBuildStep>, AuthenticationStep<HttpClientBuildStep>, HttpClientBuildStep {
  private customHandles: DittoHttpClientHandles = {};

  private constructor(private readonly requester: HttpRequester) {
    super();
  }

  /**
   * Build a new HttpClientBuilder.
   *
   * @param requester - The requester to use.
   */
  public static newBuilder(requester: HttpRequester): HttpBuilderInitialStep {
    return new HttpClientBuilder(requester);
  }

  finalize(): HttpClientBuildStep {
    return this;
  }

  // TODO: rebuild so that DittoHttpClient interface can be used
  build(): DefaultDittoHttpClient {
    const url = this.buildUrl();
    return DefaultDittoHttpClient.getInstance(new HttpRequestSenderBuilder(this.requester, url, this.authProviders), this.customHandles);
  }

  buildClient(tls: boolean, domain: string, apiVersion: ApiVersion, authProviders: AuthProvider[]) {
    this.tls = tls;
    this.domain = domain;
    this.apiVersion = apiVersion;
    this.authProviders = authProviders;
    return this.build();
  }

  private buildUrl(): DittoURL {
    const protocol = this.tls ? 'https' : 'http';
    const path = (this.customPath === undefined) ? '/api' : this.customPath;
    return ImmutableURL.newInstance(protocol, this.domain, `${path}/${this.apiVersion}`);
  }

  withCustomThingsHandle(factory: (requestSenderBuilder: HttpRequestSenderBuilder,
                                   customBuilderContext?: CustomBuilderContext) => HttpThingsHandle): this {
    this.customHandles = Object.assign(this.customHandles, { thingsHandle: factory });
    return this;
  }

  withCustomFeaturesHandle(factory: (requestSenderBuilder: HttpRequestSenderBuilder, thingId: string,
                                     customBuilderContext?: CustomBuilderContext) => FeaturesHandle): this {
    this.customHandles = Object.assign(this.customHandles, { featuresHandle: factory });
    return this;
  }

  withCustomMessagesHandle(factory: (requestSenderBuilder: HttpRequestSenderBuilder,
                                     customBuilderContext?: CustomBuilderContext) => HttpMessagesHandle): this {
    this.customHandles = Object.assign(this.customHandles, { messagesHandle: factory });
    return this;
  }

  withCustomPoliciesHandle(factory: (requestSenderBuilder: HttpRequestSenderBuilder,
                                     customBuilderContext?: CustomBuilderContext) => PoliciesHandle): this {
    this.customHandles = Object.assign(this.customHandles, { policiesHandle: factory });
    return this;
  }

  withCustomSearchHandle(factory: (requestSenderBuilder: HttpRequestSenderBuilder,
                                   customBuilderContext?: CustomBuilderContext) => SearchHandle): this {
    this.customHandles = Object.assign(this.customHandles, { searchHandle: factory });
    return this;
  }

}
