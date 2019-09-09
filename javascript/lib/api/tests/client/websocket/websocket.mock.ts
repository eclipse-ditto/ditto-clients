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

/* tslint:disable:no-duplicate-string */
import {
  ResponseHandler,
  WebSocketImplementation,
  WebSocketImplementationBuilderHandler
} from '../../../src/client/request-factory/resilience/websocket-resilience-interfaces';
import {
  WebSocketImplementationBuilderUrl
} from '../../../src/client/request-factory/websocket-request-handler';
import { isEqual } from 'lodash';
import { AuthProvider, DittoURL } from '../../../src/auth/auth-provider';

export interface MockWebSocket extends WebSocketImplementation {
  setHandler(handler: ResponseHandler): MockWebSocket;

  executeCommand(request: string): void;
}

export class DefaultMockWebSocket implements MockWebSocket {
  private readonly requests: Map<any, any> = new Map();
  private readonly listener: Map<object, () => void> = new Map();
  private readonly errorResponse: object = {
    status: -1,
    headers: {},
    value: 'TestRequest not found!'
  };
  private handler: ResponseHandler;

  setHandler(handler: ResponseHandler): DefaultMockWebSocket {
    this.handler = handler;
    return this;
  }

  executeCommand(request: string): void {
    if (request.startsWith('START-SEND') || request.startsWith('STOP-SEND')) {
      this.handler.handleInput(`${request}:ACK`);
      return;
    }
    let correlationId: string;
    const requestObj = JSON.parse(request);
    if (requestObj !== undefined && requestObj.headers !== undefined) {
      correlationId = requestObj.headers['correlation-id'];
      delete requestObj.headers['correlation-id'];
    }
    this.requests.forEach((response, testRequest) => {
      if (isEqual(testRequest, requestObj)) {
        response['headers']['correlation-id'] = correlationId;
        this.handler.handleInput(JSON.stringify(response));
      }
    });
    this.listener.forEach((callback, testRequest) => {
      if (isEqual(testRequest, requestObj)) {
        callback();
      }
    });
  }

  addResponse(request: object, response: object) {
    this.requests.set(request, response);
  }

  sendMessage(message: string) {
    this.handler.handleInput(message);
  }

  addListener(request: object, callback: () => void) {
    this.listener.set(request, callback);
  }

  closeWebSocket(time?: number): Promise<WebSocketImplementation> {
    const onConnected: Promise<WebSocketImplementation> = new Promise((resolve, reject) => {
      if (time) {
        setTimeout(() => resolve(this), time);
      } else {
        reject('Connection was closed');
      }
    });
    this.handler.handleClose(onConnected);
    return onConnected;
  }
}

export class ErrorMockWebSocket implements MockWebSocket {
  private handler: ResponseHandler;

  public constructor(private readonly errorResponse: object) {
  }

  executeCommand(request: string): void {
    const requestObj = JSON.parse(request);
    let correlationId: string;
    if (requestObj !== undefined && requestObj.headers !== undefined) {
      correlationId = requestObj.headers['correlation-id'];
    }
    const response = this.errorResponse;
    response['headers']['correlation-id'] = correlationId;
    this.handler.handleInput(JSON.stringify(response));
  }

  setHandler(handler: ResponseHandler): MockWebSocket {
    this.handler = handler;
    return this;
  }
}

export class MockWebSocketBuilder implements WebSocketImplementationBuilderUrl, WebSocketImplementationBuilderHandler {

  public constructor(private readonly webSocket: MockWebSocket) {
  }

  public withHandler(handler: ResponseHandler): Promise<MockWebSocket> {
    return Promise.resolve(this.webSocket.setHandler(handler));
  }

  withConnectionDetails(url: DittoURL, authenticationProviders: AuthProvider[]): WebSocketImplementationBuilderHandler {
    return this;
  }
}
