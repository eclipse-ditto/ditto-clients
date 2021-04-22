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
package org.eclipse.ditto.client.live.commands;

import static org.eclipse.ditto.base.model.common.ConditionChecker.checkNotNull;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.live.commands.base.LiveCommand;
import org.eclipse.ditto.client.live.commands.modify.ModifyLiveCommandFactory;
import org.eclipse.ditto.client.live.commands.query.QueryLiveCommandFactory;
import org.eclipse.ditto.base.model.signals.commands.Command;
import org.eclipse.ditto.things.model.signals.commands.modify.CreateThing;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.DeleteThing;
import org.eclipse.ditto.things.model.signals.commands.modify.MergeThing;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttribute;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyAttributes;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeature;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyFeatures;
import org.eclipse.ditto.things.model.signals.commands.modify.ModifyThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveAttribute;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveAttributes;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeature;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureDefinition;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureProperties;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatureProperty;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveFeatures;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThings;

/**
 * A factory for creating immutable instances of {@link LiveCommand} based on existing <em>Twin Commands</em>.
 *
 * @since 2.0.0
 */
@Immutable
public final class LiveCommandFactory {

    private static final int STRATEGIES_NUMBER = 26;
    private static final LiveCommandFactory INSTANCE = new LiveCommandFactory();

    private final Map<String, Function<Command<?>, LiveCommand<?, ?>>> mappingStrategies;

    private LiveCommandFactory() {
        mappingStrategies = Collections.unmodifiableMap(initMappingStrategies());
    }

    private static Map<String, Function<Command<?>, LiveCommand<?, ?>>> initMappingStrategies() {
        final Map<String, Function<Command<?>, LiveCommand<?, ?>>> result = new HashMap<>(STRATEGIES_NUMBER);
        result.put(CreateThing.TYPE, ModifyLiveCommandFactory::createThing);
        result.put(DeleteAttribute.TYPE, ModifyLiveCommandFactory::deleteAttribute);
        result.put(DeleteAttributes.TYPE, ModifyLiveCommandFactory::deleteAttributes);
        result.put(DeleteFeature.TYPE, ModifyLiveCommandFactory::deleteFeature);
        result.put(DeleteFeatureDefinition.TYPE, ModifyLiveCommandFactory::deleteFeatureDefinition);
        result.put(DeleteFeatureProperties.TYPE, ModifyLiveCommandFactory::deleteFeatureProperties);
        result.put(DeleteFeatureProperty.TYPE, ModifyLiveCommandFactory::deleteFeatureProperty);
        result.put(DeleteFeatures.TYPE, ModifyLiveCommandFactory::deleteFeatures);
        result.put(DeleteThing.TYPE, ModifyLiveCommandFactory::deleteThing);
        result.put(ModifyAttribute.TYPE, ModifyLiveCommandFactory::modifyAttribute);
        result.put(ModifyAttributes.TYPE, ModifyLiveCommandFactory::modifyAttributes);
        result.put(ModifyFeature.TYPE, ModifyLiveCommandFactory::modifyFeature);
        result.put(ModifyFeatureDefinition.TYPE, ModifyLiveCommandFactory::modifyFeatureDefinition);
        result.put(ModifyFeatureProperties.TYPE, ModifyLiveCommandFactory::modifyFeatureProperties);
        result.put(ModifyFeatureProperty.TYPE, ModifyLiveCommandFactory::modifyFeatureProperty);
        result.put(ModifyFeatures.TYPE, ModifyLiveCommandFactory::modifyFeatures);
        result.put(ModifyThing.TYPE, ModifyLiveCommandFactory::modifyThing);
        result.put(MergeThing.TYPE, ModifyLiveCommandFactory::mergeThing);

        result.put(RetrieveAttribute.TYPE, QueryLiveCommandFactory::retrieveAttribute);
        result.put(RetrieveAttributes.TYPE, QueryLiveCommandFactory::retrieveAttributes);
        result.put(RetrieveFeature.TYPE, QueryLiveCommandFactory::retrieveFeature);
        result.put(RetrieveFeatureDefinition.TYPE, QueryLiveCommandFactory::retrieveFeatureDefinition);
        result.put(RetrieveFeatureProperties.TYPE, QueryLiveCommandFactory::retrieveFeatureProperties);
        result.put(RetrieveFeatureProperty.TYPE, QueryLiveCommandFactory::retrieveFeatureProperty);
        result.put(RetrieveFeatures.TYPE, QueryLiveCommandFactory::retrieveFeatures);
        result.put(RetrieveThing.TYPE, QueryLiveCommandFactory::retrieveThing);
        result.put(RetrieveThings.TYPE, QueryLiveCommandFactory::retrieveThings);
        return result;
    }

    /**
     * Returns an instance of {@code LiveCommandFactory}.
     *
     * @return the instance.
     */
    @Nonnull
    public static LiveCommandFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Returns an immutable {@link LiveCommand} which is associated with the specified Command.
     *
     * @param command the command to get a LiveCommand for.
     * @return the LiveCommand.
     * @throws NullPointerException if {@code command} is {@code null}.
     * @throws IllegalArgumentException if the given command cannot be mapped to a LiveCommand because there is no
     * mapping strategy associated with the command's type.
     */
    @Nonnull
    public LiveCommand<?, ?> getLiveCommand(@Nonnull final Command<?> command) {
        checkNotNull(command, "command");

        final String commandType = command.getType();
        final Function<Command<?>, LiveCommand<?, ?>> commandToLiveCommand = mappingStrategies.get(commandType);
        if (null == commandToLiveCommand) {
            final String msgTemplate = "No mapping strategy for command <{0}> available!"
                    + " The command type <{1}> is unknown!";
            throw new IllegalArgumentException(MessageFormat.format(msgTemplate, command, commandType));
        }
        return commandToLiveCommand.apply(command);
    }

}
