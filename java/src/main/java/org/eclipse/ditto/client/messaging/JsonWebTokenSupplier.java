/*
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
package org.eclipse.ditto.client.messaging;

import java.util.function.Supplier;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.jwt.JsonWebToken;

/**
 * A supplier of OAuth 2 Tokens.
 *
 * @since 1.0.0
 */
@Immutable
public interface JsonWebTokenSupplier extends Supplier<JsonWebToken> {

    /**
     * Supplies a base64 encoded access token.
     *
     * @return the token.
     */
    @Override
    JsonWebToken get();

}
