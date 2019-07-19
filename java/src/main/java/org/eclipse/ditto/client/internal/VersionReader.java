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
package org.eclipse.ditto.client.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read service version and build date from ditto-client-version.properties file.
 *
 * @since 1.0.0
 */
public final class VersionReader {

    private static final Logger LOG = LoggerFactory.getLogger(VersionReader.class);
    private static final String VERSION_PROPERTIES = "/ditto-client-version.properties";
    private static final String VERSION_PROPERTY_NAME = "version";
    private static final String BUILD_DATE_PROPERTY_NAME = "builddate";
    private static final String DEFAULT_VALUE = "COULD NOT BE DETERMINED";

    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try (final InputStream is = VersionReader.class.getResourceAsStream(VERSION_PROPERTIES)) {
            PROPERTIES.load(is);
        } catch (final IOException e) {
            LOG.warn("Could not load version properties.", e);
        }
    }

    private VersionReader() {
        // helper class
    }

    /**
     * @return version of this client
     */
    public static String determineClientVersion() {
        return PROPERTIES.getProperty(VERSION_PROPERTY_NAME, DEFAULT_VALUE);
    }

    /**
     * @return build timestamp
     */
    public static String determineBuildTimeStamp() {
        return PROPERTIES.getProperty(BUILD_DATE_PROPERTY_NAME, DEFAULT_VALUE);
    }
}
