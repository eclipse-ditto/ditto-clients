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
package org.eclipse.ditto.client.live.commands.modify;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.signals.commands.base.Command;

/**
 * A factory for getting immutable instances of modify {@link LiveCommand LiveCommand}s.
 *
 * @since 2.0.0
 */
@ParametersAreNonnullByDefault
@Immutable
public final class ModifyLiveCommandFactory {

    private ModifyLiveCommandFactory() {
        throw new AssertionError();
    }

    /**
     * Returns a new immutable instance of {@code CreateThingLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.CreateThing}.
     */
    @Nonnull
    public static CreateThingLiveCommand createThing(final Command<?> command) {
        return CreateThingLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteAttributeLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute}.
     */
    @Nonnull
    public static DeleteAttributeLiveCommand deleteAttribute(final Command<?> command) {
        return DeleteAttributeLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteAttributesLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes}.
     */
    @Nonnull
    public static DeleteAttributesLiveCommand deleteAttributes(final Command<?> command) {
        return DeleteAttributesLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteFeatureLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeature}.
     */
    @Nonnull
    public static DeleteFeatureLiveCommand deleteFeature(final Command<?> command) {
        return DeleteFeatureLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteFeatureDefinitionLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinition}.
     */
    @Nonnull
    public static DeleteFeatureDefinitionLiveCommand deleteFeatureDefinition(final Command<?> command) {
        return DeleteFeatureDefinitionLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteFeaturePropertiesLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties}.
     */
    @Nonnull
    public static DeleteFeaturePropertiesLiveCommand deleteFeatureProperties(final Command<?> command) {
        return DeleteFeaturePropertiesLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteFeaturePropertyLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty}.
     */
    @Nonnull
    public static DeleteFeaturePropertyLiveCommand deleteFeatureProperty(final Command<?> command) {
        return DeleteFeaturePropertyLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteFeaturesLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures}.
     */
    @Nonnull
    public static DeleteFeaturesLiveCommand deleteFeatures(final Command<?> command) {
        return DeleteFeaturesLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code DeleteThingLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.DeleteThing}.
     */
    @Nonnull
    public static DeleteThingLiveCommand deleteThing(final Command<?> command) {
        return DeleteThingLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyAttributeLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute}.
     */
    @Nonnull
    public static ModifyAttributeLiveCommand modifyAttribute(final Command<?> command) {
        return ModifyAttributeLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyAttributesLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes}.
     */
    @Nonnull
    public static ModifyAttributesLiveCommand modifyAttributes(final Command<?> command) {
        return ModifyAttributesLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyFeatureLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeature}.
     */
    @Nonnull
    public static ModifyFeatureLiveCommand modifyFeature(final Command<?> command) {
        return ModifyFeatureLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyFeatureDefinitionLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition}.
     */
    @Nonnull
    public static ModifyFeatureDefinitionLiveCommand modifyFeatureDefinition(final Command<?> command) {
        return ModifyFeatureDefinitionLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyFeaturePropertiesLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties}.
     */
    @Nonnull
    public static ModifyFeaturePropertiesLiveCommand modifyFeatureProperties(final Command<?> command) {
        return ModifyFeaturePropertiesLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyFeaturePropertyLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty}.
     */
    @Nonnull
    public static ModifyFeaturePropertyLiveCommand modifyFeatureProperty(final Command<?> command) {
        return ModifyFeaturePropertyLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyFeaturesLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures}.
     */
    @Nonnull
    public static ModifyFeaturesLiveCommand modifyFeatures(final Command<?> command) {
        return ModifyFeaturesLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code ModifyThingLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.ModifyThing}.
     */
    @Nonnull
    public static ModifyThingLiveCommand modifyThing(final Command<?> command) {
        return ModifyThingLiveCommandImpl.of(command);
    }

    /**
     * Returns a new immutable instance of {@code MergeThingLiveCommand}.
     *
     * @param command the command to base the result on.
     * @return the instance.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws ClassCastException if {@code command} is not an instance of
     * {@link org.eclipse.ditto.signals.commands.things.modify.MergeThing}.
     */
    @Nonnull
    public static MergeThingLiveCommand mergeThing(final Command<?> command) {
        return MergeThingLiveCommandImpl.of(command);
    }

}
