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
import { BufferlessResilienceHandlerFactory } from './request-factory/resilience/websocket-resilience-handler-bufferless';
import { StandardResilienceHandlerFactory } from './request-factory/resilience/websocket-resilience-handler-standard';
import {
  NoopWebSocketStateHandler,
  ResilienceHandlerFactoryContextStep,
  WebSocketStateHandler
} from './request-factory/resilience/websocket-resilience-interfaces';
import { WebSocketRequestSenderFactory } from './request-factory/websocket-request-sender';
import { WebSocketImplementationBuilderUrl, WebSocketRequestHandler } from './request-factory/websocket-request-handler';
import {
  AllDittoWebSocketHandles,
  DefaultDittoWebSocketClient,
  DittoWebSocketClient,
  DittoWebSocketLiveClient,
  DittoWebSocketTwinClient
} from './ditto-client-websocket';
import { WebSocketThingsHandle } from './handles/things.interfaces';
import { WebSocketMessagesHandle } from './handles/messages-websocket';
import { CommandsHandle } from './handles/commands';
import { EventsHandle } from './handles/events';
import { FeaturesHandle } from './handles/features.interfaces';
import { AuthProvider, ImmutableURL } from '../auth/auth-provider';
import { ApiVersion, Channel } from '../model/ditto-protocol';
import {
  AbstractBuilder,
  BuildStep,
  CustomBuilderContext,
  CustomCommandsHandleStep,
  CustomEventsHandleStep,
  CustomFeaturesHandleStep,
  CustomMessagesHandleStep,
  CustomThingsHandleStep,
  ProtocolStep
} from './builder-steps';

export interface WebSocketBuilderInitialStep extends ProtocolStep<WebSocketBufferStep> {
}

export interface WebSocketBufferStep extends BuildStep {
  withoutBuffer(): WebSocketChannelStep;

  withBuffer(size: number): WebSocketChannelStep;
}

export interface WebSocketChannelStep extends BuildStep {
  liveChannel(): WebSocketBuildStepLive;

  twinChannel(): WebSocketBuildStepTwin;
}

export interface WebSocketStateHandlerStep extends BuildStep {
  withStateHandler(handler: WebSocketStateHandler): this;
}

export interface WebSocketBuildStep extends BuildStep, WebSocketStateHandlerStep {

  /**
   * Builds a DittoClient for the selected API.
   *
   * @returns The DittoClient
   */
  build(): DittoWebSocketClient;
}

/**
 * Step that allows to add custom handles for a client.
 */
export interface WebSocketCustomHandlesBuildStep extends BuildStep, WebSocketBuildStep,
  CustomThingsHandleStep<WebSocketRequestSenderFactory, WebSocketThingsHandle>,
  CustomFeaturesHandleStep<WebSocketRequestSenderFactory, FeaturesHandle>,
  CustomEventsHandleStep<WebSocketRequestSenderFactory, EventsHandle>,
  CustomMessagesHandleStep<WebSocketRequestSenderFactory, WebSocketMessagesHandle>,
  CustomCommandsHandleStep<WebSocketRequestSenderFactory, CommandsHandle> {
}

export interface WebSocketBuildStepLive extends WebSocketCustomHandlesBuildStep {
  build(): DittoWebSocketLiveClient;
}

export interface WebSocketBuildStepTwin extends WebSocketCustomHandlesBuildStep {
  build(): DittoWebSocketTwinClient;
}

/**
 * Implementation of all the methods to build a Context.
 */
export class WebSocketClientBuilder extends AbstractBuilder<WebSocketBufferStep>
  implements WebSocketBufferStep, WebSocketChannelStep, WebSocketBuildStepLive,
    WebSocketBuildStepTwin {

  private channel!: Channel;
  private stateHandler: WebSocketStateHandler;
  private resilienceFactory!: ResilienceHandlerFactoryContextStep;
  private customHandles: AllDittoWebSocketHandles;

  private constructor(private readonly builder: WebSocketImplementationBuilderUrl) {
    super();
    this.customHandles = {};
    this.stateHandler = new NoopWebSocketStateHandler();
  }

  /**
   * Creates a new WebSocket Builder.
   *
   * @param urlBuilder The url builder used by the builder.
   */
  public static newBuilder(urlBuilder: WebSocketImplementationBuilderUrl): WebSocketBuilderInitialStep {
    return new WebSocketClientBuilder(urlBuilder);
  }

  finalize(): WebSocketBufferStep {
    return this;
  }

  withStateHandler(handler: WebSocketStateHandler): this {
    this.stateHandler = handler;
    return this;
  }

  withBuffer(size: number): WebSocketChannelStep {
    this.resilienceFactory = StandardResilienceHandlerFactory.getInstance(size);
    return this;
  }

  withoutBuffer(): WebSocketChannelStep {
    this.resilienceFactory = BufferlessResilienceHandlerFactory.getInstance();
    return this;
  }

  liveChannel(): WebSocketBuildStepLive {
    this.channel = Channel.live;
    return this;
  }

  twinChannel(): WebSocketBuildStepTwin {
    this.channel = Channel.twin;
    return this;
  }

  withCustomThingsHandle(factory: (requestSenderFactory: WebSocketRequestSenderFactory,
                                   customBuilderContext?: CustomBuilderContext) => WebSocketThingsHandle): this {
    this.customHandles = Object.assign(this.customHandles, { thingsHandle: factory });
    return this;
  }

  withCustomFeaturesHandle(factory: (requestSenderFactory: WebSocketRequestSenderFactory, thingsId: string,
                                     customBuilderContext?: CustomBuilderContext) => FeaturesHandle): this {
    this.customHandles = Object.assign(this.customHandles, { featuresHandle: factory });
    return this;
  }

  withCustomMessagesHandle(factory: (requestSenderFactory: WebSocketRequestSenderFactory,
                                     customBuilderContext?: CustomBuilderContext) => WebSocketMessagesHandle): this {
    this.customHandles = Object.assign(this.customHandles, { messagesHandle: factory });
    return this;
  }

  withCustomEventsHandle(factory: (requestSenderFactory: WebSocketRequestSenderFactory,
                                   customBuilderContext?: CustomBuilderContext) => EventsHandle): this {
    this.customHandles = Object.assign(this.customHandles, { eventsHandle: factory });
    return this;
  }

  withCustomCommandsHandle(factory: (requestSenderFactory: WebSocketRequestSenderFactory,
                                     customBuilderContext?: CustomBuilderContext) => CommandsHandle): this {
    this.customHandles = Object.assign(this.customHandles, { commandsHandle: factory });
    return this;
  }

  build(): DefaultDittoWebSocketClient {
    const protocol = this.tls ? 'wss' : 'ws';
    const path = (this.customPath === undefined) ? '/ws' : this.customPath;

    const resilienceHandlerFactory = this.resilienceFactory.withContext(
      this.builder.withConnectionDetails(
        ImmutableURL.newInstance(protocol, this.domain, `${path}/${this.apiVersion}`),
        this.authProviders),
      this.stateHandler);
    const requester = new WebSocketRequestHandler(resilienceHandlerFactory);
    return DefaultDittoWebSocketClient.getInstance(new WebSocketRequestSenderFactory(this.apiVersion, this.channel, requester), requester,
      this.customHandles);
  }

  buildClient(tls: boolean, domain: string, apiVersion: ApiVersion, stateHandler: WebSocketStateHandler, channel: Channel,
              authProviders: AuthProvider[]): DittoWebSocketClient {
    this.tls = tls;
    this.domain = domain;
    this.apiVersion = apiVersion;
    this.stateHandler = stateHandler;
    this.channel = channel;
    this.resilienceFactory = BufferlessResilienceHandlerFactory.getInstance();
    this.authProviders = authProviders;
    return this.build();
  }
}
