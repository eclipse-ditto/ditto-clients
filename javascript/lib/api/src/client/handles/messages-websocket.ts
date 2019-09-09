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

/* tslint:disable:no-identical-functions bool-param-default */
import { MessagesOptions } from '../../options/request.options';
import { SubscribeRequest, WebSocketRequestSender, WebSocketRequestSenderFactory } from '../request-factory/websocket-request-sender';
import { AllSubscription, ProtocolResponseValue, WebSocketRequestHandler } from '../request-factory/websocket-request-handler';
import { WebSocketBindingMessage } from '../request-factory/resilience/websocket-resilience-interfaces';
import { MessagesHandle } from './messages';
import { GenericResponse } from '../../model/response';

export interface WebSocketMessagesHandle extends MessagesHandle {

  /**
   * Initiates claiming the specified Thing.
   *
   * @param thingId - The ID of the Thing to claim.
   * @param claimMessage - The message to send to the Thing.
   * @param options - Options to use for the request.
   */
  claimWithoutResponse(thingId: string, claimMessage: any, options?: MessagesOptions): Promise<void>;

  /**
   * Sends a message to a Thing and returns its response.
   *
   * @param thingId - The ID of the Thing to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageToThingWithoutResponse(thingId: string, messageSubject: string, message: string,
                                contentType: string, options?: MessagesOptions): Promise<void>;

  /**
   * Sends a message from a Thing and returns the response.
   *
   * @param thingId - The ID of the Thing to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageFromThingWithoutResponse(thingId: string, messageSubject: string, message: string,
                                  contentType: string, options?: MessagesOptions): Promise<void>;

  /**
   * Sends a message to a Feature and returns its response.
   *
   * @param thingId - The ID of the Thing that the Feature belongs to.
   * @param featureId - The ID of the Feature to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageToFeatureWithoutResponse(thingId: string, featureId: string, messageSubject: string, message: string,
                                  contentType: string, options?: MessagesOptions): Promise<void>;

  /**
   * Sends a message from a Feature and returns the response.
   *
   * @param thingId - The ID of the Thing that the Feature belongs to.
   * @param featureId - The ID of the Feature to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageFromFeatureWithoutResponse(thingId: string, featureId: string, messageSubject: string, message: string,
                                    contentType: string, options?: MessagesOptions): Promise<void>;

  /**
   * Registers the provided callback function. registerMessages() needs to be called first.
   * It will be called every time a Message is received
   *
   * @param callback - The function that gets called for every Event.
   * @returns The id for the registered subscription.
   */
  subscribeToAllMessages(callback: (message: ProtocolResponseValue) => void): string;

  /**
   * Registers the provided callback function. registerMessages() needs to be called first.
   * It will be called every time a Message concerning the specified Thing is received
   *
   * @param thingId - The ID of the Thing to listen to.
   * @param callback - The function that gets called for every Event.
   * @param topic - The message topic to listen for.
   * @param direction - The direction to listen for (outbox/inbox).
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToThing(thingId: string,
                   callback: (message: ProtocolResponseValue) => void,
                   topic?: string, direction?: 'inbox' | 'outbox'): string;

  /**
   * Registers the provided callback function. registerMessages() needs to be called first.
   * It will be called every time a Message concerning the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature to listen to.
   * @param callback - The function that gets called for every Event.
   * @param topic - The message topic to listen for.
   * @param direction - The direction to listen for (outbox/inbox).
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToFeature(thingId: string, featureId: string,
                     callback: (message: ProtocolResponseValue) => void,
                     topic?: string, direction?: 'inbox' | 'outbox'): string;

  /**
   * Deletes the subscription with the specified ID so it's callback function will no longer be called.
   * If you want to stop receiving any Messages you need to call stopMessages().
   *
   * @param id - The ID of the subscription to remove.
   */
  deleteSubscription(id: string): void;

  /**
   * Requests that Messages be sent from the server. This is needed in order to register subscriptions.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Messages are already registered.
   */
  requestMessages(): Promise<void>;

  /**
   * Requests that Messages no longer be sent from the server. None of the subscriptions will be deleted.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Messages are already stopped.
   */
  stopMessages(): Promise<void>;

  /**
   * Builds and registers the subscription with the specified options.
   *
   * @param options - The options to use for the subscription.
   * @returns The ID of the subscription that was registered.
   * @throws Error - Throws an Error if Messages were not requested.
   */
  buildSubscription(options: DirectionSubscribeRequest): string;

  /**
   * Checks if Messages were requested.
   *
   * @throws Error - Throws an Error if Messages were not requested.
   */
  checkMessages(): void;
}

/**
 * Handle to send and receive Messages. To be able to subscribe to Messages requestMessages() needs to be called first
 */
export class DefaultWebSocketMessagesHandle implements WebSocketMessagesHandle {
  private messages = false;

  private constructor(private readonly requestFactory: WebSocketRequestSender,
                      private readonly requester: WebSocketRequestHandler) {
  }

  /**
   * returns an instance of WebSocketMessagesHandle using the provided RequestSender.
   *
   * @param builder - The builder for the RequestSender to work with.
   * @param requester - Requester to use for subscriptions.
   * @returns The WebSocketMessagesHandle
   */
  public static getInstance(builder: WebSocketRequestSenderFactory, requester: WebSocketRequestHandler): DefaultWebSocketMessagesHandle {
    return new DefaultWebSocketMessagesHandle(builder.buildInstance('things'), requester);
  }

  /**
   * Initiates claiming the specified Thing and returns its response.
   *
   * @param thingId - The ID of the Thing to claim.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  claim(thingId: string, claimMessage: any, options?: MessagesOptions): Promise<GenericResponse> {
    return this.requestFactory.sendMessageWithResponse({
      options,
      id: thingId,
      path: '',
      messageSubject: 'claim',
      message: claimMessage,
      direction: 'inbox',
      contentType: 'text/plain'
    });
  }

  /**
   * Initiates claiming the specified Thing.
   *
   * @param thingId - The ID of the Thing to claim.
   * @param claimMessage - The message to send to the Thing.
   * @param options - Options to use for the request.
   */
  claimWithoutResponse(thingId: string, claimMessage: any, options?: MessagesOptions): Promise<void> {
    return this.requestFactory.sendMessage({
      options,
      id: thingId,
      messageSubject: 'claim',
      message: claimMessage,
      direction: 'inbox',
      contentType: 'text/plain'
    });
  }

  /**
   * Sends a message to a Thing.
   *
   * @param thingId - The ID of the Thing to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  messageToThing(thingId: string, messageSubject: string, message: string,
                 contentType: string, options?: MessagesOptions): Promise<GenericResponse> {
    return this.requestFactory.sendMessageWithResponse({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'inbox'
    });
  }

  /**
   * Sends a message to a Thing and returns its response.
   *
   * @param thingId - The ID of the Thing to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageToThingWithoutResponse(thingId: string, messageSubject: string, message: string,
                                contentType: string, options?: MessagesOptions): Promise<void> {
    return this.requestFactory.sendMessage({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'inbox'
    });
  }

  /**
   * Sends a message from a Thing.
   *
   * @param thingId - The ID of the Thing to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  messageFromThing(thingId: string, messageSubject: string, message: string,
                   contentType: string, options?: MessagesOptions): Promise<GenericResponse> {
    return this.requestFactory.sendMessageWithResponse({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'outbox'
    });
  }

  /**
   * Sends a message from a Thing and returns the response.
   *
   * @param thingId - The ID of the Thing to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageFromThingWithoutResponse(thingId: string, messageSubject: string, message: string,
                                  contentType: string, options?: MessagesOptions): Promise<void> {
    return this.requestFactory.sendMessage({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'outbox'
    });
  }

  /**
   * Sends a message to a Feature.
   *
   * @param thingId - The ID of the Thing that the Feature belongs to.
   * @param featureId - The ID of the Feature to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  messageToFeature(thingId: string, featureId: string, messageSubject: string, message: string,
                   contentType: string, options?: MessagesOptions): Promise<GenericResponse> {
    return this.requestFactory.sendMessageWithResponse({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'inbox',
      path: `/features/${featureId}`
    });
  }

  /**
   * Sends a message to a Feature and returns its response.
   *
   * @param thingId - The ID of the Thing that the Feature belongs to.
   * @param featureId - The ID of the Feature to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageToFeatureWithoutResponse(thingId: string, featureId: string, messageSubject: string, message: string,
                                  contentType: string, options?: MessagesOptions): Promise<void> {
    return this.requestFactory.sendMessage({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'inbox',
      path: `/features/${featureId}`
    });
  }

  /**
   * Sends a message from a Feature.
   *
   * @param thingId - The ID of the Thing that the Feature belongs to.
   * @param featureId - The ID of the Feature to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  messageFromFeature(thingId: string, featureId: string, messageSubject: string, message: string,
                     contentType: string, options?: MessagesOptions): Promise<GenericResponse> {
    return this.requestFactory.sendMessageWithResponse({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'outbox',
      path: `/features/${featureId}`
    });
  }

  /**
   * Sends a message from a Feature and returns the response.
   *
   * @param thingId - The ID of the Thing that the Feature belongs to.
   * @param featureId - The ID of the Feature to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - Content type of the message.
   * @param options - Options to use for the request.
   */
  messageFromFeatureWithoutResponse(thingId: string, featureId: string, messageSubject: string, message: string,
                                    contentType: string, options?: MessagesOptions): Promise<void> {
    return this.requestFactory.sendMessage({
      messageSubject,
      message,
      contentType,
      options,
      id: thingId,
      direction: 'outbox',
      path: `/features/${featureId}`
    });
  }

  /**
   * Registers the provided callback function. registerMessages() needs to be called first.
   * It will be called every time a Message is received
   *
   * @param callback - The function that gets called for every Event.
   * @returns The id for the registered subscription.
   */
  subscribeToAllMessages(callback: (message: ProtocolResponseValue) => void): string {
    this.checkMessages();
    return this.requester.subscribe(new AllSubscription(callback, 'messages'));
  }

  /**
   * Registers the provided callback function. registerMessages() needs to be called first.
   * It will be called every time a Message concerning the specified Thing is received
   *
   * @param thingId - The ID of the Thing to listen to.
   * @param callback - The function that gets called for every Event.
   * @param topic - The message topic to listen for.
   * @param direction - The direction to listen for (outbox/inbox).
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToThing(thingId: string,
                   callback: (message: ProtocolResponseValue) => void,
                   topic?: string, direction?: 'inbox' | 'outbox'): string {
    return this.buildSubscription({
      callback,
      direction,
      action: topic,
      id: thingId,
      type: 'messages'
    });
  }

  /**
   * Registers the provided callback function. registerMessages() needs to be called first.
   * It will be called every time a Message concerning the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature to listen to.
   * @param callback - The function that gets called for every Event.
   * @param topic - The message topic to listen for.
   * @param direction - The direction to listen for (outbox/inbox).
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToFeature(thingId: string, featureId: string,
                     callback: (message: ProtocolResponseValue) => void,
                     topic?: string, direction?: 'inbox' | 'outbox'): string {
    return this.buildSubscription({
      callback,
      direction,
      action: topic,
      id: thingId,
      type: 'messages',
      path: `/features/${featureId}`
    });
  }

  /**
   * Deletes the subscription with the specified ID so it's callback function will no longer be called.
   * If you want to stop receiving any Messages you need to call stopMessages().
   *
   * @param id - The ID of the subscription to remove.
   */
  deleteSubscription(id: string): void {
    this.requester.deleteSubscription(id);
  }

  /**
   * Requests that Messages be sent from the server. This is needed in order to register subscriptions.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Messages are already registered.
   */
  requestMessages(): Promise<void> {
    if (this.messages) {
      return Promise.resolve();
    }
    return this.requester.sendProtocolMessage(WebSocketBindingMessage.START_SEND_MESSAGES)
      .then(() => {
        this.messages = true;
      });
  }

  /**
   * Requests that Messages no longer be sent from the server. None of the subscriptions will be deleted.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Messages are already stopped.
   */
  stopMessages(): Promise<void> {
    if (!this.messages) {
      return Promise.resolve();
    }
    return this.requester.sendProtocolMessage(WebSocketBindingMessage.STOP_SEND_MESSAGES)
      .then(() => {
        this.messages = false;
      });
  }

  /**
   * Builds and registers the subscription with the specified options.
   *
   * @param options - The options to use for the subscription.
   * @returns The ID of the subscription that was registered.
   * @throws Error - Throws an Error if Messages were not requested.
   */
  buildSubscription(options: DirectionSubscribeRequest): string {
    this.checkMessages();
    const originalPath = options['path'] !== undefined ? options['path'] : '';
    const path = options['direction'] ? `${originalPath}/${options['direction']}` : originalPath;
    options['path'] = path;
    return this.requestFactory.subscribe(options);
  }

  /**
   * Checks if Messages were requested.
   *
   * @throws Error - Throws an Error if Messages were not requested.
   */
  checkMessages(): void {
    if (!this.messages) {
      throw Error('No Messages were requested. Please call requestMessages() first');
    }
  }
}

export interface DirectionSubscribeRequest extends SubscribeRequest {
  direction?: 'inbox' | 'outbox';
}
