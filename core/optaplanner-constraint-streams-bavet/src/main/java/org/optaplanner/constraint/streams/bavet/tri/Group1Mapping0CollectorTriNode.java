package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.function.TriFunction;

final class Group1Mapping0CollectorTriNode<OldA, OldB, OldC, A>
        extends AbstractGroupTriNode<OldA, OldB, OldC, UniTuple<A>, A, Void, Void> {

    private final int outputStoreSize;

    public Group1Mapping0CollectorTriNode(TriFunction<OldA, OldB, OldC, A> groupKeyMapping, int groupStoreIndex,
            TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple), null, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, OldA, OldB, OldC> A createGroupKey(TriFunction<OldA, OldB, OldC, A> groupKeyMapping,
            TriTuple<OldA, OldB, OldC> tuple) {
        return groupKeyMapping.apply(tuple.factA, tuple.factB, tuple.factC);
    }

    @Override
    protected UniTuple<A> createOutTuple(A a) {
        return new UniTuple<>(a, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(UniTuple<A> aUniTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

    @Override
    public String toString() {
        return "GroupTriNode 1+0";
    }

}
