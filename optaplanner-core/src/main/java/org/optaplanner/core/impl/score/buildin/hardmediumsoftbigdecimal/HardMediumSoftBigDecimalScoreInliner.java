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

package org.optaplanner.core.impl.score.buildin.hardmediumsoftbigdecimal;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import org.optaplanner.core.impl.score.inliner.BigDecimalWeightedScoreImpacter;
import org.optaplanner.core.impl.score.inliner.JustificationsSupplier;
import org.optaplanner.core.impl.score.inliner.ScoreInliner;

public class HardMediumSoftBigDecimalScoreInliner extends ScoreInliner<HardMediumSoftBigDecimalScore> {

    private BigDecimal hardScore = BigDecimal.ZERO;
    private BigDecimal mediumScore = BigDecimal.ZERO;
    private BigDecimal softScore = BigDecimal.ZERO;

    protected HardMediumSoftBigDecimalScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, HardMediumSoftBigDecimalScore.ZERO);
    }

    @Override
    public BigDecimalWeightedScoreImpacter buildWeightedScoreImpacter(String constraintPackage, String constraintName,
            HardMediumSoftBigDecimalScore constraintWeight) {
        assertNonZeroConstraintWeight(constraintWeight);
        BigDecimal hardConstraintWeight = constraintWeight.getHardScore();
        BigDecimal mediumConstraintWeight = constraintWeight.getMediumScore();
        BigDecimal softConstraintWeight = constraintWeight.getSoftScore();
        if (mediumConstraintWeight.equals(BigDecimal.ZERO) && softConstraintWeight.equals(BigDecimal.ZERO)) {
            return (BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                BigDecimal hardImpact = hardConstraintWeight.multiply(matchWeight);
                this.hardScore = this.hardScore.add(hardImpact);
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.hardScore = this.hardScore.subtract(hardImpact),
                        () -> HardMediumSoftBigDecimalScore.ofHard(hardImpact),
                        justificationsSupplier);
            };
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO) && softConstraintWeight.equals(BigDecimal.ZERO)) {
            return (BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                BigDecimal mediumImpact = mediumConstraintWeight.multiply(matchWeight);
                this.mediumScore = this.mediumScore.add(mediumImpact);
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.mediumScore = this.mediumScore.subtract(mediumImpact),
                        () -> HardMediumSoftBigDecimalScore.ofMedium(mediumImpact),
                        justificationsSupplier);
            };
        } else if (hardConstraintWeight.equals(BigDecimal.ZERO) && mediumConstraintWeight.equals(BigDecimal.ZERO)) {
            return (BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                BigDecimal softImpact = softConstraintWeight.multiply(matchWeight);
                this.softScore = this.softScore.add(softImpact);
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> this.softScore = this.softScore.subtract(softImpact),
                        () -> HardMediumSoftBigDecimalScore.ofSoft(softImpact),
                        justificationsSupplier);
            };
        } else {
            return (BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                BigDecimal hardImpact = hardConstraintWeight.multiply(matchWeight);
                BigDecimal mediumImpact = mediumConstraintWeight.multiply(matchWeight);
                BigDecimal softImpact = softConstraintWeight.multiply(matchWeight);
                this.hardScore = this.hardScore.add(hardImpact);
                this.mediumScore = this.mediumScore.add(mediumImpact);
                this.softScore = this.softScore.add(softImpact);
                return buildUndo(constraintPackage, constraintName, constraintWeight,
                        () -> {
                            this.hardScore = this.hardScore.subtract(hardImpact);
                            this.mediumScore = this.mediumScore.subtract(mediumImpact);
                            this.softScore = this.softScore.subtract(softImpact);
                        },
                        () -> HardMediumSoftBigDecimalScore.of(hardImpact, mediumImpact, softImpact),
                        justificationsSupplier);
            };
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
