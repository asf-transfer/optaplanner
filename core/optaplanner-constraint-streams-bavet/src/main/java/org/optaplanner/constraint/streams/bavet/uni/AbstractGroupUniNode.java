package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.bavet.common.AbstractGroupNode;
import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

abstract class AbstractGroupUniNode<OldA, OutTuple_ extends Tuple, GroupKey_, ResultContainer_, Result_>
        extends AbstractGroupNode<UniTuple<OldA>, OutTuple_, GroupKey_, ResultContainer_, Result_> {

    private final BiFunction<ResultContainer_, OldA, Runnable> accumulator;

    protected AbstractGroupUniNode(int groupStoreIndex,
            UniConstraintCollector<OldA, ResultContainer_, Result_> collector,
            TupleLifecycle<OutTuple_> nextNodesTupleLifecycle) {
        super(groupStoreIndex,
                collector == null ? null : collector.supplier(),
                collector == null ? null : collector.finisher(),
                nextNodesTupleLifecycle);
        accumulator = collector == null ? null : collector.accumulator();
    }

    @Override
    protected final Runnable accumulate(ResultContainer_ resultContainer, UniTuple<OldA> tuple) {
        return accumulator.apply(resultContainer, tuple.factA);
    }
}
