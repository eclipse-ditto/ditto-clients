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
  HttpBuilderInitialStep,
  HttpClientBuilder,
  WebSocketBuilderInitialStep,
  WebSocketClientBuilder
} from '@eclipse-ditto/ditto-javascript-client-api_0';
import { NodeRequester } from './node-http';
import { NodeWebSocketBuilder } from './node-websocket';
import { ProxyAgent, ProxyOptions } from './proxy-settings';

/**
 * Starting point to build clients that can be used to get handles for NodeJS.
 */
export class DittoNodeClient {

  /**
   * Returns a mutable builder with a fluent API for creating a Http-Ditto-Client.
   * The returned builder utilizes *Object scoping* to guide you through the building process.
   *
   * @param proxyOptions - Options to establish a proxy connection.
   * @return the builder.
   */
  public static newHttpClient(proxyOptions?: ProxyOptions): HttpBuilderInitialStep {
    return HttpClientBuilder.newBuilder(new NodeRequester(new ProxyAgent(proxyOptions)));
  }

  /**
   * Returns a mutable builder with a fluent API for creating a Web-Socket-Ditto-Client.
   * The returned builder utilizes *Object scoping* to guide you through the building process.
   *
   * @param proxyOptions - Options to establish a proxy connection.
   * @return the builder.
   */
  public static newWebSocketClient(proxyOptions?: ProxyOptions): WebSocketBuilderInitialStep {
    return WebSocketClientBuilder.newBuilder(new NodeWebSocketBuilder(new ProxyAgent(proxyOptions)));
  }
}
