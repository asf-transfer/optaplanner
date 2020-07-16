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

import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsUniAccumulateFunction;

final class UniGroupBy0Map1CollectMutator<A, NewA> extends AbstractUniGroupByMutator<A> {

    private final UniConstraintCollector<A, ?, NewA> collector;

    public UniGroupBy0Map1CollectMutator(UniConstraintCollector<A, ?, NewA> collector) {
        this.collector = collector;
    }

    @Override
    public AbstractRuleBuilder apply(AbstractRuleBuilder ruleBuilder) {
        DroolsUniAccumulateFunction<A, ?, NewA> bridge = new DroolsUniAccumulateFunction<>(collector);
        return collect(ruleBuilder, bridge);
    }
}
