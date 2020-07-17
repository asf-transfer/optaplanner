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

import org.drools.model.Global;
import org.optaplanner.core.impl.score.holder.AbstractScoreHolder;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.common.nodes.ConstraintGraphNode;

public interface RuleAssembler {

    String VARIABLE_TYPE_RULE_METADATA_KEY = "constraintStreamVariableTypes";

    static RuleAssembler from(ConstraintGraphNode node, int expectedGroupByCount) {
        return new UniRuleAssembler(node, expectedGroupByCount);
    }

    RuleAssembler andThen(ConstraintGraphNode node);

    RuleAssembler join(RuleAssembler ruleAssembler, ConstraintGraphNode joinNode);

    RuleAssembly assemble(Global<? extends AbstractScoreHolder<?>> scoreHolderGlobal, DroolsConstraint constraint);

}
