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

import * as _ from 'lodash';
import { ProtocolResponseValue } from '../../../src/client/request-factory/websocket-request-handler';
import { WebSocketHelper as H, WebSocketHelper } from './websocket.helper';

export class EventsHelper extends WebSocketHelper {

  public static async testEvent(options: EventsTestOptions) {
    let id: string;
    let timesCalled = 0;
    id = options.method(response => {
      timesCalled += 1;
      if (!_.isEqual(options.message, response)) {
        fail('Request didn\'t match');
      }
    });
    H.requester.sendMessage(JSON.stringify(options.message));
    await new Promise(resolve => setTimeout(resolve, 1));
    options.deleteSubscription(id);
    H.requester.sendMessage(JSON.stringify(options.message));
    await new Promise(resolve => setTimeout(resolve, 1));
    expect(timesCalled).toBe(1);
  }
}

export interface EventsTestOptions {
  method: (callback: (message: ProtocolResponseValue) => any) => string;
  deleteSubscription: (id: string) => void;
  message: ProtocolResponseValue;
}
