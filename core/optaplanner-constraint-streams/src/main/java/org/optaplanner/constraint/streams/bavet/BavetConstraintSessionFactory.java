/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.optaplanner.constraint.streams.bavet.common.AbstractNode;
import org.optaplanner.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.BavetScoringConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.NodeBuildHelper;
import org.optaplanner.constraint.streams.bavet.uni.ForEachUniNode;
import org.optaplanner.constraint.streams.common.inliner.AbstractScoreInliner;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

public final class BavetConstraintSessionFactory<Solution_, Score_ extends Score<Score_>> {

    private final SolutionDescriptor<Solution_> solutionDescriptor;
    private final List<BavetConstraint<Solution_>> constraintList;

    public BavetConstraintSessionFactory(SolutionDescriptor<Solution_> solutionDescriptor,
            List<BavetConstraint<Solution_>> constraintList) {
        this.solutionDescriptor = solutionDescriptor;
        this.constraintList = constraintList;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    public BavetConstraintSession<Solution_, Score_> buildSession(boolean constraintMatchEnabled,
            Solution_ workingSolution) {
        ScoreDefinition<Score_> scoreDefinition = solutionDescriptor.getScoreDefinition();
        AbstractScoreInliner<Score_> scoreInliner = AbstractScoreInliner.buildScoreInliner(scoreDefinition,
                constraintMatchEnabled);

        Score_ zeroScore = scoreDefinition.getZeroScore();
        Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet = new LinkedHashSet<>(constraintList.size() * 10);
        Map<Constraint, Score_> constraintWeightMap = new HashMap<>(constraintList.size());
        for (BavetConstraint<Solution_> constraint : constraintList) {
            // Expensive, only do once.
            Score_ constraintWeight = constraint.extractConstraintWeight(workingSolution);
            // Filter out constraints with zero weight
            if (!constraintWeight.equals(zeroScore)) {
                // Node sharing: each CS.buildNode() is called exactly once (even if multiple constraints use it)
                // because CS implement equals/hashcode and they are collected in a Set.
                constraint.collectActiveConstraintStreams(constraintStreamSet);
                constraintWeightMap.put(constraint, constraintWeight);
            }
        }
        NodeBuildHelper<Score_> buildHelper = new NodeBuildHelper<>(constraintStreamSet, constraintWeightMap, scoreInliner);
        // Reverse constraintStreamSet order to create downstream nodes first
        List<BavetAbstractConstraintStream<Solution_>> reversedConstraintStreamList = new ArrayList<>(constraintStreamSet);
        Collections.reverse(reversedConstraintStreamList);
        for (BavetAbstractConstraintStream<Solution_> constraintStream : reversedConstraintStreamList) {
            constraintStream.buildNode(buildHelper);
        }
        List<AbstractNode> nodeList = buildHelper.destroyAndGetNodeList();
        Map<Class<?>, ForEachUniNode<Object>> declaredClassToNodeMap = new LinkedHashMap<>();
        for (AbstractNode node : nodeList) {
            if (node instanceof ForEachUniNode) {
                ForEachUniNode<Object> forEachUniNode = (ForEachUniNode<Object>) node;
                declaredClassToNodeMap.put(forEachUniNode.getForEachClass(), forEachUniNode);
            }
        }
        return new BavetConstraintSession<>(scoreInliner, declaredClassToNodeMap, nodeList.toArray(AbstractNode[]::new));
    }

}
