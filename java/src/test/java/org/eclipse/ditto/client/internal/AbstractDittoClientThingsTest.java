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

import java.util.Arrays;
import java.util.List;

import org.eclipse.ditto.client.management.CommonManagement;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Prepares a Ditto Client instance for testing with uncaught error and dispatch handlers.
 */
@RunWith(Parameterized.class)
public abstract class AbstractDittoClientThingsTest extends AbstractDittoClientTest {

    @Parameterized.Parameters(name = "channel={0}")
    public static List<TopicPath.Channel> channels() {
        return Arrays.asList(TopicPath.Channel.TWIN, TopicPath.Channel.LIVE);
    }

    @Parameterized.Parameter
    public TopicPath.Channel channel;

    protected DittoHeaders headersWithChannel() {
        return DittoHeaders.newBuilder().channel(channel.getName()).build();
    }

    protected CommonManagement<?, ?> getManagement() {
        switch (channel) {
            case TWIN:
                return client.twin();
            case LIVE:
                return client.live();
            default:
                throw new AssertionError("Unexpected channel=" + channel);
        }
    }

    protected AcknowledgementLabel getChannelAcknowledgementLabel() {
        return channel == TopicPath.Channel.TWIN
                ? DittoAcknowledgementLabel.TWIN_PERSISTED
                : DittoAcknowledgementLabel.LIVE_RESPONSE;
    }
}
