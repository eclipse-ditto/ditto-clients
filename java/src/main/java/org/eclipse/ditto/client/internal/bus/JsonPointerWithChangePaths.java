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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonKey;
import org.eclipse.ditto.json.JsonPointer;

/**
 * Contains a {@code targetPath} where a Change was initially created and a list of changed paths relative to this base
 * {@code targetPath} containing information about which elements got changes together with the "root".
 *
 * @since 1.0.0
 */
final class JsonPointerWithChangePaths implements JsonPointer {

    private final JsonPointer targetPath;
    private final List<JsonPointer> changePaths;

    /**
     * Constructs a new {@code JsonPointerWithChangePaths}.
     *
     * @param targetPath the JsonPointer where a Change was initially created
     * @param changePaths a list of changed paths relative to the {@code targetPath} which were also involved in a
     * change
     */
    JsonPointerWithChangePaths(final JsonPointer targetPath, final List<JsonPointer> changePaths) {
        this.targetPath = targetPath;
        this.changePaths = Collections.unmodifiableList(new ArrayList<>(changePaths));
    }

    /**
     * Returns the JsonPointer where a Change was initially created.
     *
     * @return the JsonPointer where a Change was initially created
     */
    JsonPointer getTargetPath() {
        return targetPath;
    }

    /**
     * Returns a list of changed paths relative to the {@code targetPath} which were also involved in a change.
     *
     * @return a list of changed paths relative to the {@code targetPath} which were also involved in a change.
     */
    List<JsonPointer> getChangePaths() {
        return changePaths;
    }

    public static JsonPointer empty() {return JsonPointer.empty();}

    public static JsonPointer of(final CharSequence slashDelimitedCharSequence) {
        return JsonPointer.of(slashDelimitedCharSequence);
    }

    @Override
    public JsonPointer addLeaf(final JsonKey jsonKey) {return targetPath.addLeaf(jsonKey);}

    @Override
    public JsonPointer append(final JsonPointer jsonPointer) {return targetPath.append(jsonPointer);}

    @Override
    public int getLevelCount() {return targetPath.getLevelCount();}

    @Override
    public boolean isEmpty() {return targetPath.isEmpty();}

    @Override
    public Optional<JsonKey> get(final int i) {return targetPath.get(i);}

    @Override
    public Optional<JsonKey> getRoot() {return targetPath.getRoot();}

    @Override
    public Optional<JsonKey> getLeaf() {return targetPath.getLeaf();}

    @Override
    public JsonPointer cutLeaf() {return targetPath.cutLeaf();}

    @Override
    public JsonPointer nextLevel() {return targetPath.nextLevel();}

    @Override
    public Optional<JsonPointer> getSubPointer(final int i) {return targetPath.getSubPointer(i);}

    @Override
    public Optional<JsonPointer> getPrefixPointer(final int i) {return targetPath.getPrefixPointer(i);}

    @Override
    public JsonFieldSelector toFieldSelector() {return targetPath.toFieldSelector();}

    @Override
    public int length() {return targetPath.length();}

    @Override
    public char charAt(final int index) {return targetPath.charAt(index);}

    @Override
    public CharSequence subSequence(final int start, final int end) {return targetPath.subSequence(start, end);}

    @Override
    public Iterator<JsonKey> iterator() {return targetPath.iterator();}

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final JsonPointerWithChangePaths that = (JsonPointerWithChangePaths) o;
        return Objects.equals(targetPath, that.targetPath) && Objects.equals(changePaths, that.changePaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetPath, changePaths);
    }

    @Override
    public String toString() {
        return targetPath + " + " + changePaths;
    }
}
