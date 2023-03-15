package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractFlattenLastNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.BiTuple;

final class FlattenLastBiNode<A, B, NewB> extends AbstractFlattenLastNode<BiTuple<A, B>, BiTuple<A, NewB>, B, NewB> {

    private final int outputStoreSize;

    FlattenLastBiNode(int flattenLastStoreIndex, Function<B, Iterable<NewB>> mappingFunction,
            TupleLifecycle<BiTuple<A, NewB>> nextNodesTupleLifecycle, int outputStoreSize) {
        super(flattenLastStoreIndex, mappingFunction, nextNodesTupleLifecycle);
        this.outputStoreSize = outputStoreSize;
    }

    @Override
    protected BiTuple<A, NewB> createTuple(BiTuple<A, B> originalTuple, NewB newB) {
        return BiTuple.of(originalTuple.getA(), newB, outputStoreSize);
    }

    @Override
    protected B getEffectiveFactIn(BiTuple<A, B> tuple) {
        return tuple.getB();
    }

    @Override
    protected NewB getEffectiveFactOut(BiTuple<A, NewB> outTuple) {
        return outTuple.getB();
    }
}
