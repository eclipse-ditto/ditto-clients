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

import { DittoNodeClient } from '../src/ditto-node-client';
import { HttpClientBuilder, WebSocketClientBuilder } from '@eclipse-ditto/ditto-javascript-client-api_0';
import { NodeRequester } from '../src/node-http';
import { ProxyAgent, ProxyOptions } from '../src/proxy-settings';
import { NodeWebSocketBuilder } from '../src/node-websocket';

// tslint:disable-next-line:no-hardcoded-credentials
const proxyOptions = { url: 'http://localhost:3128', username: 'ditto', password: 'foobar' } as ProxyOptions;

describe('DittoNodeClient', () => {

  it('Creates new Http builder', () => {
    const expected = HttpClientBuilder.newBuilder(new NodeRequester(new ProxyAgent(proxyOptions)));

    const client = DittoNodeClient.newHttpClient(proxyOptions);
    expect(client).toBeTruthy();
    expect(client).toEqual(expected);
  });

  it('Creates new WebSocket builder', () => {
    const expected = WebSocketClientBuilder.newBuilder(new NodeWebSocketBuilder(new ProxyAgent(proxyOptions)));

    const client = DittoNodeClient.newWebSocketClient(proxyOptions);
    expect(client).toBeTruthy();
    expect(client).toEqual(expected);
  });

});
