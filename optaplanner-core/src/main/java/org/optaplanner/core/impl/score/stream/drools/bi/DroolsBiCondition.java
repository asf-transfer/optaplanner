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

package org.optaplanner.core.impl.score.stream.drools.bi;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.drools.core.base.accumulators.CollectSetAccumulateFunction;
import org.drools.model.DSL;
import org.drools.model.Drools;
import org.drools.model.Global;
import org.drools.model.Index;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.drools.model.Variable;
import org.drools.model.consequences.ConsequenceBuilder;
import org.drools.model.functions.Block4;
import org.drools.model.functions.Predicate3;
import org.drools.model.view.ExprViewItem;
import org.drools.model.view.ViewItem;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsCondition;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsPatternBuilder;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriCondition;
import org.optaplanner.core.impl.score.stream.drools.tri.DroolsTriRuleStructure;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniCondition;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniRuleStructure;
import org.optaplanner.core.impl.score.stream.tri.AbstractTriJoiner;

import static org.drools.model.DSL.accFunction;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.alphaIndexedBy;
import static org.drools.model.PatternDSL.pattern;

public final class DroolsBiCondition<A, B> extends DroolsCondition<DroolsBiRuleStructure<A, B>> {

    public DroolsBiCondition(DroolsBiRuleStructure<A, B> ruleStructure) {
        super(ruleStructure);
    }

    public DroolsBiCondition<A, B> andFilter(BiPredicate<A, B> predicate) {
        Predicate3<Object, A, B> filter = (__, a, b) -> predicate.test(a, b);
        Variable<A> aVariable = ruleStructure.getA();
        Variable<B> bVariable = ruleStructure.getB();
        DroolsPatternBuilder<Object> newTargetPattern = ruleStructure.getPrimaryPattern()
                .expand(p -> p.expr("Filter using " + predicate, aVariable, bVariable, filter));
        DroolsBiRuleStructure<A, B> newRuleStructure = new DroolsBiRuleStructure<>(aVariable, bVariable,
                newTargetPattern, ruleStructure.getOpenRuleItems(), ruleStructure.getVariableIdSupplier());
        return new DroolsBiCondition<>(newRuleStructure);
    }

    public <NewA> DroolsUniCondition<NewA> andGroup(BiFunction<A, B, NewA> groupKeyMapping) {
        Variable<NewA> mappedVariable = ruleStructure.createVariable("biMapped");
        PatternDSL.PatternDef<Object> mainAccumulatePattern = ruleStructure.getPrimaryPattern()
                        .expand(p -> p.bind(mappedVariable, ruleStructure.getA(), (b, a) -> groupKeyMapping.apply((A) a, (B) b)))
                        .build();
        ViewItem[] items = Stream.concat(ruleStructure.getOpenRuleItems().stream(), Stream.of(mainAccumulatePattern))
                .toArray(ViewItem[]::new);
        ViewItem<?> innerAccumulatePattern = PatternDSL.and(items[0], Arrays.copyOfRange(items, 1, items.length));
        Variable<Set<NewA>> setOfGroupKeys =
                (Variable<Set<NewA>>) ruleStructure.createVariable(Set.class, "setOfGroupKey");
        PatternDSL.PatternDef<Set<NewA>> pattern = pattern(setOfGroupKeys)
                .expr("Set of groupKey", set -> !set.isEmpty(),
                        alphaIndexedBy(Integer.class, Index.ConstraintType.GREATER_THAN, -1, Set::size, 0));
        ExprViewItem<Object> accumulate = DSL.accumulate(innerAccumulatePattern,
                accFunction(CollectSetAccumulateFunction.class, mappedVariable).as(setOfGroupKeys));
        DroolsUniRuleStructure<NewA> newRuleStructure = ruleStructure.regroup(setOfGroupKeys, pattern, accumulate);
        return new DroolsUniCondition<>(newRuleStructure);
    }

    public <C> DroolsTriCondition<A, B, C> andJoin(DroolsUniCondition<C> cCondition,
            AbstractTriJoiner<A, B, C> triJoiner) {
        DroolsUniRuleStructure<C> cRuleStructure = cCondition.getRuleStructure();
        Variable<C> cVariable = cRuleStructure.getA();
        UnaryOperator<PatternDSL.PatternDef<Object>> expander = p -> p.expr("Filter using " + triJoiner,
                ruleStructure.getA(), ruleStructure.getB(), cVariable, (__, a, b, c) -> matches(triJoiner, a, b, c));
        DroolsUniRuleStructure<C> newCRuleStructure = cRuleStructure.amend(expander);
        return new DroolsTriCondition<>(new DroolsTriRuleStructure<>(ruleStructure, newCRuleStructure,
                ruleStructure.getVariableIdSupplier()));
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, __, ___) -> impactScore(drools, scoreHolder));
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToIntBiFunction<A, B> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, a, b) -> impactScore(drools, scoreHolder, matchWeighter.applyAsInt(a, b)));
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            ToLongBiFunction<A, B> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, a, b) -> impactScore(drools, scoreHolder, matchWeighter.applyAsLong(a, b)));
    }

    public List<RuleItemBuilder<?>> completeWithScoring(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal,
            BiFunction<A, B, BigDecimal> matchWeighter) {
        return completeWithScoring(scoreHolderGlobal,
                (drools, scoreHolder, a, b) -> impactScore(drools, scoreHolder, matchWeighter.apply(a, b)));
    }

    private <ScoreHolder extends AbstractScoreHolder<?>> List<RuleItemBuilder<?>> completeWithScoring(
            Global<ScoreHolder> scoreHolderGlobal, Block4<Drools, ScoreHolder, A, B> consequenceImpl) {
        ConsequenceBuilder._3<ScoreHolder, A, B> consequence =
                on(scoreHolderGlobal, ruleStructure.getA(), ruleStructure.getB())
                        .execute(consequenceImpl);
        return ruleStructure.finish(consequence);
    }

    private static <A, B, C> boolean matches(AbstractTriJoiner<A, B, C> triJoiner, A a, B b, C c) {
        JoinerType[] joinerTypes = triJoiner.getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = triJoiner.getLeftMapping(i).apply(a, b);
            Object rightMapping = triJoiner.getRightMapping(i).apply(c);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }

}
