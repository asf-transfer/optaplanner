/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.bavet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;
import org.optaplanner.core.impl.score.stream.ConstraintSession;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetAbstractTuple;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetNodeBuildPolicy;
import org.optaplanner.core.impl.score.stream.bavet.common.BavetTupleState;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetFromUniNode;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetFromUniTuple;

public final class BavetConstraintSession<Solution_> implements ConstraintSession<Solution_>  {

    private final Map<Class<?>, BavetFromUniNode<Object>> declaredClassToNodeMap; // TODO should the value be object instead of list?
    private final Map<Class<?>, List<BavetFromUniNode<Object>>> effectiveClassToNodeListMap;

    private final int nodeOrderSize;
    private final List<Queue<BavetAbstractTuple>> nodeOrderedQueueList;
    private final Map<Object, BavetFromUniTuple<Object>> fromTupleMap;

    private ScoreInliner scoreInliner;

    public BavetConstraintSession(Map<BavetConstraint<Solution_>, Score<?>> constraintToWeightMap,
            ScoreInliner scoreInliner) {
        this.scoreInliner = scoreInliner;
        declaredClassToNodeMap = new HashMap<>(50);
        BavetNodeBuildPolicy<Solution_> buildPolicy = new BavetNodeBuildPolicy<>(this);
        constraintToWeightMap.forEach((constraint, constraintWeight) -> {
            constraint.createNodes(buildPolicy, declaredClassToNodeMap, constraintWeight);
        });
        effectiveClassToNodeListMap = new HashMap<>(declaredClassToNodeMap.size());
        this.nodeOrderSize = buildPolicy.getNodeOrderMaximum() + 1;
        nodeOrderedQueueList = new ArrayList<>(nodeOrderSize);
        for (int i = 0; i < nodeOrderSize; i++) {
            nodeOrderedQueueList.add(new ArrayDeque<>(1000));
        }
        fromTupleMap = new IdentityHashMap<>(1000);
    }

    public List<BavetFromUniNode<Object>> findFromNodeList(Class<?> factClass) {
        return effectiveClassToNodeListMap.computeIfAbsent(factClass, key -> {
            List<BavetFromUniNode<Object>> nodeList = new ArrayList<>();
            declaredClassToNodeMap.forEach((declaredClass, declaredNode) -> {
                if (declaredClass.isAssignableFrom(factClass)) {
                    nodeList.add(declaredNode);
                }
            });
            return nodeList;
        });
    }

    @Override
    public void insert(Object fact) {
        Class<?> factClass = fact.getClass();
        List<BavetFromUniNode<Object>> fromNodeList = findFromNodeList(factClass);
        for (BavetFromUniNode<Object> node : fromNodeList) {
            BavetFromUniTuple<Object> tuple = node.createTuple(fact);
            BavetFromUniTuple<Object> old = fromTupleMap.put(fact, tuple);
            if (old != null) {
                throw new IllegalStateException("The fact (" + fact + ") was already inserted, so it cannot insert again.");
            }
            transitionTuple(tuple, BavetTupleState.CREATING);
        }
    }

    @Override
    public void update(Object fact) {
        BavetFromUniTuple<Object> tuple = fromTupleMap.get(fact);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + fact + ") was never inserted, so it cannot update.");
        }
        transitionTuple(tuple, BavetTupleState.UPDATING);
    }

    @Override
    public void retract(Object fact) {
        BavetFromUniTuple<Object> tuple = fromTupleMap.remove(fact);
        if (tuple == null) {
            throw new IllegalStateException("The fact (" + fact + ") was never inserted, so it cannot retract.");
        }
        transitionTuple(tuple, BavetTupleState.DYING);
    }

    @Override
    public Score<?> calculateScore(int initScore) {
        for (int i = 0; i < nodeOrderSize; i++) {
            Queue<BavetAbstractTuple> queue = nodeOrderedQueueList.get(i);
            BavetAbstractTuple tuple = queue.poll();
            while (tuple != null) {
                tuple.refresh();
                tuple = queue.poll();
            }
        }
        return scoreInliner.extractScore(initScore);
    }

    public void transitionTuple(BavetAbstractTuple tuple, BavetTupleState newState) {
        if (tuple.isDirty()) {
            if (tuple.getState() != newState) {
                if ((tuple.getState() == BavetTupleState.CREATING && newState == BavetTupleState.DYING)) {
                    tuple.setState(BavetTupleState.ABORTING);
                } else if ((tuple.getState() == BavetTupleState.UPDATING && newState == BavetTupleState.DYING)) {
                    tuple.setState(BavetTupleState.DYING);
                } else {
                    throw new IllegalStateException("The tuple (" + tuple
                            + ") already has a dirty state (" + tuple.getState()
                            + ") so it cannot transition to newState (" + newState + ").");
                }
            }
            // Don't add it to the queue twice
            return;
        }
        tuple.setState(newState);
        nodeOrderedQueueList.get(tuple.getNodeOrder()).add(tuple);
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    public ScoreInliner getScoreInliner() {
        return scoreInliner;
    }

}
