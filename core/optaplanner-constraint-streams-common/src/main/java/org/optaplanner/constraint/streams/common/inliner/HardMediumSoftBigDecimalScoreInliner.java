package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftBigDecimalScoreInliner extends AbstractScoreInliner<HardMediumSoftBigDecimalScore> {

    private BigDecimal hardScore = BigDecimal.ZERO;
    private BigDecimal mediumScore = BigDecimal.ZERO;
    private BigDecimal softScore = BigDecimal.ZERO;

    HardMediumSoftBigDecimalScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<HardMediumSoftBigDecimalScore> buildWeightedScoreImpacter(Constraint constraint,
            HardMediumSoftBigDecimalScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        BigDecimal hardConstraintWeight = constraintWeight.getHardScore();
        BigDecimal mediumConstraintWeight = constraintWeight.getMediumScore();
        BigDecimal softConstraintWeight = constraintWeight.getSoftScore();
        ScoreImpacterContext<HardMediumSoftBigDecimalScore> context =
                new ScoreImpacterContext<>(constraint, constraintWeight, constraintMatchEnabled);
        if (mediumConstraintWeight.equals(BigDecimal.ZERO) && softConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftBigDecimalScore> ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal hardImpact = ctx.getConstraintWeight().getHardScore().multiply(matchWeight);
                        this.hardScore = this.hardScore.add(hardImpact);
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore = this.hardScore.subtract(hardImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftBigDecimalScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO) && softConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftBigDecimalScore> ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal mediumImpact = ctx.getConstraintWeight().getMediumScore().multiply(matchWeight);
                        this.mediumScore = this.mediumScore.add(mediumImpact);
                        UndoScoreImpacter undoScoreImpact = () -> this.mediumScore = this.mediumScore.subtract(mediumImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                HardMediumSoftBigDecimalScore.ofMedium(mediumImpact), justificationsSupplier);
                    });
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO) && mediumConstraintWeight.equals(BigDecimal.ZERO)) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftBigDecimalScore> ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal softImpact = ctx.getConstraintWeight().getSoftScore().multiply(matchWeight);
                        this.softScore = this.softScore.add(softImpact);
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore = this.softScore.subtract(softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftBigDecimalScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftBigDecimalScore> ctx, BigDecimal matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        BigDecimal hardImpact = ctx.getConstraintWeight().getHardScore().multiply(matchWeight);
                        BigDecimal mediumImpact = ctx.getConstraintWeight().getMediumScore().multiply(matchWeight);
                        BigDecimal softImpact = ctx.getConstraintWeight().getSoftScore().multiply(matchWeight);
                        this.hardScore = this.hardScore.add(hardImpact);
                        this.mediumScore = this.mediumScore.add(mediumImpact);
                        this.softScore = this.softScore.add(softImpact);
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore = this.hardScore.subtract(hardImpact);
                            this.mediumScore = this.mediumScore.subtract(mediumImpact);
                            this.softScore = this.softScore.subtract(softImpact);
                        };
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                HardMediumSoftBigDecimalScore.of(hardImpact, mediumImpact, softImpact), justificationsSupplier);
                    });
        }
    }

    @Override
    public HardMediumSoftBigDecimalScore extractScore(int initScore) {
        return HardMediumSoftBigDecimalScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

    @Override
    public String toString() {
        return HardMediumSoftBigDecimalScore.class.getSimpleName() + " inliner";
    }

}
