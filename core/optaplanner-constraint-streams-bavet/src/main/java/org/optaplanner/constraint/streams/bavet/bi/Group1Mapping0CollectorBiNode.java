package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTupleImpl;
import org.optaplanner.core.config.solver.EnvironmentMode;

final class Group1Mapping0CollectorBiNode<OldA, OldB, A>
        extends AbstractGroupBiNode<OldA, OldB, UniTuple<A>, UniTupleImpl<A>, A, Void, Void> {

    private final int outputStoreSize;

    public Group1Mapping0CollectorBiNode(BiFunction<OldA, OldB, A> groupKeyMapping, int groupStoreIndex,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple), nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, OldA, OldB> A createGroupKey(BiFunction<OldA, OldB, A> groupKeyMapping, BiTuple<OldA, OldB> tuple) {
        return groupKeyMapping.apply(tuple.getA(), tuple.getB());
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
