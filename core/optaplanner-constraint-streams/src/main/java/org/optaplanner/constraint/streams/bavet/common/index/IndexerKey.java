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

import java.util.Objects;

final class IndexerKey {

    private final IndexProperties indexProperties;
    private final int fromInclusive;
    private final int toExclusive;

    public IndexerKey(IndexProperties indexProperties, int fromInclusive, int toExclusive) {
        this.indexProperties = indexProperties;
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
    }

    @Override
    public int hashCode() {
        if (indexProperties == null) {
            return 0;
        }
        int result = 1;
        for (int i = fromInclusive; i < toExclusive; i++) {
            Object element = indexProperties.getProperty(i);
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof IndexerKey)) {
            return false;
        }
        IndexerKey other = (IndexerKey) o;
        for (int i = fromInclusive; i < toExclusive; i++) {
            Object a = indexProperties.getProperty(i);
            Object b = other.indexProperties.getProperty(i);
            if (!Objects.equals(a, b)) {
                return false;
            }
        }
        return true;
    }

}
