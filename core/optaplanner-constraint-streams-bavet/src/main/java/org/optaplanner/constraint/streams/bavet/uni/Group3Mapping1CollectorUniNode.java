package org.optaplanner.constraint.streams.bavet.uni;

import static org.optaplanner.constraint.streams.bavet.uni.Group3Mapping0CollectorUniNode.createGroupKey;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.quad.QuadTupleImpl;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.util.Triple;

final class Group3Mapping1CollectorUniNode<OldA, A, B, C, D, ResultContainer_>
        extends AbstractGroupUniNode<OldA, QuadTupleImpl<A, B, C, D>, Triple<A, B, C>, ResultContainer_, D> {

    private final int outputStoreSize;

    public Group3Mapping1CollectorUniNode(Function<OldA, A> groupKeyMappingA, Function<OldA, B> groupKeyMappingB,
            Function<OldA, C> groupKeyMappingC,
            int groupStoreIndex, UniConstraintCollector<OldA, ResultContainer_, D> collector,
            TupleLifecycle<QuadTupleImpl<A, B, C, D>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, groupKeyMappingC, tuple),
                collector, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(Triple<A, B, C> groupKey) {
        return new QuadTupleImpl<>(groupKey.getA(), groupKey.getB(), groupKey.getC(), null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(QuadTupleImpl<A, B, C, D> outTuple, D d) {
        outTuple.factD = d;
    }

    @Override
    public String toString() {
        return "GroupUniNode 3+1";
    }

}
