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

package org.optaplanner.core.impl.score.stream.drools.bi;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.kie.api.runtime.rule.AccumulateFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.BiTuple;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAccumulateContext;

final class DroolsBiAccumulateFunctionBridge<A, B, ResultContainer_, NewA>
        implements AccumulateFunction<DroolsAccumulateContext<ResultContainer_>> {

    private final Supplier<ResultContainer_> supplier;
    private final TriFunction<ResultContainer_, A, B, Runnable> accumulator;
    private final Function<ResultContainer_, NewA> finisher;

    public DroolsBiAccumulateFunctionBridge(BiConstraintCollector<A, B, ResultContainer_, NewA> collector) {
        this.supplier = collector.supplier();
        this.accumulator = collector.accumulator();
        this.finisher = collector.finisher();
    }

    public DroolsBiAccumulateFunctionBridge() {
        throw new UnsupportedOperationException("Serialization is not supported.");
    }

    @Override
    public DroolsAccumulateContext<ResultContainer_> createContext() {
        return new DroolsAccumulateContext<>(supplier.get());
    }

    @Override
    public void init(DroolsAccumulateContext<ResultContainer_> context) {
        context.getUndoMap().clear();
    }

    @Override
    public void accumulate(DroolsAccumulateContext<ResultContainer_> context, Object value) {
        Map<Object, Runnable> undoMap = context.getUndoMap();
        if (undoMap.containsKey(value)) {
            throw new IllegalStateException("Undo for (" + value +  ") already exists.");
        }
        BiTuple<A, B> values = (BiTuple<A, B>) value;
        Runnable undo = accumulator.apply(context.getContainer(), values._1, values._2);
        undoMap.put(value, undo);
    }

    @Override
    public void reverse(DroolsAccumulateContext<ResultContainer_> context, Object value) {
        Runnable undo = context.getUndoMap().remove(value);
        if (undo == null) {
            throw new IllegalStateException("Undo for (" + value +  ") does not exist.");
        }
        undo.run();
    }

    @Override
    public Object getResult(DroolsAccumulateContext<ResultContainer_> context) {
        return finisher.apply(context.getContainer());
    }

    @Override
    public boolean supportsReverse() {
        return true;
    }

    @Override
    public Class<?> getResultType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeExternal(ObjectOutput out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readExternal(ObjectInput in) {
        throw new UnsupportedOperationException();
    }

}
