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

import { AuthProvider } from '../auth/auth-provider';
import { ThingsHandle } from './handles/things.interfaces';
import { FeaturesHandle } from './handles/features.interfaces';
import { MessagesHandle } from './handles/messages';
import { PoliciesHandle } from './handles/policies';
import { SearchHandle } from './handles/search';
import { EventsHandle } from './handles/events';
import { CommandsHandle } from './handles/commands';
import { ApiVersion } from '../model/ditto-protocol';

export {
  AbstractBuilder,
  ApiVersionStep,
  AuthenticationStep,
  BuildStep,
  CustomBuilderContext,
  CustomCommandsHandleStep,
  CustomEventsHandleStep,
  CustomFeaturesHandleStep,
  CustomMessagesHandleStep,
  CustomPoliciesHandleStep,
  CustomSearchHandleStep,
  CustomThingsHandleStep,
  EnvironmentStep,
  ProtocolStep
};

/**
 * Default interface for builder steps.
 */
// tslint:disable-next-line:no-empty-interface
interface BuildStep {
}

/**
 * Interface to choose which protocol to use.
 * @param <T> - type of API 1 Build steps.
 * @param <U> - type of API 2 Build steps.
 */
interface ProtocolStep<T extends BuildStep, U extends BuildStep> extends BuildStep {
  /**
   * Use with TLS (e.g. wss:// or https://).
   */
  withTls(): EnvironmentStep<T, U>;

  /**
   * Use without TLS (e.g. ws:// or http://).
   */
  withoutTls(): EnvironmentStep<T, U>;
}

/**
 * Interface to select an Environment to use for requests.
 * @param <T> - type of API 1 Build steps.
 * @param <U> - type of API 2 Build steps.
 */
interface EnvironmentStep<T extends BuildStep, U extends BuildStep> extends BuildStep {
  /**
   * Sets a custom domain to use for requests.
   *
   * @param domain - The domain where Ditto is hosted, e.g. ditto.eclipse.org, localhost:8080, ...
   * @returns The next step to take
   */
  withDomain(domain: string): AuthenticationStep<T, U>;
}

/**
 * Interface to select the authentication type and credentials to use for requests.
 * @param <T> - type of API 1 Build steps.
 * @param <U> - type of API 2 Build steps.
 */
interface AuthenticationStep<T extends BuildStep, U extends BuildStep> extends BuildStep {
  /**
   * Sets the auth providers for the client.
   *
   * @param authProvider The authentication to use.
   * @param additionalAuthProviders Additional auth providers to use.
   */
  withAuthProvider(authProvider: AuthProvider, ...additionalAuthProviders: AuthProvider[]): ApiVersionStep<T, U>;
}

/**
 * Interface to select the API version to use for requests.
 * @param <T> - type of API 1 Build steps.
 * @param <U> - type of API 2 Build steps.
 */
interface ApiVersionStep<T extends BuildStep, U extends BuildStep> extends BuildStep {

  /**
   * Sets the API version to 1.
   *
   * @returns The next step to take
   */
  apiVersion1(): T;

  /**
   * Sets the API version to 2.
   *
   * @returns The next step to take
   */
  apiVersion2(): U;
}

/**
 * Custom builder context can be used when providing custom handles. The will be called with the given custom context.
 */
// tslint:disable-next-line:no-empty-interface
interface CustomBuilderContext {
}


/**
 * @param <R> - Type of the request sender builder.
 * @param <H> - Type of the handle.
 */
interface CustomThingsHandleStep<R, H extends ThingsHandle> extends BuildStep {
  /**
   * Use a custom things handle.
   * @param factory - factory for building the custom things handle.
   */
  withCustomThingsHandle(factory: (requestSenderBuilder: R, customBuilderContext?: CustomBuilderContext) => H): this;
}

/**
 * @param <R> - Type of the request sender builder.
 * @param <H> - Type of the handle.
 */
interface CustomFeaturesHandleStep<R, H extends FeaturesHandle> extends BuildStep {
  /**
   * Use a custom features handle.
   * @param factory - factory for building the custom features handle.
   */
  withCustomFeaturesHandle(factory: (requestSenderBuilder: R, thingId: string, customBuilderContext?: CustomBuilderContext) => H): this;
}

/**
 * @param <R> - Type of the request sender builder.
 * @param <H> - Type of the handle.
 */
interface CustomMessagesHandleStep<R, H extends MessagesHandle> extends BuildStep {
  /**
   * Use a custom messages handle.
   * @param factory - factory for building the custom handle.
   */
  withCustomMessagesHandle(factory: (requestSenderBuilder: R, customBuilderContext?: CustomBuilderContext) => H): this;
}

/**
 * @param <R> - Type of the request sender builder.
 * @param <H> - Type of the handle.
 */
interface CustomEventsHandleStep<R, H extends EventsHandle> extends BuildStep {
  /**
   * Use a custom events handle.
   * @param factory - factory for building the custom handle.
   */
  withCustomEventsHandle(factory: (requestSenderBuilder: R, customBuilderContext?: CustomBuilderContext) => H): this;
}

/**
 * @param <R> - Type of the request sender builder.
 * @param <H> - Type of the handle.
 */
interface CustomCommandsHandleStep<R, H extends CommandsHandle> extends BuildStep {
  /**
   * Use a custom commands handle.
   * @param factory - factory for building the custom handle.
   */
  withCustomCommandsHandle(factory: (requestSenderBuilder: R, customBuilderContext?: CustomBuilderContext) => H): this;
}

/**
 * @param <R> - Type of the request sender builder.
 * @param <H> - Type of the handle.
 */
interface CustomPoliciesHandleStep<R, H extends PoliciesHandle> extends BuildStep {
  /**
   * Use a custom policies handle.
   * @param factory - factory for building the custom handle.
   */
  withCustomPoliciesHandle(factory: (requestSenderBuilder: R, customBuilderContext?: CustomBuilderContext) => H): this;
}

/**
 * @param <R> - Type of the request sender builder.
 * @param <H> - Type of the handle.
 */
interface CustomSearchHandleStep<R, H extends SearchHandle> extends BuildStep {
  /**
   * Use a custom search handle.
   * @param factory - factory for building the custom handle.
   */
  withCustomSearchHandle(factory: (requestSenderBuilder: R, customBuilderContext?: CustomBuilderContext) => H): this;
}

/**
 * Abstract implementation of the relevant builder steps that are probably the same for all builder implementations.
 */
abstract class AbstractBuilder<T extends BuildStep, U extends BuildStep> implements ProtocolStep<T, U>, EnvironmentStep<T, U>,
  AuthenticationStep<T, U>, ApiVersionStep<T, U> {
  protected domain!: string;
  protected apiVersion!: ApiVersion;
  protected authProviders!: AuthProvider[];
  protected tls!: boolean;


  withTls(): EnvironmentStep<T, U> {
    this.tls = true;
    return this;
  }

  withoutTls(): EnvironmentStep<T, U> {
    this.tls = false;
    return this;
  }

  withDomain(url: string): AuthenticationStep<T, U> {
    this.domain = url;
    return this;
  }

  withAuthProvider(authProvider: AuthProvider, ...additionalAuthProviders: AuthProvider[]):
    ApiVersionStep<T, U> {
    this.authProviders = [authProvider, ...additionalAuthProviders];
    return this;
  }

  abstract apiVersion1(): T;

  abstract apiVersion2(): U;
}
