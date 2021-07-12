/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.client.configuration;

import java.time.Duration;

/**
 * Additional JWT / Token specific configurations.
 *
 * @since 2.1.0
 */
public interface TokenAuthenticationConfiguration extends AuthenticationConfiguration {

    /**
     * Returns the grace period which will be subtracted from token expiry to trigger the configured token supplier.
     *
     * @return the grace period.
     */
    Duration getExpiryGracePeriod();

}
