package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.QuadTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.QuadTupleImpl;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Quadruple;

final class Group4Mapping0CollectorTriNode<OldA, OldB, OldC, A, B, C, D>
        extends
        AbstractGroupTriNode<OldA, OldB, OldC, QuadTuple<A, B, C, D>, QuadTupleImpl<A, B, C, D>, Quadruple<A, B, C, D>, Void, Void> {

    private final int outputStoreSize;

    public Group4Mapping0CollectorTriNode(TriFunction<OldA, OldB, OldC, A> groupKeyMappingA,
            TriFunction<OldA, OldB, OldC, B> groupKeyMappingB, TriFunction<OldA, OldB, OldC, C> groupKeyMappingC,
            TriFunction<OldA, OldB, OldC, D> groupKeyMappingD, int groupStoreIndex,
            TupleLifecycle<QuadTuple<A, B, C, D>> nextNodesTupleLifecycle, int outputStoreSize,
            EnvironmentMode environmentMode) {
        super(groupStoreIndex,
                tuple -> createGroupKey(groupKeyMappingA, groupKeyMappingB, groupKeyMappingC, groupKeyMappingD, tuple),
                nextNodesTupleLifecycle, environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    private static <A, B, C, D, OldA, OldB, OldC> Quadruple<A, B, C, D> createGroupKey(
            TriFunction<OldA, OldB, OldC, A> groupKeyMappingA,
            TriFunction<OldA, OldB, OldC, B> groupKeyMappingB,
            TriFunction<OldA, OldB, OldC, C> groupKeyMappingC,
            TriFunction<OldA, OldB, OldC, D> groupKeyMappingD,
            TriTuple<OldA, OldB, OldC> tuple) {
        OldA oldA = tuple.getA();
        OldB oldB = tuple.getB();
        OldC oldC = tuple.getC();
        A a = groupKeyMappingA.apply(oldA, oldB, oldC);
        B b = groupKeyMappingB.apply(oldA, oldB, oldC);
        C c = groupKeyMappingC.apply(oldA, oldB, oldC);
        D d = groupKeyMappingD.apply(oldA, oldB, oldC);
        return Quadruple.of(a, b, c, d);
    }

    @Override
    protected QuadTupleImpl<A, B, C, D> createOutTuple(Quadruple<A, B, C, D> groupKey) {
        return QuadTuple.of(groupKey.getA(), groupKey.getB(), groupKey.getC(), groupKey.getD(), outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(QuadTupleImpl<A, B, C, D> outTuple, Void unused) {
        throw new IllegalStateException("Impossible state: collector is null.");
    }

}
