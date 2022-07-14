package org.optaplanner.constraint.streams.bavet.uni;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractIfExistsNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;

final class IfExistsUniWithUniNode<A, B> extends AbstractIfExistsNode<UniTupleImpl<A>, B> {

    private final Function<A, IndexProperties> mappingA;
    private final BiPredicate<A, B> filtering;

    public IfExistsUniWithUniNode(boolean shouldExist,
            Function<A, IndexProperties> mappingA, Function<B, IndexProperties> mappingB,
            int inputStoreIndexA, int inputStoreIndexB,
            TupleLifecycle<UniTupleImpl<A>> nextNodesTupleLifecycle,
            Indexer<UniTupleImpl<A>, Counter<UniTupleImpl<A>>> indexerA,
            Indexer<UniTupleImpl<B>, Set<Counter<UniTupleImpl<A>>>> indexerB,
            BiPredicate<A, B> filtering) {
        super(shouldExist, mappingB, inputStoreIndexA, inputStoreIndexB, nextNodesTupleLifecycle, indexerA, indexerB);
        this.mappingA = mappingA;
        this.filtering = filtering;
    }

    @Override
    protected IndexProperties createIndexProperties(UniTupleImpl<A> aUniTuple) {
        return mappingA.apply(aUniTuple.factA);
    }

    @Override
    protected boolean isFiltering() {
        return filtering != null;
    }

    @Override
    protected boolean isFiltered(UniTupleImpl<A> aUniTuple, UniTupleImpl<B> rightTuple) {
        return filtering.test(aUniTuple.factA, rightTuple.factA);
    }

    @Override
    public String toString() {
        return "IfExistsUniWithUniNode";
    }

}
