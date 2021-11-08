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
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.reteoo.SubnetworkTuple;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Tuple;
import org.drools.model.Variable;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

final class UniAccumulator<A, ResultContainer_, Result_> extends AbstractAccumulator<ResultContainer_, Result_> {

    private final String varA;
    private final BiFunction<ResultContainer_, A, Runnable> accumulator;

    private Declaration declaration;
    private UnaryOperator<Tuple> tupleExtractor;

    public UniAccumulator(Variable<A> varA, UniConstraintCollector<A, ResultContainer_, Result_> collector) {
        super(collector.supplier(), collector.finisher());
        this.varA = varA.getName();
        this.accumulator = Objects.requireNonNull(collector.accumulator());
    }

    @Override
    protected Runnable accumulate(ResultContainer_ context, Tuple leftTuple, InternalFactHandle handle,
            Declaration[] innerDeclarations) {
        InternalFactHandle factHandle = getFactHandle(leftTuple, handle);
        A a = (A) declaration.getValue(null, factHandle.getObject());
        return accumulator.apply(context, a);
    }

    private InternalFactHandle getFactHandle(Tuple leftTuple, InternalFactHandle handle) {
        if (tupleExtractor != null) {
            return tupleExtractor.apply(leftTuple).getFactHandle();
        } else {
            return handle;
        }
    }

    @Override
    protected void initialize(Tuple leftTuple, Declaration[] innerDeclarations) {
        for (Declaration declaration : innerDeclarations) {
            if (declaration.getBindingName().equals(varA)) {
                this.declaration = declaration;
                break;
            }
        }
        if (leftTuple instanceof SubnetworkTuple) {
            tupleExtractor = getTupleExtractor(declaration, leftTuple);
        }
    }

}
