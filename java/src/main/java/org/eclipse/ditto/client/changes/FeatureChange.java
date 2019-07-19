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
package org.eclipse.ditto.client.changes;

import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.signals.base.WithFeatureId;

/**
 * Represents a change of a Thing regarding a single {@link Feature}.
 *
 * @since 1.0.0
 */
public interface FeatureChange extends Change, WithFeatureId {

    /**
     * Returns the Feature which was object of the change.
     *
     * @return the modified Feature.
     */
    Feature getFeature();

    @Override
    default String getFeatureId() {
        return getFeature().getId();
    }
}
