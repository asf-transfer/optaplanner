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
    public WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint, HardMediumSoftLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        long hardConstraintWeight = constraintWeight.getHardScore();
        long mediumConstraintWeight = constraintWeight.getMediumScore();
        long softConstraintWeight = constraintWeight.getSoftScore();
        if (mediumConstraintWeight == 0L && softConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = hardConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        if (!constraintMatchEnabled) {
                            return undoScoreImpact;
                        }
                        Runnable undoConstraintMatch = addConstraintMatch(constraint, constraintWeight,
                                HardMediumSoftLongScore.ofHard(hardImpact), justificationsSupplier);
                        return () -> {
                            undoScoreImpact.run();
                            undoConstraintMatch.run();
                        };
                    });
        } else if (hardConstraintWeight == 0L && softConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long mediumImpact = mediumConstraintWeight * matchWeight;
                        this.mediumScore += mediumImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.mediumScore -= mediumImpact;
                        if (!constraintMatchEnabled) {
                            return undoScoreImpact;
                        }
                        Runnable undoConstraintMatch = addConstraintMatch(constraint, constraintWeight,
                                HardMediumSoftLongScore.ofMedium(mediumImpact), justificationsSupplier);
                        return () -> {
                            undoScoreImpact.run();
                            undoConstraintMatch.run();
                        };
                    });
        } else if (hardConstraintWeight == 0L && mediumConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long softImpact = softConstraintWeight * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        if (!constraintMatchEnabled) {
                            return undoScoreImpact;
                        }
                        Runnable undoConstraintMatch = addConstraintMatch(constraint, constraintWeight,
                                HardMediumSoftLongScore.ofSoft(softImpact), justificationsSupplier);
                        return () -> {
                            undoScoreImpact.run();
                            undoConstraintMatch.run();
                        };
                    });
        } else {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = hardConstraintWeight * matchWeight;
                        long mediumImpact = mediumConstraintWeight * matchWeight;
                        long softImpact = softConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        this.mediumScore += mediumImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.mediumScore -= mediumImpact;
                            this.softScore -= softImpact;
                        };
                        if (!constraintMatchEnabled) {
                            return undoScoreImpact;
                        }
                        Runnable undoConstraintMatch = addConstraintMatch(constraint, constraintWeight,
                                HardMediumSoftLongScore.of(hardImpact, mediumImpact, softImpact), justificationsSupplier);
                        return () -> {
                            undoScoreImpact.run();
                            undoConstraintMatch.run();
                        };
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
