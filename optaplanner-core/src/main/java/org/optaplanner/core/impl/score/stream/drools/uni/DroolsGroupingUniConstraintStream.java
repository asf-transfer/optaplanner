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

package org.optaplanner.core.impl.score.stream.drools.uni;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.Rule;
import org.drools.model.RuleItemBuilder;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsLogicalTuple;

import static org.drools.model.PatternDSL.declarationOf;
import static org.drools.model.PatternDSL.pattern;

public final class DroolsGroupingUniConstraintStream<Solution_, A, GroupKey_>
        extends DroolsAbstractUniConstraintStream<Solution_, GroupKey_> {

    private final DroolsAbstractUniConstraintStream<Solution_, A> parent;
    private final Function<A, GroupKey_> groupKeyMapping;

    public DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, GroupKey_> groupKeyMapping) {
        super(constraintFactory, new DroolsUniCondition<>(declarationOf(DroolsLogicalTuple.class),
                (contextId, var) -> pattern(var).expr(logicalTuple -> Objects.equals(logicalTuple.getContext(), contextId))));
        this.parent = parent;
        this.groupKeyMapping = groupKeyMapping;
    }

    @Override
    public List<DroolsFromUniConstraintStream<Solution_, Object>> getFromStreamList() {
        return parent.getFromStreamList();
    }

    @Override
    public Optional<Rule> buildRule(DroolsConstraint<Solution_> constraint, Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        final String currentContextId = getCondition().getContextId();
        final String ruleName = UUID.randomUUID().toString(); // Generated == groupings can be shared by many rules.
        Rule rule = PatternDSL.rule(constraint.getConstraintPackage(), ruleName)
                .build(parent.getCondition().completeWithLogicalInsert(currentContextId, groupKeyMapping)
                        .toArray(new RuleItemBuilder<?>[0]));
        return Optional.of(rule);
    }
    @Override
    public String toString() {
        return "GroupBy()";
    }

}
