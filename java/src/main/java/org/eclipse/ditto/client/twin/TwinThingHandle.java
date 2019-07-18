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
package org.eclipse.ditto.client.twin;

import org.eclipse.ditto.client.management.ThingHandle;

/**
 * A {@code TwinThingHandle} provides management and registration functionality for specific {@code Twin Thing}s hosted
 * at and managed by Eclipse Ditto.
 *
 * @since 1.0.0
 */
public interface TwinThingHandle extends ThingHandle<TwinFeatureHandle> {

}
