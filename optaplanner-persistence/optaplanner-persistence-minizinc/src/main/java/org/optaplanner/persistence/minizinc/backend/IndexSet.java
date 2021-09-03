/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.persistence.minizinc.backend;

import java.util.BitSet;
import java.util.List;

public class IndexSet {
    public static final IndexSet EMPTY = new IndexSet(List.of());
    final BitSet indexBitSet;

    public IndexSet(int index) {
        this.indexBitSet = new BitSet();
        indexBitSet.set(index);
    }

    public IndexSet(List<Integer> indexList) {
        this.indexBitSet = new BitSet();
        for (Integer index : indexList) {
            indexBitSet.set(index);
        }
    }

    public boolean hasIndex(int index) {
        return indexBitSet.get(index);
    }
}
