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

package org.optaplanner.constraint.streams.bavet.tri;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.bi.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.Group;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.impl.util.Pair;

public final class Group2Mapping0CollectorTriNode<OldA, OldB, OldC, A, B>
        extends AbstractGroupTriNode<OldA, OldB, OldC, BiTuple<A, B>, Pair<A, B>, Void, Void> {

    static final TriConstraintCollector NOOP_COLLECTOR = new TriConstraintCollector<>() {
        @Override
        public Supplier supplier() {
            return () -> null;
        }

        @Override
        public QuadFunction accumulator() {
            return (nothing, a, b, c) -> (Runnable) () -> {
                // DO NOTHING
            };
        }

        @Override
        public Function finisher() {
            return a -> a;
        }
    };

    private final TriFunction<OldA, OldB, OldC, A> groupKeyMappingA;
    private final TriFunction<OldA, OldB, OldC, B> groupKeyMappingB;
    private final int outputStoreSize;

    public Group2Mapping0CollectorTriNode(TriFunction<OldA, OldB, OldC, A> groupKeyMappingA,
            TriFunction<OldA, OldB, OldC, B> groupKeyMappingB, int groupStoreIndex,
            Consumer<BiTuple<A, B>> nextNodesInsert, Consumer<BiTuple<A, B>> nextNodesRetract,
            int outputStoreSize) {
        super(groupStoreIndex, NOOP_COLLECTOR, nextNodesInsert, nextNodesRetract);
        this.groupKeyMappingA = groupKeyMappingA;
        this.groupKeyMappingB = groupKeyMappingB;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected Pair<A, B> createGroupKey(TriTuple<OldA, OldB, OldC> tuple) {
        OldA oldA = tuple.factA;
        OldB oldB = tuple.factB;
        OldC oldC = tuple.factC;
        A a = groupKeyMappingA.apply(oldA, oldB, oldC);
        B b = groupKeyMappingB.apply(oldA, oldB, oldC);
        return Pair.of(a, b);
    }

    @Override
    protected BiTuple<A, B> createDownstreamTuple(Group<BiTuple<A, B>, Pair<A, B>, Void> group) {
        Pair<A, B> key = group.groupKey;
        return new BiTuple<>(key.getKey(), key.getValue(), outputStoreSize);
    }

    @Override
    public String toString() {
        return "GroupTriNode 2+0";
    }

}
