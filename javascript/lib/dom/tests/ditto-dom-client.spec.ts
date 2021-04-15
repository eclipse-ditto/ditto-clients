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

import { HttpClientBuilder } from '../../api/src/client/http-client-builder';
import { WebSocketClientBuilder } from '../../api/src/client/websocket-client-builder';
import { DittoDomClient } from '../src/ditto-dom-client';
import { FetchRequester } from '../src/fetch-http';
import { FetchWebSocketBuilder } from '../src/fetch-websocket';

describe('DittoDomClient', () => {

  it('Creates new Http builder', () => {
    const expected = HttpClientBuilder.newBuilder(new FetchRequester());

    const client = DittoDomClient.newHttpClient();
    expect(client).toBeTruthy();
    expect(client).toEqual(expected);
  });

  it('Creates new WebSocket builder', () => {
    const expected = WebSocketClientBuilder.newBuilder(new FetchWebSocketBuilder());

    const client = DittoDomClient.newWebSocketClient();
    expect(client).toBeTruthy();
    expect(client).toEqual(expected);
  });

});
