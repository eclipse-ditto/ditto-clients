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

export {
  ApiVersion, Channel, DittoHeaders, ReservedDittoProtocolDittoHeaders, DittoProtocolEnvelope, DittoProtocolResponse,
  DefaultDittoProtocolEnvelope, DefaultDittoProtocolResponse
};

enum ApiVersion {
  V2 = 2
}

enum Channel {
  twin = 'twin',
  live = 'live'
}


/**
 * Represents headers that can be used with Ditto.
 */
interface DittoHeaders extends Map<string, string> {
}

/**
 * Known and reserved headers in Ditto.
 */
enum ReservedDittoProtocolDittoHeaders {
  CORRELATION_ID = 'correlation-id'
}

interface DittoProtocolEnvelope {
  topic: string;
  headers: object;
  path: string;
  value: object;
  fields?: string;

  toJson(): string;
}

class DefaultDittoProtocolEnvelope implements DittoProtocolEnvelope {
  topic: string;
  headers: object;
  path: string;
  value: object;
  fields?: string;

  constructor(topic: string, headers: object, path: string, value: object, fields?: string) {
    this.topic = topic;
    this.headers = headers;
    this.path = path;
    this.value = value;
    this.fields = fields;
  }

  toJson(): string {
    return JSON.stringify(this);
  }
}

interface DittoProtocolResponse {
  topic: string;
  path: string;
  status: number;
  headers: { [key: string]: any };
  value: any;

  correlationId(): string | undefined;
}

class DefaultDittoProtocolResponse implements DittoProtocolResponse {
  topic: string;
  headers: { [key: string]: any };
  path: string;
  value: object;
  status: number;

  constructor(topic: string, headers: object, path: string, value: any, status: number) {
    this.topic = topic;
    this.headers = headers;
    this.path = path;
    this.value = value;
    this.status = status;
  }

  private static tryParseJson(json: string) {
    try {
      return JSON.parse(json);
    } catch (e) {
      console.error('Unable to parse json: ', json);
      return {};
    }
  }

  public static fromJson(json: string): DittoProtocolResponse {
    const parsed = this.tryParseJson(json);
    return new DefaultDittoProtocolResponse(
      parsed['topic'],
      parsed['headers'] !== undefined ? parsed['headers'] : {},
      parsed['path'],
      parsed['value'],
      parsed['status']
    );
  }

  correlationId(): string | undefined {
    if (this.headers !== undefined) {
      return this.headers[ReservedDittoProtocolDittoHeaders.CORRELATION_ID];
    }
  }
}
