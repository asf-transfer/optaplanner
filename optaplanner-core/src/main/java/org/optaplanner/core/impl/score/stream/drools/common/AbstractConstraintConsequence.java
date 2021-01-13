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

import static org.drools.model.PatternDSL.rule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.drools.model.*;
import org.drools.model.consequences.ConsequenceBuilder;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;

public abstract class AbstractConstraintConsequence<LeftHandSide_ extends AbstractLeftHandSide> {

    /**
     * The left hand side on which the consequence will be applied.
     * 
     * @return never null
     */
    protected abstract LeftHandSide_ getLeftHandSide();

    /**
     * The numeric type of the match weight that score will be impacted with.
     * See Javadoc for extending interfaces for more.
     *
     * @return never null
     */
    protected abstract ConsequenceMatchWeightType getMatchWeightType();

    public abstract int getCardinality();

    protected abstract ConsequenceBuilder.ValidBuilder buildConsequence(DroolsConstraint constraint,
            Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, Variable... variables);

    protected final Stream<Class> getExpectedJustificationTypes() {
        Variable<?>[] variables = getLeftHandSide().getVariables();
        Variable<?> lastVariable = variables[variables.length - 1];
        Class<?> type = lastVariable.getType();
        if (FactTuple.class.isAssignableFrom(type)) {
            // There is one expected constraint justification, and that is of the tuple type.
            return Stream.of(type);
        }
        // There are plenty expected constraint justifications, one for each variable.
        return Arrays.stream(variables)
                .map(Argument::getType);
    }

    public final <Solution_> RuleAssembly assemble(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            DroolsConstraint<Solution_> constraint) {
        LeftHandSide_ leftHandSide = getLeftHandSide();
        List<RuleItemBuilder<?>> ruleItemBuilderList = new ArrayList<>(leftHandSide.get());
        ConsequenceBuilder.ValidBuilder consequence = buildConsequence(constraint, scoreHolderGlobal,
                leftHandSide.getVariables());
        ruleItemBuilderList.add(consequence);
        Rule rule = rule(constraint.getConstraintPackage(), constraint.getConstraintName())
                .build(ruleItemBuilderList.toArray(new RuleItemBuilder[0]));
        return new RuleAssembly(rule, getExpectedJustificationTypes().toArray(Class[]::new));
    }

    protected static void impactScore(Drools drools, AbstractScoreHolder scoreHolder) {
        RuleContext kcontext = (RuleContext) drools;
        scoreHolder.impactScore(kcontext);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            int impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            long impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

    protected static void impactScore(DroolsConstraint constraint, Drools drools, AbstractScoreHolder scoreHolder,
            BigDecimal impact) {
        RuleContext kcontext = (RuleContext) drools;
        constraint.assertCorrectImpact(impact);
        scoreHolder.impactScore(kcontext, impact);
    }

}
