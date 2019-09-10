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

import {
  authenticateWithUrlAndHeaders,
  AuthProvider,
  DittoURL,
  ResponseHandler,
  WebSocketImplementation,
  WebSocketImplementationBuilderHandler,
  WebSocketImplementationBuilderUrl
} from '@eclipse-ditto/ditto-javascript-client-api_0';
import { ClientOptions } from 'ws';
import { ProxyAgent } from './proxy-settings';
import WebSocket = require('ws');

/**
 * Converts a Map to a plain js object.
 *
 * @param aMap - the map to convert.
 * @return the plain js object.
 */
const mapToPlainObject = (aMap: Map<string, string>): { [key: string]: string } => {
  const objects = [...aMap.entries()].map(([k, v]) => ({ [k]: v }));
  return Object.assign({}, ...objects);
};

/**
 * NodeJs implementation of a web socket requester.
 */
export class NodeWebSocket implements WebSocketImplementation {
  private connected = true;

  private constructor(private webSocket: WebSocket,
                      private readonly webSocketUrl: string,
                      private readonly handler: ResponseHandler,
                      private readonly options: ClientOptions) {
    this.setHandles();
  }

  /**
   * Builds an instance of NodeWebSocket.
   *
   * @param url - The Url of the service.
   * @param handler - The handler that gets called for responses from the web socket.
   * @param authProviders - The auth providers to use.
   * @param agent - The proxy agent to use to establish the connection.
   * @return a Promise for the web socket connection.
   */
  public static buildInstance(url: DittoURL, handler: ResponseHandler,
                              authProviders: AuthProvider[], agent: ProxyAgent): Promise<NodeWebSocket> {
    return new Promise<NodeWebSocket>(resolve => {
      const [authenticatedUrl, authenticatedHeaders] = authenticateWithUrlAndHeaders(url, new Map(), authProviders);
      const plainHeaders = mapToPlainObject(authenticatedHeaders);
      const options: ClientOptions = {
        agent: agent.proxyAgent,
        rejectUnauthorized: false,
        headers: plainHeaders
      };

      const plainUrl = authenticatedUrl.toString();
      const webSocket = new WebSocket(plainUrl, options);
      webSocket.on('open', () => {
        resolve(new NodeWebSocket(webSocket, plainUrl, handler, options));
      });
    });
  }

  public executeCommand(request: string): void {
    this.webSocket.send(request);
  }

  /**
   * Reestablishes a failed web socket connection.
   *
   * @param retry - The amount of time to wait to reconnect.
   * @return a Promise for the reestablished web socket connection.
   */
  private reconnect(retry: number): Promise<WebSocketImplementation> {
    return new Promise<WebSocketImplementation>((resolve, reject) => {
      this.webSocket = new WebSocket(this.webSocketUrl, this.options);
      this.webSocket.on('open', () => {
        this.connected = true;
        this.setHandles();
        resolve(this);
      });
      setTimeout(() => {
        if (this.connected) {
          return;
        }
        if (retry <= 120000) {
          console.log('Reconnect failed! Trying again!');
          resolve(this.reconnect(retry * 2));
        }
        console.log('Reconnect failed!');
        reject('Reconnect failed: Timed out');
      }, retry);
    });
  }

  /**
   * Sets up the handler so it receives events from the web socket.
   */
  private setHandles(): void {
    this.webSocket.on('message', data => {
      this.handler.handleInput(data.toString());
    });
    this.webSocket.on('close', () => {
      this.connected = false;
      this.handler.handleClose(Promise.resolve(this.reconnect(1000)));
    });
    this.webSocket.on('error', event => {
      this.handler.handleError(event.toString());
    });
  }
}

/**
 * Builder for the Node implementation of a web socket.
 */
export class NodeWebSocketBuilder implements WebSocketImplementationBuilderUrl, WebSocketImplementationBuilderHandler {
  private authProviders!: AuthProvider[];
  private dittoUrl!: DittoURL;

  public constructor(private readonly agent: ProxyAgent) {
  }

  public withHandler(handler: ResponseHandler): Promise<NodeWebSocket> {
    return NodeWebSocket.buildInstance(this.dittoUrl, handler, this.authProviders, this.agent);
  }

  withConnectionDetails(url: DittoURL, authProviders: AuthProvider[]): WebSocketImplementationBuilderHandler {
    this.dittoUrl = url;
    this.authProviders = authProviders;
    return this;
  }
}
