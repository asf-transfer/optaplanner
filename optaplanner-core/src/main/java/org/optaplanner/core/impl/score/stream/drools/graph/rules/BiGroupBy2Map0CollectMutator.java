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

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.function.BiFunction;

import org.drools.model.Variable;

class BiGroupBy2Map0CollectMutator<A, B, NewA, NewB>
        extends BiGroupBy2Map1CollectMutator<A, B, NewA, NewB, Void> {

    public BiGroupBy2Map0CollectMutator(BiFunction<A, B, NewA> groupKeyMappingA,
            BiFunction<A, B, NewB> groupKeyMappingB) {
        super(groupKeyMappingA, groupKeyMappingB, null);
    }

    @Override
    public AbstractRuleBuilder apply(AbstractRuleBuilder ruleBuilder) {
        AbstractRuleBuilder newRuleBuilder = super.apply(ruleBuilder);
        // Downgrade the tri-stream to a bi-stream by ignoring the dummy no-op collector variable.
        List<Variable> allVariablesButLast = newRuleBuilder.getVariables()
                .subList(0, newRuleBuilder.getVariables().size() - 1);
        return new BiRuleBuilder(newRuleBuilder::generateNextId, newRuleBuilder.getExpectedGroupByCount(),
                newRuleBuilder.getFinishedExpressions(), allVariablesButLast, newRuleBuilder.getPrimaryPatterns(),
                emptyMap());
    }
}
