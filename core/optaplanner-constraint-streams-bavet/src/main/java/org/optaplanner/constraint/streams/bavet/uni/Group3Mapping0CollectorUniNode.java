package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTupleImpl;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Triple;

final class Group3Mapping0CollectorUniNode<OldA, A, B, C>
        extends AbstractGroupUniNode<OldA, TriTuple<A, B, C>, TriTupleImpl<A, B, C>, Triple<A, B, C>, Void, Void> {

    private final int outputStoreSize;

    public Group3Mapping0CollectorUniNode(Function<OldA, A> groupKeyMappingA, Function<OldA, B> groupKeyMappingB,
            Function<OldA, C> groupKeyMappingC, int groupStoreIndex, TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle,
            int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, groupKeyMappingC, tuple),
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    static <A, B, C, OldA> Triple<A, B, C> createGroupKey(Function<OldA, A> groupKeyMappingA,
            Function<OldA, B> groupKeyMappingB, Function<OldA, C> groupKeyMappingC, UniTuple<OldA> tuple) {
        OldA oldA = tuple.getA();
        A a = groupKeyMappingA.apply(oldA);
        B b = groupKeyMappingB.apply(oldA);
        C c = groupKeyMappingC.apply(oldA);
        return Triple.of(a, b, c);
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(Triple<A, B, C> groupKey) {
        return new TriTupleImpl<>(groupKey.getA(), groupKey.getB(), groupKey.getC(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(TriTupleImpl<A, B, C> outTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
