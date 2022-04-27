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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.BavetTupleState;
import org.optaplanner.constraint.streams.bavet.common.index.IndexProperties;
import org.optaplanner.constraint.streams.bavet.common.index.Indexer;
import org.optaplanner.constraint.streams.bavet.tri.JoinTriNode;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;

public final class JoinBiNode<A, B> extends AbstractNode {

    private final Function<A, IndexProperties> mappingA;
    private final Function<B, IndexProperties> mappingB;
    private final int inputStoreIndexA;
    private final int inputStoreIndexB;
    /**
     * Calls for example {@link BiScorer#insert(BiTuple)}, {@link JoinTriNode#insertAB(BiTuple)} and/or ...
     */
    private final Consumer<BiTuple<A, B>> nextNodesInsert;
    /**
     * Calls for example {@link BiScorer#retract(BiTuple)}, {@link JoinTriNode#retractAB(BiTuple)} and/or ...
     */
    private final Consumer<BiTuple<A, B>> nextNodesRetract;
    private final int outputStoreSize;

    private final Indexer<UniTuple<A>, Map<UniTuple<B>, BiTuple<A, B>>> indexerA;
    private final Indexer<UniTuple<B>, Map<UniTuple<A>, BiTuple<A, B>>> indexerB;
    private final Queue<BiTuple<A, B>> dirtyTupleQueue;

    public JoinBiNode(Function<A, IndexProperties> mappingA, Function<B, IndexProperties> mappingB,
            int inputStoreIndexA, int inputStoreIndexB,
            Consumer<BiTuple<A, B>> nextNodesInsert, Consumer<BiTuple<A, B>> nextNodesRetract,
            int outputStoreSize,
            Indexer<UniTuple<A>, Map<UniTuple<B>, BiTuple<A, B>>> indexerA,
            Indexer<UniTuple<B>, Map<UniTuple<A>, BiTuple<A, B>>> indexerB) {
        this.mappingA = mappingA;
        this.mappingB = mappingB;
        this.inputStoreIndexA = inputStoreIndexA;
        this.inputStoreIndexB = inputStoreIndexB;
        this.nextNodesInsert = nextNodesInsert;
        this.nextNodesRetract = nextNodesRetract;
        this.outputStoreSize = outputStoreSize;
        this.indexerA = indexerA;
        this.indexerB = indexerB;
        dirtyTupleQueue = new ArrayDeque<>(1000);
    }

    public void insertA(UniTuple<A> tupleA) {
        if (tupleA.store[inputStoreIndexA] != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + tupleA
                    + ") was already added in the tupleStore.");
        }
        IndexProperties indexProperties = mappingA.apply(tupleA.factA);
        tupleA.store[inputStoreIndexA] = indexProperties;

        Map<UniTuple<B>, BiTuple<A, B>> tupleABMapA = new HashMap<>();
        indexerA.put(indexProperties, tupleA, tupleABMapA);
        indexerB.visit(indexProperties, (tupleB, tupleABMapB) -> {
            BiTuple<A, B> tupleAB = new BiTuple<>(tupleA.factA, tupleB.factA, outputStoreSize);
            tupleAB.state = BavetTupleState.CREATING;
            tupleABMapA.put(tupleB, tupleAB);
            tupleABMapB.put(tupleA, tupleAB);
            dirtyTupleQueue.add(tupleAB);
        });
    }

    public void retractA(UniTuple<A> tupleA) {
        IndexProperties indexProperties = (IndexProperties) tupleA.store[inputStoreIndexA];
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        tupleA.store[inputStoreIndexA] = null;

        indexerA.remove(indexProperties, tupleA);
        // Remove tupleABs from the other side
        indexerB.visit(indexProperties, (tupleB, tupleABMapB) -> {
            BiTuple<A, B> tupleAB = tupleABMapB.remove(tupleA);
            if (tupleAB == null) {
                throw new IllegalStateException("Impossible state: the tuple (" + tupleA
                        + ") with indexProperties (" + indexProperties
                        + ") has tuples on the A side that didn't exist on the B side.");
            }
            killTuple(tupleAB);
        });
    }

    public void insertB(UniTuple<B> tupleB) {
        if (tupleB.store[inputStoreIndexB] != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + tupleB
                    + ") was already added in the tupleStore.");
        }
        IndexProperties indexProperties = mappingB.apply(tupleB.factA);
        tupleB.store[inputStoreIndexB] = indexProperties;

        Map<UniTuple<A>, BiTuple<A, B>> tupleABMapB = new HashMap<>();
        indexerB.put(indexProperties, tupleB, tupleABMapB);
        indexerA.visit(indexProperties, (tupleA, tupleABMapA) -> {
            BiTuple<A, B> tupleAB = new BiTuple<>(tupleA.factA, tupleB.factA, outputStoreSize);
            tupleAB.state = BavetTupleState.CREATING;
            tupleABMapB.put(tupleA, tupleAB);
            tupleABMapA.put(tupleB, tupleAB);
            dirtyTupleQueue.add(tupleAB);
        });
    }

    public void retractB(UniTuple<B> tupleB) {
        IndexProperties indexProperties = (IndexProperties) tupleB.store[inputStoreIndexB];
        if (indexProperties == null) {
            // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
            return;
        }
        tupleB.store[inputStoreIndexB] = null;

        indexerB.remove(indexProperties, tupleB);
        // Remove tupleABs from the other side
        indexerA.visit(indexProperties, (tupleA, tupleABMapA) -> {
            BiTuple<A, B> tupleAB = tupleABMapA.remove(tupleB);
            if (tupleAB == null) {
                throw new IllegalStateException("Impossible state: the tuple (" + tupleA
                        + ") with indexProperties (" + indexProperties
                        + ") has tuples on the B side that didn't exist on the A side.");
            }
            killTuple(tupleAB);
        });
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
                    throw new IllegalStateException("Impossible state: The tuple (" + tupleAB
                            + ") has the dirty state (" + tupleAB.state + ").");
            }
        } else {
            tupleAB.state = BavetTupleState.DYING;
            dirtyTupleQueue.add(tupleAB);
        }
    }

    @Override
    public void calculateScore() {
        dirtyTupleQueue.forEach(tuple -> {
            // Retract
            if (tuple.state == BavetTupleState.UPDATING || tuple.state == BavetTupleState.DYING) {
                nextNodesRetract.accept(tuple);
            }
            // Insert
            if (tuple.state == BavetTupleState.CREATING || tuple.state == BavetTupleState.UPDATING) {
                nextNodesInsert.accept(tuple);
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
                    throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
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
