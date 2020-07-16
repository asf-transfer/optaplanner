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

package org.optaplanner.core.impl.score.stream.drools.common.rules;

import static org.drools.model.PatternDSL.PatternDef;

import org.drools.model.Variable;
import org.optaplanner.core.impl.score.stream.drools.common.QuadTuple;

abstract class AbstractQuadGroupByMutator<A, B, C, D> extends AbstractGroupByMutator {

    @Override
    protected <InTuple> PatternDef bindTupleVariableOnFirstGrouping(AbstractRuleAssembler ruleAssembler, PatternDef pattern,
            Variable<InTuple> inTupleVariable) {
        return pattern.bind(inTupleVariable, ruleAssembler.getVariable(0), ruleAssembler.getVariable(1),
                ruleAssembler.getVariable(2), (d, a, b, c) -> new QuadTuple<>(a, b, c, d));
    }

}
