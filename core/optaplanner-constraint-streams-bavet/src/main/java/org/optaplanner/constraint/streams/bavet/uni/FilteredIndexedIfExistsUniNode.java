package org.optaplanner.constraint.streams.bavet.uni;

import java.util.function.BiPredicate;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractFilteredIndexedIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.ExistsCounter;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;

final class FilteredIndexedIfExistsUniNode<A, B> extends AbstractFilteredIndexedIfExistsNode<UniTuple<A>, B> {

    private final Function<A, IndexProperties> mappingA;
    private final BiPredicate<A, B> filtering;

    public FilteredIndexedIfExistsUniNode(boolean shouldExist, Function<A, IndexProperties> mappingA,
            Function<B, IndexProperties> mappingB, int inputStoreIndexLeftProperties, int inputStoreIndexLeftCounterEntry,
            int inputStoreIndexLeftTrackerList, int inputStoreIndexRightProperties, int inputStoreIndexRightEntry,
            int inputStoreIndexRightTrackerList, TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle,
            Indexer<ExistsCounter<UniTuple<A>>> indexerA, Indexer<UniTuple<B>> indexerB, BiPredicate<A, B> filtering) {
        super(shouldExist, mappingB, inputStoreIndexLeftProperties, inputStoreIndexLeftCounterEntry,
                inputStoreIndexLeftTrackerList, inputStoreIndexRightProperties, inputStoreIndexRightEntry,
                inputStoreIndexRightTrackerList, nextNodesTupleLifecycle, indexerA, indexerB);
        this.mappingA = mappingA;
        this.filtering = filtering;
    }

    @Override
    protected IndexProperties createIndexProperties(UniTuple<A> leftTuple) {
        return mappingA.apply(leftTuple.getFactA());
    }

    @Override
    protected boolean testFiltering(UniTuple<A> leftTuple, UniTuple<B> rightTuple) {
        return filtering.test(leftTuple.getFactA(), rightTuple.getFactA());
    }

}
