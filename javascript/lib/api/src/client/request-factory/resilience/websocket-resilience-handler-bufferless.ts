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
  AbstractResilienceHandler,
  connectionLostError,
  connectionUnavailableError,
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
 * An implementation of ResilienceHandler without buffering.
 */
export class BufferlessResilienceHandler extends AbstractResilienceHandler {

  private readonly requests: Map<string, string> = new Map();
  private webSocket!: WebSocketImplementation;

  public constructor(webSocketBuilder: WebSocketImplementationBuilderHandler,
                     stateHandler: WebSocketStateHandler,
                     requestHandler: RequestHandler) {
    super(stateHandler, requestHandler);
    this.resolveWebSocket(webSocketBuilder.withHandler(this));
  }

  public sendRequest(id: string, request: DittoProtocolEnvelope): void {
    if (this.stateHandler.isConnected()) {
      const jsonified = request.toJson();
      this.requests.set(id, jsonified);
      this.webSocket.executeCommand(jsonified);
    } else {
      this.handleFailure(id, this.connectionProblemRejectionReason());
    }
  }

  public send(message: string): Promise<void> {
    if (this.stateHandler.isConnected()) {
      this.webSocket.executeCommand(message);
      return Promise.resolve();
    }
    return Promise.reject(this.connectionProblemRejectionReason());
  }

  handleResponse(correlationId: string, response: DittoProtocolResponse): void {
    this.requests.delete(correlationId);
    this.requestHandler.handleInput(correlationId, response);
  }


  public handleFailure(id: string, reason: any): void {
    this.requests.delete(id);
    this.requestHandler.handleError(id, reason);
  }

  /**
   * Returns the reason to reject a Promise based on the current state of the connection.
   *
   * @returns The error to reject with
   */
  private connectionProblemRejectionReason(): object {
    return this.stateHandler.isWorking() ? connectionUnavailableError : connectionLostError;
  }

  /**
   * Handles the promise for a new web socket. Once the Promise is resolved it will be set as the web socket for the resilience handler.
   * If the Promise gets rejected the reconnection process will be stopped and all further requests rejected.
   *
   * @param promise - The promise for the new web socket
   */
  protected resolveWebSocket(promise: Promise<WebSocketImplementation>): void {
    promise
      .then(socket => {
        this.webSocket = socket;
        this.stateHandler.connected();
      }, error => {
        this.stateHandler.disconnected();
        this.rejectAllOngoing(connectionLostError);
        throw error;
      });
  }

  /**
   * Rejects all ongoing requests.
   *
   * @param reason - The reason to reject the requests with
   */
  protected rejectAllOngoing(reason: object): void {
    const ongoingRequests = new Map(this.requests);
    ongoingRequests.forEach((_, id) => this.handleFailure(id, reason));
  }
}

/**
 * A Factory for a BufferlessResilienceHandler.
 */
export class BufferlessResilienceHandlerFactory extends ResilienceHandlerFactory {

  private constructor() {
    super();
  }

  /**
   * Provides an instance of BufferlessResilienceHandlerFactory.
   *
   * @returns The instance of BufferlessResilienceHandlerFactory
   */
  public static getInstance(): ResilienceHandlerFactoryContextStep {
    return new BufferlessResilienceHandlerFactory();
  }

  public withRequestHandler(requestHandler: RequestHandler): ResilienceHandler {
    return new BufferlessResilienceHandler(this.webSocketBuilder, this.stateHandler, requestHandler);
  }
}
