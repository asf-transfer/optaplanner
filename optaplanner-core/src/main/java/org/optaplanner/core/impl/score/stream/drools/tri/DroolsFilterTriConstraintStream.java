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

package org.optaplanner.core.impl.score.stream.drools.tri;

import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.graph.nodes.TriConstraintGraphNode;

public final class DroolsFilterTriConstraintStream<Solution_, A, B, C>
        extends DroolsAbstractTriConstraintStream<Solution_, A, B, C> {

    private final TriConstraintGraphNode<A, B, C> node;
    private final DroolsTriCondition<A, B, C, ?> condition;

    public DroolsFilterTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriPredicate<A, B, C> triPredicate) {
        super(constraintFactory);
        this.node = constraintFactory.getConstraintGraph().filter(parent.getConstraintGraphNode(), triPredicate);
        this.condition = parent.getCondition().andFilter(triPredicate);
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public TriConstraintGraphNode<A, B, C> getConstraintGraphNode() {
        return node;
    }

    @Override
    public DroolsTriCondition<A, B, C, ?> getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return "TriFilter() with " + getChildStreams().size() + " children";
    }

}
