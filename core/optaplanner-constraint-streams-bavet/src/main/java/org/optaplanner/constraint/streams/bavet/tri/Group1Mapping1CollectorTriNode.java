package org.optaplanner.constraint.streams.bavet.tri;

import static org.optaplanner.constraint.streams.bavet.tri.Group1Mapping0CollectorTriNode.createGroupKey;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;

final class Group1Mapping1CollectorTriNode<OldA, OldB, OldC, A, B, ResultContainer_>
        extends AbstractGroupTriNode<OldA, OldB, OldC, BiTuple<A, B>, A, ResultContainer_, B> {

    private final int outputStoreSize;

    public Group1Mapping1CollectorTriNode(TriFunction<OldA, OldB, OldC, A> groupKeyMapping, int groupStoreIndex,
            int undoStoreIndex, TriConstraintCollector<OldA, OldB, OldC, ResultContainer_, B> collector,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple), collector,
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected BiTuple<A, B> createOutTuple(A a) {
        return BiTuple.of(a, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(BiTuple<A, B> outTuple, B b) {
        outTuple.setB(b);
    }

}
