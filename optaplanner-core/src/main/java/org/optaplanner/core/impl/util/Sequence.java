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

package org.optaplanner.core.impl.util;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Sequence<T> {
    private final TreeSet<T> consecutiveItemsSet;
    private final Map<T, Integer> count;
    private final ConsecutiveSetTree<T, ?, ?> sourceTree;

    protected Sequence(ConsecutiveSetTree<T, ?, ?> sourceTree) {
        this(sourceTree, new TreeSet<>(sourceTree.comparator), new IdentityHashMap<>());
    }

    protected Sequence(ConsecutiveSetTree<T, ?, ?> sourceTree, TreeSet<T> consecutiveItemsSet, Map<T, Integer> count) {
        this.sourceTree = sourceTree;
        this.consecutiveItemsSet = consecutiveItemsSet;
        this.count = count;
    }

    public TreeSet<T> getItems() {
        return consecutiveItemsSet;
    }

    public int getLength() {
        return consecutiveItemsSet.size();
    }

    protected boolean isEmpty() {
        return consecutiveItemsSet.isEmpty();
    }

    protected int getCountIncludingDuplicates() {
        return count.values().stream().reduce(Integer::sum).orElse(0);
    }

    protected Stream<T> getDuplicatedStream() {
        return consecutiveItemsSet.stream()
                .flatMap(item -> IntStream.range(0, count.get(item)).mapToObj(index -> item));
    }

    protected void add(T item) {
        if (!count.containsKey(item)) {
            consecutiveItemsSet.add(item);
        }
        count.merge(item, 1, Integer::sum);
    }

    protected Sequence<T> split(T fromElement) {
        TreeSet<T> splitConsecutiveItemsSet = new TreeSet<>(consecutiveItemsSet.tailSet(fromElement));
        Map<T, Integer> newCountMap = new IdentityHashMap<>();
        splitConsecutiveItemsSet.forEach(item -> {
            newCountMap.put(item, count.remove(item));
            consecutiveItemsSet.remove(item);
        });
        return new Sequence<>(sourceTree, splitConsecutiveItemsSet, newCountMap);
    }

    protected boolean remove(T item) {
        if (!count.containsKey(item)) {
            return true;
        }
        Integer newCount = count.merge(item, -1, (a, b) -> {
            int out = a + b;
            if (out == 0) {
                return null;
            }
            return out;
        });
        if (newCount == null) {
            consecutiveItemsSet.remove(item);
            return true;
        }
        return false;
    }

    protected void putAll(Sequence<T> other) {
        other.getItems().forEach(item -> {
            consecutiveItemsSet.add(item);
            count.merge(item, other.count.get(item), Integer::sum);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Sequence<?> sequence = (Sequence<?>) o;
        return consecutiveItemsSet.equals(sequence.consecutiveItemsSet) && count.equals(sequence.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consecutiveItemsSet, count);
    }

    @Override
    public String toString() {
        return consecutiveItemsSet.stream().map(Objects::toString).collect(Collectors.joining(", ", "Sequence [", "]"));
    }
}
