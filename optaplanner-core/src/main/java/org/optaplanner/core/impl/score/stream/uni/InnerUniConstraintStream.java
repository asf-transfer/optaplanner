package org.optaplanner.core.impl.score.stream.uni;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;

public interface InnerUniConstraintStream<A> extends UniConstraintStream<A> {

    @Override
    default Constraint penalize(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntFunction<A> matchWeigher) {
        return impactScore(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    default Constraint penalizeLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongFunction<A> matchWeigher) {
        return impactScoreLong(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    default Constraint penalizeBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher) {
        return impactScoreBigDecimal(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    default Constraint penalizeConfigurable(String constraintPackage, String constraintName,
            ToIntFunction<A> matchWeigher) {
        return impactScoreConfigurable(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    default Constraint penalizeConfigurableLong(String constraintPackage, String constraintName,
            ToLongFunction<A> matchWeigher) {
        return impactScoreConfigurableLong(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    default Constraint penalizeConfigurableBigDecimal(String constraintPackage, String constraintName,
            Function<A, BigDecimal> matchWeigher) {
        return impactScoreConfigurableBigDecimal(constraintPackage, constraintName, matchWeigher, false);

    }

    @Override
    default Constraint reward(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntFunction<A> matchWeigher) {
        return impactScore(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    default Constraint rewardLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongFunction<A> matchWeigher) {
        return impactScoreLong(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    default Constraint rewardBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher) {
        return impactScoreBigDecimal(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    default Constraint rewardConfigurable(String constraintPackage, String constraintName,
            ToIntFunction<A> matchWeigher) {
        return impactScoreConfigurable(constraintPackage, constraintName, matchWeigher, true);
    }

    @Override
    default Constraint rewardConfigurableLong(String constraintPackage, String constraintName,
            ToLongFunction<A> matchWeigher) {
        return impactScoreConfigurableLong(constraintPackage, constraintName, matchWeigher, true);
    }

    @Override
    default Constraint rewardConfigurableBigDecimal(String constraintPackage, String constraintName,
            Function<A, BigDecimal> matchWeigher) {
        return impactScoreConfigurableBigDecimal(constraintPackage, constraintName, matchWeigher, true);
    }

    Constraint impactScore(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntFunction<A> matchWeigher, boolean positive);

    Constraint impactScoreLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongFunction<A> matchWeigher, boolean positive);

    Constraint impactScoreBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            Function<A, BigDecimal> matchWeigher, boolean positive);

    Constraint impactScoreConfigurable(String constraintPackage, String constraintName, ToIntFunction<A> matchWeigher,
            boolean positive);

    Constraint impactScoreConfigurableLong(String constraintPackage, String constraintName,
            ToLongFunction<A> matchWeigher, boolean positive);

    Constraint impactScoreConfigurableBigDecimal(String constraintPackage, String constraintName,
            Function<A, BigDecimal> matchWeigher, boolean positive);


}
