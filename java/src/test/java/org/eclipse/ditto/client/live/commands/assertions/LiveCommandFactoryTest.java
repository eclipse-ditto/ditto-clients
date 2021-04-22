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
package org.eclipse.ditto.client.live.commands.assertions;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.ditto.base.model.signals.commands.assertions.CommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.assumingFields;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ditto.client.live.commands.LiveCommandFactory;
import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.modify.CreateThingLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteAttributeLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteAttributesLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteFeatureLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteFeaturePropertiesLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteFeaturePropertyLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteFeaturesLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.DeleteThingLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.MergeThingLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyAttributeLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyAttributesLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyFeatureLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyFeaturePropertiesLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyFeaturePropertyLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyFeaturesLiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyThingLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveAttributeLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveAttributesLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveFeatureLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveFeaturePropertiesLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveFeaturePropertyLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveFeaturesLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveThingLiveCommand;
import org.eclipse.ditto.client.live.commands.query.RetrieveThingsLiveCommand;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.things.model.signals.commands.TestConstants;
import org.eclipse.ditto.things.model.signals.commands.modify.CreateThing;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteThing;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveAttribute;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveAttributes;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeature;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatures;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link org.eclipse.ditto.client.live.commands.LiveCommandFactory}.
 */
public final class LiveCommandFactoryTest {

    private LiveCommandFactory underTest;

    @Before
    public void setUp() {
        underTest = LiveCommandFactory.getInstance();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(LiveCommandFactory.class,
                areImmutable(),
                assumingFields("mappingStrategies").areSafelyCopiedUnmodifiableCollectionsWithImmutableElements());
    }

    @Test
    public void liveCommandFactoryIsSingleton() {
        assertThat(underTest).isSameAs(LiveCommandFactory.getInstance());
    }

    /**
     * This test runs in Maven just fine. Under IntelliJ it fails because IntelliJ reacts to violations of {@code
     * @Nonnull} annotations by its own with an IllegalArgumentException.
     */
    @SuppressWarnings("ConstantConditions")
    @Test
    public void tryToGetLiveCommandForNullCommand() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> underTest.getLiveCommand(null))
                .withMessage(MessageFormat.format("The {0} must not be null!", "command"))
                .withNoCause();
    }

    @Test
    public void tryToGetLiveCommandForCommandWithUnknownType() {
        final Command<?> commandMock = Mockito.mock(Command.class);
        Mockito.when(commandMock.getType()).thenReturn("Harambe");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> underTest.getLiveCommand(commandMock))
                .withMessage("No mapping strategy for command <%s> available! The command type <%s> is unknown!",
                        commandMock, commandMock.getType())
                .withNoCause();
    }

    @Test
    public void getCreateThingLiveCommandForCreateThing() {
        final CreateThing twinCommand = CreateThing.of(TestConstants.Thing.THING, null, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, CreateThingLiveCommand.class);
    }

    @Test
    public void getDeleteAttributeLiveCommandForDeleteAttribute() {
        final DeleteAttribute twinCommand = DeleteAttribute.of(TestConstants.Thing.THING_ID,
                TestConstants.Thing.LOCATION_ATTRIBUTE_POINTER, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, DeleteAttributeLiveCommand.class);
    }

    @Test
    public void getDeleteAttributesLiveCommandForDeleteAttributes() {
        final DeleteAttributes twinCommand = DeleteAttributes.of(TestConstants.Thing.THING_ID, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, DeleteAttributesLiveCommand.class);
    }

    @Test
    public void getDeleteFeatureLiveCommandForDeleteFeature() {
        final DeleteFeature twinCommand =
                DeleteFeature.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        DittoHeaders
                                .empty());
        createAndCheckLiveCommandFor(twinCommand, DeleteFeatureLiveCommand.class);
    }

    @Test
    public void getDeleteFeaturePropertiesLiveCommandForDeleteFeatureProperties() {
        final DeleteFeatureProperties twinCommand =
                DeleteFeatureProperties.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, DeleteFeaturePropertiesLiveCommand.class);
    }

    @Test
    public void getDeleteFeaturePropertyLiveCommandForDeleteFeatureProperty() {
        final DeleteFeatureProperty twinCommand =
                DeleteFeatureProperty.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_POINTER, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, DeleteFeaturePropertyLiveCommand.class);
    }

    @Test
    public void getDeleteFeaturesLiveCommandForDeleteFeatures() {
        final DeleteFeatures twinCommand = DeleteFeatures.of(TestConstants.Thing.THING_ID, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, DeleteFeaturesLiveCommand.class);
    }

    @Test
    public void getDeleteThingLiveCommandForDeleteThing() {
        final DeleteThing twinCommand = DeleteThing.of(TestConstants.Thing.THING_ID, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, DeleteThingLiveCommand.class);
    }

    @Test
    public void getModifyAttributeLiveCommandForModifyAttribute() {
        final ModifyAttribute twinCommand =
                ModifyAttribute.of(TestConstants.Thing.THING_ID, TestConstants.Thing.LOCATION_ATTRIBUTE_POINTER,
                        TestConstants.Thing.LOCATION_ATTRIBUTE_VALUE, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, ModifyAttributeLiveCommand.class);
    }

    @Test
    public void getModifyAttributesLiveCommandForModifyAttributes() {
        final ModifyAttributes twinCommand =
                ModifyAttributes.of(TestConstants.Thing.THING_ID, TestConstants.Thing.ATTRIBUTES,
                        DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, ModifyAttributesLiveCommand.class);
    }

    @Test
    public void getModifyFeatureLiveCommandForModifyFeature() {
        final ModifyFeature twinCommand =
                ModifyFeature.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR,
                        DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, ModifyFeatureLiveCommand.class);
    }

    @Test
    public void getModifyFeaturePropertiesLiveCommandForModifyFeatureProperties() {
        final ModifyFeatureProperties twinCommand =
                ModifyFeatureProperties.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        TestConstants.Feature.FLUX_CAPACITOR_PROPERTIES, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, ModifyFeaturePropertiesLiveCommand.class);
    }

    @Test
    public void getModifyFeaturePropertyLiveCommandForModifyFeatureProperty() {
        final ModifyFeatureProperty twinCommand =
                ModifyFeatureProperty.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_POINTER,
                        TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_VALUE, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, ModifyFeaturePropertyLiveCommand.class);
    }

    @Test
    public void getModifyFeaturesLiveCommandForModifyFeatures() {
        final ModifyFeatures twinCommand =
                ModifyFeatures.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FEATURES, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, ModifyFeaturesLiveCommand.class);
    }

    /**
     *
     */
    @Test
    public void getModifyThingLiveCommandForModifyThing() {
        final ModifyThing twinCommand = ModifyThing.of(TestConstants.Thing.THING_ID, TestConstants.Thing.THING, null,
                DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, ModifyThingLiveCommand.class);
    }

    /**
     *
     */
    @Test
    public void getMergeThingLiveCommandForMergeThing() {
        final MergeThing twinCommand =
                MergeThing.of(TestConstants.Thing.THING_ID, TestConstants.PATH, TestConstants.VALUE,
                        DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, MergeThingLiveCommand.class);
    }

    /**
     *
     */
    @Test
    public void getRetrieveAttributeLiveCommandForRetrieveAttribute() {
        final RetrieveAttribute twinCommand =
                RetrieveAttribute.of(TestConstants.Thing.THING_ID, TestConstants.Thing.LOCATION_ATTRIBUTE_POINTER,
                        DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, RetrieveAttributeLiveCommand.class);
    }

    @Test
    public void getRetrieveAttributesLiveCommandForRetrieveAttributes() {
        final RetrieveAttributes twinCommand =
                RetrieveAttributes.of(TestConstants.Thing.THING_ID, TestConstants.JSON_FIELD_SELECTOR_ATTRIBUTES,
                        DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, RetrieveAttributesLiveCommand.class);
    }

    @Test
    public void getRetrieveFeatureLiveCommandForRetrieveFeature() {
        final RetrieveFeature twinCommand =
                RetrieveFeature.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, RetrieveFeatureLiveCommand.class);
    }

    @Test
    public void getRetrieveFeaturePropertiesLiveCommandForRetrieveFeatureProperties() {
        final RetrieveFeatureProperties twinCommand =
                RetrieveFeatureProperties.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        TestConstants.JSON_FIELD_SELECTOR_FEATURE_PROPERTIES, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, RetrieveFeaturePropertiesLiveCommand.class);
    }

    @Test
    public void getRetrieveFeaturePropertyLiveCommandForRetrieveFeatureProperty() {
        final RetrieveFeatureProperty twinCommand =
                RetrieveFeatureProperty.of(TestConstants.Thing.THING_ID, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        TestConstants.Feature.FLUX_CAPACITOR_PROPERTY_POINTER, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, RetrieveFeaturePropertyLiveCommand.class);
    }

    @Test
    public void getRetrieveFeaturesLiveCommandForRetrieveFeatures() {
        final RetrieveFeatures twinCommand = RetrieveFeatures.of(TestConstants.Thing.THING_ID, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, RetrieveFeaturesLiveCommand.class);
    }

    @Test
    public void getRetrieveThingLiveCommandForRetrieveThing() {
        final RetrieveThing twinCommand = RetrieveThing.of(TestConstants.Thing.THING_ID, DittoHeaders.empty());
        createAndCheckLiveCommandFor(twinCommand, RetrieveThingLiveCommand.class);
    }

    @Test
    public void getRetrieveThingsLiveCommandForRetrieveThing() {
        final List<ThingId> thingIds = Arrays.asList(ThingId.inDefaultNamespace("boatyMcBoatface"),
                ThingId.inDefaultNamespace("Harambe"));
        final RetrieveThings twinCommand = RetrieveThings.getBuilder(thingIds)
                .dittoHeaders(DittoHeaders.empty())
                .build();
        createAndCheckLiveCommandFor(twinCommand, RetrieveThingsLiveCommand.class);
    }

    private void createAndCheckLiveCommandFor(final Command<?> twinCommand,
            final Class<? extends LiveCommand<?, ?>> expectedLiveCommandClass) {
        final LiveCommand<?, ?> liveCommand = underTest.getLiveCommand(twinCommand);

        assertThat(liveCommand)
                .withType(twinCommand.getType())
                .withResourcePath(twinCommand.getResourcePath())
                .withDittoHeaders(twinCommand.getDittoHeaders())
                .withManifest(twinCommand.getManifest())
                .hasJsonString(twinCommand.toJsonString())
                .isInstanceOf(expectedLiveCommandClass);
    }

}
