
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

package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractGroupNode;
import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

abstract class AbstractGroupUniNode<OldA, OutTuple_ extends Tuple, GroupKey_, ResultContainer_, Result_>
        extends AbstractGroupNode<UniTuple<OldA>, OutTuple_, GroupKey_, ResultContainer_> {

    private final BiFunction<ResultContainer_, OldA, Runnable> accumulator;
    protected final Function<ResultContainer_, Result_> finisher;

    protected AbstractGroupUniNode(int groupStoreIndex,
            UniConstraintCollector<OldA, ResultContainer_, Result_> collector,
            Consumer<OutTuple_> nextNodesInsert, Consumer<OutTuple_> nextNodesRetract) {
        super(groupStoreIndex,
                collector == null ? null : collector.supplier(),
                nextNodesInsert, nextNodesRetract);
        accumulator = collector == null ? null : collector.accumulator();
        finisher = collector == null ? null : collector.finisher();
    }

    @Override
    protected final Runnable accumulate(ResultContainer_ resultContainer, UniTuple<OldA> tuple) {
        return accumulator.apply(resultContainer, tuple.factA);
    }
}
