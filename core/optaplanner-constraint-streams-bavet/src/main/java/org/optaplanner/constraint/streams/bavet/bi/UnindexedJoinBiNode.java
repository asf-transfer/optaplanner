package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.BiPredicate;

import org.optaplanner.constraint.streams.bavet.common.AbstractUnindexedJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;

final class UnindexedJoinBiNode<A, B>
        extends AbstractUnindexedJoinNode<UniTuple<A>, B, BiTuple<A, B>> {

    private final BiPredicate<A, B> filtering;
    private final int outputStoreSize;

    public UnindexedJoinBiNode(
            int inputStoreIndexLeftEntry, int inputStoreIndexLeftOutTupleList,
            int inputStoreIndexRightEntry, int inputStoreIndexRightOutTupleList,
            TupleLifecycle<BiTuple<A, B>> nextNodesTupleLifecycle, BiPredicate<A, B> filtering,
            int outputStoreSize,
            int outputStoreIndexLeftOutEntry, int outputStoreIndexRightOutEntry) {
        super(inputStoreIndexLeftEntry, inputStoreIndexLeftOutTupleList,
                inputStoreIndexRightEntry, inputStoreIndexRightOutTupleList,
                nextNodesTupleLifecycle, filtering != null,
                outputStoreIndexLeftOutEntry, outputStoreIndexRightOutEntry);
        this.filtering = filtering;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected BiTuple<A, B> createOutTuple(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return BiTuple.of(leftTuple.getA(), rightTuple.getA(), outputStoreSize);
    }

    @Override
    protected void setOutTupleLeftFacts(BiTuple<A, B> outTuple, UniTuple<A> leftTuple) {
        outTuple.setA(leftTuple.getA());
    }

    @Override
    protected void setOutTupleRightFact(BiTuple<A, B> outTuple, UniTuple<B> rightTuple) {
        outTuple.setB(rightTuple.getA());
    }

    @Override
    protected boolean testFiltering(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return filtering.test(leftTuple.getA(), rightTuple.getA());
    }

}
