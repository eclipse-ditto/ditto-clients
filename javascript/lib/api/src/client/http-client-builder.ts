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

import { HttpThingsHandle, HttpThingsHandleV1, HttpThingsHandleV2 } from './handles/things.interfaces';
import { HttpRequester, HttpRequestSenderBuilder } from './request-factory/http-request-sender';
import { AllDittoHttpHandles, DefaultDittoHttpClient, DittoHttpClient, DittoHttpClientV1, DittoHttpClientV2 } from './ditto-client-http';
import { HttpMessagesHandle } from './handles/messages-http';
import { PoliciesHandle } from './handles/policies';
import { SearchHandle } from './handles/search';
import { FeaturesHandle } from './handles/features.interfaces';
import { AuthProvider, DittoURL, ImmutableURL } from '../auth/auth-provider';
import { ApiVersion } from '../model/ditto-protocol';
import {
  AbstractBuilder,
  ApiVersionStep,
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

export interface HttpBuilderInitialStep extends ProtocolStep<BuildStepApi1, BuildStepApi2> {
}

export interface HttpBuildStep<H extends HttpThingsHandle, C extends DittoHttpClient<H>> extends BuildStep {

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
export interface HttpCustomHandlesBuildStep<H extends HttpThingsHandle, C extends DittoHttpClient<H>>
  extends HttpBuildStep<H, C>,
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
export interface BuildStepApi1 extends HttpCustomHandlesBuildStep<HttpThingsHandleV1, DittoHttpClientV1>, BuildStep {
}

/**
 * Interface to build the Context.
 */
export interface BuildStepApi2 extends HttpCustomHandlesBuildStep<HttpThingsHandleV2, DittoHttpClientV2>, BuildStep {
}

/**
 * Implementation of all the methods to build a Context.
 */
export class HttpClientBuilder extends AbstractBuilder<BuildStepApi1, BuildStepApi2> implements HttpBuilderInitialStep,
  EnvironmentStep<BuildStepApi1, BuildStepApi2>, ApiVersionStep<BuildStepApi1, BuildStepApi2>,
  AuthenticationStep<BuildStepApi1, BuildStepApi2>, BuildStepApi1, BuildStepApi2 {
  private customHandles: AllDittoHttpHandles = {};

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

  apiVersion1(): BuildStepApi1 {
    this.apiVersion = ApiVersion.V1;
    return this;
  }

  apiVersion2(): BuildStepApi2 {
    this.apiVersion = ApiVersion.V2;
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
    return ImmutableURL.newInstance(protocol, this.domain, `/api/${this.apiVersion}`);
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
