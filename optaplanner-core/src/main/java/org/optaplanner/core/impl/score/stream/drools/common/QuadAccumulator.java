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

import java.util.Objects;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Tuple;
import org.drools.model.Variable;
import org.optaplanner.core.api.function.PentaFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;

final class QuadAccumulator<A, B, C, D, ResultContainer_, Result_>
        extends AbstractAccumulator<ResultContainer_, Result_> {

    private final String varA;
    private final String varB;
    private final String varC;
    private final String varD;
    private final PentaFunction<ResultContainer_, A, B, C, D, Runnable> accumulator;

    private Declaration declarationA;
    private Declaration declarationB;
    private Declaration declarationC;
    private Declaration declarationD;
    private int offsetToA;
    private int offsetToB;
    private int offsetToC;
    private int offsetToD;

    public QuadAccumulator(Variable<A> varA, Variable<B> varB, Variable<C> varC, Variable<D> varD,
            QuadConstraintCollector<A, B, C, D, ResultContainer_, Result_> collector) {
        super(collector.supplier(), collector.finisher());
        this.accumulator = Objects.requireNonNull(collector.accumulator());
        this.varA = varA.getName();
        this.varB = varB.getName();
        this.varC = varC.getName();
        this.varD = varD.getName();
    }

    @Override
    public Object accumulate(Object workingMemoryContext, Object context, Tuple leftTuple, InternalFactHandle handle,
            Declaration[] declarations, Declaration[] innerDeclarations, WorkingMemory workingMemory) {
        if (declarationA == null) {
            init(leftTuple, innerDeclarations);
        }

        A a = extractValue(declarationA, offsetToA, leftTuple);
        B b = extractValue(declarationB, offsetToB, leftTuple);
        C c = extractValue(declarationC, offsetToC, leftTuple);
        D d = extractValue(declarationD, offsetToD, leftTuple);
        return accumulator.apply((ResultContainer_) context, a, b, c, d);
    }

    private void init(Tuple leftTuple, Declaration[] innerDeclarations) {
        for (Declaration declaration : innerDeclarations) {
            if (declaration.getBindingName().equals(varA)) {
                declarationA = declaration;
            } else if (declaration.getBindingName().equals(varB)) {
                declarationB = declaration;
            } else if (declaration.getBindingName().equals(varC)) {
                declarationC = declaration;
            } else if (declaration.getBindingName().equals(varD)) {
                declarationD = declaration;
            }
        }

        offsetToA = findTupleOffset(declarationA, leftTuple);
        offsetToB = findTupleOffset(declarationB, leftTuple);
        offsetToC = findTupleOffset(declarationC, leftTuple);
        offsetToD = findTupleOffset(declarationD, leftTuple);
    }

}
