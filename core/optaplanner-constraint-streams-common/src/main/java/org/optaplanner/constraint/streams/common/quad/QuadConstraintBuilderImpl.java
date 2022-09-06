package org.optaplanner.constraint.streams.common.quad;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintBuilder;

public final class QuadConstraintBuilderImpl<A, B, C, D>
        extends AbstractConstraintBuilder<QuadConstraintBuilder<A, B, C, D>>
        implements QuadConstraintBuilder<A, B, C, D> {

    public QuadConstraintBuilderImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintBuilder, impactType, constraintWeight);
    }

}
