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
package org.eclipse.ditto.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.atteo.classindex.ClassIndex;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.live.messages.MessageSerializer;
import org.eclipse.ditto.client.messaging.MessagingException;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.base.model.auth.AuthorizationContext;
import org.eclipse.ditto.base.model.exceptions.DittoRuntimeException;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.json.Jsonifiable;
import org.eclipse.ditto.jwt.model.JsonWebToken;
import org.eclipse.ditto.messages.model.Message;
import org.eclipse.ditto.messages.model.MessagesModelFactory;
import org.eclipse.ditto.policies.model.PoliciesModelFactory;
import org.eclipse.ditto.things.model.AttributesModelFactory;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.adapter.DittoProtocolAdapter;
import org.eclipse.ditto.base.model.signals.announcements.Announcement;
import org.eclipse.ditto.policies.model.signals.announcements.PolicyAnnouncement;
import org.eclipse.ditto.base.model.signals.JsonParsable;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.base.model.signals.commands.CommandResponse;
import org.eclipse.ditto.messages.model.signals.commands.MessageCommand;
import org.eclipse.ditto.messages.model.signals.commands.MessageCommandResponse;
import org.eclipse.ditto.things.model.signals.commands.ThingCommand;
import org.eclipse.ditto.things.model.signals.commands.ThingErrorResponse;
import org.eclipse.ditto.things.model.signals.commands.modify.ThingModifyCommand;
import org.eclipse.ditto.things.model.signals.commands.query.ThingQueryCommand;
import org.eclipse.ditto.base.model.signals.events.Event;
import org.eclipse.ditto.base.model.signals.events.EventRegistry;
import org.eclipse.ditto.things.model.signals.events.ThingEvent;
import org.eclipse.ditto.thingsearch.model.signals.commands.subscription.CancelSubscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neovisionaries.ws.client.WebSocket;

/**
 * IntegrationTest starting all Ditto-Client bundles and verifying if they work.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class RunOSGiContainerIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(RunOSGiContainerIntegrationTest.class);

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] configure() throws IOException {
        final String clientBundlesLocation = System.getProperty("client.bundles.zip.location",
                "target/ditto-client-" + System.getProperty("project.version") + "-bundles.zip");

        final String bundlesZipOutputDirectory = unzipBundlesZip(clientBundlesLocation);

        final List<Option> bundleOptions;
        try (final DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(bundlesZipOutputDirectory))) {
            bundleOptions = StreamSupport.stream(dirStream.spliterator(), false)
                    .map(file -> bundle("reference:file:" + file))
                    .collect(Collectors.toList());
        }

        final String clientBundleLocation = System.getProperty("client.bundle.artifact.location",
                "target/ditto-client-" + System.getProperty("project.version") + ".jar");

        // no Logger implementation loaded here, so use "System.out":
        System.out.println("Configuring bundle context with clientBundle: " + clientBundleLocation);

        final List<Option> allOptions = new ArrayList<>();
        allOptions.add(cleanCaches());
        allOptions.addAll(bundleOptions);
        allOptions.add(mavenBundle("ch.qos.logback", "logback-core", "1.2.3"));
        allOptions.add(mavenBundle("ch.qos.logback", "logback-classic", "1.2.3"));
        allOptions.add(junitBundles());
        allOptions.add(systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"));

        final Option[] options = allOptions.toArray(new Option[]{});
        return options(options);
    }

    private static String unzipBundlesZip(final String clientBundlesLocation) {
        //We will unzip files in this folder
        final String outputBase = System.getProperty("project.build.directory") + "/uncompressed/";
        String outputDirectory = outputBase;

        try (ZipFile zipFile = new ZipFile(Paths.get(clientBundlesLocation).toFile())) {
            final FileSystem fileSystem = FileSystems.getDefault();
            //Get file entries
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            if (!Paths.get(outputDirectory).toFile().exists()) {
                Files.createDirectory(fileSystem.getPath(outputDirectory));
            }

            //Iterate over entries
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    outputDirectory += entry.getName();
                    if (!Paths.get(outputDirectory).toFile().exists()) {
                        System.out.println("Creating Directory: " + outputDirectory);
                        Files.createDirectories(fileSystem.getPath(outputDirectory));
                    }
                } else {
                    final InputStream is = zipFile.getInputStream(entry);
                    final BufferedInputStream bis = new BufferedInputStream(is);
                    final String uncompressedFileName = outputBase + entry.getName();
                    final Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                    Files.createFile(uncompressedFilePath);
                    try (final FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName)) {
                        while (bis.available() > 0) {
                            fileOutput.write(bis.read());
                        }
                    }
                    System.out.println("Written: " + entry.getName());
                }
            }
        } catch (final IOException e) {
            System.err.println("IOException: " + e.getMessage());
            // error while opening a ZIP file
        }

        return outputDirectory;
    }

    @Test
    public void shouldBeAbleToResolveBundlesOfUsedDependencies() {
        assertNotNull(this.bundleContext);

        // slf4j-api:
        LOG.info("Ensuring slf4j-api is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Logger.class));

        // minimal-json:
        LOG.info("Ensuring minimal-json is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(com.eclipsesource.json.JsonParser.class));

        // nv-websocket-client:
        LOG.info("Ensuring nv-websocket-client is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(WebSocket.class));

        // classindex:
        LOG.info("Ensuring classindex is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ClassIndex.class));

        //reactive-streams:
        LOG.info("Ensuring rective-streams is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Subscription.class));

        // ditto-json:
        LOG.info("Ensuring ditto-json is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(JsonFactory.class));

        // ditto-base-model:
        LOG.info("Ensuring ditto-base-model is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(AuthorizationContext.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(DittoHeaders.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Jsonifiable.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(DittoRuntimeException.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Event.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(EventRegistry.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Announcement.class));

        // ditto-jwt-model
        LOG.info("Ensuring ditto-jwt-model is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(JsonWebToken.class));

        // ditto-policies-model
        LOG.info("Ensuring ditto-policies-model is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(PoliciesModelFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(PolicyAnnouncement.class));

        // ditto-things-model:
        LOG.info("Ensuring ditto-things-model is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(AttributesModelFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingsModelFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingEvent.class));

        // ditto-thingsearch-model:
        LOG.info("Ensuring ditto-thingsearch-model is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(CancelSubscription.class));

        // ditto-messages-model:
        LOG.info("Ensuring ditto-model-model is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Message.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessagesModelFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessageCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessageCommandResponse.class));

        // ditto-protocol:
        LOG.info("Ensuring ditto-protocol is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Adaptable.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(DittoProtocolAdapter.class));

        // ditto-client:
        LOG.info("Ensuring ditto-client is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(DittoClient.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessagingException.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessageSender.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(TwinThingHandle.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(LiveThingHandle.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Options.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(DittoClients.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessageSerializer.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessagingProvider.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessagingProviders.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessagingConfiguration.class));
    }

    private static void checkBundleIsPresentInstalledAndActive(final Bundle bundle) {
        assertNotNull(bundle);
        assertEquals("Expecting " + bundle.getSymbolicName() + " bundle to be active", Bundle.ACTIVE,
                bundle.getState());
    }

}
