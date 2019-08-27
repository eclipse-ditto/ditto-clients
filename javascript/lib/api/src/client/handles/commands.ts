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

/* tslint:disable:bool-param-default */
import { SubscribeRequest, WebSocketRequestSender, WebSocketRequestSenderFactory } from '../request-factory/websocket-request-sender';
import { AllSubscription, ProtocolResponseValue, WebSocketRequestHandler } from '../request-factory/websocket-request-handler';
import { WebSocketBindingMessage } from '../request-factory/resilience/websocket-resilience-interfaces';

export interface CommandsHandle {
  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command is received
   *
   * @param callback - The function that gets called for every Command.
   * @returns The id for the registered subscription.
   */
  subscribeToAllCommands(callback: (command: ProtocolResponseValue) => any): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the specified Thing is received
   *
   * @param thingId - The ID of the Thing to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Thing should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToThing(thingId: string,
                   callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Attributes of the specified Thing is received
   *
   * @param thingId - The ID of the Thing the Attributes belong to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Attributes should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToAttributes(thingId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the specified Attribute is received
   *
   * @param thingId - The ID of the Thing the Attribute belongs to.
   * @param attributePath - The path of the Attribute to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Attribute should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToAttribute(thingId: string, attributePath: string,
                       callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Features of the specified Thing is received
   *
   * @param thingId - The ID of the Thing the Features belong to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Features should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  // tslint:disable-next-line:no-identical-functions
  subscribeToFeatures(thingId: string,
                      callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Feature should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToFeature(thingId: string, featureId: string,
                     callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Definition of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Definition belongs to
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Definition should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToDefinition(thingId: string, featureId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Properties of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Properties belong to
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Properties should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToProperties(thingId: string, featureId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Property of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Property belongs to
   * @param propertyPath - The path to the Property to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Property should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToProperty(thingId: string, featureId: string, propertyPath: string,
                      callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Deletes the subscription with the specified ID so it's callback function will no longer be called.
   * If you want to stop receiving any Commands you need to call stopCommands().
   *
   * @param id - The ID of the subscription to remove.
   */
  deleteSubscription(id: string): void;

  /**
   * Requests that Commands be sent from the server. This is needed in order to register subscriptions.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Commands are already registered.
   */
  requestCommands(): Promise<void>;

  /**
   * Requests that Commands no longer be sent from the server.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Commands are already stopped.
   */
  stopCommands(): Promise<void>;

  /**
   * Builds and registers the subscription with the specified options.
   *
   * @param options - The options to use for the subscription.
   * @returns The ID of the subscription that was registered.
   * @throws Error - Throws an Error if Commands were not requested.
   */
  buildSubscription(options: SubscribeRequest): string;

  /**
   * Checks if Commands were requested.
   *
   * @throws Error - Throws an Error if Commands were not requested.
   */
  checkCommands(): void;
}

/**
 * Handle to receive Commands. To be able to subscribe to Commands requestCommands() needs to be called first
 */
export class DefaultCommandsHandle implements CommandsHandle {
  private commands = false;

  public constructor(private readonly requestFactory: WebSocketRequestSender,
                     private readonly requester: WebSocketRequestHandler) {
  }

  /**
   * returns an instance of CommandsHandle using the requester provided.
   *
   * @param builder - The builder for the RequestSender to use.
   * @param requester - Requester to use for subscriptions.
   * @returns The CommandsHandle
   */
  public static getInstance(builder: WebSocketRequestSenderFactory, requester: WebSocketRequestHandler): DefaultCommandsHandle {
    return new DefaultCommandsHandle(builder.buildInstance('things'), requester);
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command is received
   *
   * @param callback - The function that gets called for every Command.
   * @returns The id for the registered subscription.
   */
  subscribeToAllCommands(callback: (command: ProtocolResponseValue) => any): string {
    this.checkCommands();
    return this.requester.subscribe(new AllSubscription(callback, 'commands'));
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the specified Thing is received
   *
   * @param thingId - The ID of the Thing to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Thing should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToThing(thingId: string,
                   callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      type: 'commands'
    });
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Attributes of the specified Thing is received
   *
   * @param thingId - The ID of the Thing the Attributes belong to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Attributes should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToAttributes(thingId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      path: '/attributes',
      type: 'commands'
    });
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the specified Attribute is received
   *
   * @param thingId - The ID of the Thing the Attribute belongs to.
   * @param attributePath - The path of the Attribute to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Attribute should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToAttribute(thingId: string, attributePath: string,
                       callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      path: `/attributes/${attributePath}`,
      type: 'commands'
    });
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Features of the specified Thing is received
   *
   * @param thingId - The ID of the Thing the Features belong to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Features should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  // tslint:disable-next-line:no-identical-functions
  subscribeToFeatures(thingId: string,
                      callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      path: '/features',
      type: 'commands'
    });
  }


  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Feature should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToFeature(thingId: string, featureId: string,
                     callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      path: `/features/${featureId}`,
      type: 'commands'
    });
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Definition of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Definition belongs to
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Definition should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToDefinition(thingId: string, featureId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      path: `/features/${featureId}/definition`,
      type: 'commands'
    });
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Properties of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Properties belong to
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Properties should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToProperties(thingId: string, featureId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      path: `/features/${featureId}/properties`,
      type: 'commands'
    });
  }

  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time a Command concerning the Property of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Property belongs to
   * @param propertyPath - The path to the Property to listen to.
   * @param callback - The function that gets called for every Command.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Property should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Commands were not requested by calling requestCommands()
   */
  subscribeToProperty(thingId: string, featureId: string, propertyPath: string,
                      callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string {
    return this.buildSubscription({
      callback,
      action,
      subResources,
      id: thingId,
      path: `/features/${featureId}/properties/${propertyPath}`,
      type: 'commands'
    });
  }

  /**
   * Deletes the subscription with the specified ID so it's callback function will no longer be called.
   * If you want to stop receiving any Commands you need to call stopCommands().
   *
   * @param id - The ID of the subscription to remove.
   */
  deleteSubscription(id: string): void {
    this.requester.deleteSubscription(id);
  }

  /**
   * Requests that Commands be sent from the server. This is needed in order to register subscriptions.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Commands are already registered.
   */
  requestCommands(): Promise<void> {
    if (this.commands) {
      return Promise.resolve();
    }
    return this.requester.sendProtocolMessage(WebSocketBindingMessage.START_SEND_LIVE_COMMANDS)
      .then(() => {
        this.commands = true;
      });
  }

  /**
   * Requests that Commands no longer be sent from the server.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Commands are already stopped.
   */
  stopCommands(): Promise<void> {
    if (!this.commands) {
      return Promise.resolve();
    }
    return this.requester.sendProtocolMessage(WebSocketBindingMessage.STOP_SEND_LIVE_COMMANDS)
      .then(() => {
        this.commands = false;
      });
  }

  /**
   * Builds and registers the subscription with the specified options.
   *
   * @param options - The options to use for the subscription.
   * @returns The ID of the subscription that was registered.
   * @throws Error - Throws an Error if Commands were not requested.
   */
  buildSubscription(options: SubscribeRequest): string {
    this.checkCommands();
    return this.requestFactory.subscribe(options);
  }

  /**
   * Checks if Commands were requested.
   *
   * @throws Error - Throws an Error if Commands were not requested.
   */
  checkCommands(): void {
    if (!this.commands) {
      throw Error('No Commands were requested. Please call requestCommands() first');
    }
  }
}
