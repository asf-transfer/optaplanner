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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Accumulator;
import org.drools.core.spi.Tuple;

abstract class AbstractAccumulator<ResultContainer_, Result_> implements Accumulator {

    private final Supplier<ResultContainer_> containerSupplier;
    private final Function<ResultContainer_, Result_> finisher;

    private final AtomicBoolean initialized = new AtomicBoolean();

    protected AbstractAccumulator(Supplier<ResultContainer_> containerSupplier,
            Function<ResultContainer_, Result_> finisher) {
        this.containerSupplier = Objects.requireNonNull(containerSupplier);
        this.finisher = Objects.requireNonNull(finisher);
    }

    protected static <X> Function<Tuple, X> getValueExtractor(Declaration declaration, Tuple leftTuple) {
        return new ValueExtractor<>(declaration, leftTuple);
    }

    @Override
    public final Object createWorkingMemoryContext() {
        return null;
    }

    @Override
    public final Object createContext() {
        return null; // We always create and init during init(...).
    }

    @Override
    public final ResultContainer_ init(Object workingMemoryContext, Object context, Tuple leftTuple,
            Declaration[] declarations, ReteEvaluator reteEvaluator) {
        return containerSupplier.get();
    }

    @Override
    public final Object accumulate(Object workingMemoryContext, Object context, Tuple leftTuple,
            InternalFactHandle handle, Declaration[] declarations, Declaration[] innerDeclarations,
            ReteEvaluator reteEvaluator) {
        /*
         * Accumulator instances are created within the KieBase, not within the KieSession.
         * This means that sessions may be calling this method on a shared accumulator from different threads.
         * Unfortunately, we need access to innerDeclarations to be able to perform the accumulation quickly,
         * and it only becomes available when this accumulate(...) method is called.
         * Therefore, initialization of our accumulator can only happen the first time the method is called,
         * and therefore the initialization needs to be properly synchronized.
         *
         * We do not care if the initialization happens multiple times,
         * as each of the attempts will fill the variables with the same data.
         * We just need to ensure that the variables are initialized before the actual accumulation happens.
         *
         * Technically running this more than once is wasteful,
         * but the initialized() check will make sure this only happens during the cold start of the session.
         */
        if (!initialized.get()) {
            initialize(leftTuple, innerDeclarations);
            initialized.set(true);
        }
        return accumulate((ResultContainer_) context, leftTuple, handle, innerDeclarations);
    }

    protected abstract Runnable accumulate(ResultContainer_ context, Tuple leftTuple, InternalFactHandle handle,
            Declaration[] innerDeclarations);

    protected abstract void initialize(Tuple leftTuple, Declaration[] innerDeclarations);

    @Override
    public final boolean supportsReverse() {
        return true;
    }

    @Override
    public final boolean tryReverse(Object workingMemoryContext, Object context, Tuple leftTuple,
            InternalFactHandle handle, Object value, Declaration[] declarations, Declaration[] innerDeclarations,
            ReteEvaluator reteEvaluator) {
        ((Runnable) value).run();
        return true;
    }

    @Override
    public final Result_ getResult(Object workingMemoryContext, Object context, Tuple leftTuple,
            Declaration[] declarations, ReteEvaluator reteEvaluator) {
        return finisher.apply((ResultContainer_) context);
    }
}
