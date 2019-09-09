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

import { DefaultDittoProtocolResponse, DittoProtocolEnvelope, DittoProtocolResponse } from '../../../model/ditto-protocol';

/**
 * An abstract factory that builds ResilienceHandlers.
 */
export abstract class ResilienceHandlerFactory implements ResilienceHandlerFactoryBuildStep, ResilienceHandlerFactoryContextStep {
  protected webSocketBuilder!: WebSocketImplementationBuilderHandler;
  protected stateHandler!: WebSocketStateHandler;

  public withContext(webSocketBuilder: WebSocketImplementationBuilderHandler,
                     stateHandler: WebSocketStateHandler): ResilienceHandlerFactoryBuildStep {
    this.webSocketBuilder = webSocketBuilder;
    this.stateHandler = stateHandler;
    return this;
  }

  public abstract withRequestHandler(requestHandler: RequestHandler): ResilienceHandler;
}

export interface ResilienceHandlerFactoryBuildStep {

  /**
   * Builds a ResilienceHandler using the specified RequestHandler.
   *
   * @param requestHandler - The RequestHandler to use
   * @returns The ResilienceHandler
   */
  withRequestHandler(requestHandler: RequestHandler): ResilienceHandler;
}

export interface ResilienceHandlerFactoryContextStep {

  /**
   * Uses the web socket and state handler to build the ResilienceHandler.
   *
   * @param webSocketBuilder - The web socket to use.
   * @param stateHandler - The state handler to use.
   * @returns The ResilienceHandlerFactoryBuildStep
   */
  withContext(webSocketBuilder: WebSocketImplementationBuilderHandler,
              stateHandler: WebSocketStateHandler): ResilienceHandlerFactoryBuildStep;
}

export interface RequestHandler {

  /**
   * Analyzes a message from the web socket connection and passes it on to the correct handle.
   *
   * @param correlationId - The correlation-id of the message.
   * @param message - The message.
   */
  handleInput(correlationId: string, message: DittoProtocolResponse): void;

  /**
   * Matches an incoming message to the registered subscriptions and triggers them.
   *
   * @param message - The message.
   */
  handleMessage(message: DittoProtocolResponse): void;

  /**
   * Rejects and deletes a failed request.
   *
   * @param correlationId - The correlation-id of the request.
   * @param cause - The cause of the failure.
   */
  handleError(correlationId: string, cause: object): void;
}

export enum WebSocketBindingMessage {
  START_SEND_EVENTS = 'START-SEND-EVENTS',
  STOP_SEND_EVENTS = 'STOP-SEND-EVENTS',
  START_SEND_MESSAGES = 'START-SEND-MESSAGES',
  STOP_SEND_MESSAGES = 'STOP-SEND-MESSAGES',
  START_SEND_LIVE_COMMANDS = 'START-SEND-LIVE-COMMANDS',
  STOP_SEND_LIVE_COMMANDS = 'STOP-SEND-LIVE-COMMANDS',
  START_SEND_LIVE_EVENTS = 'START-SEND-LIVE-EVENTS',
  STOP_SEND_LIVE_EVENTS = 'STOP-SEND-LIVE-EVENTS'
}

export interface ResilienceHandler extends ResponseHandler {

  /**
   * Sends a request over the web socket connection.
   *
   * @param correlationId - The id of the request to send.
   * @param request - The request to send.
   */
  sendRequest(correlationId: string, request: DittoProtocolEnvelope): void;

  /**
   * Sends a websocket specific binding message.
   *
   * @param message - The message to send.
   * @returns A Promise that resolves once the request was acknowledged
   */
  sendProtocolMessage(message: WebSocketBindingMessage): Promise<void>;

  /**
   * Sends a message.
   *
   * @param message - The message to send.
   * @returns A Promise that resolves once the message was sent and rejects if the sending was unsuccessful
   */
  send(message: string): Promise<void>;
}

export abstract class AbstractResilienceHandler implements ResilienceHandler {

  private readonly protocolMessages: Map<string, PromiseResponse> = new Map();

  protected readonly stateHandler: ResilienceStateHandler;

  constructor(stateHandler: WebSocketStateHandler, protected readonly requestHandler: RequestHandler) {
    this.stateHandler = new ResilienceStateHandler(ConnectionState.Connecting, stateHandler);
  }

  private static isWebSocketBindingMessage(message: string): boolean {
    return message.endsWith(':ACK');
  }

  public handleInput(input: string): void {
    if (AbstractResilienceHandler.isWebSocketBindingMessage(input)) {
      this.handleWebSocketBindingMessage(input);
      return;
    }
    const response: DittoProtocolResponse = DefaultDittoProtocolResponse.fromJson(input);
    if (response.correlationId() !== undefined) {
      this.handleResponse(response.correlationId(), response);
    } else {
      this.handleMessage(response);
    }
  }

  public sendProtocolMessage(message: WebSocketBindingMessage): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      this.protocolMessages.set(message, { resolve, reject });
      this.send(message)
        .catch(error => {
          reject(error);
          this.protocolMessages.delete(message);
        });
    });
  }

  private handleWebSocketBindingMessage(message: string): void {
    this.protocolMessages.get(message.replace(':ACK', ''))!.resolve();
  }

  handleMessage(message: DittoProtocolResponse): void {
    this.requestHandler.handleMessage(message);
  }

  public handleClose(promise: Promise<WebSocketImplementation>): void {
    this.stateHandler.reconnecting();
    this.rejectAllOngoing(connectionInterruptedError);
    this.resolveWebSocket(promise);
  }

  public handleError(error: string): void {
    console.error(error);
  }

  protected abstract rejectAllOngoing(reason: object): void;

  protected abstract resolveWebSocket(promise: Promise<WebSocketImplementation>): void;

  abstract handleFailure(correlationId: string, reason: any): void;

  abstract handleResponse(correlationId: string | undefined, response: DittoProtocolResponse): void;

  abstract send(message: string): Promise<void>;

  abstract sendRequest(correlationId: string, request: DittoProtocolEnvelope): void;
}

export interface WebSocketStateHandler {

  /**
   * Sets the connection state to 'connected' and if the state changed sends the according event.
   */
  connected(): void;

  /**
   * Sets the connection state to 'buffering' and if the state changed sends the according event.
   */
  buffering(): void;

  /**
   * Sets the connection state to 'buffering' and if the state changed sends the according event.
   */
  backPressure(): void;

  /**
   * Sets the connection state to 'reconnecting' and if the state changed sends the according event.
   */
  reconnecting(): void;

  /**
   * Sets the connection state to 'bufferFull' and if the state changed sends the according event.
   */
  bufferFull(): void;

  /**
   * Sets the connection state to 'disconnected' and if the state changed sends the according event.
   */
  disconnected(): void;
}

/**
 * No-op implementation of WebSocketStateHandler.
 */
export class NoopWebSocketStateHandler implements WebSocketStateHandler {
  backPressure(): void {
  }

  bufferFull(): void {
  }

  buffering(): void {
  }

  connected(): void {
  }

  disconnected(): void {
  }

  reconnecting(): void {
  }

}

export interface WebSocketImplementation {

  /**
   * Sends the request to over the web socket connection.
   *
   * @param request - The request to send.
   * @return a Promise for the reestablished web socket connection.
   */
  executeCommand(request: string): void;
}

export interface WebSocketImplementationBuilderHandler {

  /**
   * Sets the handler to send responses to.
   *
   * @param handler - The handler that gets called for responses from the web socket.
   * @return a Promise for the established web socket connection.
   */
  withHandler(handler: ResponseHandler): Promise<WebSocketImplementation>;
}

export interface ResponseHandler {

  /**
   * Parses and processes an input received through the web socket connection.
   *
   * @param input - The received input.
   */
  handleInput(input: string): void;

  /**
   * Processes a response to a request.
   *
   * @param id - the correlation-id of the received response
   * @param response - The response.
   */
  handleResponse(id: string, response: DittoProtocolResponse): void;

  /**
   * Passes a Message that doesn't belong to a request on.
   *
   * @param message - The message.
   */
  handleMessage(message: DittoProtocolResponse): void;

  /**
   * Initiates the reconnecting process and rejects all ongoing requests.
   *
   * @param promise - The promise for the new reconnected web socket.
   */
  handleClose(promise: Promise<WebSocketImplementation>): void;

  /**
   * Deletes a failed request and passes it on as failed.
   *
   * @param id - The id of the failed request.
   * @param reason - The reason the request failed.
   */
  handleFailure(id: string, reason: any): void;

  /**
   * Handles an error received by the web socket connection.
   *
   * @param error - The error.
   */
  handleError(error: string): void;
}

export interface PromiseResponse {
  resolve: (response?: any) => void;
  reject: (reason: any) => void;
}

export interface MessageInformation {
  resolve: ((response?: any) => void)[];
  reject: ((reason: any) => void)[];
}

export const connectionUnavailableError = {
  status: 0,
  error: 'connection.unavailable',
  message: 'The websocket is not connected.',
  description: 'The websocket connection to the server failed.'
};

export const connectionInterruptedError = {
  status: 1,
  error: 'connection.interrupted',
  message: 'The websocket connection to the server was interrupted.',
  description: 'The request might have been sent and processed.'
};

export const connectionLostError = {
  status: 2,
  error: 'connection.lost',
  message: 'The websocket connection to the server was lost.',
  description: 'The reconnection to the server was unsuccessful.'
};

export const bufferFullError = {
  status: 3,
  error: 'buffer.overflow',
  message: 'The buffer limit is reached.',
  description: 'You can set a higher buffer size to buffer more requests.'
};


/**
 * A handler to keep track of the state of a connection and to communicate changes to it.
 */
export class ResilienceStateHandler implements WebSocketStateHandler {
  public constructor(private state: ConnectionState,
                     private readonly stateHandler: WebSocketStateHandler) {
  }

  /**
   * Checks whether a buffer is used.
   *
   * @returns Whether a buffer is used
   */
  public isBuffering(): boolean {
    return this.state >= ConnectionState.Buffering;
  }

  /**
   * Checks whether the connection is still working or completely disconnected.
   *
   * @returns Whether the connection is still working
   */
  public isWorking(): boolean {
    return this.state < ConnectionState.Disconnected;
  }

  /**
   * Checks whether the connection is able to send requests.
   *
   * @returns Whether the connection is able to send requests
   */
  public canSend(): boolean {
    return this.state < ConnectionState.BackPressure;
  }

  /**
   * Checks whether the connection is connected and not using a buffer.
   *
   * @returns Whether the connection is connected and not using a buffer
   */
  public isConnected(): boolean {
    return this.state === ConnectionState.Connected;
  }

  public connected(): void {
    if (this.state !== ConnectionState.Connected && this.state !== ConnectionState.Disconnected) {
      this.state = ConnectionState.Connected;
      this.stateHandler.connected();
    }
  }

  public buffering(): void {
    if (this.state !== ConnectionState.Buffering && this.state !== ConnectionState.Disconnected) {
      this.state = ConnectionState.Buffering;
      this.stateHandler.buffering();
    }
  }

  public backPressure(): void {
    if (this.state < ConnectionState.BackPressure) {
      this.state = ConnectionState.BackPressure;
      this.stateHandler.backPressure();
    }
  }

  public reconnecting(): void {
    if (this.state < ConnectionState.Reconnecting) {
      this.state = ConnectionState.Reconnecting;
      this.stateHandler.reconnecting();
    }
  }

  public bufferFull(): void {
    if (this.state !== ConnectionState.BufferFull && this.state !== ConnectionState.Disconnected) {
      this.state = ConnectionState.BufferFull;
      this.stateHandler.bufferFull();
    }
  }

  public disconnected(): void {
    if (this.state !== ConnectionState.Disconnected) {
      this.state = ConnectionState.Disconnected;
      this.stateHandler.disconnected();
    }
  }
}

/**
 * An enum containing the different states a connection can be in.
 */
export enum ConnectionState {
  Connected = 0,
  Buffering = 1,
  BackPressure = 2,
  Reconnecting = 3,
  Connecting = 3,
  BufferFull = 4,
  Disconnected = 5
}
