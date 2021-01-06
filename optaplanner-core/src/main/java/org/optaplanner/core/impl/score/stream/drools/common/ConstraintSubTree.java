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

package org.optaplanner.core.impl.score.stream.drools.common;

import static org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNodeType.FROM;
import static org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNodeType.JOIN;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNodeType;
import org.optaplanner.core.impl.score.stream.drools.common.rules.RuleAssembler;

final class ConstraintSubTree {

    private final DroolsVariableFactory variableFactory;
    private final boolean isJoin;
    private final ConstraintSubTree leftSubTree;
    private final ConstraintSubTree rightSubTree;
    private final List<ConstraintGraphNode> nodeList;

    public ConstraintSubTree(List<ConstraintGraphNode> joinlessNodeList, DroolsVariableFactory variableFactory) {
        this.variableFactory = variableFactory;
        this.isJoin = false;
        this.leftSubTree = null;
        this.rightSubTree = null;
        this.nodeList = Collections.unmodifiableList(joinlessNodeList);
        if (nodeList.isEmpty()) {
            throw new IllegalStateException("Impossible state: Node list is empty.");
        }
        ConstraintGraphNode firstNode = nodeList.get(0);
        ConstraintGraphNodeType firstNodeType = firstNode.getType();
        if (firstNodeType != FROM) {
            throw new IllegalStateException("Impossible state: First node (" + firstNode + ") is not " +
                    ConstraintGraphNodeType.FROM + " (" + firstNodeType + ").");
        }
    }

    public ConstraintSubTree(ConstraintSubTree leftSubTree, ConstraintSubTree rightSubTree,
            List<ConstraintGraphNode> joinAndOtherNodesList, DroolsVariableFactory variableFactory) {
        this.variableFactory = variableFactory;
        this.isJoin = true;
        this.leftSubTree = Objects.requireNonNull(leftSubTree);
        this.rightSubTree = Objects.requireNonNull(rightSubTree);
        this.nodeList = Collections.unmodifiableList(joinAndOtherNodesList);
        if (nodeList.isEmpty()) {
            throw new IllegalStateException("Impossible state: Node list is empty.");
        }
        ConstraintGraphNode firstNode = nodeList.get(0);
        ConstraintGraphNodeType firstNodeType = firstNode.getType();
        if (nodeList.get(0).getType() != JOIN) {
            throw new IllegalStateException("Impossible state: First node (" + firstNode + ") is not " +
                    ConstraintGraphNodeType.JOIN + " (" + firstNodeType + ").");
        }
    }

    public RuleAssembler getRuleAssembler() {
        RuleAssembler assembler = getRuleAssemblerForFirstNode();
        for (int i = 1; i < nodeList.size(); i++) {
            ConstraintGraphNode nextNode = nodeList.get(i);
            assembler = assembler.andThen(nextNode);
        }
        return assembler;
    }

    private RuleAssembler getRuleAssemblerForFirstNode() {
        ConstraintGraphNode firstNode = nodeList.get(0);
        if (isJoin) {
            RuleAssembler left = leftSubTree.getRuleAssembler();
            RuleAssembler right = rightSubTree.getRuleAssembler();
            return left.join(right, firstNode);
        } else {
            return RuleAssembler.from(variableFactory, firstNode);
        }
    }
}
