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

/* tslint:disable:no-duplicate-string */
import { RequestOptions } from '../../options/request.options';
import { FetchRequest, RequestSender, RequestSenderFactory } from './request-sender';
import { ProtocolResponseValue, StandardSubscription, WebSocketRequestHandler } from './websocket-request-handler';
import { ApiVersion, Channel } from '../../model/ditto-protocol';
import { GenericResponse } from '../../model/response';

/**
 * Handle to send web socket requests.
 */
export class WebSocketRequestSender extends RequestSender {

  constructor(private readonly requester: WebSocketRequestHandler,
              public readonly group: string,
              public readonly channel: 'twin' | 'live',
              public readonly apiVersion: 2) {
    super();
  }

  private static buildPath(path: string | undefined) {
    if (path !== undefined) {
      return path.startsWith('/') ? path : `/${path}`;
    }
    return '/';
  }

  /**
   * Builds the path to use for a Message web socket request.
   *
   * @param path - The path to the entity within the basic object.
   * @param mailbox - The direction the message is sent in (eg. inbox).
   * @param subject - The subject of the message.
   * @returns The path for a Message request
   */
  private static buildMessagePath(path: string | undefined, mailbox: string, subject: string) {
    return `${path !== undefined ? path : ''}/${mailbox}/messages/${subject}`;
  }

  /**
   * Splits an ID into name and namespace. Element 0 is the namespace and element 1 is the name.
   *
   * @param idWithNamespace - The id to separate.
   * @returns The Array containing namespace and name
   */
  private static separateNamespace(idWithNamespace: string | undefined): string[] {
    if (idWithNamespace !== undefined) {
      const splitName = idWithNamespace.split(':');
      const namespace = splitName[0];
      splitName.shift();
      const name = splitName.join('');
      return [namespace, name];
    }
    return ['', ''];
  }

  /**
   * Translate a verb from HTTP (eg. GET) into web socket form (eg. retrieve).
   *
   * @param verb - The verb to translate.
   * @returns The translated verb
   */
  private static translateVerb(verb: string): string {
    const action = verb.toLowerCase();
    if (action === 'get') {
      return 'retrieve';
    }
    if (action === 'put') {
      return 'modify';
    }
    return action;
  }

  /**
   * The basic headers needed for every web socket request.
   *
   * @returns The headers
   */
  private get baseHeaders(): { [key: string]: any } {
    return { version: this.apiVersion };
  }

  public fetchRequest(options: FetchRequest): Promise<GenericResponse> {
    const topic = this.buildTopic(options.id, this.group, 'commands', WebSocketRequestSender.translateVerb(options.verb));
    const path = WebSocketRequestSender.buildPath(options.path);
    const headers = this.buildHeaders(options.requestOptions, { 'content-type': 'application/json' });
    return this.requester.sendRequest(topic, path, options.payload, headers)
      .then(response => {
        if (response.status >= 200 && response.status < 300) {
          return Promise.resolve(response);
        }
        return Promise.reject(response.body);
      });
  }

  /**
   * Sends a Message and returns the response.
   *
   * @param options - The options to use for the Message.
   * @returns A Promise for the response
   */
  public sendMessageWithResponse(options: MessageRequest): Promise<GenericResponse> {
    const topic = this.buildMessageTopic(options.id, options.messageSubject);
    const path = WebSocketRequestSender.buildMessagePath(options.path, options.direction, options.messageSubject);
    const headers = this.buildHeaders(options.options, { 'content-type': options.contentType });
    return this.requester.sendRequest(topic, path, options.message, headers);
  }

  /**
   * Sends a Message.
   *
   * @param options - The options to use for the Message.
   * @returns A Promise that resolves once the Message was sent
   */
  public sendMessage(options: MessageRequest): Promise<void> {
    const topic = this.buildMessageTopic(options.id, options.messageSubject);
    const path = WebSocketRequestSender.buildMessagePath(options.path, options.direction, options.messageSubject);
    const headers = this.buildHeaders(options.options, { 'content-type': options.contentType });
    return this.requester.sendMessage(topic, path, options.message, headers);
  }

  /**
   * Registers a subscription.
   *
   * @param options - The options to use for the subscription.
   * @returns The id of the subscription. It can be used to delete the subscription.
   */
  public subscribe(options: SubscribeRequest): string {
    const topic = this.buildTopic(options.id, 'things', options.type,
      options.action !== undefined ? options.action : '');
    const path = options.path !== undefined ? options.path : '';
    const subResources = typeof options.subResources === 'boolean' ? options.subResources : true;
    return this.requester.subscribe(new StandardSubscription(options.callback, topic, path, subResources));
  }

  /**
   * Builds headers to use for a web socket request. It combines base headers, options headers and then additional headers
   *
   * @param options - The options to provided in the request.
   * @param additionalHeaders - Additional headers to add.
   * @returns The combined headers
   */
  private buildHeaders(options: RequestOptions | undefined, additionalHeaders?: object) {
    let headers = this.baseHeaders;
    if (options !== undefined) {
      const headersMap: Map<string, string> = options.getHeaders();
      headersMap.forEach((v, k) => headers[k] = v);
      headers = Object.assign(headers, additionalHeaders);
      return headers;
    }
    return additionalHeaders === undefined ? this.baseHeaders : Object.assign(headers, additionalHeaders);
  }

  /**
   * Builds the topic to use for a Message web socket request.
   *
   * @param id - The id of the basic entity the request is for.
   * @param messageSubject - The subject of the message.
   * @returns The topic for a Message request
   */
  private buildMessageTopic(id: string, messageSubject: string) {
    return this.buildTopic(id, 'things', 'messages', messageSubject);
  }

  /**
   * Builds the topic to use for a web socket request.
   *
   * @param id - The id of the basic entity the request is for.
   * @param group - The group of the request (eg. things).
   * @param criterion - The area of the request (eg. commands).
   * @param action - The action to perform.
   * @returns The topic for a request
   */
  private buildTopic(id: string | undefined, group: string, criterion: string, action: string) {
    const splitName: string[] = WebSocketRequestSender.separateNamespace(id);
    return `${splitName[0]}/${splitName[1]}/${group}/${this.channel}/${criterion}/${action}`;
  }

}

/**
 * A Factory for a WebSocketRequestSender.
 */
export class WebSocketRequestSenderFactory implements RequestSenderFactory {

  public constructor(private readonly apiVersion: ApiVersion,
                     private readonly channel: Channel,
                     private readonly requester: WebSocketRequestHandler) {
  }

  public buildInstance(group: string): WebSocketRequestSender {
    return new WebSocketRequestSender(this.requester, group, this.channel, this.apiVersion);
  }
}

/**
 * The specification of a Message to be sent over the web socket connection.
 */
export interface MessageRequest {
  /** The id of the basic entity the request is for. */
  id: string;
  /** The path to the entity the request is about from the basic entity. */
  path?: string;
  /** The subject of the message. */
  messageSubject: string;
  /** The message. */
  message: string;
  /** The direction to send in (outbox/inbox). */
  direction: string;
  /** The content type of the message. */
  contentType: string;
  /** The options to use for the request. */
  options?: RequestOptions;
}

/**
 * The specification of a subscription to register.
 */
export interface SubscribeRequest {
  /** The id of the basic entity to subscribe to. */
  id: string;
  /** The callback to call every time a message that fits the criteria comes in. */
  callback: (message: ProtocolResponseValue) => any;
  /** The type of message to subscribe to (eg. event). */
  type: string;
  /** The path to the entity to subscribe to */
  path?: string;
  /** The action to listen for (eg. modify). */
  action?: string;
  /** Whether or not sub-resources of the Property should also trigger the callback. */
  subResources?: boolean;
}
