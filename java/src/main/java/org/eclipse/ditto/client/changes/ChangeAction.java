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

/**
 * An enumeration of the operations which caused a change.
 *
 * @since 1.0.0
 */
public enum ChangeAction {

    /**
     * New entry was for the first time created.
     */
    CREATED,

    /**
     * An already existing entry was updated.
     */
    UPDATED,

    /**
     * An already existing entry was deleted.
     */
    DELETED,

    /**
     * An already existing entry was merged.
     */
    MERGED

}
