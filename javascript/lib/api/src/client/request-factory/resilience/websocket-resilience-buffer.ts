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

import { bufferFullError, MessageInformation, WebSocketImplementation } from './websocket-resilience-interfaces';

/**
 * A class to buffer Messages.
 */
export class ResilienceMessageBuffer {
  private readonly messages: Map<string, MessageInformation> = new Map();

  public constructor(private readonly maxBufferSize?: number) {
  }

  /**
   * Adds a message to the buffer.
   *
   * @param message - The message to buffer.
   * @returns A Promise that resolves once the message was sent
   */
  public addMessage(message: string): Promise<void> {
    if (this.full()) {
      throw Error('The buffer is full!');
    }
    return new Promise<void>((resolve, reject) => {
      const storedMessage = this.messages.get(message);
      if (storedMessage) {
        storedMessage.resolve.push(resolve);
        storedMessage.reject.push(reject);
      } else {
        this.messages.set(message, { resolve: [resolve], reject: [reject] });
      }
    });
  }

  /**
   * Sends the messages from the buffer and resolves their promises.
   *
   * @param webSocket - The web socket to send the messages on.
   * @returns Whether all messages were successfully sent or not
   */
  public sendMessages(webSocket: WebSocketImplementation): boolean {
    try {
      const outstandingMessages = new Map(this.messages);
      outstandingMessages.forEach((response, message) => {
        webSocket.executeCommand(message);
        response.resolve.forEach(resolve => resolve());
        this.messages.delete(message);
      });
    } catch (e) {
      return false;
    }
    return this.empty();
  }

  /**
   * Rejects the Promises for all messages in the buffer and clears it.
   *
   * @param reason - The reason to reject the Promises with.
   */
  public rejectMessages(reason: any): void {
    this.messages.forEach(response => response.reject.forEach(reject => reject(reason)));
    this.messages.clear();
  }

  /**
   * Calculates whether the buffer is full.
   *
   * @returns Whether the buffer is full
   */
  public full(): boolean {
    if (this.maxBufferSize === undefined) {
      return true;
    }
    return this.size >= this.maxBufferSize;
  }

  /**
   * Calculates the number of messages in the buffer.
   *
   * @returns The number of messages in the buffer
   */
  public get size(): number {
    let size = 0;
    this.messages.forEach(information => size += information.resolve.length);
    return size;
  }

  /**
   * Calculates whether the buffer is empty.
   *
   * @returns Whether the buffer is empty
   */
  public empty(): boolean {
    return this.messages.size === 0;
  }
}

/**
 * A class to buffer requests that expect a response.
 */
export class ResilienceRequestBuffer {
  private readonly requests: Map<string, string> = new Map();
  private readonly outstanding: string[] = [];
  private readonly polling: string[] = [];

  public constructor(private readonly maxBufferSize?: number) {
  }

  /**
   * Stores a request that is waiting for a response.
   *
   * @param id - The correlation-id of the request.
   * @param request - The request to be stored.
   */
  public addRequest(id: string, request: string): void {
    this.requests.set(id, request);
  }

  /**
   * Adds a request to the buffer.
   *
   * @param id - The id of the request to buffer.
   * @param rejectionHandler - The method to use if the request needs to be rejected.
   * @returns Whether the request was successfully buffered or not
   */
  public addOutstanding(id: string, rejectionHandler: (reason: object) => void): boolean {
    if (this.full()) {
      rejectionHandler(bufferFullError);
      return false;
    }
    if (this.polling.indexOf(id) >= 0) {
      this.removeFromArray(id, this.polling);
    } else {
      this.outstanding.push(id);
    }
    return true;
  }

  /**
   * Sends the first element of the buffer that is not currently waiting for a response.
   *
   * @param webSocket - The web socket to send the requests on.
   * @returns Whether a request was successfully sent or not
   */
  public sendNextOutstanding(webSocket: WebSocketImplementation): boolean {
    if (this.outstanding.length === this.polling.length) {
      return false;
    }
    if (this.outstanding.length !== 0) {
      let id: string;
      for (id of this.outstanding) {
        if (this.polling.indexOf(id) < 0) {
          this.polling.push(id);
          // @ts-ignore
          webSocket.executeCommand(this.requests.get(id));
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Rejects all requests in the buffer.
   *
   * @param rejectionHandler - The method to use to reject the requests.
   */
  public rejectAllOngoing(rejectionHandler: (id: string) => void): void {
    this.requests.forEach((_, id) => {
      rejectionHandler(id);
      this.deleteRequest(id);
    });
  }

  /**
   * Deletes a request from the buffer.
   *
   * @param id - The id of the request to delete.
   */
  public deleteRequest(id: string): void {
    this.removeFromArray(id, this.outstanding);
    this.removeFromArray(id, this.polling);
    this.requests.delete(id);
  }

  /**
   * Removes a request from an array.
   *
   * @param id - The id of the request to delete.
   * @param array - The array to delete the request from.
   */
  private removeFromArray(id: string, array: string[]): void {
    const index = array.indexOf(id);
    if (index >= 0) {
      array.splice(index, 1);
    }
  }

  /**
   * Checks whether a buffered request is currently waiting for response.
   *
   * @param id - The id of the message.
   * @returns Whether the request is polling or not
   */
  public isPolling(id: string): boolean {
    return this.polling.indexOf(id) >= 0;
  }

  /**
   * Checks whether the buffer is empty.
   *
   * @returns Whether the buffer is empty or not
   */
  public empty(): boolean {
    return this.outstanding.length === 0;
  }

  /**
   * Checks whether the buffer is full.
   *
   * @returns Whether the buffer is full or not
   */
  public full(): boolean {
    if (this.maxBufferSize === undefined) {
      return true;
    }
    return this.outstanding.length >= this.maxBufferSize;
  }
}
