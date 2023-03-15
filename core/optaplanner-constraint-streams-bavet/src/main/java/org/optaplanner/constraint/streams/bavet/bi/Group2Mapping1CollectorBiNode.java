package org.optaplanner.constraint.streams.bavet.bi;

import static org.optaplanner.constraint.streams.bavet.bi.Group2Mapping0CollectorBiNode.createGroupKey;

import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTupleImpl;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Pair;

final class Group2Mapping1CollectorBiNode<OldA, OldB, A, B, C, ResultContainer_>
        extends AbstractGroupBiNode<OldA, OldB, TriTuple<A, B, C>, TriTupleImpl<A, B, C>, Pair<A, B>, ResultContainer_, C> {

    private final int outputStoreSize;

    public Group2Mapping1CollectorBiNode(BiFunction<OldA, OldB, A> groupKeyMappingA, BiFunction<OldA, OldB, B> groupKeyMappingB,
            int groupStoreIndex, int undoStoreIndex, BiConstraintCollector<OldA, OldB, ResultContainer_, C> collector,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, tuple), collector,
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(Pair<A, B> groupKey) {
        return new TriTupleImpl<>(groupKey.getKey(), groupKey.getValue(), null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(TriTupleImpl<A, B, C> outTuple, C c) {
        outTuple.setC(c);
    }

}
