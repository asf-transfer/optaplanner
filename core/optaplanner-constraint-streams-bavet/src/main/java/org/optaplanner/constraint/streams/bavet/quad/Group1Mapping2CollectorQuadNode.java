package org.optaplanner.constraint.streams.bavet.quad;

import static org.optaplanner.constraint.streams.bavet.quad.Group1Mapping0CollectorQuadNode.createGroupKey;

import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTupleImpl;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.util.Pair;

final class Group1Mapping2CollectorQuadNode<OldA, OldB, OldC, OldD, A, B, C, ResultContainerB_, ResultContainerC_>
        extends AbstractGroupQuadNode<OldA, OldB, OldC, OldD, TriTuple<A, B, C>, TriTupleImpl<A, B, C>, A, Object, Pair<B, C>> {

    private final int outputStoreSize;

    public Group1Mapping2CollectorQuadNode(QuadFunction<OldA, OldB, OldC, OldD, A> groupKeyMapping, int groupStoreIndex,
            int undoStoreIndex, QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerB_, B> collectorB,
            QuadConstraintCollector<OldA, OldB, OldC, OldD, ResultContainerC_, C> collectorC,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, int outputStoreSize, EnvironmentMode environmentMode) {
        super(groupStoreIndex, undoStoreIndex, tuple -> createGroupKey(groupKeyMapping, tuple),
                Group0Mapping2CollectorQuadNode.mergeCollectors(collectorB, collectorC), nextNodesTupleLifecycle,
                environmentMode);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(A a) {
        return new TriTupleImpl<>(a, null, null, outputStoreSize);
    }

    @Override
    protected void updateOutTupleToResult(TriTupleImpl<A, B, C> outTuple, Pair<B, C> result) {
        outTuple.setB(result.getKey());
        outTuple.setC(result.getValue());
    }

}
