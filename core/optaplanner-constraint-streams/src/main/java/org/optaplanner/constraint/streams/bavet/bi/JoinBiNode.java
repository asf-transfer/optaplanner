/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.constraint.streams.bavet.bi;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

public final class JoinBiNode<A, B> extends AbstractNode {

    private final Function<A, Object[]> mappingA;
    private final Function<B, Object[]> mappingB;
    private final BiOut<A, B>[] outs;

    private final Indexer<UniTuple<A>, Set<BiTuple<A, B>>> indexerA;
    private final Indexer<UniTuple<B>, Set<BiTuple<A, B>>> indexerB;
    private final Queue<BiTuple<A, B>> dirtyTupleQueue;

    public JoinBiNode(int nodeIndex,
            Function<A, Object[]> mappingA, Function<B, Object[]> mappingB,
            Indexer<UniTuple<A>, Set<BiTuple<A, B>>> indexerA, Indexer<UniTuple<B>, Set<BiTuple<A, B>>> indexerB) {
        super(nodeIndex);
        this.mappingA = mappingA;
        this.mappingB = mappingB;
        outs = (BiOut<A, B>[]) Array.newInstance(BiOut.class, 5);
        this.indexerA = indexerA;
        this.indexerB = indexerB;
        dirtyTupleQueue = new ArrayDeque<>(1000);

        // TODO fill in outs
    }

    public void insertA(UniTuple<A> tupleA) {
        Object[] indexProperties = mappingA.apply(tupleA.factA);

        Map<UniTuple<B>, Set<BiTuple<A, B>>> tupleABSetMapB = indexerB.get(indexProperties);
        // Use standard initial capacity (16) to grow into, unless we already know more is probably needed
        Set<BiTuple<A, B>> tupleABSetA = new LinkedHashSet<>(Math.max(16, tupleABSetMapB.size()));
        indexerA.put(indexProperties, tupleA, tupleABSetA);

        tupleABSetMapB.forEach((tupleB, tupleABSetB) -> {
            if (!tupleB.state.isDirty()) {
                BiTuple<A, B> tupleAB = new BiTuple<>(tupleA.factA, tupleB.factA);
                tupleAB.state = BavetTupleState.CREATING;
                tupleABSetA.add(tupleAB);
                tupleABSetB.add(tupleAB);
                dirtyTupleQueue.add(tupleAB);
            }
        });
    }

    public void retractA(UniTuple<A> tupleA) {
        Object[] indexProperties = mappingA.apply(tupleA.factA);

        Set<BiTuple<A, B>> tupleABSetA = indexerA.remove(indexProperties, tupleA);

        Map<UniTuple<B>, Set<BiTuple<A, B>>> tupleABSetMapB = indexerB.get(indexProperties);
        tupleABSetMapB.forEach((tupleB, tupleABSetB) -> {
            // TODO Performance: if tupleAB would contain tupleB, do this faster code instead:
            // for (tupleAB : tupleABSetA { tupleABSetMapB.get(tupleAB.tupleB).remove(tupleAB); }
            boolean changed = tupleABSetB.removeAll(tupleABSetA);
            if (!changed) {
                throw new IllegalStateException("Impossible state: the fact (" + tupleA.factA
                        + ") with indexProperties (" + Arrays.toString(indexProperties)
                        + ") has tuples on the B side that didn't exist on the A side.");
            }
        });
        for (BiTuple<A, B> tupleAB : tupleABSetA) {
            killTuple(tupleAB);
        }
    }

    public void insertB(UniTuple<B> tupleB) {
        Object[] indexProperties = mappingB.apply(tupleB.factA);

        Map<UniTuple<A>, Set<BiTuple<A, B>>> tupleABSetMapB = indexerA.get(indexProperties);
        // Use standard initial capacity (16) to grow into, unless we already know more is probably needed
        Set<BiTuple<A, B>> tupleABSetB = new LinkedHashSet<>(Math.max(16, tupleABSetMapB.size()));
        indexerB.put(indexProperties, tupleB, tupleABSetB);

        tupleABSetMapB.forEach((tupleA, tupleABSetA) -> {
            if (!tupleA.state.isDirty()) {
                BiTuple<A, B> tupleAB = new BiTuple<>(tupleA.factA, tupleB.factA);
                tupleAB.state = BavetTupleState.CREATING;
                tupleABSetB.add(tupleAB);
                tupleABSetA.add(tupleAB);
                dirtyTupleQueue.add(tupleAB);
            }
        });
    }

    public void retractB(UniTuple<B> tupleB) {
        Object[] indexProperties = mappingB.apply(tupleB.factA);

        Set<BiTuple<A, B>> tupleABSetB = indexerB.remove(indexProperties, tupleB);

        Map<UniTuple<A>, Set<BiTuple<A, B>>> tupleABSetMapA = indexerA.get(indexProperties);
        tupleABSetMapA.forEach((tupleA, tupleABSetA) -> {
            // TODO Performance: if tupleAB would contain tupleA, do this faster code instead:
            // for (tupleAB : tupleABSetB { tupleABSetMapA.get(tupleAB.tupleA).remove(tupleAB); }
            boolean changed = tupleABSetA.removeAll(tupleABSetB);
            if (!changed) {
                throw new IllegalStateException("Impossible state: the fact (" + tupleA.factA
                        + ") with indexProperties (" + Arrays.toString(indexProperties)
                        + ") has tuples on the B side that didn't exist on the A side.");
            }
        });
        for (BiTuple<A, B> tupleAB : tupleABSetB) {
            killTuple(tupleAB);
        }
    }

    private void killTuple(BiTuple<A, B> tupleAB) {
        // Don't add the tuple to the dirtyTupleQueue twice
        if (tupleAB.state.isDirty()) {
            switch (tupleAB.state) {
                case CREATING:
                    // Kill it before it propagates
                    tupleAB.state = BavetTupleState.ABORTING;
                    break;
                case UPDATING:
                    // Kill the original propagation
                    tupleAB.state = BavetTupleState.DYING;
                    break;
                case DYING:
                    break;
                default:
                    throw new IllegalStateException("Impossible state: The tuple for the facts ("
                            + tupleAB.factA + ", " + tupleAB.factB
                            + ") has the dirty state (" + tupleAB.state + ").");
            }
        } else {
            tupleAB.state = BavetTupleState.DYING;
            dirtyTupleQueue.add(tupleAB);
        }
    }

    public void calculateScore() {
        dirtyTupleQueue.forEach(tuple -> {
            // Retract
            if (tuple.state == BavetTupleState.UPDATING || tuple.state == BavetTupleState.DYING) {
                for (int outIndex = 0; outIndex < outs.length; outIndex++) {
                    BiOut<A, B> out = outs[outIndex];
                    out.nextNodeRetract.accept(tuple);
                }
            }
            // Insert
            if (tuple.state == BavetTupleState.CREATING || tuple.state == BavetTupleState.UPDATING) {
                for (int outIndex = 0; outIndex < outs.length; outIndex++) {
                    BiOut<A, B> out = outs[outIndex];
                    if (out.predicate.test(tuple.factA, tuple.factB)) {
                        out.nextNodeInsert.accept(tuple);
                    }
                }
            }
            switch (tuple.state) {
                case CREATING:
                case UPDATING:
                    tuple.state = BavetTupleState.OK;
                    return;
                case DYING:
                case ABORTING:
                    tuple.state = BavetTupleState.DEAD;
                    return;
                case DEAD:
                    throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
                            this + ") is already in the dead state (" + tuple.state + ").");
                default:
                    throw new IllegalStateException("Impossible state: Tuple (" + tuple + ") in node (" +
                            this + ") is in an unexpected state (" + tuple.state + ").");
            }
        });
        dirtyTupleQueue.clear();
    }

    @Override
    public String toString() {
        return "JoinBiNode";
    }

}
