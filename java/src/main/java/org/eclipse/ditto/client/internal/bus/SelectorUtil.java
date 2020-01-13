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
package org.eclipse.ditto.client.internal.bus;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.internal.HandlerRegistry;
import org.eclipse.ditto.client.internal.SpecificChangeBuilderFunction;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonKey;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.json.JsonValueContainer;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.protocoladapter.JsonifiableAdaptable;
import org.slf4j.Logger;

/**
 * Helper around creating {@link JsonPointerSelector}s, registering handlers, etc.
 *
 * @since 1.0.0
 */
public final class SelectorUtil {

    private static final char[] CHARS_TO_REPLACE = new char[]{
            '\\',
            '.',
            '[',
            '{',
            '(',
            '*',
            '+',
            '?',
            '^',
            '$',
            '|'
    };
    private static final Pattern DOUBLE_SLASH_PATTERN = Pattern.compile("//");

    private SelectorUtil() {
        throw new AssertionError();
    }

    /**
     * Formats the passed in {@code pointerFormat} with the passed {@code arguments} and wraps this formatted String in
     * an {@link JsonPointerSelector}.
     *
     * @param logger the Logger to which to log to.
     * @param pointerFormat the {@link MessageFormat} text to format.
     * @param arguments the arguments for the {@link MessageFormat} text.
     * @return the {@link JsonPointerSelector} containing the formatted text with arguments as JsonPointer.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static JsonPointerSelector formatJsonPointer(final Logger logger, final String pointerFormat,
            final Object... arguments) {

        checkNotNull(logger, "logger");
        checkNotNull(pointerFormat, "pointer format");
        checkNotNull(arguments, "arguments");
        final Matcher matcher = DOUBLE_SLASH_PATTERN.matcher(MessageFormat.format(pointerFormat, arguments));
        final String format = matcher.replaceAll("/");
        logger.trace("Created consumer for JSON pointer {}", format);

        return JsonPointerSelectors.jsonPointer(format);
    }

    /**
     * Adds a handler on the passed {@link PointerBus in Bus} handling ThingEvents with the specified {@code
     * thingEventTypeString} and {@code eventClass} by calculating the Bus address by applying the passed {@code
     * addressBuilderFunction} and creating the {@link org.eclipse.ditto.client.changes.Change} to put on the Bus at
     * this address by applying the passed {@code changeBuilderFunction}.
     *
     * @param logger the Logger to use for logging
     * @param in the "in" Bus to register the handler on
     * @param thingEventTypeString the "TYPE" string of the ThingEvent
     * @param eventClass the type of the ThingEvent to handle
     * @param addressBuilderFunction the function to build the Bus address from the occurred ThingEvent
     * @param changeBuilderFunction the function to build the {@link org.eclipse.ditto.client.changes.Change} from the
     * occurred ThingEvent
     * @param <T> the type of the ThingEvent
     */
    public static <T extends org.eclipse.ditto.signals.events.base.Event> void addHandlerForThingEvent(
            final Logger logger,
            final PointerBus in,
            final String thingEventTypeString,
            final Class<T> eventClass,
            final Function<T, String> addressBuilderFunction,
            final BiFunction<T, JsonifiableAdaptable, Change> changeBuilderFunction) {

        logger.trace("Adding bus handler for address '{}'", thingEventTypeString);

        in.on(JsonPointerSelectors.jsonPointer(thingEventTypeString), e -> {
            final T event =
                    ((Message<?>) e.getData()).getPayload()
                            .filter(p -> eventClass.isAssignableFrom(p.getClass()))
                            .map(eventClass::cast)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Could not map received event '" + thingEventTypeString +
                                            "' which should be of type '" + eventClass +
                                            "'. The actual class of the event is: " +
                                            ((Message<?>) e.getData()).getPayload()
                                                    .orElseThrow(() ->
                                                            new IllegalStateException(
                                                                    "Payload of event was not present"))
                                                    .getClass()));
            final JsonifiableAdaptable jsonifiableAdaptable = (JsonifiableAdaptable) e.getAdditionalData();

            final String address = addressBuilderFunction.apply(event);
            final Change change = changeBuilderFunction.apply(event, jsonifiableAdaptable);

            final List<JsonPointer> jsonPointers = change.getValue()
                    .filter(JsonValue::isObject)
                    .map(JsonValue::asObject)
                    .map(obj -> calculateJsonPointerHierarchy(JsonPointer.empty(), obj))
                    .orElse(Collections.singletonList(JsonPointer.empty()));

            // notify the address where the Change actually happened:
            final JsonPointer jsonPointer = JsonPointer.of(address);
            final JsonPointerWithChangePaths
                    jsonPointerWithChangePaths = new JsonPointerWithChangePaths(jsonPointer, jsonPointers);
            logger.trace("Notifying bus at address '{}' with obj: {}", jsonPointerWithChangePaths, change);
            in.notify(jsonPointerWithChangePaths, change, jsonifiableAdaptable);
        });
    }

    /**
     * Calculates a List of JsonPointers based on the passed {@code entryPointer} and the passed {@code fromJsonObject}.
     * The hierarchy of the {@code fromJsonObject} will be "mapped" to the returned List of JsonPointers.
     * <p>
     * Example: If a {@code entryPointer} of "/things/my:thing1/attributes" is given and a {@code fromJsonObject} of
     * {@code { "foo": { "sub": 1, "misc": "hello" }, "bar": { "other": false } } } the resulting List of JsonPointers
     * would be:
     * <ul>
     * <li>/things/my:thing1/attributes/foo</li>
     * <li>/things/my:thing1/attributes/bar/other</li>
     * </ul>
     */
    private static List<JsonPointer> calculateJsonPointerHierarchy(final JsonPointer entryPointer,
            final JsonValueContainer<JsonField> fromJsonObject) {

        return fromJsonObject.stream()
                .map(jsonField -> {
                    final JsonValue value = jsonField.getValue();
                    final JsonPointer pointerOnLevel = entryPointer.addLeaf(jsonField.getKey());
                    if (value.isObject()) {
                        final List<JsonPointer> pointersOnLevel = new ArrayList<>();
                        final JsonObject objectOnLevel = value.asObject();
                        if (objectOnLevel.getSize() > 1) {
                            // if more than one sub-fields are contained in the object on this level, we add the "parent"
                            // aggregating both of the changed fields:
                            pointersOnLevel.add(pointerOnLevel);
                        }

                        // recurse further "down":
                        pointersOnLevel.addAll(calculateJsonPointerHierarchy(pointerOnLevel, objectOnLevel));
                        return pointersOnLevel;
                    } else {
                        return Collections.singletonList(pointerOnLevel);
                    }
                })
                .reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });
    }

    /**
     * Registers for the passed {@code selector} and {@code changeClass} the specified {@code handler} which will get a
     * Change of type {@code <T>} when a messages on the bus matches the passed {@link DefaultJsonPointerSelector
     * selector}.
     *
     * @param inHandlerRegistry the HandlerRegistry to use for registering the {@code handler} with the passed {@code
     * registrationId}
     * @param registrationId the ID to register in the {@code inHandlerRegistry} with
     * @param selector the JsonPointerSelector to use for matching on the event bus
     * @param changeClass the type of the Change to expect
     * @param handler the handler which will be notified of the Change
     * @param changeBuilderFunction the Function to use for building a specific Change of the type {@code <T>}
     * @param <T> the type of the Change, e.g. {@link org.eclipse.ditto.client.changes.ThingChange}
     */
    public static <T extends Change> void registerForChanges(final HandlerRegistry<?, ?> inHandlerRegistry,
            final String registrationId,
            final JsonPointerSelector selector,
            final Class<T> changeClass,
            final Consumer<T> handler,
            final SpecificChangeBuilderFunction<T> changeBuilderFunction) {

        // everything after /things/{thingId}:
        final Optional<JsonPointer> subPath = selector.getPointer().getSubPointer(2);
        final JsonPointer selectorTemplatePath = subPath.orElse(JsonPointer.empty());

        // get the configured Dispatcher for the Thing Client
        final Executor busDispatcher = inHandlerRegistry.getBusExecutor();

        inHandlerRegistry.register(registrationId, selector, event -> {
            if (event.getData() instanceof Change) {
                final Change rootChange = (Change) event.getData();

                if (event.getPointer() instanceof JsonPointerWithChangePaths) {

                    final JsonPointerWithChangePaths jsonPointerWithChangePaths =
                            (JsonPointerWithChangePaths) event.getPointer();

                    final JsonPointer targetPointer = jsonPointerWithChangePaths.getTargetPath();
                    // only of the rootChange defines the passed in "changeClass" directly as interface (not inherited)
                    if (Arrays.asList(rootChange.getClass().getInterfaces()).contains(changeClass) &&
                            JsonPointerSelector.doesTargetMatchTemplate(targetPointer, selector.getPointer())) {
                        // the change is directly accepted
                        handler.accept(changeClass.cast(rootChange));
                    }
                    // if already the targetPointer matches the selector
                    else if (JsonPointerSelector.doesTargetMatchTemplate(targetPointer, selector.getPointer())) {
                        // we can directly invoke the handler
                        resolveTemplateParametersAndPassChangeToHandler(targetPointer, targetPointer,
                                selectorTemplatePath, rootChange, changeBuilderFunction, handler, busDispatcher);
                    } else {
                        // otherwise, we iterate over all "changePaths", find the matching paths and invoke the handler
                        // for them:
                        jsonPointerWithChangePaths.getChangePaths().stream() // for each change path
                                .map(targetPointer::append) // append the changePath to the targetPointer
                                // filter only matching combined paths:
                                .filter(combinedPath -> JsonPointerSelector.doesTargetMatchTemplate(combinedPath,
                                        selector.getPointer()))
                                // for each matching combinedPath, resolve its template parameters and pass it to the handler:
                                .forEach(combinedPath -> resolveTemplateParametersAndPassChangeToHandler(targetPointer,
                                        combinedPath, selectorTemplatePath, rootChange, changeBuilderFunction, handler,
                                        busDispatcher));
                    }
                } else {
                    // only of the rootChange defines the passed in "changeClass" directly as interface (not inherited)
                    if (Arrays.asList(rootChange.getClass().getInterfaces()).contains(changeClass)) {
                        // the change is directly accepted
                        handler.accept(changeClass.cast(rootChange));
                    } else {
                        final JsonPointer startPointer = JsonPointer.of(event.getPointer().toString());
                        resolveTemplateParametersAndPassChangeToHandler(startPointer, startPointer,
                                selectorTemplatePath, rootChange, changeBuilderFunction, handler, busDispatcher);
                    }
                }
            }
        });
    }

    /**
     * Resolves template parameters in the passed {@code selectorTemplatePath} with the matching values in {@code
     * targetPath}, builds the specific change with the passed {@code changeBuilderFunction} and invokes the passed
     * {@code handler} with the resulting Change wrapping the execution with the passed {@code busDispatcher}.
     */
    private static <T extends Change> void resolveTemplateParametersAndPassChangeToHandler(final JsonPointer targetPath,
            final JsonPointer combinedPath,
            final JsonPointer selectorTemplatePath,
            final Change rootChange,
            final SpecificChangeBuilderFunction<T> changeBuilderFunction,
            final Consumer<T> handler,
            final Executor busDispatcher) {

        // only select everything after /things/{thingId}:
        final JsonPointer thingRelativePointer = combinedPath.getSubPointer(2).orElse(JsonPointer.empty());

        final Map<String, String> templateParams = new HashMap<>();
        JsonPointer pathRelativePointer = thingRelativePointer;
        JsonPointer tmpResolvedSelectorTemplatePath = JsonPointer.empty();
        for (int i = 0; i < selectorTemplatePath.getLevelCount(); i++) {
            final Optional<JsonKey> trKey = thingRelativePointer.get(i);
            final Optional<JsonKey> pKey = selectorTemplatePath.get(i);
            if (trKey.equals(pKey)) {
                pathRelativePointer = pathRelativePointer.nextLevel();
                if (trKey.isPresent()) {
                    tmpResolvedSelectorTemplatePath = tmpResolvedSelectorTemplatePath.addLeaf(trKey.get());
                }
            } else if (trKey.isPresent() && pKey.isPresent() && pKey.get().toString().matches("^\\{.*}$")) {
                // matches a template, e.g. {featureId}
                final String s = pKey.get().toString();
                templateParams.put(s, trKey.get().toString());
                pathRelativePointer = pathRelativePointer.nextLevel();
                tmpResolvedSelectorTemplatePath = tmpResolvedSelectorTemplatePath.addLeaf(trKey.get());
            }
        }

        final JsonPointer diff = JsonPointer.of(combinedPath.toString().replace(targetPath.toString(), ""));


        final JsonPointer parsedPath = pathRelativePointer;
        if (!diff.isEmpty() && !diff.toString().startsWith(parsedPath.toString())) {
            // this change is not propagated as it was not subscribed for
            return;
        }

        final JsonValue jsonValue = rootChange.getValue()
                .map(value -> {
                    if (parsedPath.isEmpty()) {
                        JsonValue valueToSet = value;
                        if (value.isObject()) {
                            valueToSet = value.asObject().getValue(diff).get();
                        }
                        return valueToSet;
                    } else {
                        JsonValue valueToSet = value;
                        if (value.isObject()) {
                            if (value.asObject().contains(diff)) {
                                valueToSet = value.asObject().getValue(diff).get();
                            }
                        }
                        return JsonObject.newBuilder().set(parsedPath, valueToSet).build();
                    }
                }).orElse(null);

        final T desiredChange =
                changeBuilderFunction.buildSpecificChange(rootChange, jsonValue, pathRelativePointer, templateParams);
        // use the configured Dispatcher of the Thing Client for responding to the handlers:
        busDispatcher.execute(() -> handler.accept(desiredChange));
    }

    /**
     * Returns a {@link JsonPointerSelector} implementation which returns a match if either of the provided Selectors
     * return a match.
     *
     * @param selector1 the first selector
     * @param selector2 the second selector
     * @return {@code true}, if either of the provided selectors return a match
     */
    public static JsonPointerSelector or(final JsonPointerSelector selector1, final JsonPointerSelector selector2) {
        final Predicate<JsonPointer> predicate = o -> selector1.matches(o) || selector2.matches(o);
        return JsonPointerSelectors.predicate(predicate);
    }

    /**
     * Quotes characters with special meaning for regular expressions with a "~0~" syntax. The index is the position in
     * of the character in the {@link #CHARS_TO_REPLACE} array.
     *
     * @param stringToQuote the String potentially containing regular expression characters which to quote.
     * @return the quoted String.
     */
    public static String quoteRegexCharacters(final String stringToQuote) {
        final StringBuilder sb = new StringBuilder(stringToQuote);
        for (int i = stringToQuote.length() - 1; i > 0; i--) {
            for (int j = 0; j < CHARS_TO_REPLACE.length; j++) {
                if (sb.charAt(i) == CHARS_TO_REPLACE[j]) {
                    sb.setCharAt(i, '~');
                    sb.insert(i + 1, j);
                    if (j < 10) {
                        sb.insert(i + 2, '~');
                    } else {
                        sb.insert(i + 3, '~');
                    }
                    break;
                }

            }
        }
        return sb.toString();
    }

    /**
     * Unquotes strings quoted with method {@link #quoteRegexCharacters(String)}.
     *
     * @param stringToUnquote the String which was quoted with {@link #quoteRegexCharacters(String)}.
     * @return the unquoted String.
     */
    public static String unquoteRegexCharacters(final String stringToUnquote) {
        final StringBuilder sb = new StringBuilder(stringToUnquote);
        for (int i = stringToUnquote.length() - 1; i > 0; i--) {
            if (sb.charAt(i) == '~' && sb.charAt(i - 2) == '~') {
                final char idx = sb.charAt(i - 1);
                if (Character.isDigit(idx)) {
                    final int numericValue = Character.getNumericValue(idx);
                    sb.setCharAt(i, CHARS_TO_REPLACE[numericValue]);
                    sb.deleteCharAt(i - 1);
                    sb.deleteCharAt(i - 2);
                }
            } else if (sb.charAt(i) == '~' && sb.charAt(i - 3) == '~') // index >= 10 in the CHARS_TO_REPLACE array
            {
                final char idx = sb.charAt(i - 1);
                final char idx2 = sb.charAt(i - 2);
                if (Character.isDigit(idx) && Character.isDigit(idx2)) {
                    final int numericValue = Integer.parseInt(new String(new char[]{idx2, idx}));
                    sb.setCharAt(i, CHARS_TO_REPLACE[numericValue]);
                    sb.deleteCharAt(i - 1);
                    sb.deleteCharAt(i - 2);
                    if (numericValue >= 10) {
                        sb.deleteCharAt(i - 3);
                    }
                }
            }
        }
        return sb.toString();
    }

}
