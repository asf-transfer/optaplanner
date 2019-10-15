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
import java.util.concurrent.atomic.AtomicInteger;
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
    private final AtomicInteger ruleId = new AtomicInteger(-1);

    public DroolsGroupingUniConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, GroupKey_> groupKeyMapping) {
        super(constraintFactory);
        this.parent = parent;
        this.groupKeyMapping = groupKeyMapping;
    }

    @Override
    public List<DroolsFromUniConstraintStream<Solution_, Object>> getFromStreamList() {
        return parent.getFromStreamList();
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public Optional<Rule> buildRule(DroolsConstraint<Solution_> constraint,
            Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        Object createdRuleId = createRuleIdIfAbsent(constraint.getConstraintFactory());
        String ruleName = "Helper rule #" + createdRuleId + " (GroupBy)";
        Rule rule = PatternDSL.rule(constraint.getConstraintPackage(), ruleName)
                .build(parent.createCondition().completeWithLogicalInsert(createdRuleId, groupKeyMapping)
                        .toArray(new RuleItemBuilder<?>[0]));
        return Optional.of(rule);
    }

    @Override
    public DroolsUniCondition<GroupKey_> createCondition() {
        return new DroolsUniCondition<>(declarationOf(DroolsLogicalTuple.class), var -> {
            Object createdRuleId = createRuleIdIfAbsent(getConstraintFactory());
            return pattern(var).expr(logicalTuple ->
                    Objects.equals(logicalTuple.getRuleId(), createdRuleId));
        });
    }

    /**
     * Exists in order to be able to bind the condition in the new rule to the {@link DroolsLogicalTuple}s generated by
     * the old rule, while only generating the rule ID on-demand to maintain the requirement of rule IDs being in an
     * unbroken sequence.
     * The idea here is that rule creating ({@link #buildRule(DroolsConstraint, Global)}) would call this first,
     * establishing the rule ID with which to insert all the {@link DroolsLogicalTuple}s.
     * The later creation of the condition during {@link DroolsAbstractUniConstraintStream#createCondition()} would
     * just retrieve the value that already exists and query for all the {@link DroolsLogicalTuple}s having this value.
     * Even if the order of operations were switched (condition created before rule built), the ID would still be
     * correctly shared though.
     * @param constraintFactory never null, used to generate rule IDs
     * @return unique id for the rule
     */
    private int createRuleIdIfAbsent(DroolsConstraintFactory<Solution_> constraintFactory) {
        return ruleId.updateAndGet(currentRuleId -> (currentRuleId < 0) ?
                constraintFactory.getRuleIdAndIncrement() :
                currentRuleId);
    }

    @Override
    public String toString() {
        return "GroupBy()";
    }
}
