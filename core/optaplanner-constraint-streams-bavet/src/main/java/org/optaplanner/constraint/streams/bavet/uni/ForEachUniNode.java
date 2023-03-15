package org.optaplanner.constraint.streams.bavet.uni;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Queue;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.common.tuple.TupleState;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTuple;
import org.optaplanner.constraint.streams.bavet.common.tuple.UniTupleImpl;

public final class ForEachUniNode<A> extends AbstractNode {

    private final Class<A> forEachClass;
    /**
     * Calls for example {@link UniScorer#insert(UniTuple)}, and/or ...
     */
    private final TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle;
    private final int outputStoreSize;

    private final Map<A, UniTupleImpl<A>> tupleMap = new IdentityHashMap<>(1000);
    private final Queue<UniTupleImpl<A>> dirtyTupleQueue;

    public ForEachUniNode(Class<A> forEachClass, TupleLifecycle<UniTuple<A>> nextNodesTupleLifecycle, int outputStoreSize) {
        this.forEachClass = forEachClass;
        this.nextNodesTupleLifecycle = nextNodesTupleLifecycle;
        this.outputStoreSize = outputStoreSize;
        dirtyTupleQueue = new ArrayDeque<>(1000);
    }

    public void insert(A a) {
        UniTupleImpl<A> tuple = new UniTupleImpl<>(a, outputStoreSize);
        UniTupleImpl<A> old = tupleMap.put(a, tuple);
        if (old != null) {
            throw new IllegalStateException("The fact (" + a + ") was already inserted, so it cannot insert again.");
        }
        dirtyTupleQueue.add(tuple);
    }

    public void update(A a) {
        UniTupleImpl<A> tuple = tupleMap.get(a);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + a + ") was never inserted, so it cannot update.");
        }
        if (tuple.state.isDirty()) {
            if (tuple.state == TupleState.DYING || tuple.state == TupleState.ABORTING) {
                throw new IllegalStateException("The fact (" + a + ") was retracted, so it cannot update.");
            }
        } else {
            tuple.state = TupleState.UPDATING;
            dirtyTupleQueue.add(tuple);
        }
    }

    public void retract(A a) {
        UniTupleImpl<A> tuple = tupleMap.remove(a);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + a + ") was never inserted, so it cannot retract.");
        }
        if (tuple.state.isDirty()) {
            if (tuple.state == TupleState.DYING || tuple.state == TupleState.ABORTING) {
                throw new IllegalStateException("The fact (" + a + ") was already retracted, so it cannot retract.");
            }
            tuple.state = TupleState.ABORTING;
        } else {
            tuple.state = TupleState.DYING;
            dirtyTupleQueue.add(tuple);
        }
    }

    @Override
    public void calculateScore() {
        for (UniTupleImpl<A> tuple : dirtyTupleQueue) {
            switch (tuple.state) {
                case CREATING:
                    nextNodesTupleLifecycle.insert(tuple);
                    tuple.state = TupleState.OK;
                    break;
                case UPDATING:
                    nextNodesTupleLifecycle.update(tuple);
                    tuple.state = TupleState.OK;
                    break;
                case DYING:
                    nextNodesTupleLifecycle.retract(tuple);
                    tuple.state = TupleState.DEAD;
                    break;
                case ABORTING:
                    tuple.state = TupleState.DEAD;
                    break;
                case OK:
                case DEAD:
                default:
                    throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
                            this + ") is in an unexpected state (" + tuple.state + ").");
            }
        }
        dirtyTupleQueue.clear();
    }

    @Override
    public String toString() {
        return super.toString() + "(" + forEachClass.getSimpleName() + ")";
    }

    public Class<A> getForEachClass() {
        return forEachClass;
    }

}
