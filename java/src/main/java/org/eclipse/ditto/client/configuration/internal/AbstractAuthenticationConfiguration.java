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
package org.eclipse.ditto.client.configuration.internal;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;

/**
 * Abstract implementation for common aspects of
 * {@link org.eclipse.ditto.client.configuration.AuthenticationConfiguration}.
 *
 * @since 1.0.0
 */
@Immutable
abstract class AbstractAuthenticationConfiguration implements AuthenticationConfiguration {

    private final String sessionId;
    private final Map<String, String> additionalHeaders;
    @Nullable private final ProxyConfiguration proxyConfiguration;

    AbstractAuthenticationConfiguration(final String identifier, final Map<String, String> additionalHeaders,
            @Nullable final ProxyConfiguration proxyConfiguration) {
        checkNotNull(identifier, "identifier");
        checkNotNull(additionalHeaders, "additionalHeaders");

        sessionId = identifier + ":" + UUID.randomUUID().toString();
        this.additionalHeaders = Collections.unmodifiableMap(new HashMap<>(additionalHeaders));
        this.proxyConfiguration = proxyConfiguration;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    @Override
    public Optional<ProxyConfiguration> getProxyConfiguration() {
        return Optional.ofNullable(proxyConfiguration);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbstractAuthenticationConfiguration that = (AbstractAuthenticationConfiguration) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(additionalHeaders, that.additionalHeaders) &&
                Objects.equals(proxyConfiguration, that.proxyConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, additionalHeaders, proxyConfiguration);
    }

    @Override
    public String toString() {
        return "sessionId=" + sessionId +
                ", additionalHeaders=" + additionalHeaders +
                ", proxyConfiguration=" + proxyConfiguration;
    }

}
