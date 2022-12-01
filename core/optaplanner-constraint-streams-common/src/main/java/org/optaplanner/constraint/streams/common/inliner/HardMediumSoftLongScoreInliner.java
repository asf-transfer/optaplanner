package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftLongScoreInliner extends AbstractScoreInliner<HardMediumSoftLongScore> {

    private long hardScore;
    private long mediumScore;
    private long softScore;

    HardMediumSoftLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<HardMediumSoftLongScore, HardMediumSoftLongScoreContext> buildWeightedScoreImpacter(
            Constraint constraint, HardMediumSoftLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        long hardConstraintWeight = constraintWeight.getHardScore();
        long mediumConstraintWeight = constraintWeight.getMediumScore();
        long softConstraintWeight = constraintWeight.getSoftScore();
        HardMediumSoftLongScoreContext context = new HardMediumSoftLongScoreContext(constraint, constraintWeight,
                constraintMatchEnabled, impact -> this.hardScore += impact, impact -> this.mediumScore += impact,
                impact -> this.softScore += impact);
        if (mediumConstraintWeight == 0L && softConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(hardImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftLongScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0L && softConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long mediumImpact = ctx.getConstraintWeight().getMediumScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeMediumScoreBy(mediumImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftLongScore.ofMedium(mediumImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0L && mediumConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftLongScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        long mediumImpact = ctx.getConstraintWeight().getMediumScore() * matchWeight;
                        long softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(hardImpact, mediumImpact, softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                HardMediumSoftLongScore.of(hardImpact, mediumImpact, softImpact), justificationsSupplier);
                    });
        }
    }

    @Override
    public HardMediumSoftLongScore extractScore(int initScore) {
        return HardMediumSoftLongScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

    @Override
    public String toString() {
        return HardMediumSoftLongScore.class.getSimpleName() + " inliner";
    }

}
