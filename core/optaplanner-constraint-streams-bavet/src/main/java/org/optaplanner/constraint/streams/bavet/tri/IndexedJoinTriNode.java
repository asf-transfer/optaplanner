package org.optaplanner.constraint.streams.bavet.tri;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractIndexedJoinNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.TriTupleImpl;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.core.api.function.TriPredicate;

final class IndexedJoinTriNode<A, B, C>
        extends AbstractIndexedJoinNode<BiTuple<A, B>, C, TriTuple<A, B, C>, TriTupleImpl<A, B, C>> {

    private final BiFunction<A, B, IndexProperties> mappingAB;
    private final TriPredicate<A, B, C> filtering;
    private final int outputStoreSize;

    public IndexedJoinTriNode(BiFunction<A, B, IndexProperties> mappingAB, Function<C, IndexProperties> mappingC,
            int inputStoreIndexAB, int inputStoreIndexEntryAB, int inputStoreIndexOutTupleListAB,
            int inputStoreIndexC, int inputStoreIndexEntryC, int inputStoreIndexOutTupleListC,
            TupleLifecycle<TriTuple<A, B, C>> nextNodesTupleLifecycle, TriPredicate<A, B, C> filtering,
            int outputStoreSize,
            int outputStoreIndexOutEntryAB, int outputStoreIndexOutEntryC,
            Indexer<BiTuple<A, B>> indexerAB,
            Indexer<UniTuple<C>> indexerC) {
        super(mappingC,
                inputStoreIndexAB, inputStoreIndexEntryAB, inputStoreIndexOutTupleListAB,
                inputStoreIndexC, inputStoreIndexEntryC, inputStoreIndexOutTupleListC,
                nextNodesTupleLifecycle, filtering != null,
                outputStoreIndexOutEntryAB, outputStoreIndexOutEntryC,
                indexerAB, indexerC);
        this.mappingAB = mappingAB;
        this.filtering = filtering;
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected IndexProperties createIndexPropertiesLeft(BiTuple<A, B> leftTuple) {
        return mappingAB.apply(leftTuple.getA(), leftTuple.getB());
    }

    @Override
    protected TriTupleImpl<A, B, C> createOutTuple(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return new TriTupleImpl<>(leftTuple.getA(), leftTuple.getB(), rightTuple.getA(), outputStoreSize);
    }

    @Override
    protected void setOutTupleLeftFacts(TriTupleImpl<A, B, C> outTuple, BiTuple<A, B> leftTuple) {
        outTuple.setA(leftTuple.getA());
        outTuple.setB(leftTuple.getB());
    }

    @Override
    protected void setOutTupleRightFact(TriTupleImpl<A, B, C> outTuple, UniTuple<C> rightTuple) {
        outTuple.setC(rightTuple.getA());
    }

    @Override
    protected boolean testFiltering(BiTuple<A, B> leftTuple, UniTuple<C> rightTuple) {
        return filtering.test(leftTuple.getA(), leftTuple.getB(), rightTuple.getA());
    }

}
