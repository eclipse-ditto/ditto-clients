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

import { AuthProvider, DittoURL } from '../../auth/auth-provider';
import { DefaultDittoProtocolEnvelope, DittoProtocolEnvelope, DittoProtocolResponse } from '../../model/ditto-protocol';
import { GenericResponse } from '../../model/response';
/* tslint:disable:no-duplicate-string */
import {
  PromiseResponse,
  RequestHandler,
  ResilienceHandler,
  ResilienceHandlerFactoryBuildStep,
  WebSocketBindingMessage,
  WebSocketImplementationBuilderHandler
} from './resilience/websocket-resilience-interfaces';

/**
 * A Factory for a WebSocketRequestSender.
 */
export interface WebSocketImplementationBuilderUrl {

  /**
   * Sets the connection details for the web socket connection.
   *
   * @param url - The Url of the service.
   * @param authProviders - The auth providers to use.
   * @return a Promise for the reestablished web socket connection.
   */
  withConnectionDetails(url: DittoURL, authProviders: AuthProvider[]): WebSocketImplementationBuilderHandler;
}

export class WebSocketRequestHandler implements RequestHandler {
  private readonly resilienceHandler: ResilienceHandler;
  private readonly requests: Map<string, PromiseResponse> = new Map();
  private readonly subscriptions: Map<string, Subscription> = new Map();

  public constructor(resilienceHandlerFactory: ResilienceHandlerFactoryBuildStep) {
    this.resilienceHandler = resilienceHandlerFactory.withRequestHandler(this);
  }

  /**
   * Builds a request for the web socket connection.
   *
   * @param topic - The topic of the message.
   * @param path - The path of the message.
   * @param value - The payload of the message.
   * @param header - The headers of the message.
   * @returns A request string that can be sent over the web socket connection
   */
  private static buildRequest(topic: string, path: string, value: any, header: object): DittoProtocolEnvelope {
    return new DefaultDittoProtocolEnvelope(topic, header, path, value);
  }

  /**
   * Sends a message over the web socket connection and returns it's response.
   *
   * @param topic - The topic of the request.
   * @param path - The path of the request.
   * @param value - The payload of the request.
   * @param header - The headers of the request.
   * @returns A Promise for the request's response
   */
  public sendRequest(topic: string, path: string, value: any, header: object): Promise<GenericResponse> {
    return new Promise<GenericResponse>((resolve, reject) => {
      const correlationId = this.getRequestId();
      this.requests.set(correlationId, { resolve, reject });
      const headers: { [key: string]: any } = header === undefined ? {} : header;
      headers['correlation-id'] = correlationId;
      const request = WebSocketRequestHandler.buildRequest(topic, path, value, headers);
      this.resilienceHandler.sendRequest(correlationId, request);
    });
  }

  /**
   * Sends a message over the web socket connection.
   *
   * @param topic - The topic of the message.
   * @param path - The path of the message.
   * @param value - The payload of the message.
   * @param header - The headers of the message.
   * @returns A Promise that resolves once the message was sent
   */
  public sendMessage(topic: string, path: string, value: any, header: object): Promise<void> {
    const request = WebSocketRequestHandler.buildRequest(topic, path, value, header);
    return this.resilienceHandler.send(request.toJson());
  }

  /**
   * Registers a subscription.
   *
   * @param subscription - The subscription to register.
   * @returns The ID of the subscription
   */
  public subscribe(subscription: Subscription): string {
    const id = this.getSubscriptionId();
    this.subscriptions.set(id, subscription);
    return id;
  }

  /**
   * Deletes a subscription.
   *
   * @param id - The ID of the subscription to delete.
   */
  public deleteSubscription(id: string): void {
    this.subscriptions.delete(id);
  }

  /**
   * Sends a protocol message to request a change in the information sent to the web socket connection.
   *
   * @param message - The message to send.
   * @returns A Promise that resolves once the request was acknowledged
   */
  public sendProtocolMessage(message: WebSocketBindingMessage): Promise<void> {
    return this.resilienceHandler.sendProtocolMessage(message);
  }

  public handleInput(id: string, message: DittoProtocolResponse): void {
    if (message.headers !== null) {
      if (this.requests.has(id)) {
        this.handleResponse(id, message);
      } else {
        this.handleMessage(message);
      }
    } else if (!this.handleMessage(message)) {
      console.error(`Unmatched message: ${JSON.stringify(message)}`);
    }
  }

  public handleError(id: string, cause: object): void {
    const request = this.requests.get(id);
    this.requests.delete(id);
    if (request) {
      request.reject(cause);
    }
  }

  public handleMessage(message: DittoProtocolResponse): boolean {
    let found = false;
    this.subscriptions.forEach((subscription, _) => {
      if (subscription.matches(message)) {
        subscription.callback(message);
        found = true;
      }
    });
    return found;
  }

  /**
   * Matches an incoming response to the correlating request and resolves it.
   *
   * @param id - The correlation-id of the response.
   * @param response - The incoming response.
   */
  private handleResponse(id: string, response: DittoProtocolResponse): void {
    const options = this.requests.get(id);
    this.requests.delete(id);
    const headers = Object.keys(response.headers).reduce((map, name) => {
      map.set(name, response.headers[name]);
      return map;
    }, new Map<string, string>());
    headers.delete('correlation-id');
    if (options) {
      options.resolve({ headers, status: response.status, body: response.value });
    }
  }

  /**
   * Generates an unused request correlation-id and returns it.
   *
   * @returns The correlation-id
   */
  private getRequestId(): string {
    const id: string = WebSocketRequestHandler.generateId();
    return this.requests.has(id) ? this.getRequestId() : id;
  }

  /**
   * Generates an unused subscription id and returns it.
   *
   * @returns The id
   */
  private getSubscriptionId(): string {
    const id: string = WebSocketRequestHandler.generateId();
    return this.subscriptions.has(id) ? this.getRequestId() : id;
  }

  /**
   * Generates a uuid.
   *
   * @returns The uuid
   */
  private static generateId(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
      // tslint:disable-next-line
      const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }
}

/**
 * A Factory for a WebSocketRequestSender.
 */
export interface WebSocketResponseHandlerFactory {
  buildInstance(url: string): Promise<WebSocketRequestHandler>;
}

/**
 * A subscription to messages matching a certain pattern.
 */
abstract class Subscription {

  protected constructor(private readonly _callback: (message: ProtocolResponseValue) => any) {
  }

  /**
   * Checks whether a message matches the subscription.
   *
   * @param message - The message to check.
   * @returns Whether the message matches or not
   */
  public abstract matches(message: DittoProtocolResponse): boolean;

  /**
   * Calls the callback function with the message provided.
   *
   * @param message - The message to send.
   */
  callback(message: DittoProtocolResponse): void {
    const splittedTopic = message.topic.split('/');
    const action: string = splittedTopic.length > 0 ? splittedTopic.pop()! : '';
    this._callback({
      action,
      topic: message.topic,
      path: message.path,
      headers: message.headers,
      value: message.value
    });
  }
}

/**
 * A standard subscription to messages matching a specific pattern.
 */
export class StandardSubscription extends Subscription {

  public constructor(callback: (message: ProtocolResponseValue) => any,
                     private readonly topic: string,
                     private readonly path: string,
                     private readonly subResources: boolean) {
    super(callback);

  }

  matches(message: DittoProtocolResponse): boolean {
    return this.checkTopic(message.topic) && this.checkPath(message.path);
  }

  /**
   * Checks whether a path matches the subscription's path.
   *
   * @param path - The path to check.
   * @returns Whether the path matches or not
   */
  private checkPath(path: string): boolean {
    if (this.subResources) {
      return path.startsWith(this.path);
    }
    return path === this.path;
  }

  /**
   * Checks whether a topic matches the subscription's topic.
   *
   * @param topic - The topic to check.
   * @returns Whether the topic matches or not
   */
  private checkTopic(topic: string): boolean {
    return topic.startsWith(this.topic);
  }
}

/**
 * A subscription to all messages of a type.
 */
export class AllSubscription extends Subscription {

  public constructor(callback: (message: ProtocolResponseValue) => any,
                     private readonly type: string) {
    super(callback);
  }

  matches(message: DittoProtocolResponse): boolean {
    const messageType: string = message.topic.split('/')[4];
    return messageType === this.type;
  }
}

/**
 * The message sent to subscription callbacks.
 */
export interface ProtocolResponseValue {
  topic: string;
  path: string;
  action: string;
  headers?: { [key: string]: any };
  value?: any;
}
