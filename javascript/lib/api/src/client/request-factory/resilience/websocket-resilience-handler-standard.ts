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

import { ResilienceMessageBuffer, ResilienceRequestBuffer } from './websocket-resilience-buffer';
import {
  AbstractResilienceHandler,
  bufferFullError,
  connectionLostError,
  RequestHandler,
  ResilienceHandler,
  ResilienceHandlerFactory,
  ResilienceHandlerFactoryContextStep,
  WebSocketImplementation,
  WebSocketImplementationBuilderHandler,
  WebSocketStateHandler
} from './websocket-resilience-interfaces';
import { DittoProtocolEnvelope, DittoProtocolResponse } from '../../../model/ditto-protocol';

/**
 * An implementation of ResilienceHandler that buffers requests during temporary connection problems and backpressure.
 */
export class StandardResilienceHandler extends AbstractResilienceHandler {

  private readonly requestBuffer: ResilienceRequestBuffer;
  private readonly messageBuffer: ResilienceMessageBuffer;
  private webSocket!: WebSocketImplementation;

  public constructor(webSocketBuilder: WebSocketImplementationBuilderHandler,
                     stateHandler: WebSocketStateHandler,
                     requestHandler: RequestHandler,
                     size: number) {
    super(stateHandler, requestHandler);
    this.resolveWebSocket(webSocketBuilder.withHandler(this));
    if (size !== undefined) {
      if (size > 0) {
        this.requestBuffer = new ResilienceRequestBuffer(size);
        this.messageBuffer = new ResilienceMessageBuffer(size);
      } else {
        throw Error('Buffer size needs to be at least one');
      }
    } else {
      this.requestBuffer = new ResilienceRequestBuffer();
      this.messageBuffer = new ResilienceMessageBuffer();
    }
  }

  sendRequest(correlationId: string, request: DittoProtocolEnvelope): void {
    const jsonified = request.toJson();
    this.requestBuffer.addRequest(correlationId, jsonified);
    if (this.stateHandler.isBuffering()) {
      if (!this.stateHandler.isWorking()) {
        this.rejectRequest(correlationId, connectionLostError);
      } else {
        this.addToOutstandingBuffer(correlationId);
      }
    } else {
      this.webSocket.executeCommand(jsonified);
    }
  }

  send(message: string): Promise<void> {
    if (!this.stateHandler.canSend()) {
      if (!this.stateHandler.isWorking()) {
        return Promise.reject(connectionLostError);
      }
      if (this.messageBuffer.full()) {
        this.stateHandler.bufferFull();
        return Promise.reject(bufferFullError);
      }
      return this.messageBuffer.addMessage(message);

    }
    this.webSocket.executeCommand(message);
    return Promise.resolve();
  }

  handleResponse(correlationId: string, response: DittoProtocolResponse): void {
    if (response.status === 429) {
      this.stateHandler.backPressure();
      this.addToOutstandingBuffer(correlationId);
    } else {
      if (this.stateHandler.canSend() && this.requestBuffer.isPolling(correlationId)) {
        this.poll();
      }
      this.requestBuffer.deleteRequest(correlationId);
      this.checkBufferState();
      this.requestHandler.handleInput(correlationId, response);
    }
  }


  public handleFailure(correlationId: string, reason: any): void {
    this.requestBuffer.deleteRequest(correlationId);
    this.requestHandler.handleError(correlationId, reason);
  }

  /**
   * Handles the promise for a new web socket. Once the Promise is resolved it will be set as the web socket for the resilience handler.
   * If the Promise gets rejected the reconnection process will be stopped and all further requests rejected.
   * After the Promise is resolved emptying of the buffer will be initiated. As long as there are requests left in the buffer new
   * requests will continue to be added to the buffer.
   *
   * @param promise - The promise for the new web socket
   */
  protected resolveWebSocket(promise: Promise<WebSocketImplementation>) {
    promise
      .then(socket => {
        this.webSocket = socket;
        this.checkBufferState();
        this.poll();
        if (!this.messageBuffer.sendMessages(this.webSocket)) {
          throw Error('Messages could not be sent from Buffer');
        }
        if (this.requestBuffer.empty() && this.messageBuffer.empty()) {
          this.stateHandler.connected();
        }
      }, error => {
        this.stateHandler.disconnected();
        this.rejectAllOngoing(connectionLostError);
        throw error;
      });
  }

  /**
   * Adds a request to the buffer. It initiates polling if the buffer was empty before.
   *
   * @param id - The id of the request to buffer
   */
  private addToOutstandingBuffer(id: string): void {
    if (this.requestBuffer.addOutstanding(id, (reason: object) => this.rejectRequest(id, reason))) {
      if (!this.stateHandler.isBuffering()) {
        this.poll();
      } else {
        setTimeout(() => this.poll(), 500);
      }
    } else {
      this.stateHandler.bufferFull();
    }
  }

  /**
   * Rejects and deletes all ongoing requests.
   *
   * @param reason - The reason to reject the requests with.
   */
  protected rejectAllOngoing(reason: object): void {
    this.requestBuffer.rejectAllOngoing(id => this.handleFailure(id, reason));
    this.messageBuffer.rejectMessages(reason);
  }

  /**
   * Rejects and deletes a request.
   *
   * @param id - The id of the request to reject.
   * @param reason - The reason to reject the request with.
   */
  private rejectRequest(id: string, reason: any) {
    this.handleFailure(id, reason);
    this.requestBuffer.deleteRequest(id);
    this.checkBufferState();
  }

  /**
   * Sends the next element of the request buffer and if there was such an element will do the same thing again in 500ms.
   */
  private poll(): void {
    if (this.requestBuffer.sendNextOutstanding(this.webSocket)) {
      setTimeout(() => this.poll(), 500);
    }
  }

  /**
   * Checks whether the buffer is full, used or empty and sets the state accordingly.
   */
  private checkBufferState(): void {
    if (this.requestBuffer.full() || this.messageBuffer.full()) {
      this.stateHandler.bufferFull();
    } else if (!this.requestBuffer.empty()) {
      this.stateHandler.buffering();
    } else {
      this.stateHandler.connected();
    }
  }
}

/**
 * A Factory for a StandardResilienceHandler.
 */
export class StandardResilienceHandlerFactory extends ResilienceHandlerFactory {

  private constructor(private readonly size: number) {
    super();
  }

  /**
   * Provides an instance of StandardResilienceHandlerFactory.
   *
   * @param size - The maximum size to use for the buffers.
   * @returns The instance of StandardResilienceHandlerFactory
   */
  public static getInstance(size: number): ResilienceHandlerFactoryContextStep {
    return new StandardResilienceHandlerFactory(size);
  }

  public withRequestHandler(requestHandler: RequestHandler): ResilienceHandler {
    return new StandardResilienceHandler(this.webSocketBuilder, this.stateHandler, requestHandler, this.size);
  }
}
