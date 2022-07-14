package org.optaplanner.constraint.streams.bavet.tri;

import org.optaplanner.constraint.streams.bavet.common.AbstractScorer;
import org.optaplanner.constraint.streams.common.inliner.UndoScoreImpacter;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;

final class TriScorer<A, B, C> extends AbstractScorer<TriTupleImpl<A, B, C>> {

    private final TriFunction<A, B, C, UndoScoreImpacter> scoreImpacter;

    public TriScorer(String constraintPackage, String constraintName, Score<?> constraintWeight,
            TriFunction<A, B, C, UndoScoreImpacter> scoreImpacter, int inputStoreIndex) {
        super(constraintPackage, constraintName, constraintWeight, inputStoreIndex);
        this.scoreImpacter = scoreImpacter;
    }

    @Override
    protected UndoScoreImpacter impact(TriTupleImpl<A, B, C> tuple) {
        return scoreImpacter.apply(tuple.factA, tuple.factB, tuple.factC);
    }
}
