/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.bavet.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.optaplanner.core.impl.score.stream.bavet.BavetConstraintSession;
import org.optaplanner.core.impl.score.stream.bavet.bi.BavetGroupBridgeBiNode;
import org.optaplanner.core.impl.score.stream.bavet.uni.BavetGroupBridgeUniNode;

public class BavetNodeBuildPolicy<Solution_> {

    private final BavetConstraintSession session;

    private int nodeIndexMaximum = -1; // So that the first node starts at 0 when it increments.
    private Map<String, BavetScoringNode> constraintIdToScoringNodeMap;
    private Map<BavetJoinConstraintStream<Solution_>, BavetJoinBridgeNode> joinConstraintStreamToJoinBridgeNodeMap =
            new HashMap<>();
    private Map<BavetAbstractNode, BavetAbstractNode> sharableNodeMap = new HashMap<>();

    public BavetNodeBuildPolicy(BavetConstraintSession session, int constraintCount) {
        this.session = session;
        constraintIdToScoringNodeMap = new LinkedHashMap<>(constraintCount);
    }

    public <Node_ extends BavetAbstractNode> Node_ retrieveSharedNode(Node_ node) {
        Node_ sharedNode = (Node_) sharableNodeMap.computeIfAbsent(node, k -> node);
        int nodeIndex = sharedNode.getNodeIndex();
        System.out.println(nodeIndex + ": " + node);
        if (nodeIndexMaximum < nodeIndex) {
            nodeIndexMaximum = nodeIndex;
        }
        return sharedNode;
    }

    public void addScoringNode(BavetScoringNode scoringNode) {
        constraintIdToScoringNodeMap.put(scoringNode.getConstraintId(), scoringNode);
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    public BavetConstraintSession getSession() {
        return session;
    }

    public int getNodeIndexMaximum() {
        return nodeIndexMaximum;
    }

    public Map<String, BavetScoringNode> getConstraintIdToScoringNodeMap() {
        return constraintIdToScoringNodeMap;
    }

    public Map<BavetJoinConstraintStream<Solution_>, BavetJoinBridgeNode> getJoinConstraintStreamToJoinBridgeNodeMap() {
        return joinConstraintStreamToJoinBridgeNodeMap;
    }

}
