package org.optaplanner.constraint.streams.bavet.bi;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;

final class Group0Mapping1CollectorBiNode<OldA, OldB, A, ResultContainer_>
        extends AbstractGroupBiNode<OldA, OldB, UniTuple<A>, Void, ResultContainer_, A> {

    private final int outputStoreSize;

    public Group0Mapping1CollectorBiNode(int groupStoreIndex,
            BiConstraintCollector<OldA, OldB, ResultContainer_, A> collector,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(groupStoreIndex, null, collector, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected UniTuple<A> createOutTuple(Void groupKey) {
        return new UniTuple<>(null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(UniTuple<A> outTuple, A a) {
        outTuple.factA = a;
    }

    @Override
    public String toString() {
        return "GroupBiNode 0+1";
    }
}
