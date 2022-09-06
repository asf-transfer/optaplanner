package org.optaplanner.constraint.streams.common.bi;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.bi.BiConstraintBuilder;

public final class BiConstraintBuilderImpl<A, B>
        extends AbstractConstraintBuilder<BiConstraintBuilder<A, B>>
        implements BiConstraintBuilder<A, B> {

    public BiConstraintBuilderImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType, Score<?> constraintWeight) {
        super(constraintBuilder, impactType, constraintWeight);
    }

}
