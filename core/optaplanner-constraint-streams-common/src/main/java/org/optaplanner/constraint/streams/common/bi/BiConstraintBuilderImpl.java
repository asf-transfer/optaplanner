package org.optaplanner.constraint.streams.common.bi;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.bi.BiConstraintBuilder;

public final class BiConstraintBuilderImpl<A, B>
        extends AbstractConstraintBuilder<BiConstraintBuilder<A, B>>
        implements BiConstraintBuilder<A, B> {

    private BiFunction<A, B, Object> justificationFunction;

    public BiConstraintBuilderImpl(BiConstraintConstructor<A, B> constraintConstructor, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected BiFunction<A, B, Object> getJustificationFunction() {
        if (justificationFunction == null) {
            return null; // Will use the default.
        }
        return (a, b) -> {
            Object justification = justificationFunction.apply(a, b);
            if (justification instanceof Collection) {
                throw new IllegalStateException("Justification function returned a collection (" + justification + ").");
            }
            return justification;
        };
    }

    @Override
    public BiConstraintBuilder<A, B> justifiedWith(BiFunction<A, B, Object> justificationFunction) {
        if (this.justificationFunction != null) {
            throw new IllegalStateException("Justification function already set (" + justificationFunction + ").");
        }
        this.justificationFunction = Objects.requireNonNull(justificationFunction);
        return this;
    }

}
