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

import { WebSocketThingsHandle } from './handles/things.interfaces';
import { FeaturesHandle } from './handles/features.interfaces';
import { EventsHandle } from './handles/events';
import { DefaultWebSocketMessagesHandle, WebSocketMessagesHandle } from './handles/messages-websocket';
import { CommandsHandle, DefaultCommandsHandle } from './handles/commands';
import { WebSocketRequestSenderFactory } from './request-factory/websocket-request-sender';
import { WebSocketRequestHandler } from './request-factory/websocket-request-handler';
import { AbstractDittoClient, DittoClient, DittoClientHandles } from './ditto-client';
import { DefaultThingsHandle } from './handles/things';
import { DefaultFeaturesHandle } from './handles/features';
import { CustomBuilderContext } from './builder-steps';
import { WebSocketEventsHandle } from './handles/events-websocket';

export interface DittoWebSocketClient extends DittoClient<WebSocketThingsHandle, FeaturesHandle> {

  /**
   * Builds a handle to subscribe to Events.
   *
   * @return an EventsHandle.
   */
  getEventsHandle(customBuildContext?: CustomBuilderContext): EventsHandle;
}

export interface DittoWebSocketClientHandles extends DittoClientHandles<WebSocketRequestSenderFactory> {
  thingsHandle?: (requestSender: WebSocketRequestSenderFactory, customBuildContext?: CustomBuilderContext) => WebSocketThingsHandle;
  eventsHandle?: (requestSender: WebSocketRequestSenderFactory, requestHandler: WebSocketRequestHandler,
                  customBuildContext?: CustomBuilderContext) => EventsHandle;
}

// tslint:disable-next-line:no-empty-interface
export interface DittoWebSocketTwinClient extends DittoWebSocketClient {
}

export interface DittoWebSocketLiveClient extends DittoWebSocketClient {

  /**
   * Builds a handle to handle Messages requests and subscriptions.
   *
   * @return a WebSocketMessagesHandle.
   */
  getMessagesHandle(customBuildContext?: CustomBuilderContext): WebSocketMessagesHandle;

  /**
   * Builds a handle to subscribe to Commands.
   *
   * @return a CommandsHandle.
   */
  getCommandsHandle(customBuildContext?: CustomBuilderContext): CommandsHandle;
}

export interface DittoWebSocketLiveClientHandles extends DittoWebSocketClientHandles {
  messagesHandle?: (requestSenderFactory: WebSocketRequestSenderFactory, requestHandler: WebSocketRequestHandler,
                    customBuildContext?: CustomBuilderContext) => WebSocketMessagesHandle;
  commandsHandle?: (requestSenderFactory: WebSocketRequestSenderFactory, requestHandler: WebSocketRequestHandler,
                    customBuildContext?: CustomBuilderContext) => CommandsHandle;
}

export type AllDittoWebSocketHandles = DittoWebSocketLiveClientHandles;

class DefaultHandles implements AllDittoWebSocketHandles {
  thingsHandle = DefaultThingsHandle.getInstance;
  featuresHandle = DefaultFeaturesHandle.getInstance;
  eventsHandle = WebSocketEventsHandle.getInstance;
  messagesHandle = DefaultWebSocketMessagesHandle.getInstance;
  commandsHandle = DefaultCommandsHandle.getInstance;
}

export class DefaultDittoWebSocketClient extends AbstractDittoClient<WebSocketRequestSenderFactory, AllDittoWebSocketHandles>
  implements DittoWebSocketClient, DittoWebSocketLiveClient {

  private constructor(builder: WebSocketRequestSenderFactory,
                      private readonly responseHandler: WebSocketRequestHandler,
                      handles: DittoWebSocketLiveClientHandles) {
    super(builder, handles);
  }

  /**
   * Returns an instance of DittoClient based on the context provided.
   *
   * @param requestSenderFactory - The request sender factory to use.
   * @param requester - The requester to use.
   * @param customHandles - custom handles to use with the client.
   * @return the DittoClient instance.
   */
  public static getInstance(requestSenderFactory: WebSocketRequestSenderFactory, requester: WebSocketRequestHandler,
                            customHandles?: DittoWebSocketLiveClientHandles): DefaultDittoWebSocketClient {
    const handles: DittoWebSocketLiveClientHandles = Object.assign(new DefaultHandles(), customHandles);
    return new DefaultDittoWebSocketClient(requestSenderFactory, requester, handles);
  }

  public getThingsHandle(customBuildContext?: CustomBuilderContext): WebSocketThingsHandle {
    return this.handles.thingsHandle!(this.builder, customBuildContext);
  }

  public getEventsHandle(customBuildContext?: CustomBuilderContext): EventsHandle {
    return this.handles.eventsHandle!(this.builder, this.responseHandler, customBuildContext);
  }

  public getMessagesHandle(customBuildContext?: CustomBuilderContext): WebSocketMessagesHandle {
    return this.handles.messagesHandle!(this.builder, this.responseHandler, customBuildContext);
  }

  public getCommandsHandle(customBuildContext?: CustomBuilderContext): CommandsHandle {
    return this.handles.commandsHandle!(this.builder, this.responseHandler, customBuildContext);
  }
}
