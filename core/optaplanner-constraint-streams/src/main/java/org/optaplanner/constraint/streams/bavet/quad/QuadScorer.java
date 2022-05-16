/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet.quad;

import org.optaplanner.constraint.streams.bavet.common.AbstractScorer;
import org.optaplanner.constraint.streams.common.inliner.UndoScoreImpacter;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.Score;

public final class QuadScorer<A, B, C, D> extends AbstractScorer {

    private final String constraintPackage;
    private final String constraintName;
    private final Score<?> constraintWeight;
    private final QuadFunction<A, B, C, D, UndoScoreImpacter> scoreImpacter;
    private final int inputStoreIndex;

    public QuadScorer(String constraintPackage, String constraintName,
            Score<?> constraintWeight, QuadFunction<A, B, C, D, UndoScoreImpacter> scoreImpacter,
            int inputStoreIndex) {
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        this.constraintWeight = constraintWeight;
        this.scoreImpacter = scoreImpacter;
        this.inputStoreIndex = inputStoreIndex;
    }

    public void insert(QuadTuple<A, B, C, D> tuple) {
        if (tuple.store[inputStoreIndex] != null) {
            throw new IllegalStateException("Impossible state: the input for the tuple (" + tuple.factA
                    + ") was already added in the tupleStore.");
        }
        UndoScoreImpacter undoScoreImpacter = scoreImpacter.apply(tuple.factA, tuple.factB, tuple.factC, tuple.factD);
        tuple.store[inputStoreIndex] = undoScoreImpacter;
    }

    public void retract(QuadTuple<A, B, C, D> tuple) {
        UndoScoreImpacter undoScoreImpacter = (UndoScoreImpacter) tuple.store[inputStoreIndex];
        // No fail fast if null because we don't track which tuples made it through the filter predicate(s)
        if (undoScoreImpacter != null) {
            undoScoreImpacter.run();
            tuple.store[inputStoreIndex] = null;
        }
    }

    @Override
    public String toString() {
        return "Scorer(" + constraintName + ") with constraintWeight (" + constraintWeight + ")";
    }

}
