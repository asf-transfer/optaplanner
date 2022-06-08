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

package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.core.impl.util.Pair;

final class Group2Mapping0CollectorBiNode<OldA, OldB, A, B>
        extends AbstractGroupBiNode<OldA, OldB, BiTuple<A, B>, Pair<A, B>, Void, Void> {

    private final BiFunction<OldA, OldB, A> groupKeyMappingA;
    private final BiFunction<OldA, OldB, B> groupKeyMappingB;
    private final int outputStoreSize;

    public Group2Mapping0CollectorBiNode(BiFunction<OldA, OldB, A> groupKeyMappingA,
            BiFunction<OldA, OldB, B> groupKeyMappingB, int groupStoreIndex,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(groupStoreIndex, null, nextNodesTupleLifecycle);
        this.groupKeyMappingA = groupKeyMappingA;
        this.groupKeyMappingB = groupKeyMappingB;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected Pair<A, B> createGroupKey(BiTuple<OldA, OldB> tuple) {
        OldA oldA = tuple.factA;
        OldB oldB = tuple.factB;
        A a = groupKeyMappingA.apply(oldA, oldB);
        B b = groupKeyMappingB.apply(oldA, oldB);
        return Pair.of(a, b);
    }

    @Override
    protected BiTuple<A, B> createOutTuple(Pair<A, B> groupKey) {
        return new BiTuple<>(groupKey.getKey(), groupKey.getValue(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTuple<A, B> outTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

    @Override
    public String toString() {
        return "GroupBiNode 2+0";
    }

}
