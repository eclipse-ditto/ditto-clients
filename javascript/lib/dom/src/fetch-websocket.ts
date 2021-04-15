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

import { WebSocketImplementationBuilderUrl } from '../../api/src/client/request-factory/websocket-request-handler';
import {
  ResponseHandler,
  WebSocketImplementation,
  WebSocketImplementationBuilderHandler
} from '../../api/src/client/request-factory/resilience/websocket-resilience-interfaces';
import {
  authenticateWithUrl,
  AuthProvider,
  DittoURL
} from '../../api/src/auth/auth-provider';

/**
 * Browser implementation of a web socket requester.
 */
export class FetchWebSocket implements WebSocketImplementation {
  private connected = true;

  private constructor(private webSocket: WebSocket,
                      private readonly webSocketUrl: string,
                      private readonly handler: ResponseHandler) {
    this.setHandles();
  }

  /**
   * Builds an instance of FetchWebSocket.
   *
   * @param url - The Url of the service.
   * @param handler - The handler that gets called for responses from the web socket.
   * @param authProviders - The auth providers to use.
   * @return a Promise for the web socket connection.
   */
  public static buildInstance(url: DittoURL, handler: ResponseHandler, authProviders: AuthProvider[]): Promise<FetchWebSocket> {
    return new Promise(resolve => {
      const authenticatedUrl = authenticateWithUrl(url, authProviders);
      const finalUrl = authenticatedUrl.toString();
      const webSocket = new WebSocket(finalUrl);
      webSocket.addEventListener('open', () => {
        resolve(new FetchWebSocket(webSocket, finalUrl, handler));
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
      this.webSocket = new WebSocket(this.webSocketUrl);
      this.webSocket.addEventListener('open', () => {
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
    this.webSocket.addEventListener('message', event => {
      this.handler.handleInput(event.data);
    });
    this.webSocket.addEventListener('close', () => {
      this.connected = false;
      this.handler.handleClose(Promise.resolve(this.reconnect(1000)));
    });
    this.webSocket.addEventListener('error', event => {
      this.handler.handleError(`WebSocket: ${event}`);
    });
  }
}

/**
 * Builder for the Browser implementation of a web socket.
 */
export class FetchWebSocketBuilder implements WebSocketImplementationBuilderUrl, WebSocketImplementationBuilderHandler {
  private dittoUrl!: DittoURL;
  private authProviders!: AuthProvider[];

  public constructor() {
  }

  withHandler(handler: ResponseHandler): Promise<FetchWebSocket> {
    return FetchWebSocket.buildInstance(this.dittoUrl, handler, this.authProviders);
  }

  withConnectionDetails(url: DittoURL, authProviders: AuthProvider[]): WebSocketImplementationBuilderHandler {
    this.authProviders = authProviders;
    this.dittoUrl = url;
    return this;
  }
}
