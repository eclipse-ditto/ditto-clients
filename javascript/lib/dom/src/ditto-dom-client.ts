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

import { FetchRequester } from './fetch-http';
import { FetchWebSocketBuilder } from './fetch-websocket';
import { HttpBuilderInitialStep, HttpClientBuilder } from '../../api/src/client/http-client-builder';
import { WebSocketBuilderInitialStep, WebSocketClientBuilder } from '../../api/src/client/websocket-client-builder';

/**
 * Starting point to build clients that can be used to get handles for browsers.
 */
export class DittoDomClient {

  /**
   * Returns a mutable builder with a fluent API for creating a Http-Ditto-Client.
   * The returned builder utilizes *Object scoping* to guide you through the building process.
   *
   * @return the builder.
   */
  public static newHttpClient(): HttpBuilderInitialStep {
    return HttpClientBuilder.newBuilder(new FetchRequester());
  }

  /**
   * Returns a mutable builder with a fluent API for creating a Web-Socket-Ditto-Client.
   * The returned builder utilizes *Object scoping* to guide you through the building process.
   *
   * @return the builder.
   */
  public static newWebSocketClient(): WebSocketBuilderInitialStep {
    return WebSocketClientBuilder.newBuilder(new FetchWebSocketBuilder());
  }
}
