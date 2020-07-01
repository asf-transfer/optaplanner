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

package org.optaplanner.core.impl.score.stream.drools.model.consequences;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.impl.score.stream.drools.model.nodes.QuadConstraintModelNode;

final class QuadConstraintBigDecimalConsequence<A, B, C, D> implements QuadConstraintConsequence<A, B, C, D>,
        QuadFunction<A, B, C, D, BigDecimal> {

    private final QuadConstraintModelNode<A, B, C, D> terminalNode;
    private final QuadFunction<A, B, C, D, BigDecimal> matchWeighter;

    QuadConstraintBigDecimalConsequence(QuadConstraintModelNode<A, B, C, D> terminalNode,
            QuadFunction<A, B, C, D, BigDecimal> matchWeighter) {
        this.terminalNode = requireNonNull(terminalNode);
        this.matchWeighter = requireNonNull(matchWeighter);
    }

    @Override
    public QuadConstraintModelNode<A, B, C, D> getTerminalNode() {
        return terminalNode;
    }

    @Override
    public ConsequenceMatchWeightType getMatchWeightType() {
        return ConsequenceMatchWeightType.BIG_DECIMAL;
    }

    @Override
    public BigDecimal apply(A a, B b, C c, D d) {
        return matchWeighter.apply(a, b, c, d);
    }

}
