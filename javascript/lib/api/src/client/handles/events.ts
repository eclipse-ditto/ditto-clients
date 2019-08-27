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

/* tslint:disable:bool-param-default no-identical-functions */
import { SubscribeRequest } from '../request-factory/websocket-request-sender';
import { ProtocolResponseValue } from '../request-factory/websocket-request-handler';

/**
 * Handle to receive Events. To be able to subscribe to Events requestEvents() needs to be called first
 */
export interface EventsHandle {
  /**
   * Registers the provided callback function. registerCommands() needs to be called first.
   * It will be called every time an Event is received
   *
   * @param callback - The function that gets called for every Event.
   * @returns The id for the registered subscription.
   */
  subscribeToAllEvents(callback: (message: ProtocolResponseValue) => any): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the specified Thing is received
   *
   * @param thingId - The ID of the Thing to listen to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Thing should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToThing(thingId: string,
                   callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the Attributes of the specified Thing is received
   *
   * @param thingId - The ID of the Thing the Attributes belong to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Attributes should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToAttributes(thingId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the specified Attribute is received
   *
   * @param thingId - The ID of the Thing the Attribute belongs to.
   * @param attributePath - The path to the Attribute to listen to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Attribute should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToAttribute(thingId: string, attributePath: string,
                       callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the Features of the specified Thing is received
   *
   * @param thingId - The ID of the Thing the Features belong to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Features should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToFeatures(thingId: string,
                      callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature to listen to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Feature should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToFeature(thingId: string, featureId: string,
                     callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the Definition of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Definition belongs to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Definition should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToDefinition(thingId: string, featureId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the Properties of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Properties belong to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Properties should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToProperties(thingId: string, featureId: string,
                        callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Registers the provided callback function. registerEvents() needs to be called first.
   * It will be called every time an Event concerning the Property of the specified Feature is received
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature the Property belongs to.
   * @param callback - The function that gets called for every Event.
   * @param action - The action to listen for (eg. modify).
   * @param subResources - Whether or not sub-resources of the Property should also trigger the callback.
   * @returns The id for the registered subscription.
   * @throws Error - Throws an Error if Events were not requested by calling requestEvents()
   */
  subscribeToProperty(thingId: string, featureId: string, propertyPath: string,
                      callback: (message: ProtocolResponseValue) => any, action?: string, subResources?: boolean): string;

  /**
   * Deletes the subscription with the specified ID so it's callback function will no longer be called.
   * If you want to stop receiving any Events you need to call stopCommands().
   *
   * @param id - The ID of the subscription to remove.
   */
  deleteSubscription(id: string): void;

  /**
   * Requests that Events be sent from the server. This is needed in order to register subscriptions.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Events are already registered.
   */
  requestEvents(): Promise<void>;

  /**
   * Requests that Events no longer be sent from the server.
   *
   * @returns A Promise that resolves once the server acknowledges the request or if Events are already stopped.
   */
  stopEvents(): Promise<void>;

  /**
   * Builds and registers the subscription with the specified options.
   *
   * @param options - The options to use for the subscription.
   * @returns The ID of the subscription that was registered.
   * @throws Error - Throws an Error if Events were not requested.
   */
  buildSubscription(options: SubscribeRequest): string;

  /**
   * Checks if Events were requested.
   *
   * @throws Error - Throws an Error if Events were not requested.
   */
  checkEvents(): void;
}
