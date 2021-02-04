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

import { GenericResponse, PutResponse } from '../../model/response';
import { RequestOptions } from '../../options/request.options';

/**
 * Handle to send requests.
 */
export abstract class RequestSender {

  /**
   * Fetches the specified request and returns an object of type T.
   *
   * @typeParam T - The type of object to return
   * @param options - The options to use for the request.
   * @returns A Promise for the specified object
   */
  public fetchJsonRequest<T>(options: ParseRequest<T>): Promise<T> {
    return this.fetchRequest(options)
      .then(response => options.parser(response.body));
  }

  /**
   * Fetches the specified request and returns an object of type T.
   *
   * @typeParam T - The type of object to return
   * @param options - The options to use for the request.
   * @returns A Promise for the specified object
   */
  public fetchPutRequest<T>(options: ParseRequest<T>): Promise<PutResponse<T>> {
    return this.fetchRequest(options)
      .then(response => {
        if (response.status === 201) {
          return new PutResponse(options.parser(response.body), response.status, response.headers);
        }
        if (response.status === 204) {
          return new PutResponse<T>(null, response.status, response.headers);
        }
        return Promise.reject(`Received unknown status code: ${response.status}`);
      });
  }

  /**
   * Fetches the specified request and checks if the request was successful.
   *
   * @param options - The options to use for the request.
   * @returns A Promise for the response
   */
  public abstract fetchRequest(options: FetchRequest): Promise<GenericResponse>;
}

/**
 * A Factory for a RequestSender.
 */
export interface RequestSenderFactory {

  /**
   * Builds an instance of RequestSender using the provided group of requests.
   *
   * @param group - The group of the requests (eg. things).
   * @returns The RequestSender
   */
  buildInstance(group: string): RequestSender;
}

/**
 * The options needed for a Request.
 */
export interface FetchRequest {
  /** The action to perform (eg. DELETE). */
  verb: string;
  /** The id of the basic entity the request is for. */
  id?: string;
  /** The path to the entity the request is about from the basic entity. */
  path?: string;
  /** The options to use for the request. */
  requestOptions?: RequestOptions;
  /** The payload to send with he request. */
  payload?: any;
}

/**
 * The options needed for a Request that parses the server response.
 */
export interface ParseRequest<T> extends FetchRequest {
  /** method to parse a JSON representation into the desired object. */
  parser: (o: object) => T;
}
