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

package org.optaplanner.core.impl.score.stream.drools.graph.rules;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.drools.model.PatternDSL;
import org.drools.model.Variable;

final class UniGroupBy1Map0CollectMutator<A, NewA> extends AbstractUniGroupByMutator<A> {

    private final Function<A, NewA> groupKeyMapping;

    public UniGroupBy1Map0CollectMutator(Function<A, NewA> groupKeyMapping) {
        this.groupKeyMapping = groupKeyMapping;
    }

    @Override
    public AbstractRuleAssembler apply(AbstractRuleAssembler ruleAssembler) {
        BiFunction<PatternDSL.PatternDef, Variable<NewA>, PatternDSL.PatternDef> binder =
                (pattern, tuple) -> pattern.bind(tuple, a -> groupKeyMapping.apply((A) a));
        return universalGroup(ruleAssembler, binder,
                (var, pattern, accumulate) -> regroup(ruleAssembler, var, pattern, accumulate));
    }
}
