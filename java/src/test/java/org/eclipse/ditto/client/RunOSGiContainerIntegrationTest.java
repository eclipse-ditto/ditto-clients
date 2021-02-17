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
import org.eclipse.ditto.client.live.commands.LiveCommandFactory;
import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.base.LiveEventFactory;
import org.eclipse.ditto.client.live.commands.modify.CreateThingLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyLiveCommandFactory;
import org.eclipse.ditto.client.live.commands.query.QueryLiveCommandFactory;
import org.eclipse.ditto.client.live.commands.query.RetrieveThingLiveCommand;
import org.eclipse.ditto.client.live.messages.MessageSender;
import org.eclipse.ditto.client.live.messages.MessageSerializer;
import org.eclipse.ditto.client.messaging.MessagingException;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.Jsonifiable;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.model.messages.MessagesModelFactory;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.things.AccessControlListModelFactory;
import org.eclipse.ditto.model.things.AttributesModelFactory;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.signals.base.JsonParsable;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.messages.MessageCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommand;
import org.eclipse.ditto.signals.commands.things.query.ThingQueryCommand;
import org.eclipse.ditto.signals.events.base.Event;
import org.eclipse.ditto.signals.events.base.EventRegistry;
import org.eclipse.ditto.signals.events.things.ThingEvent;
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

        // ditto-json:
        LOG.info("Ensuring ditto-json is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(JsonFactory.class));

        // ditto-model-base:
        LOG.info("Ensuring ditto-model-base is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(AuthorizationContext.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(DittoHeaders.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Jsonifiable.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(DittoRuntimeException.class));

        // ditto-model-policies
        LOG.info("Ensuring ditto-model-policies is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(PoliciesModelFactory.class));

        // ditto-model-things:
        LOG.info("Ensuring ditto-model-things is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(AccessControlListModelFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(AttributesModelFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingsModelFactory.class));

        // ditto-model-messages:
        LOG.info("Ensuring ditto-model-messages is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Message.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessagesModelFactory.class));

        // ditto-signals-base:
        LOG.info("Ensuring ditto-signals-base is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(JsonParsable.class));

        // ditto-signals-commands-base:
        LOG.info("Ensuring ditto-signals-commands-base is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Command.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(CommandResponse.class));

        // ditto-signals-commands-things:
        LOG.info("Ensuring ditto-signals-commands-things is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingModifyCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingQueryCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingErrorResponse.class));

        // ditto-signals-commands-messages:
        LOG.info("Ensuring ditto-signals-commands-messages is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessageCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(MessageCommandResponse.class));

        // ditto-signals-commands-live:
        LOG.info("Ensuring ditto-signals-commands-live is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(LiveCommandFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(QueryLiveCommandFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(RetrieveThingLiveCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ModifyLiveCommandFactory.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(CreateThingLiveCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(LiveCommand.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(LiveEventFactory.class));

        // ditto-signals-events-base:
        LOG.info("Ensuring ditto-signals-events-base is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(Event.class));
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(EventRegistry.class));

        // ditto-signals-events-things:
        LOG.info("Ensuring ditto-signals-events-things is usable from OSGi..");
        checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ThingEvent.class));

        // ditto-protocol-adapter:
        LOG.info("Ensuring ditto-protocol-adapter is usable from OSGi..");
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
