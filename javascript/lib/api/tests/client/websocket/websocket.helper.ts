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

import { WebSocketStateHandler } from '../../../src/client/request-factory/resilience/websocket-resilience-interfaces';
import { DefaultMockWebSocket, ErrorMockWebSocket, MockWebSocket, MockWebSocketBuilder } from './websocket.mock';
import { Helper } from '../test.helper';
import { WebSocketBuilderInitialStep, WebSocketClientBuilder } from '../../../src/client/websocket-client-builder';
import { DittoWebSocketClient, DittoWebSocketLiveClient } from '../../../src/client/ditto-client-websocket';

export enum MockWebSocketStates {
  CONNECTED = 'connected',
  BUFFERING = 'buffering',
  BACK_PRESSURE = 'backPressure',
  RECONNECTING = 'reconnecting',
  DISCONNECTED = 'disconnected',
  BUFFER_FULL = 'bufferFull'
}

export class MockWebSocketStateTracker implements WebSocketStateHandler {
  public readonly events: string[] = [];

  connected() {
    this.events.push(MockWebSocketStates.CONNECTED);
  }

  buffering(): void {
    this.events.push(MockWebSocketStates.BUFFERING);
  }

  backPressure() {
    this.events.push(MockWebSocketStates.BACK_PRESSURE);
  }

  reconnecting() {
    this.events.push(MockWebSocketStates.RECONNECTING);
  }

  disconnected() {
    this.events.push(MockWebSocketStates.DISCONNECTED);
  }

  bufferFull() {
    this.events.push(MockWebSocketStates.BUFFER_FULL);
  }
}

export class WebSocketHelper extends Helper {
  public static readonly url = 'http://test.websocket';
  private static readonly errorUrl = 'http://error.websocket';
  public static readonly requester: DefaultMockWebSocket = new DefaultMockWebSocket();
  public static readonly stateTracker: MockWebSocketStateTracker = new MockWebSocketStateTracker();
  public static readonly bufferSize = 3;
  public static readonly thingsClient: DittoWebSocketLiveClient = WebSocketHelper.buildWebSocketClient(WebSocketHelper.requester)
    .withTls()
    .withDomain(WebSocketHelper.url)
    .withAuthProvider(WebSocketHelper.basicAuthProvider(WebSocketHelper.testName, WebSocketHelper.password))
    .withBuffer(WebSocketHelper.bufferSize)
    .liveChannel()
    .withStateHandler(WebSocketHelper.stateTracker)
    .build();

  private static readonly errorRequester: ErrorMockWebSocket =
    new ErrorMockWebSocket({ headers: {}, status: 403, value: Helper.errorBody });
  public static readonly errorClient: DittoWebSocketClient = WebSocketHelper.buildWebSocketClient(WebSocketHelper.errorRequester)
    .withTls()
    .withDomain(WebSocketHelper.errorUrl)
    .withAuthProvider(WebSocketHelper.basicAuthProvider(WebSocketHelper.testName, WebSocketHelper.password))
    .withBuffer(WebSocketHelper.bufferSize)
    .liveChannel()
    .withStateHandler(WebSocketHelper.stateTracker)
    .build();

  public static get standardHeaders(): { [key: string]: any } {
    return {
      version: 2,
      'content-type': 'application/json'
    };
  }

  public static splitThingId = WebSocketHelper.thing.thingId.split(':')[1];
  public static splitNamespace = WebSocketHelper.thing.thingId.split(':')[0];

  public static test<T>(options: WebSocketTestOptions<T>): Promise<any> {
    const topic = options.topic;
    const path = options.path === undefined ? '/' : options.path;
    this.requester.addResponse(this.buildRequest(options), this.buildResponse(options));

    return options.toTest()
      .then(response => {
        if (options.expected) {
          expect(response).toEqual(options.expected);
        }
      })
      .catch(reason => {
        fail(`${topic} ${path}: ${reason}`);
      });
  }

  public static buildRequest(options: TestRequest): object {
    const request = {
      topic: options.topic,
      path: options.path === undefined ? '/' : options.path,
      headers: options.requestHeaders ? options.requestHeaders : this.standardHeaders
    };
    if (options.requestBody) {
      request['value'] = options.requestBody;
    }
    return request;
  }

  public static buildResponse(options: TestResponse): object {
    const requestResponse = {
      topic: options.topic,
      path: options.path === undefined ? '/' : options.path,
      status: options.status,
      headers: options.responseHeaders ? options.responseHeaders : this.standardHeaders
    };
    if (options.responseBody) {
      requestResponse['value'] = options.responseBody;
    }
    return requestResponse;
  }

  public static buildWebSocketClient(webSocket: MockWebSocket): WebSocketBuilderInitialStep {
    return WebSocketClientBuilder.newBuilder(new MockWebSocketBuilder(webSocket));
  }
}

export interface WebSocketTestOptions<T> extends TestRequest, TestResponse {
  toTest: () => Promise<T>;
  expected?: T;
}

export interface TestRequest extends TestMessage {
  requestHeaders?: object;
  requestBody?: any;
}

export interface TestResponse extends TestMessage {
  status: number;
  responseHeaders?: object;
  responseBody?: any;
}

export interface TestMessage {
  topic: string;
  path?: string;
}
