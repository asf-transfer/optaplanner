/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.constraint.streams.bavet.common.index;

final class SingleIndexProperty implements IndexProperties {

    private final Object property;

    SingleIndexProperty(Object property) {
        this.property = property;
    }

    @Override
    public <Type_> Type_ getProperty(int index) {
        if (index != 0) {
            throw new IllegalStateException("Impossible state: index (" + index + ") != 0");
        }
        return (Type_) property;
    }

    @Override
    public <Type_> Type_ getIndexerKey(int fromInclusive, int toExclusive) {
        if (toExclusive != 1) {
            throw new IllegalStateException("Impossible state: final index (" + toExclusive + ") != 1");
        }
        return getProperty(fromInclusive);
    }

    @Override
    public String toString() {
        return "[" + property + "]";
    }

}
