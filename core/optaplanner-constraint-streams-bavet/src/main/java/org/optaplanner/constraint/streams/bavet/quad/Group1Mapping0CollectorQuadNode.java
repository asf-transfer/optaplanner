package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.QuadTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTupleImpl;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.config.solver.EnvironmentMode;

final class Group1Mapping0CollectorQuadNode<OldA, OldB, OldC, OldD, A>
        extends AbstractGroupQuadNode<OldA, OldB, OldC, OldD, UniTuple<A>, UniTupleImpl<A>, A, Void, Void> {

    private final int outputStoreSize;

    public Group1Mapping0CollectorQuadNode(QuadFunction<OldA, OldB, OldC, OldD, A> groupKeyMapping, int groupStoreIndex,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple), nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, OldA, OldB, OldC, OldD> A createGroupKey(QuadFunction<OldA, OldB, OldC, OldD, A> groupKeyMapping,
            QuadTuple<OldA, OldB, OldC, OldD> tuple) {
        return groupKeyMapping.apply(tuple.getA(), tuple.getB(), tuple.getC(), tuple.getD());
    }

    @Override
    protected UniTupleImpl<A> createOutTuple(A a) {
        return UniTuple.of(a, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(UniTupleImpl<A> aUniTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
