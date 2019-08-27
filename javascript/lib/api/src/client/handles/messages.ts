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

import { MessagesOptions } from '../../options/request.options';
import { GenericResponse } from '../../model/response';

/**
 * Handle for sending Messages.
 */
export interface MessagesHandle {
  /**
   * Initiates claiming the specified Thing.
   *
   * @param thingId - The ID of the Thing to claim.
   * @param claimMessage - The message to be sent to the Thing for authentication.
   * @param options - Options to use for the request.
   * @returns A Promise for the server response
   */
  claim(thingId: string, claimMessage: any, options?: MessagesOptions): Promise<GenericResponse>;

  /**
   * Sends a message to a Thing.
   *
   * @param thingId - The ID of the Thing to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - The content type of the message.
   * @param options?? - Options to use for the request.
   * @returns A Promise for the server response
   */
  messageToThing(thingId: string, messageSubject: string, message: string,
                 contentType: string, options?: MessagesOptions): Promise<GenericResponse>;

  /**
   * Sends a message from a Thing.
   *
   * @param thingId - The ID of the Thing to message from.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - The content type of the message.
   * @param options?? - Options to use for the request.
   * @returns A Promise for the server response
   */
  messageFromThing(thingId: string, messageSubject: string, message: string,
                   contentType: string, options?: MessagesOptions): Promise<GenericResponse>;

  /**
   * Sends a message to a Feature.
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature to message.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - The content type of the message.
   * @param options?? - Options to use for the request.
   * @returns A Promise for the server response
   */
  messageToFeature(thingId: string, featureId: string, messageSubject: string, message: string,
                   contentType: string, options?: MessagesOptions):
    Promise<GenericResponse>;

  /**
   * Sends a message from a Feature.
   *
   * @param thingId - The ID of the Thing the Feature belongs to.
   * @param featureId - The ID of the Feature to message from.
   * @param messageSubject - The subject of the message to send.
   * @param message - The message to send.
   * @param contentType - The content type of the message.
   * @param options?? - Options to use for the request.
   * @returns A Promise for the server response
   */
  messageFromFeature(thingId: string, featureId: string, messageSubject: string, message: string,
                     contentType: string, options?: MessagesOptions):
    Promise<GenericResponse>;
}
